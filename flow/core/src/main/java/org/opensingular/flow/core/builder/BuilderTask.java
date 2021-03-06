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

package org.opensingular.flow.core.builder;

import org.opensingular.flow.core.ITaskDefinition;
import org.opensingular.flow.core.ITaskPredicate;
import org.opensingular.flow.core.STask;
import org.opensingular.flow.core.StartedTaskListener;
import org.opensingular.flow.core.TaskAccessStrategy;
import org.opensingular.flow.core.property.MetaDataKey;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public interface BuilderTask {

    STask<?> getTask();

    /**
     * Cria uma nova transição da task atual para a task destino informada
     */
    @Nonnull
    default BuilderTransition<?> go(@Nonnull ITaskDefinition taskRefDestiny) {
        Objects.requireNonNull(taskRefDestiny);
        return go(taskRefDestiny.getName(), taskRefDestiny);
    }

    @Nonnull
    BuilderTransition<?> go(@Nonnull String actionName, @Nonnull ITaskDefinition taskRefDestiny);

    /**
     * Adds an automatic transition to the given {@param taskRefDestiny} using the {@param condition} predicate to
     * decide when the transition should be made
     */
    BuilderTransitionPredicate<?> go(ITaskDefinition taskRefDestiny, ITaskPredicate condition);

    BuilderTask uiAccess(TaskAccessStrategy<?> accessStrategy);

    BuilderTask addStartedTaskListener(StartedTaskListener startedTaskListener);

    @Nonnull
    <T extends Serializable> BuilderTask setMetaDataValue(@Nonnull MetaDataKey<T> key, T value);

}