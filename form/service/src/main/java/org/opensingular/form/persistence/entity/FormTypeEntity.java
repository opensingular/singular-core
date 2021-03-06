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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.opensingular.lib.support.persistence.entity.BaseEntity;
import org.opensingular.lib.support.persistence.util.Constants;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@SequenceGenerator(name = FormTypeEntity.PK_GENERATOR_NAME, sequenceName = Constants.SCHEMA + ".SQ_CO_TIPO_FORMULARIO", schema = Constants.SCHEMA)
@Table(name = "TB_TIPO_FORMULARIO", schema = Constants.SCHEMA,
        indexes = {
                @Index(columnList = "SG_TIPO_FORMULARIO ASC", name = "IX_TIPO_FORMULARIO")
        })
public class FormTypeEntity extends BaseEntity<Long> {

    public static final String PK_GENERATOR_NAME = "GENERATED_CO_TIPO_FORMULARIO";

    @Id
    @Column(name = "CO_TIPO_FORMULARIO")
    @GeneratedValue(generator = PK_GENERATOR_NAME, strategy = GenerationType.AUTO)
    private Long cod;

    @Column(name = "SG_TIPO_FORMULARIO", nullable = false, length = 200)
    private String abbreviation;

    @Column(name = "NO_LABEL_FORMULARIO", length = 200)
    private String label;

    @Column(name = "NU_VERSAO_CACHE", nullable = false)
    private Long cacheVersionNumber;

    @Override
    public Long getCod() {
        return cod;
    }

    public void setCod(Long cod) {
        this.cod = cod;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public Long getCacheVersionNumber() {
        return cacheVersionNumber;
    }

    public void setCacheVersionNumber(Long cacheVersionNumber) {
        this.cacheVersionNumber = cacheVersionNumber;
    }

    public String getLabel() {
        return label;
    }

    public FormTypeEntity setLabel(String label) {
        this.label = label;
        return this;
    }
}
