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

package org.opensingular.form;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.NotImplementedException;

public class SPackage extends SScopeBase {

    private static final Logger LOGGER = Logger.getLogger(SType.class.getName());

    private final String name;

    private SDictionary dictionary;

    public SPackage() {
        this(null);
    }

    protected SPackage(String name) {
        if (name == null) {
            if (getClass() == SPackage.class) {
                throw new SingularFormException("Deve ser utilizado o construtor " + SPackage.class.getSimpleName() + "(String) ou "
                        + SPackage.class.getSimpleName() + " deve ser derivado");
            }
            name = SFormUtil.getInfoPackageName(this.getClass());
            if (name == null) {
                name = getClass().getName();
            }
        }
        SFormUtil.validatePackageName(name);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Método para ser sobescrito que é chamado no pacote para que o mesmo crie os seus tipos, crie atributos e
     * configure os tipos.
     */
    protected void onLoadPackage(PackageBuilder pb) {
    }

    @Override
    public SScope getParentScope() {
        return null;
    }

    public <T extends SType<?>> T createType(String simpleNameNewType, Class<T> baseType) {
        // TODO implementar
        throw new NotImplementedException("TODO implementar");
    }

    @Override
    protected void debug(Appendable appendable, int level) {
        try {
            pad(appendable, level).append(getName()).append("\n");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        super.debug(appendable, level + 1);
    }

    protected static boolean isNull(SISimple<?> field) {
        return field == null || field.isNull();
    }

    protected static boolean isNotNull(SISimple<?> field) {
        return field != null && !field.isNull();
    }

    protected static boolean isTrue(SISimple<?> field) {
        if (field != null) {
            return field.getValueWithDefault(Boolean.class);
        }
        return false;
    }

    @Override
    public SDictionary getDictionary() {
        return dictionary;
    }

    final void setDictionary(SDictionary dictionary) {
        this.dictionary = dictionary;
    }

}