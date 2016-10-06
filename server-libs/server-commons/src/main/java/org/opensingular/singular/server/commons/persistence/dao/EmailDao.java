/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.opensingular.singular.server.commons.persistence.dao;

import javax.transaction.Transactional;

import org.opensingular.singular.server.commons.persistence.entity.email.EmailEntity;
import org.opensingular.singular.support.persistence.BaseDAO;

@SuppressWarnings("unchecked")
@Transactional(Transactional.TxType.MANDATORY)
public class EmailDao<T extends EmailEntity> extends BaseDAO<T, Long>{
    
    public EmailDao() {
        super((Class<T>) EmailEntity.class);
    }

    public EmailDao(Class<T> tipo) {
        super(tipo);
    }

}