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

import org.opensingular.lib.commons.base.SingularUtil;

import javax.annotation.Nonnull;
import java.util.Objects;

@FunctionalInterface
public interface ITaskDefinition {

    @Nonnull
    String getName();

    @Nonnull
    default String getKey() {
        if (this instanceof Enum) {
            return ((Enum) this).name();
        } else {
            return taskNameToTaskKey(getName());
        }
    }

    /**
     * Creates a {@link ITaskDefinition} with the name informed and the key calculated based in the name.
     */
    @Nonnull
    public static ITaskDefinition of(@Nonnull String name) {
        return of(name, taskNameToTaskKey(name));
    }

    @Nonnull
    static String taskNameToTaskKey(@Nonnull String name) {
        return SingularUtil.convertToJavaIdentifier(name).toUpperCase();
    }

    /**
     * Creates a {@link ITaskDefinition} with the name and key informed.
     */
    @Nonnull
    public static ITaskDefinition of(@Nonnull String name, @Nonnull String key) {
        return new ITaskDefinition() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public int hashCode() {
                return key.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                } else if (!(obj instanceof ITaskDefinition)) {
                    return false;
                }
                return Objects.equals(key, ((ITaskDefinition) obj).getKey());
            }
        };
    }
}
