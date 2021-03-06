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

import static org.opensingular.form.persistence.SPackageFormPersistence.ATR_COLUMN;
import static org.opensingular.form.persistence.SPackageFormPersistence.ATR_FOREIGN_COLUMN;
import static org.opensingular.form.persistence.SPackageFormPersistence.ATR_MANY_TO_MANY;
import static org.opensingular.form.persistence.SPackageFormPersistence.ATR_TABLE;
import static org.opensingular.form.persistence.SPackageFormPersistence.ATR_TABLE_FKS;
import static org.opensingular.form.persistence.SPackageFormPersistence.ATR_TABLE_PK;
import static org.opensingular.form.persistence.relational.RelationalColumnConverter.ASPECT_RELATIONAL_CONV;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Supplier;

import org.opensingular.form.SAttributeEnabled;
import org.opensingular.form.STranslatorForAttribute;
import org.opensingular.form.SType;

/**
 * Decorates an Instance to enable persistence configuration.
 *
 * @author Edmundo Andrade
 */
public class AtrSQL extends STranslatorForAttribute {
    public AtrSQL() {
    }

    public AtrSQL(SAttributeEnabled target) {
        super(target);
    }

    public AtrSQL table() {
        return table(getType().getNameSimple());
    }

    public AtrSQL table(String table) {
        setAttributeValue(ATR_TABLE, table);
        return this;
    }

    public String getTable() {
        return getAttributeValue(ATR_TABLE);
    }

    public AtrSQL tablePK(String tablePK) {
        setAttributeValue(ATR_TABLE_PK, tablePK);
        return this;
    }

    public String getTablePK() {
        return getAttributeValue(ATR_TABLE_PK);
    }

    public AtrSQL addTableFK(String keyColumns, Class<? extends SType<?>> typeClass) {
        String tableName = RelationalSQL.table(RelationalSQL.tableContext(getType()));
        return addTableFK(new RelationalFK(tableName, keyColumns, getDictionary().getType(typeClass)));
    }

    private AtrSQL addTableFK(RelationalFK fk) {
        List<RelationalFK> list = new ArrayList<>(getTableFKs());
        list.add(fk);
        StringJoiner sj = new StringJoiner(";");
        list.forEach(item -> sj.add(item.toStringPersistence()));
        setAttributeValue(ATR_TABLE_FKS, sj.toString());
        return this;
    }

    public List<RelationalFK> getTableFKs() {
        String value = getAttributeValue(ATR_TABLE_FKS);
        if (value == null) {
            return Collections.emptyList();
        }
        List<RelationalFK> result = new ArrayList<>();
        for (String item : value.split(";")) {
            result.add(RelationalFK.fromStringPersistence(item, getDictionary()));
        }
        return result;
    }

    public AtrSQL foreignColumn(String column, String keyColumns, Class<? extends SType<?>> typeClass) {
        String tableName = RelationalSQL.tableOpt(RelationalSQL.tableContext(getType())).orElse(null);
        RelationalFK foreignKey = new RelationalFK(tableName, keyColumns, getDictionary().getType(typeClass));
        return foreignColumn(column, foreignKey);
    }

    public AtrSQL foreignColumn(String column, RelationalFK foreignKey) {
        setAttributeValue(ATR_FOREIGN_COLUMN, new RelationalForeignColumn(column, foreignKey).toStringPersistence());
        return this;
    }

    public RelationalForeignColumn getForeignColumn() {
        String value = getAttributeValue(ATR_FOREIGN_COLUMN);
        if (value == null) {
            return null;
        }
        return RelationalForeignColumn.fromStringPersistence(value, getDictionary());
    }

    public AtrSQL column() {
        return column(getType().getNameSimple());
    }

    public AtrSQL column(String column) {
        setAttributeValue(ATR_COLUMN, column);
        return this;
    }

    public String getColumn() {
        return getAttributeValue(ATR_COLUMN);
    }

    public AtrSQL columnConverter(Supplier<RelationalColumnConverter> converter) {
        getType().setAspect(ASPECT_RELATIONAL_CONV, converter);
        return this;
    }

    public AtrSQL manyToMany(String table, String sourceKeyColumns, String targetKeyColumns) {
        setAttributeValue(ATR_MANY_TO_MANY, table + "|" + sourceKeyColumns + "|" + targetKeyColumns);
        return this;
    }

    public String getManyToManyTable() {
        String manyToMany = getAttributeValue(ATR_MANY_TO_MANY);
        return manyToMany == null ? null : manyToMany.split("\\|")[0];
    }

    public String getManyToManySourceKeyColumns() {
        String manyToMany = getAttributeValue(ATR_MANY_TO_MANY);
        return manyToMany == null ? null : manyToMany.split("\\|")[1];
    }

    public String getManyToManyTargetKeyColumns() {
        String manyToMany = getAttributeValue(ATR_MANY_TO_MANY);
        return manyToMany == null ? null : manyToMany.split("\\|")[2];
    }
}
