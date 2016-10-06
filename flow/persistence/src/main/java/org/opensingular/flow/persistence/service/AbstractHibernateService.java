/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.flow.persistence.service;

import java.util.Objects;

import org.opensingular.flow.persistence.entity.util.SessionWrapper;
import org.opensingular.lib.commons.base.SingularException;
import org.opensingular.flow.persistence.entity.util.SessionLocator;

public abstract class AbstractHibernateService {

    protected SessionLocator sessionLocator;

    public AbstractHibernateService() {
    }

    public AbstractHibernateService(SessionLocator sessionLocator) {
        this.sessionLocator = sessionLocator;
    }

    public SessionLocator getSessionLocator() {
        return sessionLocator;
    }

    protected SessionWrapper getSession() {
        Objects.requireNonNull(getSessionLocator());
        return new SessionWrapper(getSessionLocator().getCurrentSession());
    }

    protected static <T> T newInstanceOf(Class<T> classe) {
        try {
            return classe.newInstance();
        } catch (Exception e) {
            throw new SingularException("Erro instanciando entidade " + classe.getName(), e);
        }
    }
}
