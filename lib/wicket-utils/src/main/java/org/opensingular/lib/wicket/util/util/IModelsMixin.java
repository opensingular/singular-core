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

import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.opensingular.lib.commons.lambda.IConsumer;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.commons.lambda.ISupplier;
import org.opensingular.lib.wicket.util.model.ConditionalReadOnlyModel;
import org.opensingular.lib.wicket.util.model.FunctionalLoadableDetachableModel;
import org.opensingular.lib.wicket.util.model.FunctionalReadOnlyModel;
import org.opensingular.lib.wicket.util.model.GetSetFunctionalModel;
import org.opensingular.lib.wicket.util.model.IReadOnlyModel;
import org.opensingular.lib.wicket.util.model.IsGtReadOnlyModel;
import org.opensingular.lib.wicket.util.model.IsNotReadOnlyModel;
import org.opensingular.lib.wicket.util.model.MapperReadOnlyModel;
import org.opensingular.lib.wicket.util.model.ValueModel;

import java.io.Serializable;

@SuppressWarnings({"serial"})
public interface IModelsMixin extends Serializable {
    default <T extends Serializable> ValueModel<T> ofValue() {
        return ofValue(null);
    }

    default <T extends Serializable> ValueModel<T> ofValue(T value) {
        return ofValue(value, it -> it);
    }

    default <T extends Serializable> ValueModel<T> ofValue(T value, IFunction<T, Object> equalsHashArgsFunc) {
        return new ValueModel<>(value, equalsHashArgsFunc);
    }

    default <T> CompoundPropertyModel<T> compoundOf(T obj) {
        return new CompoundPropertyModel<>(obj);
    }

    default <T> CompoundPropertyModel<T> compound(IModel<T> model) {
        return new CompoundPropertyModel<>(model);
    }

    default <T> PropertyModel<T> property(Serializable obj, String expr) {
        return new PropertyModel<>(obj, expr);
    }

    default <T> PropertyModel<T> property(Serializable obj, String expr, Class<T> type) {
        return new PropertyModel<>(obj, expr);
    }

    default <T> IModel<T> conditional(IModel<Boolean> test, IModel<T> ifTrue, IModel<T> ifFalse) {
        return new ConditionalReadOnlyModel<>(test, ifTrue, ifFalse);
    }

    default <T, U> IModel<U> map(IModel<T> rootModel, IFunction<T, U> function) {
        return new MapperReadOnlyModel<>(rootModel, function);
    }

    default <T> IModel<T> get(ISupplier<T> supplier) {
        return new FunctionalReadOnlyModel<>(supplier);
    }

    default <T> IModel<T> getSet(ISupplier<T> getter, IConsumer<T> setter) {
        return new GetSetFunctionalModel<>(getter, setter);
    }

    default <T> LoadableDetachableModel<T> loadable(ISupplier<T> supplier) {
        return new FunctionalLoadableDetachableModel<>(supplier);
    }

    default <T> LoadableDetachableModel<T> loadable(T initialValue, ISupplier<T> supplier) {
        return new FunctionalLoadableDetachableModel<>(initialValue, supplier);
    }

    default IModel<Boolean> isNullOrEmpty(Serializable modelOrValue) {
        return new FunctionalReadOnlyModel<>(() -> WicketUtils.nullOrEmpty(modelOrValue));
    }

    default IModel<Boolean> isNotNullOrEmpty(Serializable modelOrValue) {
        return new FunctionalReadOnlyModel<>(() -> !WicketUtils.nullOrEmpty(modelOrValue));
    }

    default IReadOnlyModel<Boolean> isNot(IModel<Boolean> model) {
        return new IsNotReadOnlyModel(model);
    }

    default <C extends Comparable<C>> IReadOnlyModel<Boolean> isGt(IModel<C> lower, IModel<C> higher) {
        return new IsGtReadOnlyModel<>(lower, higher);
    }

    @SuppressWarnings("unchecked")
    default <T extends Serializable> IModel<T> wrapValue(T valueOrModel) {
        return (valueOrModel instanceof IModel) ? (IModel<T>) valueOrModel : this.ofValue(valueOrModel);
    }
}