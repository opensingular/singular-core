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

package org.opensingular.form.wicket.model;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SInstance;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface ISInstanceAwareModel<T> extends IModel<T> {

    SInstance getSInstance();

    /**
     * Recupera o {@link SInstance} associado ao componente, se o componente tiver um model do tipo {@link
     * ISInstanceAwareModel}.
     */
    @Nonnull
    static Optional<SInstance> optionalSInstance(@Nonnull Component component) {
        return optionalSInstance(component.getDefaultModel());
    }

    /**
     * Recupera o {@link SInstance} associado ao model, se o model for tipo {@linkISInstanceAwareModel}.
     */
    @Nonnull
    static Optional<SInstance> optionalSInstance(IModel<?> model) {
        if (model instanceof ISInstanceAwareModel) {
            return Optional.ofNullable(((ISInstanceAwareModel) model).getSInstance());
        }
        return Optional.empty();
    }

    static <X> Optional<ISInstanceAwareModel<X>> optionalCast(IModel<X> model) {
        if (model instanceof ISInstanceAwareModel) {
            return Optional.of((ISInstanceAwareModel<X>) model);
        }
        return Optional.empty();
    }

    static IModel<SInstance> getInstanceModel(ISInstanceAwareModel<?> model) {
        return new ISInstanceAwareModel<SInstance>() {
            public SInstance getObject() {
                return getSInstance();
            }

            public SInstance getSInstance() {
                return model.getSInstance();
            }

            @Override
            public void setObject(SInstance object) {}

            @Override
            public void detach() {}
        };
    }
}
