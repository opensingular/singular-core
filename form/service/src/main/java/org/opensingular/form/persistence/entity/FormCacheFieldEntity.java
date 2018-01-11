/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.form.persistence.entity;

import org.hibernate.annotations.GenericGenerator;
import org.opensingular.lib.support.persistence.entity.BaseEntity;
import org.opensingular.lib.support.persistence.util.Constants;
import org.opensingular.lib.support.persistence.util.HybridIdentityOrSequenceGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The persistent class for the TB_CACHE_CAMPO database table.
 */
@Entity
@GenericGenerator(name = FormCacheFieldEntity.PK_GENERATOR_NAME, strategy = HybridIdentityOrSequenceGenerator.CLASS_NAME)
@Table(name = "TB_CACHE_CAMPO", schema = Constants.SCHEMA)
public class FormCacheFieldEntity extends BaseEntity<Long> {

    public static final String PK_GENERATOR_NAME = "GENERATED_CO_CACHE_CAMPO";

    @Id
    @Column(name = "CO_CACHE_CAMPO")
    @GeneratedValue(generator = PK_GENERATOR_NAME)
    private Long cod;

    @ManyToOne
    @JoinColumn(name = "CO_TIPO_FORMULARIO")
    private FormTypeEntity formTypeEntity;

    @Column(name = "DS_CAMINHO_CAMPO", length = 255)
    private String path;

    public FormCacheFieldEntity() {
    }

    public FormCacheFieldEntity(String path, FormTypeEntity formType) {
        this.path = path;
        this.formTypeEntity = formType;
    }

    public Long getCod() {
        return cod;
    }

    public void setCod(Long cod) {
        this.cod = cod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FormTypeEntity getFormTypeEntity() {
        return formTypeEntity;
    }

    public void setFormTypeEntity(FormTypeEntity formTypeEntity) {
        this.formTypeEntity = formTypeEntity;
    }
}