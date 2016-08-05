/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.server.commons.flow.rest;

import br.net.mirante.singular.server.commons.persistence.entity.form.AbstractPetitionEntity;

public abstract class IController {

    public abstract ActionResponse execute(AbstractPetitionEntity petition, Action action);

    public boolean isExecutable() {
        return true;
    }
}
