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

package org.opensingular.form.wicket.mapper.components;

import java.util.Set;

import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import org.opensingular.form.wicket.component.SingularFormWicket;
import org.opensingular.lib.commons.lambda.IBiConsumer;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.form.SInstance;
import org.opensingular.form.type.basic.SPackageBasic;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSContainer;
import org.opensingular.lib.wicket.util.bootstrap.layout.TemplatePanel;

public abstract class MetronicPanel extends TemplatePanel {

    private SingularFormWicket<?> form = null;
    protected final boolean withForm;

    public MetronicPanel(String id) {
        this(id, true);
    }

    public MetronicPanel(String id, boolean withForm) {
        super(id);
        this.withForm = withForm;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        setRenderBodyOnly(true);
        setOutputMarkupId(false);
        setOutputMarkupPlaceholderTag(false);
        BSContainer<?>     heading   = new BSContainer<>("_hd");
        BSContainer<?>     footer    = new BSContainer<>("_ft");
        BSContainer<?>     content   = new BSContainer<>("_co");
        WebMarkupContainer container = this;
        if (withForm) {
            form = new SingularFormWicket<>("_fo");
            add(form);
            container = form;
        }
        container.add(heading);
        container.add(footer);
        container.add(content);
        buildHeading(heading, form);
        buildContent(content, form);
        buildFooter(footer, form);
    }

    public Form<?> getForm() {
        return form;
    }

    protected abstract void buildHeading(BSContainer<?> heading, Form<?> form);

    protected abstract void buildFooter(BSContainer<?> footer, Form<?> form);

    protected abstract void buildContent(BSContainer<?> content, Form<?> form);

    public void replaceContent(IBiConsumer<BSContainer<?>, Form<?>> buildContent) {
        BSContainer<?> content = new BSContainer<>("_co");
        buildContent.accept(content, form);
        form.replace(content);
    }

    protected String getPanelWrapperClass() {
        return "panel panel-default";
    }

    protected String getPanelHeadingClass() {
        return "panel-heading";
    }

    protected String getPanelBodyClass() {
        return "panel-body";
    }

    protected String getPanelFooterClass() {
        return "panel-footer";
    }
    
    @Override
    public IFunction<TemplatePanel, String> getTemplateFunction() {
        String wrapper = withForm ? "<form wicket:id='_fo'>%s</form>" : "%s";
        return (tp) -> String.format(wrapper, ""
            + "  <div class='" + getPanelWrapperClass() + "'>"
            + "    <div wicket:id='_hd' class='" + getPanelHeadingClass() + "'></div>"
            + "    <div class='" + getPanelBodyClass() + "' wicket:id='_co' >"
            + "    </div>"
            + "    <div wicket:id='_ft' class='" + getPanelFooterClass() + " text-right'></div>"
            + "  </div>"
            + "");
    }
    
    public static ClassAttributeModifier dependsOnModifier(IModel<? extends SInstance> model) {
        return new ClassAttributeModifier() {
            @Override
            protected Set<String> update(Set<String> oldClasses) {
                if (model.getObject().getAttributeValue(SPackageBasic.ATR_DEPENDS_ON_FUNCTION) != null) {
                    oldClasses.add("dependant-input-group");
                }
                return oldClasses;
            }
        };
    }

    public static final class MetronicPanelBuilder {

        private MetronicPanelBuilder() {
        }

        public static MetronicPanel build(String id,
                                          IBiConsumer<BSContainer<?>, Form<?>> buildHeading,
                                          IBiConsumer<BSContainer<?>, Form<?>> buildContent,
                                          IBiConsumer<BSContainer<?>, Form<?>> buildFooter) {
            return build(id, true, buildHeading, buildContent, buildFooter);
        }

        public static MetronicPanel build(String id,
                                          boolean withForm,
                                          IBiConsumer<BSContainer<?>, Form<?>> buildHeading,
                                          IBiConsumer<BSContainer<?>, Form<?>> buildContent,
                                          IBiConsumer<BSContainer<?>, Form<?>> buildFooter) {

            return new MetronicPanel(id, withForm) {
                @Override
                protected void buildHeading(BSContainer<?> heading, Form<?> form) {
                    buildHeading.accept(heading, form);
                }

                @Override
                protected void buildFooter(BSContainer<?> footer, Form<?> form) {
                    buildFooter.accept(footer, form);
                }

                @Override
                protected void buildContent(BSContainer<?> content, Form<?> form) {
                    buildContent.accept(content, form);
                }
            };
        }

    }

}
