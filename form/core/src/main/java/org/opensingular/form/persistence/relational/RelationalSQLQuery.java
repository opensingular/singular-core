/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.form.persistence.relational;

import static org.opensingular.form.persistence.Criteria.emptyCriteria;
import static org.opensingular.form.persistence.relational.RelationalSQLAggregator.COUNT;
import static org.opensingular.form.persistence.relational.RelationalSQLAggregator.DISTINCT;
import static org.opensingular.form.persistence.relational.RelationalSQLAggregator.NONE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import org.opensingular.form.SType;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.SingularFormException;
import org.opensingular.form.persistence.Criteria;
import org.opensingular.form.persistence.FormKey;
import org.opensingular.form.persistence.FormKeyRelational;
import org.opensingular.form.persistence.OrderByField;

/**
 * Builder for SQL queries on Relational DBMS.
 *
 * @author Edmundo Andrade
 */
public class RelationalSQLQuery extends RelationalSQL {
    private RelationalSQLAggregator aggregator;
    private Collection<SType<?>> targetFields = new ArrayList<>();
    private List<RelationalColumn> keyColumns;
    private List<RelationalColumn> targetColumns;
    private Criteria criteria;
    private List<RelationalColumn> criteriaColumns = new ArrayList<>();
    private Map<String, SType<?>> mapColumnToField;
    private List<RelationalColumn> orderingColumns = new ArrayList<>();
    private String keyFormTable;
    private Map<String, Object> keyFormColumnMap;
    private List<RelationalColumn> keyFormColumns = Collections.emptyList();
    private Long limitOffset;
    private Long limitRows;

    @SafeVarargs
    public RelationalSQLQuery(RelationalSQLAggregator aggregator, Collection<SType<?>>... fieldCollections) {
        this.aggregator = aggregator;
        for (Collection<SType<?>> fieldCollection : fieldCollections) {
            this.targetFields.addAll(fieldCollection);
        }
        this.keyColumns = new ArrayList<>();
        this.targetColumns = new ArrayList<>();
        this.mapColumnToField = new HashMap<>();
        List<SType<?>> list = new ArrayList<>();
        addFieldsToList(targetFields, list);
        for (SType<?> field : list) {
            collectKeyColumns(field, keyColumns);
            collectTargetColumn(field, targetColumns, Collections.emptyList(), mapColumnToField);
        }
    }

    public RelationalSQLQuery orderBy(OrderByField... fields) {
        orderingColumns.clear();
        for (OrderByField orderedField : fields) {
            collectTargetColumn(orderedField.getField(), orderingColumns, Collections.emptyList(), mapColumnToField);
        }
        return this;
    }

    public RelationalSQLQuery where(STypeComposite<?> type, FormKey formKey) {
        keyFormTable = table(type);
        keyFormColumnMap = ((FormKeyRelational) formKey).getValue();
        keyFormColumns = new ArrayList<>();
        collectKeyColumns(type, keyFormColumns);
        return this;
    }

    public RelationalSQLQuery where(Criteria criteria) {
        if (criteria != emptyCriteria()) {
            this.criteria = criteria;
            criteria.getReferencedFields().forEach(
                    field -> collectTargetColumn(field, criteriaColumns, Collections.emptyList(), mapColumnToField));
        }
        return this;
    }

    public RelationalSQLQuery limit(Long limitOffset, Long limitRows) {
        this.limitOffset = limitOffset;
        this.limitRows = limitRows;
        return this;
    }

    public Collection<SType<?>> getTargetFields() {
        return targetFields;
    }

