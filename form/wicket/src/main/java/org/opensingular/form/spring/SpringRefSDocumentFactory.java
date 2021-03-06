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

package org.opensingular.form.spring;

import org.opensingular.form.SingularFormException;
import org.opensingular.form.document.RefSDocumentFactory;
import org.opensingular.form.document.SDocumentFactory;
import org.opensingular.internal.lib.support.spring.SpringUtils;
import org.opensingular.lib.support.spring.util.ApplicationContextProvider;

import javax.annotation.Nonnull;

/**
 * Referência serializável a uma fábrica de documentos que utiliza referência
 * estática ao ApplicationContext do Spring e o nome do bean no Spring da
 * fábrica para recuperá-la mais adiante.
 *
 * @author Daniel C. Bordin
 */
public class SpringRefSDocumentFactory extends RefSDocumentFactory {

    private final String springBeanName;

    public SpringRefSDocumentFactory(SpringSDocumentFactory springSDocumentFactory) {
        super(springSDocumentFactory);
        this.springBeanName = SpringUtils.checkBeanName(springSDocumentFactory);
    }

    @Nonnull
    @Override
    protected SDocumentFactory retrieve() {
        SDocumentFactory f = null;
        if (springBeanName != null) {
            f = ApplicationContextProvider.get().getBean(springBeanName, SDocumentFactory.class);
        }
        if(f != null) {
            return f;
        } else {
            throw new SingularFormException("Não foi possivel recuperar o SDocumentFactory");
        }
    }

}