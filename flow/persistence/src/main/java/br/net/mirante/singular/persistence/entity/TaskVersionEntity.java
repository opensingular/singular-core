/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.persistence.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import br.net.mirante.singular.flow.core.TaskType;
import br.net.mirante.singular.persistence.util.Constants;
import br.net.mirante.singular.persistence.util.HybridIdentityOrSequenceGenerator;

/**
 * The persistent class for the TB_VERSAO_TAREFA database table.
 */
@Entity
@GenericGenerator(name = AbstractEntityTaskVersion.PK_GENERATOR_NAME, strategy = HybridIdentityOrSequenceGenerator.CLASS_NAME)
@Table(name = "TB_VERSAO_TAREFA", schema = Constants.SCHEMA)
public class TaskVersionEntity extends AbstractEntityTaskVersion<ProcessVersionEntity, TaskDefinitionEntity, TaskTransitionVersionEntity, TaskType> {
    private static final long serialVersionUID = 1L;

}