    @Override
    public List<RelationalSQLCommmand> toSQLScript() {
        List<RelationalColumn> selected = selectedColumns();
        Set<RelationalColumn> relevant = new LinkedHashSet<>(selected);
        relevant.addAll(keyFormColumns);
        relevant.addAll(criteriaColumns);
        Map<String, RelationalFK> joinMap = createJoinMap(targetTables);
        reorderTargetTables(joinMap);
        List<Object> params = new ArrayList<>();
        String wherePart = "";
        if (keyFormTable != null) {
            wherePart += " where " + where(keyFormTable, keyFormColumns, keyFormColumnMap, relevant, params);
        } else if (criteria != null) {
            wherePart += " where " + criteria
                    .toSQL(fieldToColumnMap(criteria.getReferencedFields(), criteriaColumns, relevant), params);
        }
        String orderPart = "";
        if (!orderingColumns.isEmpty()) {
            orderPart = " order by " + concatenateOrderingColumns(", ", relevant);
        }
        String sql = "select " + selectPart(concatenateColumnNames(selected, ", ", relevant)) + " from "
                + joinTables(relevant, joinMap) + wherePart + orderPart;
        return Arrays.asList(new RelationalSQLCommmand(sql, params, null, selected, limitOffset, limitRows));
    }

    private String selectPart(String columnsSequence) {
        String result;
        if (aggregator == COUNT) {
            result = "count(*)";
        } else if (aggregator == DISTINCT) {
            result = "distinct " + columnsSequence;
        } else {
            result = columnsSequence;
        }
        return result;
    }

    private List<RelationalColumn> selectedColumns() {
        Set<RelationalColumn> result = new LinkedHashSet<>(targetColumns);
        keyColumns.forEach(column -> {
            if (aggregator == NONE) {
                result.add(column);
            }
        });
        return new ArrayList<>(result);
    }

    private Map<SType<?>, String> fieldToColumnMap(Collection<SType<?>> fields, List<RelationalColumn> columns,
            Collection<RelationalColumn> relevantColumns) {
        Iterator<RelationalColumn> iterator = columns.iterator();
        Map<SType<?>, String> result = new HashMap<>();
        fields.forEach(field -> result.put(field, toSQLColumn(iterator.next(), relevantColumns)));
        return result;
    }

    private String concatenateColumnNames(List<RelationalColumn> columns, String separator,
            Collection<RelationalColumn> relevantColumns) {
        StringJoiner sj = new StringJoiner(separator);
        columns.forEach(column -> sj.add(toSQLColumn(column, relevantColumns)));
        return sj.toString();
    }

    private String joinTables(Collection<RelationalColumn> relevantColumns, Map<String, RelationalFK> joinMap) {
        StringJoiner sj = new StringJoiner(" left join ");
        Set<String> joinedTables = new LinkedHashSet<>();
        String intermediaryTable = detectIntermediaryTable(relevantColumns, joinMap);
        if (intermediaryTable != null) {
            intermediaryTables.add(intermediaryTable);
            joinedTables.add(intermediaryTable);
            String intermerdiaryTableAlias = tableAlias(intermediaryTable, relevantColumns);
            sj.add(intermediaryTable + " " + intermerdiaryTableAlias);
        }
        for (SType<?> tableContext : targetTables) {
            String table = table(tableContext);
            for (List<RelationalColumn> sourceKeyColumns : distinctJoins(table, relevantColumns)) {
                addClause(tableContext, sourceKeyColumns, relevantColumns, joinedTables, joinMap, sj);
            }
            if (aggregator == COUNT) {
                break;
            }
        }
        return sj.toString();
    }

    private String detectIntermediaryTable(Collection<RelationalColumn> relevantColumns,
            Map<String, RelationalFK> joinMap) {
        Set<String> joinedTables = new LinkedHashSet<>();
        for (SType<?> tableContext : targetTables) {
            String table = table(tableContext);
            for (List<RelationalColumn> sourceKeyColumns : distinctJoins(table, relevantColumns)) {
                String result = detectIntermediaryTable(table, sourceKeyColumns, joinedTables, joinMap);
                if (result != null) {
                    return result;
                }
                joinedTables.add(table);
            }
            if (aggregator == COUNT) {
                break;
            }
        }
        return null;
    }

