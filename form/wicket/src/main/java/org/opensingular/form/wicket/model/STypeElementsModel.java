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

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SIList;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.STypeList;
import org.opensingular.lib.wicket.util.model.IReadOnlyModel;

import java.io.Serializable;

public class STypeElementsModel
    implements IReadOnlyModel<SType<SInstance>> {

    private final Serializable rootTarget;

    public STypeElementsModel(Serializable rootTarget) {
        this.rootTarget = rootTarget;
    }

    @Override
    public SType<SInstance> getObject() {
        return getElementsType(rootTarget);
    }

    @SuppressWarnings("unchecked")
    public static SType<SInstance> getElementsType(Object obj) {
        if (obj instanceof SIList<?>)
            return ((SIList<SInstance>) obj).getElementsType();
        if (obj instanceof STypeList<?, ?>)
            return ((STypeList<SType<SInstance>, SInstance>) obj).getElementsType();
        if (obj instanceof IModel<?>)
            return getElementsType(((IModel<?>) obj).getObject());

        throw new IllegalArgumentException();
    }

    @Override
    public void detach() {
        if (rootTarget instanceof IDetachable) {
            ((IDetachable) rootTarget).detach();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rootTarget == null) ? 0 : rootTarget.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        STypeElementsModel other = (STypeElementsModel) obj;
        if (rootTarget == null) {
            if (other.rootTarget != null)
                return false;
        } else if (!rootTarget.equals(other.rootTarget))
            return false;
        return true;
    }
}
