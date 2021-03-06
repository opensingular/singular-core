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

import static org.opensingular.form.persistence.relational.RelationalColumnConverter.ASPECT_RELATIONAL_CONV;
import static org.opensingular.form.persistence.relational.RelationalMapper.ASPECT_RELATIONAL_MAP;
import static org.opensingular.form.persistence.relational.RelationalSQLAggregator.COUNT;
import static org.opensingular.form.persistence.relational.RelationalSQLAggregator.DISTINCT;
import static org.opensingular.form.persistence.relational.RelationalSQLAggregator.NONE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.STypeList;
import org.opensingular.form.SingularFormException;
import org.opensingular.form.persistence.FormKey;
import org.opensingular.form.persistence.relational.strategy.PersistenceStrategy;

/**
 * Abstract class for relational SQL builders.
 *
 * @author Edmundo Andrade
 */
public abstract class RelationalSQL {
    static BasicRelationalMapper singletonBasicRelationalMapper = new BasicRelationalMapper();
    protected Set<SType<?>> targetTables = new LinkedHashSet<>();
    protected Set<String> intermediaryTables = new LinkedHashSet<>();

    public abstract List<RelationalSQLCommmand> toSQLScript();

    @SafeVarargs
    public static RelationalSQLQuery select(Collection<SType<?>>... fieldCollections) {
        return new RelationalSQLQuery(NONE, fieldCollections);
    }

    public static RelationalSQLQuery selectCount(SType<?> type) {
        return new RelationalSQLQuery(COUNT, Arrays.asList(type));
    }

    @SafeVarargs
    public static RelationalSQLQuery selectDistinct(Collection<SType<?>>... fieldCollections) {
        return new RelationalSQLQuery(DISTINCT, fieldCollections);
    }

    public static RelationalSQLInsert insert(SIComposite instance) {
        return new RelationalSQLInsert(instance);
    }

    public static RelationalSQLUpdate update(SIComposite instance, SIComposite previousInstance) {
        return new RelationalSQLUpdate(instance, previousInstance);
    }

    public static RelationalSQLDelete delete(STypeComposite<?> type, FormKey formKey) {
        return new RelationalSQLDelete(type, formKey);
    }

    public static SType<?> tableContext(SType<?> type) {
        return aspectRelationalMap(type).tableContext(type);
    }

    public static String table(SType<?> field) {
        return tableOpt(field).orElseThrow(() -> new SingularFormException(
                "Relational mapping should provide table name for the type '" + field.getName() + "'."));
    }

    public static Optional<String> tableOpt(SType<?> field) {
        return Optional.ofNullable(aspectRelationalMap(field).table(field));
    }

    public static List<String> tablePK(SType<?> type) {
        return aspectRelationalMap(type).tablePK(type);
    }

    public static List<RelationalFK> tableFKs(SType<?> type) {
        return aspectRelationalMap(type).tableFKs(type);
    }

    public static String column(SType<?> field) {
        return aspectRelationalMap(field).column(field);
    }

    public static RelationalForeignColumn foreignColumn(SType<?> type) {
        return aspectRelationalMap(type).foreignColumn(type);
    }

    public static PersistenceStrategy persistenceStrategy(SType<?> field) {
        return aspectRelationalMap(field).persistenceStrategy(field);
    }

    public static RelationalMapper aspectRelationalMap(SType<?> type) {
        Optional<RelationalMapper> mapper = type.getAspect(ASPECT_RELATIONAL_MAP);
        if (mapper.isPresent()) {
            return mapper.get();
        }
        return singletonBasicRelationalMapper;
    }

    public static Optional<RelationalColumnConverter> aspectRelationalColumnConverter(SType<?> type) {
        return type.getAspect(ASPECT_RELATIONAL_CONV);
    }

    public static boolean isListWithTableBound(SType<?> type) {
        return type.isList() && tableOpt(((STypeList<?, ?>) type).getElementsType()).isPresent();
    }

