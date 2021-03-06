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

package org.opensingular.flow.persistence.entity.util;

import org.opensingular.flow.core.SUser;
import org.opensingular.flow.persistence.entity.Actor;

public class ActorWrapper {

    private ActorWrapper() {}

    public static SUser wrap(final Actor actor) {
        return new SUser() {

            @Override
            public Integer getCod() {
                return actor.getCod();
            }

            @Override
            public String getSimpleName() {
                return null;
            }

            @Override
            public String getEmail() {
                return null;
            }

            public String getCodUsuario() {
                return actor.getCodUsuario();
            }
        };
    }
}
