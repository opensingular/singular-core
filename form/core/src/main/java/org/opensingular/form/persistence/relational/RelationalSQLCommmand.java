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

import static org.opensingular.form.persistence.relational.RelationalSQL.tupleKeyRef;

import java.util.List;

import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;

/**
 * Relational SQL command, including its parameters.
 *
 * @author Edmundo Andrade
 */
public class RelationalSQLCommmand {
    private String sql;
    private List<Object> parameters;
    private SIComposite instance;
    private List<RelationalColumn> columns;
    private Long limitOffset;
    private Long limitRows;

    public RelationalSQLCommmand(String sql, List<Object> parameters, SIComposite instance,
            List<RelationalColumn> columns) {
        this.sql = sql;
        this.parameters = parameters;
        this.instance = instance;
        this.columns = columns;
    }

    public RelationalSQLCommmand(String sql, List<Object> parameters, SIComposite instance,
            List<RelationalColumn> columns, Long limitOffset, Long limitRows) {
        this(sql, parameters, instance, columns);
        this.limitOffset = limitOffset;
        this.limitRows = limitRows;
    }

    public String getSQL() {
        return sql;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public SIComposite getInstance() {
        return instance;
    }

    public SInstance getTupleKeyRef() {
        return tupleKeyRef(instance);
    }

    public void setInstance(SIComposite instance) {
        this.instance = instance;
    }

    public List<RelationalColumn> getColumns() {
        return columns;
    }

    public Long getLimitOffset() {
        return limitOffset;
    }

    public Long getLimitRows() {
        return limitRows;
    }
}