    public static SType<?> tableRef(SType<?> field) {
        SType<?> tableContext = tableContext(field);
        String columnName = column(field);
        for (RelationalFK fk : tableFKs(tableContext)) {
            if (fk.getKeyColumns().size() == 1 && fk.getKeyColumns().get(0).getName().equalsIgnoreCase(columnName)) {
                return fk.getForeignType();
            }
        }
        return null;
    }

    public static SInstance tupleKeyRef(SInstance instance) {
        return findAncestor(instance, tableContext(instance.getType())).orElseThrow(() -> new SingularFormException(
                "Relational mapping should provide table name for an ancestor type of the instance '"
                        + instance.getName() + "'."));
    }

    public static Object fieldValue(SInstance instance) {
        Optional<RelationalColumnConverter> converter = aspectRelationalColumnConverter(instance.getType());
        if (converter.isPresent()) {
            return converter.get().toRelationalColumn(instance);
        }
        return instance.getValue();
    }

    public static void setFieldValue(SInstance instance, List<RelationalData> fromList) {
        SType<?> field = instance.getType();
        SType<?> tableContext;
        String fieldName;
        List<RelationalColumn> sourceKeyColumns;
        RelationalForeignColumn foreignColumn = foreignColumn(field);
        if (foreignColumn == null) {
            tableContext = tableContext(field);
            fieldName = column(field);
            sourceKeyColumns = Collections.emptyList();
        } else {
            tableContext = tableContext(foreignColumn.getForeignKey().getForeignType());
            fieldName = foreignColumn.getForeignColumn();
            sourceKeyColumns = foreignColumn.getForeignKey().getKeyColumns();
        }
        String tableName = table(tableContext);
        Object value = getFieldValue(tableName, tupleKeyRef(instance), fieldName, sourceKeyColumns, fromList);
        Optional<RelationalColumnConverter> converter = aspectRelationalColumnConverter(instance.getType());
        if (converter.isPresent()) {
            converter.get().fromRelationalColumn(value, instance);
        } else if (value == null) {
            instance.clearInstance();
        } else {
            instance.setValue(value);
        }
    }

    static Object getFieldValue(String tableName, SInstance tupleKeyRef, String fieldName,
            List<RelationalColumn> sourceKeyColumns, List<RelationalData> fromList) {
        for (RelationalData data : fromList) {
            if (data.getTableName().equalsIgnoreCase(tableName) && data.getTupleKeyRef().equals(tupleKeyRef)
                    && data.getFieldName().equalsIgnoreCase(fieldName)
                    && data.getSourceKeyColumns().equals(sourceKeyColumns)) {
                return data.getFieldValue();
            }
        }
        return null;
    }

    public static List<SType<?>> getFields(SIComposite instance) {
        List<SType<?>> list = new ArrayList<>();
        instance.getAllChildren().forEach(child -> addFieldToList(child.getType(), list));
        return list;
    }

    public static List<SType<?>> getFields(STypeComposite<?> type) {
        List<SType<?>> list = new ArrayList<>();
        addFieldsToList(type.getFields(), list);
        return list;
    }

    public static Map<String, RelationalFK> createJoinMap(Collection<SType<?>> targets) {
        Map<String, RelationalFK> result = new HashMap<>();
        for (SType<?> tableContext : targets) {
            for (RelationalFK relationship : RelationalSQL.tableFKs(tableContext)) {
                List<RelationalColumn> sourceKeyColumns = relationship.getKeyColumns();
                result.put(relationship.getTable() + ">" + RelationalSQL.table(relationship.getForeignType()) + "@"
                        + serialize(sourceKeyColumns), relationship);
            }
            ((STypeComposite<?>) tableContext).getContainedTypes().stream()
                    .filter(item -> item.asSQL().getManyToManyTable() != null).forEach(item -> {
                        RelationalFK sourceRelationship = new RelationalFK(item.asSQL().getManyToManyTable(),
                                item.asSQL().getManyToManySourceKeyColumns(), tableContext);
                        result.put(sourceRelationship.getTable() + ">"
                                + RelationalSQL.table(sourceRelationship.getForeignType()) + "@"
                                + serialize(sourceRelationship.getKeyColumns()), sourceRelationship);
                        SType<?> targetType = ((STypeList<?, ?>) item).getElementsType();
                        RelationalFK targetRelationship = new RelationalFK(item.asSQL().getManyToManyTable(),
                                item.asSQL().getManyToManyTargetKeyColumns(), targetType);
                        result.put(targetRelationship.getTable() + ">"
                                + RelationalSQL.table(targetRelationship.getForeignType()) + "@"
                                + serialize(targetRelationship.getKeyColumns()), targetRelationship);
                    });
        }
        return result;
    }

