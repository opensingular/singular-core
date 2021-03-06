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

package org.opensingular.form.type.core.annotation;

import org.opensingular.form.SInfoType;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.STypeList;
import org.opensingular.form.TypeBuilder;
import org.opensingular.form.type.core.SPackageCore;

/**
 * This type encloses a MTipoLista of MTipoAnnotation and can be used in order to persist
 * a set of anotations without much hassle.
 *
 * @author Fabricio Buzeto
 */
@SInfoType(name = STypeAnnotationList.NAME, spackage = SPackageCore.class)
public class STypeAnnotationList<E extends SType<I>, I extends SInstance> extends STypeList<E, I> {

    public static final String NAME = "AnnotationList";

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void onLoadType(TypeBuilder tb) {
        setElementsType((Class) STypeAnnotation.class);
    }
}
