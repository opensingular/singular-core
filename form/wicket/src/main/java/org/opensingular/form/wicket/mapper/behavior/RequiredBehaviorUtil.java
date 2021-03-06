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

package org.opensingular.form.wicket.mapper.behavior;

import org.opensingular.form.SInstance;
import org.opensingular.form.type.basic.SPackageBasic;

import java.util.Optional;
import java.util.Set;

public class RequiredBehaviorUtil {

    /**
     * This method is responsible for include the css class for te required field.
     * Note: Prefer use <code>RequiredLabelClassAppender</code>
     *
     * @param oldClasses The class Css.
     * @param instance   The instance of object.
     * @return Set with the classes Css that will be included.
     * @see RequiredLabelClassAppender
     */
    public static Set<String> updateRequiredClasses(Set<String> oldClasses, SInstance instance) {
        Boolean required = Optional.ofNullable(instance.getAttributeValue(SPackageBasic.ATR_REQUIRED)).orElse(Boolean.FALSE);
        if (instance.getType().isList()) {
            final Integer minimumSize = instance.getAttributeValue(SPackageBasic.ATR_MINIMUM_SIZE);
            required |= (minimumSize != null && minimumSize > 0);
        }
        if (required) {
            oldClasses.add("singular-form-required");
        } else {
            oldClasses.remove("singular-form-required");
        }
        return oldClasses;
    }
}
