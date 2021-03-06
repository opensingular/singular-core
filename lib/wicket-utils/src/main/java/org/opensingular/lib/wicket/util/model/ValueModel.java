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

package org.opensingular.lib.wicket.util.model;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IObjectClassAwareModel;
import org.opensingular.lib.commons.lambda.IFunction;

import java.io.Serializable;
import java.util.Objects;

//Duplica a lógica de org.apache.wicket.model.Model<T> com alguma variações no equals e hashcode()
public final class ValueModel<T extends Serializable> implements IMappingModel<T>, IObjectClassAwareModel<T> {

    /** Backing object. */
    private T object;

    private final IFunction<T, Object> equalsHashArgsFunc;

    public ValueModel(T object) {
        this(object, it -> it);
    }
    public ValueModel(T object, IFunction<T, Object> equalsHashArgsFunc) {
        setObject(object);
        this.equalsHashArgsFunc = equalsHashArgsFunc;
    }

    /**
     * @see org.apache.wicket.model.IModel#getObject()
     */
    @Override
    public T getObject() {
        return object;
    }

    /**
     * Set the model object; calls setObject(java.io.Serializable). The model object must be
     * serializable, as it is stored in the session
     *
     * @param object the model object
     * @see org.apache.wicket.model.IModel#setObject(Object)
     */
    @Override
    public void setObject(final T object) {
        this.object = object;
    }

    /**
     * @see org.apache.wicket.model.IDetachable#detach()
     */
    @Override
    public void detach() {
        if (object instanceof IDetachable) {
            ((IDetachable) object).detach();
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Model:classname=[");
        sb.append(getClass().getName()).append(']');
        sb.append(":object=[").append(object).append(']');
        return sb.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getObjectClass() {
        return object != null ? (Class<T>) object.getClass() : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(equalsHashArgsFunc.apply(this.getObject()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        return (obj instanceof ValueModel<?>) && Objects.deepEquals(equalsHashArgsFunc.apply(this.getObject()),
                equalsHashArgsFunc.apply(((ValueModel<T>) obj).getObject()));
    }
}