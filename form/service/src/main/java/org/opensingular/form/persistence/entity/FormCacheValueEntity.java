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

package org.opensingular.form.persistence.entity;

import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.STypeSimple;
import org.opensingular.form.type.core.STypeDate;
import org.opensingular.form.type.core.STypeDateTime;
import org.opensingular.form.type.core.STypeDecimal;
import org.opensingular.form.type.core.STypeInteger;
import org.opensingular.form.type.core.STypeLong;
import org.opensingular.form.type.core.STypeMonetary;
import org.opensingular.form.type.core.STypeTime;
import org.opensingular.lib.support.persistence.entity.BaseEntity;
import org.opensingular.lib.support.persistence.util.Constants;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;

/**
 * The persistent class for the TB_CACHE_VALOR database table.
 */
@Entity
@SequenceGenerator(name = FormCacheValueEntity.PK_GENERATOR_NAME, sequenceName = Constants.SCHEMA + ".SQ_CO_CACHE_VALOR", schema = Constants.SCHEMA)
@Table(name = "TB_CACHE_VALOR", schema = Constants.SCHEMA)
public class FormCacheValueEntity extends BaseEntity<Long> {
    public static final String PK_GENERATOR_NAME = "GENERATED_CO_CACHE_VALOR";

    @Id
    @Column(name = "CO_CACHE_VALOR")
    @GeneratedValue(generator = PK_GENERATOR_NAME, strategy = GenerationType.AUTO)
    private Long cod;

    @ManyToOne
    @JoinColumn(name = "CO_CACHE_CAMPO", foreignKey = @ForeignKey(name = "FK_CACHE_VALOR_CACHE_CAMPO"))
    private FormCacheFieldEntity cacheField;

    @ManyToOne
    @JoinColumn(name = "CO_VERSAO_FORMULARIO", foreignKey = @ForeignKey(name = "FK_CACHE_VAL_VERSAO_FORMULARIO"))
    private FormVersionEntity formVersion;

    @Column(name = "DS_VALOR", length = 8192)
    private String stringValue;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_VALOR")
    private Date dateValue;

    @Column(name = "NU_VALOR")
    private BigDecimal numberValue;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CO_PARENT", foreignKey = @ForeignKey(name = "FK_CACHE_VALOR_CO_PARENT"))
    private FormCacheValueEntity parent;

    public FormCacheValueEntity() {
    }

    public FormCacheValueEntity(FormCacheFieldEntity formField, FormVersionEntity formVersion, SInstance field, FormCacheValueEntity parent) {
        this.setCacheField(formField);
        this.setFormVersion(formVersion);
        this.setValue(field);
        this.setParent(parent);
    }

    @Override
    public Long getCod() {
        return cod;
    }

    public void setCod(long cod) {
        this.cod = cod;
    }

    public FormCacheFieldEntity getCacheField() {
        return cacheField;
    }

    public void setCacheField(FormCacheFieldEntity cacheField) {
        this.cacheField = cacheField;
    }

    public FormVersionEntity getFormVersion() {
        return formVersion;
    }

    public void setFormVersion(FormVersionEntity formVersion) {
        this.formVersion = formVersion;
    }

    public void setValue(SInstance instance) {
        if (instance.getValue() == null) {
            return;
        }

        SType type = instance.getType();
        if (! (type instanceof STypeSimple)) {
            return;
        }

        if (setDateValue(instance)){
            return;
        }
        if (setNumberValue(instance)){
            return;
        }
        setStringValue(instance);
    }

    private boolean setStringValue(SInstance instance) {
        String value = instance.getValue().toString();
        if (value.length() >= 2048) {
            value = instance.getValue().toString().substring(0, 2047);
        }
        stringValue = value;
        return true;
    }

    private boolean setDateValue(SInstance instance) {
        SType type = instance.getType();
        STypeSimple typeSimple = (STypeSimple) type;

        if (typeSimple instanceof STypeDate
                || type instanceof STypeDateTime
                || type instanceof STypeTime) {
            dateValue = (Date) typeSimple.convert(instance.getValue(), typeSimple.getValueClass());
            return true;
        }
        return false;
    }

    private boolean setNumberValue(SInstance instance) {
        SType type = instance.getType();
        STypeSimple typeSimple = (STypeSimple) type;

        if (type instanceof STypeInteger) {
            Integer value = (Integer) typeSimple.convert(instance.getValue(), typeSimple.getValueClass());
            numberValue = new BigDecimal(value);
            return true;
        }

        if (type instanceof STypeLong) {
            Long value = (Long) typeSimple.convert(instance.getValue(), typeSimple.getValueClass());
            numberValue = new BigDecimal(value);
            return true;
        }

        if (type instanceof STypeDecimal || type instanceof STypeMonetary) {
            numberValue = (BigDecimal) typeSimple.convert(instance.getValue(), typeSimple.getValueClass());
            return true;
        }
        return false;
    }

    public Date getDateValue() {
        return new Date(dateValue.getTime());
    }

    public BigDecimal getNumberValue() {
        return numberValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public FormCacheValueEntity getParent() {
        return parent;
    }

    public void setParent(FormCacheValueEntity parent) {
        this.parent = parent;
    }
}
