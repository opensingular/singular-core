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

package org.opensingular.form.persistence.dao;

public class IndexedDataQueryBuilder {

    private final String schema;
    private StringBuilder select;
    private StringBuilder from = new StringBuilder(" FROM ");
    private StringBuilder join = new StringBuilder();
    private StringBuilder where;
    private int colCount = 0;


    private StringBuilder fromCache;
    private StringBuilder joinCache;

    public IndexedDataQueryBuilder(String schema) {
        this.schema = schema;

        select = new StringBuilder("select distinct tipoformulario.co_tipo_formulario as co_tipo_formulario \n");
        fromCache = new StringBuilder()
                .append(this.schema)
                .append(".tb_tipo_formulario tipoformulario\n");
        joinCache = new StringBuilder(" left join ")
                .append(this.schema)
                .append(".tb_formulario formulario on tipoformulario.co_tipo_formulario = formulario.co_tipo_formulario \n");
        where = new StringBuilder(" WHERE 1 = 1 ");
    }


    public void appendToSelect(String columnName) {
        select.append(',')
                .append(columnName);
    }

    public void appendToJoin(String joinClause) {
        join.append(joinClause);
    }


    public void appendToWhere(String joinClause) {
        where.append(joinClause);
    }

    public void appendToFrom(String fromClause) {
        from.append(fromClause)
                .append(',');
    }

    public IndexedDataQueryBuilder addColumn(String columnAlias, String[] fieldName) {
        addColumnToSelect(columnAlias);

        String fieldsNames = getFieldsNames(fieldName);
        addJoinClause(columnAlias, fieldsNames);
        return this;
    }

    /**
     * Cria um código SQL que sempre terá as colunas CO_TIPO_FORMULARIO e CO_VERSAO_FORMULARIO.
     * A partir dessas colunas é possível fazer outros joins com as tabelas do singular.
     *
     * @return
     */
    public String createQueryForIndexedData() {
        return new StringBuilder()
                .append(select)
                .append(from)
                .append(fromCache)
                .append(join)
                .append(joinCache)
                .append(where).toString();
    }

    private void addColumnToSelect(String column) {
        if (colCount == 0) {
            select.append("  , ").append(column).append(".co_versao_formulario as co_versao_formulario \n");
        }
        select.append("  , CONCAT( ")
                .append(column)
                .append(".DS_VALOR, ")
                .append(column)
                .append(" .NU_VALOR, ")
                .append(column)
                .append(" .DT_VALOR) as ")
                .append(column)
                .append('\n');
    }

    private void addJoinClause(String columnAlias, String fieldsNames) {
        String joinAlias = "tb_cache_campo_" + ++colCount;

        joinCache.append("  inner join ")
                .append(schema)
                .append(".tb_cache_campo ")
                .append(joinAlias)
                .append(" on ")
                .append(joinAlias)
                .append(".co_tipo_formulario = tipoformulario.co_tipo_formulario \n");

        joinCache.append("  inner join ")
                .append(schema)
                .append(".tb_cache_valor ")
                .append(columnAlias)
                .append(" on ")
                .append(columnAlias)
                .append(".co_cache_campo = ")
                .append(joinAlias)
                .append(".co_cache_campo \n");

        joinCache.append("          and ")
                .append(columnAlias)
                .append(".co_versao_formulario = formulario.co_versao_atual \n");

        joinCache.append("          and ")
                .append(joinAlias)
                .append(".ds_caminho_campo in (")
                .append(fieldsNames).append(") \n");
    }

    private String getFieldsNames(String[] fields) {
        StringBuilder fieldsNames = new StringBuilder();

        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                fieldsNames.append('\'').append(fields[i]).append('\'');
                if (fields.length - i > 1) {
                    fieldsNames.append(", ");
                }
            }
        }

        return fieldsNames.toString();
    }

}
