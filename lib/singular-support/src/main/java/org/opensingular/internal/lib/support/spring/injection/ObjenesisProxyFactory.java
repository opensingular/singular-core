/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.internal.lib.support.spring.injection;

import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import org.objenesis.ObjenesisStd;

import java.io.Serializable;

public class ObjenesisProxyFactory {
    private static final ObjenesisStd OBJENESIS = new ObjenesisStd(false);

    public static Object createProxy(final Class<?> type, final IProxyTargetLocator locator, NamingPolicy namingPolicy) {
        ObjenesisCGLibInterceptor handler = new ObjenesisCGLibInterceptor(type, locator);
        Enhancer                  e       = new Enhancer();
        e.setInterfaces(new Class[]{Serializable.class, ILazyInitProxy.class, LazyInitProxyFactory.IWriteReplace.class});
        e.setSuperclass(type);
        e.setCallbackType(handler.getClass());
        e.setNamingPolicy(namingPolicy);
        e.setUseCache(false);
        Class<?> proxyClass    = e.createClass();
        Factory  proxyInstance = (Factory) OBJENESIS.newInstance(proxyClass);
        proxyInstance.setCallbacks(new Callback[]{handler});
        return proxyInstance;
    }
}