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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SPackage extends SScopeBase {

    private static final Logger LOGGER = Logger.getLogger(SType.class.getName());

    private final String name;

    private SDictionary dictionary;

    private String nameSimple;

    public SPackage() {
        this(null);
    }

    protected SPackage(@Nullable String name) {
        String nameResolved = name;
        if (nameResolved == null) {
            if (getClass() == SPackage.class) {
                throw new SingularFormException(
                        "Deve ser utilizado o construtor " + SPackage.class.getSimpleName() + "(String) ou " +
                                SPackage.class.getSimpleName() + " deve ser derivado");
            }
            nameResolved = SFormUtil.getInfoPackageName(this.getClass());
        } else if (getClass() != SPackage.class) {
            throw new SingularFormException(
                    "Para uma classe derivada de " + getClass().getSimpleName() + ", não deve ser usado o construtor " +
                            SPackage.class.getSimpleName() + "(String) . Use o construtor " +
                            SPackage.class.getSimpleName() + "() e informe o nome do pacote usando a anotação @" +
                            SInfoPackage.class.getSimpleName());
        }
        this.nameSimple = SFormUtil.validatePackageName(nameResolved);
        this.name = nameResolved;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getNameSimple() {
        return nameSimple;
    }

    /**
     * Método para ser sobescrito que é chamado no pacote para que o mesmo crie os seus tipos, crie atributos e
     * configure os tipos.
     */
    protected void onLoadPackage(@Nonnull PackageBuilder pb) {
    }

    @Override
    @Nullable
    public SScope getParentScope() {
        return null;
    }

    @Nonnull
    @Override
    public SPackage getPackage() {
        return this;
    }

    @Override
    protected void debug(Appendable appendable, int level) {
        try {
            pad(appendable, level).append(getName()).append('\n');
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        super.debug(appendable, level + 1);
    }

    protected static boolean isNull(@Nullable SISimple<?> field) {
        return field == null || field.isEmptyOfData();
    }

    protected static boolean isNotNull(@Nullable SISimple<?> field) {
        return field != null && !field.isEmptyOfData();
    }

    protected static boolean isTrue(@Nullable SISimple<?> field) {
        if (field != null) {
            return field.getValueWithDefault(Boolean.class);
        }
        return false;
    }

    @Override
    @Nonnull
    public SDictionary getDictionary() {
        return Objects.requireNonNull(dictionary);
    }

    final void setDictionary(@Nonnull SDictionary dictionary) {
        this.dictionary = dictionary;
    }

}
