/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.flow.core.entity;

public interface IEntityTaskPermission extends IEntityByCod<Integer> {

    IEntityTaskDefinition getTaskDefinition();

    String getPermission();

}