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

package org.opensingular.form.wicket;

import static org.opensingular.form.wicket.mapper.SingularEventsHandlers.FUNCTION.ADD_TEXT_FIELD_HANDLERS;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.opensingular.form.wicket.behavior.AjaxUpdateChoiceBehavior;
import org.opensingular.form.wicket.component.SingularFormComponentPanel;
import org.slf4j.LoggerFactory;

import org.opensingular.form.SInstance;
import org.opensingular.form.context.UIComponentMapper;
import org.opensingular.form.wicket.behavior.AjaxUpdateInputBehavior;
import org.opensingular.form.wicket.behavior.AjaxUpdateSingularFormComponentPanel;
import org.opensingular.form.wicket.mapper.SingularEventsHandlers;

@FunctionalInterface
public interface IWicketComponentMapper extends UIComponentMapper {

    /*  Evento utilizado para capturar mudanças via change ou blur sem chamadas repetidas.
     *  Esse evento é utilizado em alguns js da aplicação, para mudar o nome é preciso fazer uma varredura de texto.
     */
    @Deprecated
    String SINNGULAR_BLUR_CHANGE_EVENT = "singular:blurchange";

    /** Evento javascript padrão para ativar uma requisição ajax para validação do campo */
    String SINGULAR_VALIDATE_EVENT = "singular:validate";

    /** Evento javascript padrão para ativar uma requisição ajax para processamento do campo */
    String SINGULAR_PROCESS_EVENT = "singular:process";

    void buildView(WicketBuildContext ctx);

    default void addAjaxUpdate(Component component, IModel<SInstance> model, IAjaxUpdateListener listener) {
        component.setOutputMarkupId(true);
        adjustJSEvents(component);

        if ((component instanceof RadioChoice) ||
            (component instanceof CheckBoxMultipleChoice) ||
            (component instanceof RadioGroup) ||
            (component instanceof CheckGroup)) {
            component.add(new AjaxUpdateChoiceBehavior(model, listener));
            //component.add(SINGULAR_FORM_GROUP_HEIGHT_FIX);

        } else if (component instanceof SingularFormComponentPanel) {
            component.add(new AjaxUpdateSingularFormComponentPanel<>(model, listener));

        } else if (!(component instanceof FormComponentPanel<?>)) {
            //component.add(SINGULAR_BLUR_CHANGE_DEBOUNCER);
            component.add(new AjaxUpdateInputBehavior(SINGULAR_VALIDATE_EVENT, model, true, listener));
            component.add(new AjaxUpdateInputBehavior(SINGULAR_PROCESS_EVENT, model, false, listener));
            //component.add(SINGULAR_FORM_GROUP_HEIGHT_FIX);
        } else {
            LoggerFactory.getLogger(IWicketComponentMapper.class).warn("Atualização ajax não suportada para {}", component);
        }
    }

    default void adjustJSEvents(Component comp) {
        comp.add(new SingularEventsHandlers(ADD_TEXT_FIELD_HANDLERS));
    }

    @FunctionalInterface
    interface HintKey<T> extends Serializable {
        T getDefaultValue();
        default boolean isInheritable() {
            return true;
        }
    }

}
