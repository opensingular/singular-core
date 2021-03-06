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

package org.opensingular.form.builder.selection;

import org.apache.commons.lang3.StringUtils;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.STypeList;
import org.opensingular.form.converter.SInstanceConverter;
import org.opensingular.form.internal.freemarker.FormFreemarkerUtil;
import org.opensingular.form.util.transformer.Value;
import org.opensingular.form.util.transformer.Value.Content;
import org.opensingular.lib.commons.lambda.IFunction;

import static org.opensingular.form.util.transformer.Value.hydrate;


public class SSelectionDisplayBuilder extends AbstractBuilder {

    public SSelectionDisplayBuilder(SType type) {
        super(type);
    }

    public SProviderBuilder selfDisplay() {
        return display(type);
    }

    public SProviderBuilder display(final SType display) {
        type.asAtrProvider().asAtrProvider().displayFunction((IFunction<Content, String>) (content) -> {
            SType elementsType;
            if (type.isList()) {
                elementsType = ((STypeList) type).getElementsType();
            } else {
                elementsType = type;
            }
            SInstance ins = elementsType.newInstance();
            Value.hydrate(ins, content);
            Object v;
            if (ins instanceof SIComposite) {
                v = ((SIComposite) ins).getValue(display);
            } else {
                v = ins.getValue();
            }
            return v == null ? StringUtils.EMPTY : v.toString();
        });
        addConverter();
        return new SProviderBuilder(super.type);
    }

    public SProviderBuilder display(String freemakerTemplate) {
        type.asAtrProvider().asAtrProvider().displayFunction((IFunction<Content, String>) (content) -> {
            SType elementsType;
            if (type.isList()) {
                elementsType = ((STypeList) type).getElementsType();
            } else {
                elementsType = type;
            }
            SInstance dummy = elementsType.newInstance();
            Value.hydrate(dummy, content);
            hydrate(dummy, content);
            return FormFreemarkerUtil.get().merge(dummy, freemakerTemplate, false, true);
        });
        addConverter();
        return new SProviderBuilder(super.type);
    }

    private void addConverter() {
        type.asAtrProvider().asAtrProvider().converter(new SInstanceConverter<Content, SIComposite>() {
            @Override
            public void fillInstance(SIComposite ins, Content obj) {
                hydrate(ins, obj);
            }

            @Override
            public Content toObject(SIComposite ins) {
                return Value.dehydrate(ins);
            }
        });
    }

}