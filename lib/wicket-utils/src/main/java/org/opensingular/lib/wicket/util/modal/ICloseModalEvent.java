/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.lib.wicket.util.modal;

import java.util.function.Predicate;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;

public interface ICloseModalEvent {

    boolean matchesBodyContent(Component bodyComponent);

    AjaxRequestTarget getTarget();

    default void bubble(Component component) {
        component.send(component, Broadcast.BUBBLE, this);
    }

    static ICloseModalEvent of(AjaxRequestTarget target, Predicate<Component> predicate) {
        return new ICloseModalEvent() {
            @Override
            public boolean matchesBodyContent(Component bodyComponent) {
                return predicate.test(bodyComponent);
            }
            @Override
            public AjaxRequestTarget getTarget() {
                return target;
            }
        };
    }
}