    private String detectIntermediaryTable(String table, List<RelationalColumn> sourceKeyColumns,
            Set<String> joinedTables, Map<String, RelationalFK> joinMap) {
        if (locateRelationship(table, sourceKeyColumns, joinedTables, joinMap) == null) {
            List<RelationalFK> intermediary = locateIntermediaryRelationships(table, sourceKeyColumns, joinedTables,
                    joinMap);
            if (!intermediary.isEmpty()) {
                return intermediary.get(0).getTable();
            }
        }
        return null;
    }

    private void addClause(SType<?> tableContext, List<RelationalColumn> sourceKeyColumns,
            Collection<RelationalColumn> relevantColumns, Set<String> joinedTables, Map<String, RelationalFK> joinMap,
            StringJoiner sjResult) {
        String table = table(tableContext);
        String tableAlias = tableAlias(table, sourceKeyColumns, relevantColumns);
        if (joinedTables.isEmpty()) {
            joinedTables.add(table);
            sjResult.add(table + " " + tableAlias);
            return;
        }
        RelationalFK relationship = locateRelationship(table, sourceKeyColumns, joinedTables, joinMap);
        if (relationship == null) {
            throw new SingularFormException(
                    "Relational mapping should provide foreign key for relevant relationships with table '" + table
                            + "'.");
        }
        addClause(table, tableAlias, tablePK(tableContext), relationship, relevantColumns, sjResult);
        joinedTables.add(table);
    }

    private void addClause(String table, String tableAlias, List<String> tablePK, RelationalFK relationship,
            Collection<RelationalColumn> relevantColumns, StringJoiner sjResult) {
        String result = table + " " + tableAlias;
        String leftTable = relationship.getTable();
        List<RelationalColumn> leftColumns = relationship.getKeyColumns();
        List<String> rightColumns = tablePK;
        if (leftColumns.size() != rightColumns.size()) {
            throw new SingularFormException(
                    "Relational mapping should provide compatible-size foreign key for the relationship between table '"
                            + leftTable + "' and '" + table + "'.");
        }
        StringJoiner sj = new StringJoiner(" and ");
        for (int i = 0; i < leftColumns.size(); i++) {
            sj.add(tableAlias(leftTable, relevantColumns) + "." + leftColumns.get(i).getName() + " = " + tableAlias
                    + "." + rightColumns.get(i));
        }
        sjResult.add(result + " on " + sj);
    }

    private RelationalFK locateRelationship(String table, List<RelationalColumn> sourceKeyColumns,
            Set<String> joinedTables, Map<String, RelationalFK> joinMap) {
        RelationalFK result = null;
        for (String joinedTable : joinedTables) {
            result = joinMap.get(joinedTable + ">" + table + "@" + serialize(sourceKeyColumns));
            if (result == null) {
                Optional<String> key = joinMap.keySet().stream()
                        .filter(item -> item.startsWith(joinedTable + ">" + table + "@")).findFirst();
                if (key.isPresent()) {
                    result = joinMap.get(key.get());
                }
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    private List<RelationalFK> locateIntermediaryRelationships(String table, List<RelationalColumn> sourceKeyColumns,
            Set<String> joinedTables, Map<String, RelationalFK> joinMap) {
        List<RelationalFK> result = new ArrayList<>();
        for (String joinedTable : joinedTables) {
            joinMap.keySet().stream().filter(item -> item.contains(">" + table + "@" + serialize(sourceKeyColumns))
                    || item.contains(">" + joinedTable + "@")).forEach(item -> result.add(joinMap.get(item)));
        }
        return result;
    }

    private String concatenateOrderingColumns(String separator, Set<RelationalColumn> relevantColumns) {
        StringJoiner sj = new StringJoiner(separator);
        orderingColumns.forEach(column -> sj.add(toSQLColumn(column, relevantColumns)));
        return sj.toString();
    }

    private String toSQLColumn(RelationalColumn column, Collection<RelationalColumn> relevantColumns) {
        return tableAlias(column.getTable(), column.getSourceKeyColumns(), relevantColumns) + "." + column.getName();
    }
}
