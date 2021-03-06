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

import org.apache.wicket.model.IChainingModel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SIList;
import org.opensingular.form.SInstance;

import java.io.Serializable;

public abstract class AbstractSInstanceItemListModel<I extends SInstance>
    extends AbstractSInstanceModel<I>
    implements IChainingModel<I> {

    private Serializable rootTarget;

    public AbstractSInstanceItemListModel(Serializable rootTarget) {
        this.rootTarget = rootTarget;
    }

    public int getIndex() {
        return index();
    }

    protected abstract int index();

    @Override
    public I getObject() {
        SIList<I> iList = getRootTarget();
        if (iList == null || getIndex() < 0 || getIndex() >= iList.size()) {
            return null;
        }
        return (I) iList.get(getIndex());
    }

    @SuppressWarnings("unchecked")
    public SIList<I> getRootTarget() {
        return (SIList<I>) ((rootTarget instanceof IModel<?>)
            ? ((IModel<?>) rootTarget).getObject()
            : rootTarget);
    }

    @Override
    public void detach() {
        if (rootTarget instanceof IDetachable) {
            ((IDetachable) rootTarget).detach();
        }
    }

    @Override
    public void setChainedModel(IModel<?> rootModel) {
        this.rootTarget = rootModel;
    }
    @Override
    public IModel<?> getChainedModel() {
        return (rootTarget instanceof IModel) ? (IModel<?>) rootTarget : null;
    }

    @Override
    public int hashCode() {
        final I object = this.getObject();
        final int prime = 31;
        int result = 1;
        result = prime * result + ((object == null) ? 0 : object.getPathFull().hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        final AbstractSInstanceItemListModel<?> other = (AbstractSInstanceItemListModel<?>) obj;
        final I object = this.getObject();
        final I otherObject = (I) other.getObject();

        if (object == null) {
            if (otherObject != null)
                return false;
        } else if (!object.getPathFull().equals(otherObject.getPathFull()))
            return false;
        if (getIndex() != other.getIndex())
            return false;
        return true;
    }
}
