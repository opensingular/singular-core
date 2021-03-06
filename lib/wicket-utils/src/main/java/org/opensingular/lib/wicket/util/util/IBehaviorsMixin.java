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

package org.opensingular.lib.wicket.util.util;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.opensingular.lib.commons.lambda.IBiConsumer;
import org.opensingular.lib.commons.lambda.IConsumer;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.commons.lambda.IPredicate;
import org.opensingular.lib.commons.lambda.ISupplier;
import org.opensingular.lib.wicket.util.behavior.ConditionalAttributeAppender;
import org.opensingular.lib.wicket.util.behavior.ConditionalAttributeModifier;
import org.opensingular.lib.wicket.util.behavior.FormChoiceAjaxUpdateBehavior;
import org.opensingular.lib.wicket.util.behavior.FormComponentAjaxUpdateBehavior;
import org.opensingular.lib.wicket.util.behavior.IAjaxUpdateConfiguration;
import org.opensingular.lib.wicket.util.behavior.OnComponentTagFunctionalBehaviour;
import org.opensingular.lib.wicket.util.behavior.OnConfigureFunctionalBehaviour;
import org.opensingular.lib.wicket.util.behavior.RenderHeadFunctionalBehavior;
import org.opensingular.lib.wicket.util.behavior.UpdateValueAttributeAppender;
import org.opensingular.lib.wicket.util.jquery.JQuery;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.opensingular.lib.wicket.util.util.Shortcuts.$b;
import static org.opensingular.lib.wicket.util.util.Shortcuts.$m;

@SuppressWarnings("serial")
public interface IBehaviorsMixin extends Serializable {

    default AttributeAppender attrAppender(String attribute, Serializable valueOrModel, String separator) {
        return attrAppender(attribute, valueOrModel, separator, Model.of(Boolean.TRUE));
    }

    default AttributeAppender attrAppender(String attribute, Serializable valueOrModel, String separator, IModel<Boolean> enabledModel) {
        ConditionalAttributeAppender conditionalAttributeAppender = new ConditionalAttributeAppender(attribute,
                (valueOrModel instanceof IModel<?>) ? (IModel<?>) valueOrModel : Model.of(valueOrModel), separator);
        conditionalAttributeAppender.setEnabled(enabledModel);
        return conditionalAttributeAppender;
    }

    default AttributeModifier attrRemover(String attribute, Serializable patternToRemove, boolean isolateWord) {
        return new UpdateValueAttributeAppender(attribute, patternToRemove, isolateWord);
    }

    default AttributeModifier attr(String attribute, Serializable valueOrModel) {
        return attr(attribute, valueOrModel, Model.of(Boolean.TRUE));
    }

    default AttributeModifier attr(String attribute, Serializable valueOrModel, IModel<Boolean> enabledModel) {
        ConditionalAttributeModifier conditionalAttributeModifier = new ConditionalAttributeModifier(attribute,
                (valueOrModel instanceof IModel<?>) ? (IModel<?>) valueOrModel : Model.of(valueOrModel));
        conditionalAttributeModifier.setEnabled(enabledModel);
        return conditionalAttributeModifier;
    }

    default AttributeAppender styleAppender(IModel<? extends Map<String, String>> stylesModel) {
        return styleAppender(stylesModel, $m.ofValue(Boolean.TRUE));
    }

    default AttributeAppender styleAppender(IModel<? extends Map<String, String>> stylesModel, IModel<Boolean> enabledModel) {
        IModel<Object> stylesStringModel = $m.map(stylesModel, styles -> styles.entrySet().stream()
                .map(it -> it.getKey() + ":" + it.getValue())
                .collect(joining(";")));
        return attrAppender("style", stylesStringModel, ";", enabledModel);
    }

    default AttributeAppender styleAppender(Map<String, String> styles) {
        return styleAppender(styles, $m.ofValue(Boolean.TRUE));
    }

    default AttributeAppender styleAppender(Map<String, String> styles, IModel<Boolean> enabledModel) {
        return styleAppender($m.ofValue(new HashMap<>(styles)), enabledModel);
    }

    default AttributeAppender styleAppender(String name, Serializable valueOrModel, IModel<Boolean> enabledModel) {
        return styleAppender(name, valueOrModel, false, enabledModel);
    }

    default AttributeAppender styleAppender(String name, Serializable valueOrModel, boolean important, IModel<Boolean> enabledModel) {
        return attrAppender(
                "style",
                $m.map($m.wrapValue(valueOrModel), it -> name + ":" + it + (important ? " !important" : "")),
                ";",
                enabledModel);
    }

