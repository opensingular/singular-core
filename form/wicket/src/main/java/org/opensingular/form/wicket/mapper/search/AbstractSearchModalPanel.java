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

package org.opensingular.form.wicket.mapper.search;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.behavior.AjaxUpdateInputBehavior;
import org.opensingular.form.wicket.component.BFModalWindow;
import org.opensingular.form.wicket.model.AbstractSInstanceAwareModel;
import org.opensingular.form.wicket.model.ISInstanceAwareModel;

public abstract class AbstractSearchModalPanel extends Panel {

    public static final String VALUE_FIELD_ID = "valueField";
    public static final String SELECT_INPUT_MODAL_CONTENT_ID = "selectInputModalContent";
    public static final String MODAL_TRIGGER_ID = "modalTrigger";

    protected final WicketBuildContext ctx;
    protected final ISInstanceAwareModel<String> valueModel;

    protected TextField<String> valueField;

    protected AbstractSearchModalPanel(String id, WicketBuildContext ctx) {
        super(id);
        this.ctx = ctx;
        this.valueModel = new AbstractSInstanceAwareModel<String>() {
            @Override
            public String getObject() {
                final SInstance mi = getSInstance();
                if (mi != null && mi.getValue() != null) {
                    if (!mi.isEmptyOfData()) {
                        if (mi.asAtr().getDisplayString() != null) {
                            return mi.toStringDisplay();
                        }
                        if (!(mi instanceof SIComposite)) {
                            return String.valueOf(mi.getValue());
                        }
                        return mi.toString();
                    }
                }
                return null;
            }

            @Override
            public SInstance getSInstance() {
                return ctx.getModel().getObject();
            }
        };
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildAndAppendModalToRootContainer();
        add(valueField());
        add(modalTrigger());
    }

    protected abstract void buildAndAppendModalToRootContainer();

    protected abstract BFModalWindow getModal();

    protected WicketBuildContext getCtx() {
        return ctx;
    }

    private TextField<String> valueField() {
       return valueField = new TextField<>(VALUE_FIELD_ID, valueModel);
    }

    private AjaxLink<Void> modalTrigger() {
        return new AjaxLink<Void>(MODAL_TRIGGER_ID){
            @Override
            public void onClick(AjaxRequestTarget target) {
                getModal().show(target);
            }
        };
    }

    protected void accept(AjaxRequestTarget target) {
        getModal().hide(target);
        target.add(valueField);
        valueField.getBehaviors(AjaxUpdateInputBehavior.class).forEach(ajax -> ajax.onUpdate(target));
    }
}
