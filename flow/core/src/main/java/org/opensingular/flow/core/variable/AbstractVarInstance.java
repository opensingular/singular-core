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

package org.opensingular.flow.core.variable;

import org.opensingular.flow.core.property.MetaDataMap;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractVarInstance implements VarInstance {

    private MetaDataMap metaDataMap;

    private final VarDefinition definition;

    private VarInstanceMap<?,?> changeListener;

    public AbstractVarInstance(VarDefinition definition) {
        this.definition = definition;
    }


    @Override
    public VarDefinition getDefinition() {
        return definition;
    }

    @Override
    public String getStringDisplay() {
        return getDefinition().toDisplayString(this);
    }

    @Override
    public String getPersistentString() {
        return getDefinition().toPersistenceString(this);
    }

    @Override
    @Nonnull
    public Optional<MetaDataMap> getMetaDataOpt() {
        return Optional.ofNullable(metaDataMap);
    }

    @Override
    public MetaDataMap getMetaData() {
        if (metaDataMap == null) {
            metaDataMap = new MetaDataMap();
        }
        return metaDataMap;
    }

    @Override
    public void setChangeListner(VarInstanceMap<?,?> changeListener) {
        this.changeListener = changeListener;
    }

    protected final boolean needToNotifyAboutValueChanged() {
        return changeListener != null;
    }

    protected void notifyValueChanged() {
        changeListener.onValueChanged(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [definicao=" + getRef() + ", codigo=" + getValue() + "]";
    }
}
