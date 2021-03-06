/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package org.opensingular.flow.test.support;

import org.opensingular.flow.core.ITaskDefinition;
import org.opensingular.flow.core.STask;
import org.opensingular.flow.core.TaskInstance;
import org.opensingular.lib.commons.test.AssertionsBase;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Classe de apoio a construção de asertivas de teste para {@link TaskInstance}.
 *
 * @author Daniel on 18/03/2017.
 */
public class AssertionsTaskInstance extends AssertionsBase<AssertionsTaskInstance, TaskInstance> {

    public AssertionsTaskInstance(TaskInstance target) {
        super(target);
    }

    public AssertionsTaskInstance(Optional<? extends TaskInstance> target) {
        super(target);
    }

    @Override
    protected  Optional<String> generateDescriptionForCurrentTarget(@Nonnull Optional<TaskInstance> current) {
        return current.map(task -> "taskInstance=" + task );
    }

    public AssertionsTaskInstance isAtTask(ITaskDefinition expectedType) {
        if(!getTarget().isAtTask(expectedType)) {
            Optional<STask<?>> currentTtype = getTarget().getFlowTask();
            if (currentTtype.isPresent()) {
                throw new AssertionError(
                        errorMsg("A task não é do tipo esperado", expectedType, currentTtype.get()));
            }
            throw new AssertionError(
                    errorMsg("A task não é do tipo esperado", expectedType, getTarget().getAbbreviation()));
        }
        return this;
    }
}
