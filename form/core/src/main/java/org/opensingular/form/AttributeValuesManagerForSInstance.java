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

package org.opensingular.form;

import org.opensingular.form.calculation.CalculationContextInstanceOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Representa um mapa de valores de atributos associados a um {@link SInstance}.
 *
 * @author Daniel C. Bordin on 01/05/2017.
 */
final class AttributeValuesManagerForSInstance extends AttributeValuesManager<SInstance> {

    AttributeValuesManagerForSInstance(@Nonnull SInstance owner) {
        super(owner);
    }

    @Nullable
    public <V> V getAttributeValue(@Nonnull AttrInternalRef ref, @Nullable Class<V> resultClass) {
        SInstance attribute = get(ref);
        if (attribute != null) {
            return attribute.getValueInTheContextOf(new CalculationContextInstanceOptional(getOwner()), resultClass);
        }
        return getAttributeValueFromType(getOwner(), ref, resultClass);
    }

    @Nullable
    static <V> V getAttributeValueFromType(@Nonnull SInstance instance, @Nonnull AttrInternalRef ref,
            @Nullable Class<V> resultClass) {
        return SAttributeUtil.getAttributeValueInTheContextOf(instance.getType(), instance, ref, resultClass);
    }

    @Override
    void setEntryAsAttribute(@Nonnull SInstance entry, @Nonnull AttrInternalRef ref) {
        entry.setAsAttribute(ref, getOwner());
    }

    @Override
    @Nonnull
    protected SInstance createNewAttribute(@Nonnull AttrInternalRef ref) {
        SType<?> attributeType = SAttributeUtil.getAttributeDefinitionInHierarchy(getOwner().getType(), ref);
        SInstance instanceAtr = attributeType.newInstance(getOwner().getDocument());
        instanceAtr.setAsAttribute(ref, getOwner());
        return instanceAtr;
    }
}
