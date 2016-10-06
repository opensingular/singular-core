/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.form.document;

import org.opensingular.form.RefService;

import java.io.Serializable;
import java.util.Map;

/**
 * Service Registry which provides a ḿeans to register and lookup for services.
 * 
 * @author Fabricio Buzeto
 *
 */
public interface ServiceRegistry {

    @SuppressWarnings("serial")
    public static class Pair implements Serializable{
        final public Class<?>      type;
        final public RefService<?> provider;

        public Pair(Class<?> type, RefService<?> provider) {
            this.type = type;
            this.provider = provider;
        }
    }

    /**
     * List all factories for all registered services;
     * @return factory map.
     */
    Map<String, Pair> services();


    /**
     * Tries to find a service based on its class;
     *
     * @return <code>Null</code> if not found.
     */
    public <T> T lookupService(Class<T> targetClass);

    /**
     * Tries to find a service based on its name, casting to the desired type;
     *
     * @return <code>Null</code> if not found.
     */
    <T> T lookupService(String name, Class<T> targetClass);

    /**
     * Tries to find a service based on its name;
     * 
     * @return <code>Null</code> if not found.
     */
    Object lookupService(String name);
}