    protected static String serialize(List<RelationalColumn> columns) {
        StringJoiner sj = new StringJoiner(",");
        columns.forEach(column -> sj.add(column.toStringPersistence()));
        return sj.toString().toUpperCase();
    }

    protected static void addFieldsToList(Collection<SType<?>> fields, List<SType<?>> list) {
        fields.forEach(field -> addFieldToList(field, list));
    }

    protected static void addFieldToList(SType<?> field, List<SType<?>> list) {
        if (column(field) != null || foreignColumn(field) != null) {
            list.add(field);
        } else if (field.isComposite()) {
            addFieldsToList(getFields((STypeComposite<?>) field), list);
        }
    }

    protected static Optional<SInstance> findAncestor(SInstance instance, SType<?> type) {
        for (SInstance current = instance; current != null; current = current.getParent()) {
            if (current.getType().getSuperType() == type || current.getType() == type) {
                return Optional.of(current);
            }
        }
        return Optional.empty();
    }

    protected static Optional<SInstance> findDescendant(SIComposite instance, SType<?> type) {
        for (SInstance current : instance.getAllChildren()) {
            if (current.getType().getSuperType() == type) {
                return Optional.of(current);
            }
        }
        return Optional.empty();
    }

    protected void collectKeyColumns(SType<?> type, List<RelationalColumn> keyColumns) {
        SType<?> tableContext = tableContext(type);
        String tableName = table(tableContext);
        addTargetTable(tableContext);
        List<String> pk = tablePK(tableContext);
        if (pk != null) {
            for (String columnName : pk) {
                RelationalColumn column = new RelationalColumn(tableName, columnName);
                if (!keyColumns.contains(column)) {
                    keyColumns.add(column);
                }
            }
        }
    }

    protected void collectTargetColumn(SType<?> field, List<RelationalColumn> targetColumns,
            List<RelationalColumn> keyColumns, Map<String, SType<?>> mapColumnToField) {
        if (field.isList()) {
            return;
        }
        SType<?> tableContext;
        String columnName;
        List<RelationalColumn> sourceKeyColumns;
        RelationalForeignColumn foreignColumn = foreignColumn(field);
        if (foreignColumn == null) {
            tableContext = tableContext(field);
            columnName = column(field);
            sourceKeyColumns = Collections.emptyList();
        } else {
            tableContext = tableContext(foreignColumn.getForeignKey().getForeignType());
            columnName = foreignColumn.getForeignColumn();
            sourceKeyColumns = foreignColumn.getForeignKey().getKeyColumns();
        }
        String tableName = table(tableContext);
        addTargetTable(tableContext);
        RelationalColumn column = new RelationalColumn(tableName, columnName, sourceKeyColumns);
        mapColumnToField.put(column.toStringPersistence(), field);
        if (!targetColumns.contains(column) && !keyColumns.contains(column)) {
            targetColumns.add(column);
        }
    }

    private void addTargetTable(SType<?> tableContext) {
        if (targetTables.stream().noneMatch(item -> item.getName().equals(tableContext.getName()))) {
            targetTables.add(tableContext);
        }
    }

