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

import javax.annotation.Nonnull;
import java.util.Date;

@SuppressWarnings("unchecked")
public class STaskWait extends STaskUserExecutable<STaskWait> {

    private final IExecutionDateStrategy<FlowInstance> executionDateStrategy;

    public STaskWait(FlowMap flowMap, String name, String abbreviation, IExecutionDateStrategy<?> executionDateStrategy) {
        super(flowMap, name, abbreviation);
        this.executionDateStrategy = (IExecutionDateStrategy<FlowInstance>) executionDateStrategy;
    }

    public Date getExecutionDate(FlowInstance instance, TaskInstance taskInstance) {
        return executionDateStrategy.apply(instance, taskInstance);
    }

    public boolean hasExecutionDateStrategy() {
        return executionDateStrategy != null;
    }

    @Override
    @Nonnull
    public <T extends FlowInstance> STaskWait withTargetDate(@Nonnull IExecutionDateStrategy<T> targetDateExecutionStrategy) {
        if (executionDateStrategy != null) {
            throw new SingularFlowException("Tarefas agendadas não suportam data alvo.", this);
        }
        super.withTargetDate(targetDateExecutionStrategy);
        return this;
    }

    @Override
    public boolean isImmediateExecution() {
        return false;
    }

    @Override
    public boolean canReallocate() {
        return false;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.WAIT;
    }

    @Override
    void verifyConsistency() {
        super.verifyConsistency();
        if (getExecutionPage() != null && getAccessStrategy() == null) {
            throw new SingularFlowException(
                    createErrorMsg("Não foi definida a estrategia de verificação de acesso da tarefa"), this);
        }
        if(getTransitions().size() > 1 && getDefaultTransition() == null && hasExecutionDateStrategy()){
            throw new SingularFlowException(createErrorMsg("A transição default não foi definida"), this)
                    .addTransitions(this);
        }
    }
}
