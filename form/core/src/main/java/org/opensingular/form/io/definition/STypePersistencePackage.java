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

package org.opensingular.form.io.definition;

import org.opensingular.form.SInfoType;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.TypeBuilder;

@SInfoType(spackage = SPackageDefinitionPersitence.class, name = "package")
public class STypePersistencePackage extends STypeComposite<SIPersistencePackage> {

    public static final String FIELD_PACKAGE_NAME = "packageName";
    public static final String FIELD_TYPES = "types";

    public STypePersistencePackage() {
        super(SIPersistencePackage.class);
    }

    @Override
    protected void onLoadType(TypeBuilder tb) {
        addFieldString(FIELD_PACKAGE_NAME);
        addFieldListOf(FIELD_TYPES, STypePersistenceType.class);
    }

}
