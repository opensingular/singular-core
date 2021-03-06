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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.opensingular.lib.support.persistence.entity.BaseEntity;
import org.opensingular.lib.support.persistence.util.Constants;

@Entity
@SequenceGenerator(name = FormVersionEntity.PK_GENERATOR_NAME, sequenceName = Constants.SCHEMA + ".SQ_CO_VERSAO_FORMULARIO", schema = Constants.SCHEMA)
@Table(name = "TB_VERSAO_FORMULARIO", schema = Constants.SCHEMA)
public class FormVersionEntity extends BaseEntity<Long> {

    public static final String PK_GENERATOR_NAME = "GENERATED_CO_VERSAO_FORMULARIO";

    @Id
    @Column(name = "CO_VERSAO_FORMULARIO")
    @GeneratedValue(generator = PK_GENERATOR_NAME, strategy = GenerationType.AUTO)
    private Long cod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_FORMULARIO", nullable = false, foreignKey = @ForeignKey(name = "FK_VER_FORM_FORMULARIO"))
    private FormEntity formEntity;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_INCLUSAO", nullable = false)
    private Date inclusionDate;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "XML_CONTEUDO", nullable = false)
    private String xml;

    @Column(name = "CO_AUTOR_INCLUSAO")
    private Integer inclusionActor;

    @Column(name = "NU_VERSAO_CACHE")
    private Long cacheVersion;

    @Column(name = "ST_INDEXADO")
    private Character indexed;

    @OneToMany
    @JoinColumn(referencedColumnName = "CO_VERSAO_FORMULARIO", name = "CO_VERSAO_FORMULARIO", insertable = false, updatable = false)
    private List<FormAnnotationEntity> formAnnotations = new ArrayList<>(0);

    public FormVersionEntity() {
        setInclusionDate(new Date());
    }

    @Override
    public Long getCod() {
        return cod;
    }

    public void setCod(Long cod) {
        this.cod = cod;
    }

    public FormEntity getFormEntity() {
        return formEntity;
    }

    public void setFormEntity(FormEntity formEntity) {
        this.formEntity = formEntity;
    }

    public Date getInclusionDate() {
        return inclusionDate;
    }

    public void setInclusionDate(Date inclusionDate) {
        this.inclusionDate = inclusionDate;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public Integer getInclusionActor() {
        return inclusionActor;
    }

    public void setInclusionActor(Integer inclusionActor) {
        this.inclusionActor = inclusionActor;
    }

    public Long getCacheVersion() {
        return cacheVersion;
    }

    public void setCacheVersion(Long cacheVersion) {
        this.cacheVersion = cacheVersion;
    }

    public List<FormAnnotationEntity> getFormAnnotations() {
        return formAnnotations;
    }

    public void setFormAnnotations(List<FormAnnotationEntity> formAnnotations) {
        this.formAnnotations = formAnnotations;
    }
    public Character getIndexed() {
        return indexed;
    }
    public void setIndexed(Character indexed) {
        this.indexed = indexed;
    }
}