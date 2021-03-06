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

package org.opensingular.form.internal.freemarker;

import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import org.opensingular.form.type.core.SINumber;

public class SNumberTemplateModel<INSTANCE extends SINumber<?>> extends SSimpleTemplateModel<INSTANCE>
        implements TemplateNumberModel {

    public SNumberTemplateModel(INSTANCE instance, FormObjectWrapper formObjectWrapper) {
        super(instance, formObjectWrapper, false);
    }

    public SNumberTemplateModel(INSTANCE instance, FormObjectWrapper formObjectWrapper, boolean escapeContentHtml) {
        super(instance, formObjectWrapper, escapeContentHtml);
    }

    @Override
    public Number getAsNumber() throws TemplateModelException {
        return getInstance().getValue();
    }

}