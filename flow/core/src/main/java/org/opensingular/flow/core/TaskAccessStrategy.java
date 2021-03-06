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

package org.opensingular.flow.core;

import org.opensingular.flow.core.entity.AccessStrategyType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"serial", "unchecked"})
public abstract class TaskAccessStrategy<K extends FlowInstance> {

    public abstract boolean canExecute(K instance, SUser user);

    public <T extends TaskInstance> boolean canExecute(T instance, SUser user) {
        return canExecute((K) instance.getFlowInstance(), user);
    }

    public boolean canVisualize(K instance, SUser user) {
        return canExecute(instance, user);
    }

    public abstract Set<Integer> getFirstLevelUsersCodWithAccess(K instance);

    /**
     * Lists all users that may be allocated to the current task. One case of use of this method is to list on a user's
     * interface the candidates of users to be allocate to the task.
     * @return it may return a empty list.
     */
    @Nonnull
    public abstract List<? extends SUser> listAllowedUsers(@Nonnull K instance);

    @Nonnull
    public abstract List<String> getExecuteRoleNames(FlowDefinition<?> definition, STask<?> task);

    @Nonnull
    public List<String> getVisualizeRoleNames(FlowDefinition<?> definition, STask<?> task) {
        return getExecuteRoleNames(definition, task);
    }

    public SUser getAutomaticAllocatedUser(K instance, TaskInstance task) {
        return null;
    }

    public boolean isNotifyAutomaticAllocation(K instance, TaskInstance task) {
        return true;
    }

    public boolean isReadOnly() {
        return false;
    }

    public TaskAccessStrategy<K> or(TaskAccessStrategy<K> e2) {
        return or(this, e2);
    }

    public AccessStrategyType getType() {
        return AccessStrategyType.E;
    }

    public static <T extends FlowInstance> TaskAccessStrategy<T> or(TaskAccessStrategy<T> e1, TaskAccessStrategy<T> e2) {
        if (e1 == null) {
            return e2;
        } else if (e2 == null) {
            return e1;
        }
        return new DisjunctionTaskAccessStrategy<>(e1, e2);
    }

    private static class DisjunctionTaskAccessStrategy<K extends FlowInstance> extends TaskAccessStrategy<K> {

        private final List<TaskAccessStrategy<K>> disjunction = new ArrayList<>();

        public DisjunctionTaskAccessStrategy(TaskAccessStrategy<K> e1, TaskAccessStrategy<?> e2) {
            add(e1);
            add(e2);
            if(disjunction.isEmpty()){
                throw new SingularFlowException();
            }
        }

        private void add(TaskAccessStrategy<?> e1) {
            if (e1 != null) {
                if (e1 instanceof DisjunctionTaskAccessStrategy) {
                    disjunction.addAll(((DisjunctionTaskAccessStrategy<K>) e1).disjunction);
                } else {
                    disjunction.add((TaskAccessStrategy<K>) e1);
                }
            }
        }

        @Override
        public boolean canExecute(K instance, SUser user) {
            return disjunction.stream().anyMatch(e -> e.canExecute(instance, user));
        }

        @Override
        public boolean canVisualize(K instance, SUser user) {
            return disjunction.stream().anyMatch(e -> e.canVisualize(instance, user));
        }

        @Override
        public Set<Integer> getFirstLevelUsersCodWithAccess(K instance) {
            Set<Integer> cods = new HashSet<>();
            for (TaskAccessStrategy<K> taskAccessStrategy : disjunction) {
                cods.addAll(taskAccessStrategy.getFirstLevelUsersCodWithAccess(instance));
            }
            return cods;
        }

        @Override
        @Nonnull
        public List<SUser> listAllowedUsers(@Nonnull K instance) {
            Set<SUser> users = new LinkedHashSet<>();
            for (TaskAccessStrategy<K> taskAccessStrategy : disjunction) {
                users.addAll(taskAccessStrategy.listAllowedUsers(instance));
            }
            return new ArrayList<>(users);
        }

        @Override
        public SUser getAutomaticAllocatedUser(K instance, TaskInstance task) {
            for (TaskAccessStrategy<K> taskAccessStrategy : disjunction) {
                SUser alocadoAutomatico = taskAccessStrategy.getAutomaticAllocatedUser(instance, task);
                if (alocadoAutomatico != null) {
                    return alocadoAutomatico;
                }
            }
            return null;
        }

        @Override
        public List<String> getExecuteRoleNames(FlowDefinition<?> definition, STask<?> task) {
            return disjunction.stream().flatMap(p -> p.getExecuteRoleNames(definition, task).stream()).collect(Collectors.toList());
        }

        @Override
        public List<String> getVisualizeRoleNames(FlowDefinition<?> definition, STask<?> task) {
            return disjunction.stream().flatMap(p -> p.getVisualizeRoleNames(definition, task).stream()).collect(Collectors.toList());
        }
    }

    private static class VisualizeOnlyTaskAccessStrategy<K extends FlowInstance> extends TaskAccessStrategy<K> {

        private final TaskAccessStrategy<K> original;

        public VisualizeOnlyTaskAccessStrategy(TaskAccessStrategy<K> original) {
            this.original = original;
        }

        @Override
        public boolean canExecute(K instance, SUser user) {
            return false;
        }

        @Override
        public boolean canVisualize(K instance, SUser user) {
            return original.canVisualize(instance, user);
        }

        @Override
        public Set<Integer> getFirstLevelUsersCodWithAccess(K instance) {
            return Collections.emptySet();
        }

        @Override
        @Nonnull
        public List<SUser> listAllowedUsers(@Nonnull K instance) {
            return Collections.emptyList();
        }

        @Override
        public List<String> getExecuteRoleNames(FlowDefinition<?> definition, STask<?> task) {
            return Collections.emptyList();
        }

        @Override
        public List<String> getVisualizeRoleNames(FlowDefinition<?> definition, STask<?> task) {
            return original.getVisualizeRoleNames(definition, task);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    }
}
