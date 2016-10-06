/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.flow.persistence.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import org.opensingular.lib.support.persistence.util.Constants;
import org.opensingular.lib.support.persistence.util.HybridIdentityOrSequenceGenerator;

/**
 * The persistent class for the TB_INSTANCIA_PAPEL database table.
 */
@Entity
@GenericGenerator(name = AbstractRoleInstanceEntity.PK_GENERATOR_NAME, strategy = HybridIdentityOrSequenceGenerator.CLASS_NAME)
@Table(name = "TB_INSTANCIA_PAPEL", schema = Constants.SCHEMA)
public class RoleInstanceEntity extends AbstractRoleInstanceEntity<Actor, ProcessInstanceEntity, RoleDefinitionEntity> {
    private static final long serialVersionUID = 1L;

}