    protected String where(String table, List<RelationalColumn> filterColumns, Map<String, Object> mapColumnToValue,
            Collection<RelationalColumn> relevantColumns, List<Object> params) {
        StringJoiner sj = new StringJoiner(" and ");
        filterColumns.forEach(column -> {
            if (column.getTable().equals(table)) {
                sj.add(tableAlias(table, column.getSourceKeyColumns(), relevantColumns) + "." + column.getName()
                        + " = ?");
                params.add(columnValue(column, mapColumnToValue));
            }
        });
        return sj.toString();
    }

    protected Object columnValue(RelationalColumn column, Map<String, Object> mapColumnToValue) {
        return mapColumnToValue.get(column.getName());
    }

    protected String tableAlias(String table, Collection<RelationalColumn> relevantColumns) {
        return tableAlias(table, null, relevantColumns);
    }

    protected String tableAlias(String table, List<RelationalColumn> sourceKeyColumns,
            Collection<RelationalColumn> relevantColumns) {
        List<String> tables = new ArrayList<>();
        targetTables.forEach(item -> tables.add(table(item)));
        String result = internalTableAlias(table, sourceKeyColumns, tables, distinctJoins(table, relevantColumns), "T");
        if (result == null) {
            result = internalTableAlias(table, intermediaryTables, "TX");
        }
        return result;
    }

    private String internalTableAlias(String table, Collection<String> tables, String prefix) {
        int index = 1;
        for (String tableName : tables) {
            if (table.equals(tableName)) {
                return prefix + index;
            }
            index++;
        }
        return null;
    }

    private String internalTableAlias(String table, List<RelationalColumn> sourceKeyColumns, Collection<String> tables,
            List<List<RelationalColumn>> joins, String prefix) {
        int index = 1;
        for (String tableName : tables) {
            for (List<RelationalColumn> join : joins) {
                if (table.equals(tableName) && (sourceKeyColumns == null || sourceKeyColumns.equals(join))) {
                    return prefix + index;
                }
                index++;
            }
        }
        return null;
    }

    protected List<List<RelationalColumn>> distinctJoins(String table, Collection<RelationalColumn> relevantColumns) {
        List<List<RelationalColumn>> distinctJoins = new ArrayList<>();
        relevantColumns.stream().filter(
                column -> column.getTable().equals(table) && !distinctJoins.contains(column.getSourceKeyColumns()))
                .forEach(column -> distinctJoins.add(column.getSourceKeyColumns()));
        return distinctJoins;
    }

    protected Object fieldValue(SIComposite instance, SType<?> field) {
        String fieldPath = field.getName().replaceFirst(instance.getType().getName() + ".", "");
        return fieldValue(instance.getField(fieldPath));
    }

    protected void reorderTargetTables(Map<String, RelationalFK> joinMap) {
        List<SType<?>> tables = new ArrayList<>(targetTables);
        for (int i = 0; i < tables.size() - 1; i++) {
            String tableLeft = RelationalSQL.table(tables.get(i));
            for (int j = i + 1; j < tables.size(); j++) {
                String tableRight = RelationalSQL.table(tables.get(j));
                String info = tableRight + '>' + tableLeft + "@";
                if (joinMap.keySet().stream().anyMatch(item -> item.startsWith(info))) {
                    SType<?> newLeft = tables.get(j);
                    tables.remove(j);
                    tables.add(i, newLeft);
                    i--;
                    break;
                }
            }
        }
        targetTables.clear();
        targetTables.addAll(tables);
    }

    protected List<SInstance> getContainerInstances(SIComposite instance) {
        List<SInstance> result = new ArrayList<>();
        for (RelationalFK fk : RelationalSQL.tableFKs(instance.getType())) {
            Optional<SInstance> containerInstance = findAncestor(instance, fk.getForeignType());
            if (!containerInstance.isPresent()) {
                containerInstance = findDescendant(instance, fk.getForeignType());
            }
            containerInstance.ifPresent(result::add);
        }
        return result;
    }
}
