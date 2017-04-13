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
import org.opensingular.flow.core.ProcessInstance;
import org.opensingular.flow.core.SParametersEnabled;
import org.opensingular.flow.core.STransition;
import org.opensingular.flow.core.TaskInstance;
import org.opensingular.flow.core.TransitionAccessStrategy;
import org.opensingular.flow.core.TransitionAccessStrategyImpl;
import org.opensingular.flow.core.property.MetaDataRef;

@SuppressWarnings({"rawtypes", "unchecked"})
public interface BuilderTransition<SELF extends BuilderTransition<SELF>> extends BuilderParametersEnabled<SELF> {

    public abstract STransition getTransition();

    public default SParametersEnabled getParametersEnabled() {
        return getTransition();
    }

    public FlowBuilder getFlowBuilder();

    public default SELF markAsDefault() {
        getTransition().getOrigin().setDefaultTransition(getTransition());
        return (SELF) self();
    }

    public default SELF thenGo(ITaskDefinition destination) {
        STransition transition = getTransition().thenGo(destination);
        return (SELF) getFlowBuilder().newTransition(transition);
    }

    public default SELF thenGo(String actionName, ITaskDefinition destination) {
        STransition transition = getTransition().thenGo(actionName, destination);
        return (SELF) getFlowBuilder().newTransition(transition);
    }

    public default SELF hideInExecution() {
        getTransition().withAccessControl(TransitionAccessStrategyImpl.enabled(false));
        return self();
    }
    
    public default SELF withAccessControl(TransitionAccessStrategy<? extends TaskInstance> accessStrategy) {
        getTransition().withAccessControl(accessStrategy);
        return self();
    }

    public default SELF defineUserRoleInTransition(BuilderProcessRole<?> processRole) {
        getTransition().defineUserRoleInTransition(processRole.getProcessRole());
        return self();
    }

    public default <K extends ProcessInstance> SELF setParametersInitializer(STransition.ITransitionParametersInitializerProcess<K> parametrosInicializer) {
        getTransition().setParametersInitializer(parametrosInicializer);
        return self();
    }

    public default <K extends ProcessInstance> SELF setParametersValidator(STransition.ITransitionParametersValidatorProcess<K> parametrosValidator) {
        getTransition().setParametersValidator(parametrosValidator);
        return self();
    }

    public default <T> SELF setMetaDataValue(MetaDataRef<T> propRef, T value) {
        getTransition().setMetaDataValue(propRef, value);
        return self();
    }
}