    default AttributeAppender classAppender(Serializable valueOrModel) {
        return classAppender(valueOrModel, Model.of(Boolean.TRUE));
    }

    default AttributeAppender classAppender(Serializable valueOrModel, IModel<Boolean> enabledModel) {
        $m.map($m.wrapValue(valueOrModel), it -> (it instanceof Collection<?>)
                ? ((Collection<?>) it).stream()
                .map(Object::toString)
                .collect(joining(" "))
                : it);
        return attrAppender("class", valueOrModel, " ", enabledModel);
    }

    default Behavior renderBodyOnly(IModel<Boolean> renderBodyOnly) {
        return OnConfigureFunctionalBehaviour.of(c -> c.setRenderBodyOnly(renderBodyOnly.getObject()));
    }

    default Behavior notVisibleIf(ISupplier<Boolean> model) {
        return OnConfigureFunctionalBehaviour.of(c -> c.setVisible(!model.get()));
    }

    default Behavior visibleIf(ISupplier<Boolean> model) {
        return OnConfigureFunctionalBehaviour.of(c -> c.setVisible(model.get()));
    }

    @SuppressWarnings("unchecked")
    default <T> Behavior visibleIfModelObject(IPredicate<T> predicate) {
        return OnConfigureFunctionalBehaviour.of(c -> c.setVisible(predicate.test((T) c.getDefaultModelObject())));
    }

    default Behavior visibleIf(IModel<Boolean> model) {
        return OnConfigureFunctionalBehaviour.of(c -> c.setVisible(model.getObject()));
    }

    default Behavior visibleIfAlso(Component otherComponent) {
        return OnConfigureFunctionalBehaviour.of(c -> c.setVisible(otherComponent.isVisibleInHierarchy()));
    }

    default Behavior enabledIf(ISupplier<Boolean> supplier) {
        return OnConfigureFunctionalBehaviour.of(c -> c.setEnabled(supplier.get()));
    }

    default Behavior enabledIf(IModel<Boolean> model) {
        return OnConfigureFunctionalBehaviour.of(c -> c.setEnabled(model.getObject()));
    }

    default Behavior onConfigure(IConsumer<Component> onConfigure) {
        return OnConfigureFunctionalBehaviour.of(onConfigure);
    }

    default Behavior onComponentTag(IBiConsumer<Component, ComponentTag> onComponentTag) {
        return new OnComponentTagFunctionalBehaviour(onComponentTag);
    }

    default <C extends Component> IAjaxUpdateConfiguration<C> addAjaxUpdate(C component) {
        return addAjaxUpdate(component, null);
    }

    @SuppressWarnings("unchecked")
    default <C extends Component> IAjaxUpdateConfiguration<C> addAjaxUpdate(C component, IBiConsumer<AjaxRequestTarget, Component> onUpdate) {

        final Behavior behavior;

        if (component instanceof RadioChoice<?> || component instanceof CheckBoxMultipleChoice<?> || component instanceof RadioGroup<?> || component instanceof CheckGroup<?>) {
            behavior = new FormChoiceAjaxUpdateBehavior(onUpdate);
            component.add(behavior);

        } else if (component instanceof FormComponent<?>) {
            behavior = new FormComponentAjaxUpdateBehavior("change", onUpdate);
            component.add(behavior);

        } else {
            return null;
        }

        return (IAjaxUpdateConfiguration<C>) behavior;
    }

    default Behavior on(String event, IFunction<Component, CharSequence> scriptFunction) {
        return onReadyScript(comp -> String.format("Wicket.Event.add('%s', '%s', function(event) { %s; });",
                comp.getMarkupId(), event, scriptFunction.apply(comp)));
    }

    default Behavior onReadyScript(ISupplier<CharSequence> scriptSupplier) {
        return onReadyScript(comp -> scriptSupplier.get());
    }

    default Behavior onReadyScript(IFunction<Component, CharSequence> scriptFunction) {
        return onReadyScript(scriptFunction,
                comp -> comp.isVisibleInHierarchy() && comp.isEnabledInHierarchy());
    }

    default Behavior onReadyScript(IFunction<Component, CharSequence> scriptFunction, IFunction<Component, Boolean> isEnabled) {
        return new RenderHeadFunctionalBehavior(scriptFunction, isEnabled);
    }

    default Behavior onEnterDelegate(Component newTarget, String originalTargetEvent) {
        return $b.onReadyScript(c -> JQuery.on(c, "keypress", "if((e.keyCode || e.which) == 13){" +
                (originalTargetEvent != null ? "$(e.target).trigger('" + originalTargetEvent + "');" : "") +
                "e.preventDefault(); " + JQuery.$(newTarget) + ".click();}"));
    }

}
