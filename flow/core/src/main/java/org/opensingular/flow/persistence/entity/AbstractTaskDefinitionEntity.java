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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.opensingular.flow.core.entity.AccessStrategyType;
import org.opensingular.flow.core.entity.IEntityFlowDefinition;
import org.opensingular.flow.core.entity.IEntityRoleTask;
import org.opensingular.flow.core.entity.IEntityTaskDefinition;
import org.opensingular.flow.core.entity.IEntityTaskVersion;
import org.opensingular.lib.support.persistence.entity.BaseEntity;

/**
 * The base persistent class for the TB_DEFINICAO_TAREFA database table.
 * <p>
 * Must declare a {@link GenericGenerator} with name
 * {@link AbstractTaskDefinitionEntity#PK_GENERATOR_NAME}.
 * </p>
 * <code>@GenericGenerator(name = AbstractTaskDefinitionEntity.PK_GENERATOR_NAME, strategy = "org.hibernate.id.IdentityGenerator")</code>
 *
 * @param <FLOW_DEFINITION>
 * @param <TASK_VERSION>
 */
@MappedSuperclass
@Table(name = "TB_DEFINICAO_TAREFA")
public abstract class AbstractTaskDefinitionEntity<FLOW_DEFINITION extends IEntityFlowDefinition, TASK_VERSION extends IEntityTaskVersion, ROLE_TASK extends IEntityRoleTask> extends BaseEntity<Integer> implements IEntityTaskDefinition {

    public static final String PK_GENERATOR_NAME = "GENERATED_CO_DEFINICAO_TAREFA";

    @Id
    @Column(name = "CO_DEFINICAO_TAREFA")
    @GeneratedValue(generator = PK_GENERATOR_NAME, strategy = GenerationType.AUTO)
    private Integer cod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_DEFINICAO_PROCESSO", nullable = false, foreignKey = @ForeignKey(name = "FK_DEFI_TAR_DEFINICAO_PROCESSO"))
    private FLOW_DEFINITION flowDefinition;

    @Column(name = "SG_TAREFA", length = 100, nullable = false)
    private String abbreviation;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "taskDefinition")
    private List<TASK_VERSION> versions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_ESTRATEGIA_SEGURANCA", length = 1)
    private AccessStrategyType accessStrategyType;

    @OneToMany(mappedBy = "taskDefinition", fetch = FetchType.LAZY)
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

    public void setFlowDefinition(FLOW_DEFINITION flowDefinition) {
        this.flowDefinition = flowDefinition;
    }

    @Override
    public String getAbbreviation() {
        return abbreviation;
    }

    @Override
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @Override
    public List<TASK_VERSION> getVersions() {
        return versions;
    }

    public void setVersions(List<TASK_VERSION> versions) {
        this.versions = versions;
    }

    @Override
    public AccessStrategyType getAccessStrategyType() {
        return accessStrategyType;
    }

    @Override
    public void setAccessStrategyType(AccessStrategyType accessStrategyType) {
        this.accessStrategyType = accessStrategyType;
    }

    @Override
    public List<ROLE_TASK> getRolesTask() {
        return rolesTask;
    }

    public void setRolesTask(List<ROLE_TASK> rolesTask) {
        this.rolesTask = rolesTask;
    }
}
