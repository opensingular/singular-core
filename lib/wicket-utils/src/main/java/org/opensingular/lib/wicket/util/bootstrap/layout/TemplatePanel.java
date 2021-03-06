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

package org.opensingular.lib.wicket.util.bootstrap.layout;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.resource.StringResourceStream;

import org.opensingular.lib.commons.base.SingularUtil;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.commons.lambda.ISupplier;

@SuppressWarnings("serial")
public class TemplatePanel extends Panel {

    private IFunction<TemplatePanel, String> templateFunction;

    public TemplatePanel(String id, String template) {
        this(id, p -> template);
    }

    public TemplatePanel(String id, IModel<?> model, String template) {
        this(id, model, p -> template);
    }

    public TemplatePanel(String id, ISupplier<String> templateSupplier) {
        this(id, p -> templateSupplier.get());
    }

    public TemplatePanel(String id, IModel<?> model, ISupplier<String> templateSupplier) {
        this(id, model, p -> templateSupplier.get());
    }

    public TemplatePanel(String id) {
        this(id, "");
    }

    public TemplatePanel(String id, IModel<?> model) {
        this(id, model, "");
    }

    public TemplatePanel(String id, IFunction<TemplatePanel, String> templateFunction) {
        super(id);
        this.templateFunction = templateFunction;
    }

    public TemplatePanel(String id, IModel<?> model, IFunction<TemplatePanel, String> templateFunction) {
        super(id, model);
        this.templateFunction = templateFunction;
    }

    protected void onBeforeComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {}

    protected void onAfterComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {}

    public IFunction<TemplatePanel, String> getTemplateFunction() {
        return templateFunction;
    }

    @Override
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy() {
        return new PanelMarkupSourcingStrategy(false) {
            @Override
            public IMarkupFragment getMarkup(MarkupContainer parent, Component child) {
                // corrige o problema de encoding
                StringResourceStream stringResourceStream = new StringResourceStream("<wicket:panel>" + getTemplateFunction().apply(TemplatePanel.this) + "</wicket:panel>", "text/html");
                stringResourceStream.setCharset(Charset.forName(Optional.ofNullable(Application.get().getMarkupSettings().getDefaultMarkupEncoding()).orElse(StandardCharsets.UTF_8.name())));

                MarkupParser markupParser = new MarkupParser(new MarkupResourceStream(stringResourceStream));
                markupParser.setWicketNamespace(MarkupParser.WICKET);
                Markup markup;
                try {
                    markup = markupParser.parse();
                } catch (Exception e) {
                    throw SingularUtil.propagate(e);
                }

                // If child == null, than return the markup fragment starting
                // with <wicket:panel>
                if (child == null) {
                    return markup;
                }

                // Copiado da superclasse. buscando markup do child
                IMarkupFragment associatedMarkup = markup.find(child.getId());
                if (associatedMarkup != null) {
                    return associatedMarkup;
                }
                associatedMarkup = searchMarkupInTransparentResolvers(parent, parent.getMarkup(), child);
                if (associatedMarkup != null) {
                    return associatedMarkup;
                }
                return findMarkupInAssociatedFileHeader(parent, child);
            }

            @Override
            public void onComponentTagBody(Component component, MarkupStream markupStream, ComponentTag openTag) {
                TemplatePanel.this.onBeforeComponentTagBody(markupStream, openTag);
                super.onComponentTagBody(component, markupStream, openTag);
                TemplatePanel.this.onAfterComponentTagBody(markupStream, openTag);
            }
        };
    }
}
