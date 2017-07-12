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

package org.opensingular.flow.core.defaults;

import org.opensingular.flow.core.FlowDefinition;
import org.opensingular.flow.core.FlowInstance;
import org.opensingular.flow.core.STask;
import org.opensingular.flow.core.SUser;
import org.opensingular.flow.core.TaskAccessStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NullTaskAccessStrategy extends TaskAccessStrategy<FlowInstance> {

    @Override
    public boolean canExecute(FlowInstance instance, SUser user) {
        return true;
    }

    @Override
    public Set<Integer> getFirstLevelUsersCodWithAccess(FlowInstance instancia) {
        return Collections.emptySet();
    }

    @Override
    public List<? extends SUser> listAllocableUsers(FlowInstance instancia) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getExecuteRoleNames(FlowDefinition<?> definicao, STask<?> task) {
        return Collections.emptyList();
    }
}
