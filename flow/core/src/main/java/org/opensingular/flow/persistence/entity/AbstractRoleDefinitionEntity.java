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

package org.opensingular.flow.persistence.entity;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.opensingular.flow.core.entity.IEntityFlowDefinition;
import org.opensingular.flow.core.entity.IEntityRoleDefinition;
import org.opensingular.flow.core.entity.IEntityRoleTask;
import org.opensingular.lib.support.persistence.entity.BaseEntity;

/**
 * The base persistent class for the TB_DEFINICAO_PAPEL database table.
 * <p>
 * Must declare a {@link GenericGenerator} with name
 * {@link AbstractRoleDefinitionEntity#PK_GENERATOR_NAME}.
 * </p>
 * <code>@GenericGenerator(name = AbstractRoleDefinitionEntity.PK_GENERATOR_NAME, strategy = "org.hibernate.id.IdentityGenerator")</code>
 *
 * @param <FLOW_DEFINITION>
 */
@MappedSuperclass
@Table(name = "TB_DEFINICAO_PAPEL")
public abstract class AbstractRoleDefinitionEntity<FLOW_DEFINITION extends IEntityFlowDefinition, ROLE_TASK extends IEntityRoleTask> extends BaseEntity<Integer> implements IEntityRoleDefinition {

    public static final String PK_GENERATOR_NAME = "GENERATED_CO_DEFINICAO_PAPEL";

    @Id
    @Column(name = "CO_DEFINICAO_PAPEL")
    @GeneratedValue(generator = PK_GENERATOR_NAME, strategy = GenerationType.AUTO)
    private Integer cod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_DEFINICAO_PROCESSO", nullable = false, foreignKey = @ForeignKey(name = "FK_DEFI_PPL_DEFINICAO_PROCESSO"))
    private FLOW_DEFINITION flowDefinition;

    @Column(name = "NO_PAPEL", length = 300, nullable = false)
    private String name;

    @Column(name = "SG_PAPEL", length = 100, nullable = false)
    private String abbreviation;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "roleDefinition", cascade = CascadeType.REMOVE)
    private List<ROLE_TASK> rolesTask;

    @Override
    public Integer getCod() {
        return cod;
    }

    public void setCod(Integer cod) {
        this.cod = cod;
    }

    @Override
    public FLOW_DEFINITION getFlowDefinition() {
        return flowDefinition;
    }

    public void setFlowDefinition(IEntityFlowDefinition flowDefinition) {
        this.flowDefinition = (FLOW_DEFINITION) flowDefinition;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getAbbreviation() {
        return abbreviation;
    }

    @Override
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public List<ROLE_TASK> getRolesTask() {
        return rolesTask;
    }

    public void setRolesTask(List<ROLE_TASK> rolesTask) {
        this.rolesTask = rolesTask;
    }
}
