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

import org.opensingular.flow.core.TransitionAccess.TransitionVisibilityLevel;

import java.util.function.Function;

public class UITransitionAccessStrategyImplUI<X extends TaskInstance> implements UITransitionAccessStrategy<X> {

    private final Function<X, TransitionAccess> strategyImpl;

    private UITransitionAccessStrategyImplUI(Function<X, TransitionAccess> strategyImpl) {
        super();
        this.strategyImpl = strategyImpl;
    }

    public static <T extends TaskInstance> UITransitionAccessStrategy<T> of(Function<T, TransitionAccess> strategyImpl) {
        return new UITransitionAccessStrategyImplUI<>(strategyImpl);
    }

    public static <T extends TaskInstance> UITransitionAccessStrategy<T> enabled(boolean enabled, String message) {
        return (instance) -> {
            if (enabled) {
                return new TransitionAccess(TransitionVisibilityLevel.ENABLED_AND_VISIBLE, message);
            } else {
                return new TransitionAccess(TransitionVisibilityLevel.DISABLED_AND_VISIBLE, message);
            }
        };
    }

    public static <T extends TaskInstance> UITransitionAccessStrategy<T> visible(boolean visible) {
        return (instance) -> {
            if (visible) {
                return new TransitionAccess(TransitionVisibilityLevel.ENABLED_AND_VISIBLE, null);
            } else {
                return new TransitionAccess(TransitionVisibilityLevel.DISABLED_AND_HIDDEN, null);
            }
        };
    }

    public static <T extends TaskInstance> UITransitionAccessStrategy<T> sameStrategyOf(final STask<?> task, boolean visible) {
        return (instance) -> {
            SUser user = Flow.getUserIfAvailable();
            if (user != null && task.getAccessStrategy().canExecute(instance, user)) {
                if (visible) {
                    return new TransitionAccess(TransitionVisibilityLevel.ENABLED_AND_VISIBLE, null);
                } else {
                    return new TransitionAccess(TransitionVisibilityLevel.DISABLED_AND_HIDDEN, null);
                }
            } else {
                if (visible) {
                    return new TransitionAccess(TransitionVisibilityLevel.DISABLED_AND_VISIBLE, "Unauthorized action");
                } else {
                    return new TransitionAccess(TransitionVisibilityLevel.DISABLED_AND_HIDDEN, "Unauthorized action");
                }
            }
        };
    }

    @Override
    public TransitionAccess getAccess(X taskInstance) {
        return strategyImpl.apply(taskInstance);
    }
}
