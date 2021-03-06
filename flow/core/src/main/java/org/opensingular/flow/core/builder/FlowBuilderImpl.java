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

import org.opensingular.flow.core.FlowDefinition;
import org.opensingular.flow.core.FlowMap;
import org.opensingular.flow.core.IConditionalTaskAction;
import org.opensingular.flow.core.ITaskDefinition;
import org.opensingular.flow.core.ITaskPredicate;
import org.opensingular.flow.core.SBusinessRole;
import org.opensingular.flow.core.SStart;
import org.opensingular.flow.core.STask;
import org.opensingular.flow.core.STaskEnd;
import org.opensingular.flow.core.STaskHuman;
import org.opensingular.flow.core.STaskJava;
import org.opensingular.flow.core.STaskWait;
import org.opensingular.flow.core.STransition;
import org.opensingular.schedule.IScheduleData;

import javax.annotation.Nonnull;
import java.util.Objects;

public class FlowBuilderImpl extends
        FlowBuilder<FlowDefinition<?>, FlowMap, BuilderTask, BuilderJava<?>, BuilderHuman<?>, BuilderWait<?>, BuilderEnd<?>, BuilderStart<?>, BuilderTransition<?>, BuilderTransitionPredicate<?>, BuilderBusinessRole<?>, ITaskDefinition> {

    public FlowBuilderImpl(FlowDefinition<?> flowDefinition) {
        super(flowDefinition);
    }

    @Override
    protected FlowMap newFlowMap(FlowDefinition<?> flowDefinition) {
        return new FlowMap(flowDefinition);
    }

    @Override
    protected BuilderTask newTask(STask<?> task) {
        return new ImplBuilderTask(this, task);
    }

    @Override
    protected BuilderJava<?> newJavaTask(STaskJava task) {
        return new ImplBuilderJava<>(this, task);
    }

    @Override
    protected BuilderHuman<?> newHumanTask(STaskHuman task) {
        return new ImplBuilderHuman<>(this, task);
    }

    @Override
    protected BuilderWait<?> newWaitTask(STaskWait task) {
        return new ImplBuilderWait<>(this, task);
    }

    @Override
    protected BuilderEnd<?> newEndTask(STaskEnd task) {
        return new ImplBuilderEnd<>(this, task);
    }

    @Override
    protected BuilderStart<?> newStartTask(SStart start) {
        return new ImplBuilderStart<>(start);
    }

    @Override
    protected BuilderTransition<?> newTransition(STransition transition) {
        return new ImplBuilderTransition<>(this, transition);
    }

    @Override
    protected BuilderTransitionPredicate<?> newAutomaticTransition(STransition transition) {
        return new ImplBuilderTransitionPredicate<>(this, transition);
    }

    @Override
    protected BuilderBusinessRole<?> newBusinessRole(SBusinessRole businessRole) {
        return new ImplBuilderBusinessRole<>(businessRole);
    }

    public static class ImplBuilderTask<SELF extends ImplBuilderTask<SELF, TASK>, TASK extends STask<?>> implements BuilderTaskSelf<SELF, TASK> {

        private final FlowBuilder<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> flowBuilder;
        private final TASK                                            task;

        public ImplBuilderTask(FlowBuilder<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> flowBuilder, TASK task) {
            this.flowBuilder = flowBuilder;
            this.task = task;
        }

        @Override
        public TASK getTask() {
            return task;
        }

        @SuppressWarnings("rawtypes")
        protected FlowBuilder getFlowBuilder() {
            return flowBuilder;
        }

        /**
         * Cria uma nova transição da task atual para a task destino informada
         * com o nome informado.
         */
        @Override
        @Nonnull
        public BuilderTransition<?> go(@Nonnull String actionName, @Nonnull ITaskDefinition taskRefDestiny) {
            return getFlowBuilder().addTransition(this, actionName, taskRefDestiny);
        }

        @Override
        public BuilderTransitionPredicate<?> go(ITaskDefinition taskRefDestiny, ITaskPredicate condition) {
            FlowMap flowMap = getFlowBuilder().getFlowMap();
            return getFlowBuilder().newAutomaticTransition(getTask().addAutomaticTransition(condition, flowMap.getTask(taskRefDestiny)));
        }
    }

    protected static class ImplBuilderJava<SELF extends ImplBuilderJava<SELF>> extends ImplBuilderTask<SELF, STaskJava>
            implements BuilderJava<SELF> {
        public ImplBuilderJava(FlowBuilderImpl flowBuilder, STaskJava task) {
            super(flowBuilder, task);
        }
    }

    protected static class ImplBuilderHuman<SELF extends ImplBuilderHuman<SELF>> extends ImplBuilderTask<SELF, STaskHuman>
            implements BuilderHuman<SELF> {
        public ImplBuilderHuman(FlowBuilderImpl flowBuilder, STaskHuman task) {
            super(flowBuilder, task);
        }
    }

    protected static class ImplBuilderWait<SELF extends ImplBuilderWait<SELF>> extends ImplBuilderTask<SELF, STaskWait>
            implements BuilderWait<SELF> {
        public ImplBuilderWait(FlowBuilderImpl flowBuilder, STaskWait task) {
            super(flowBuilder, task);
        }
    }

    protected static class ImplBuilderEnd<SELF extends ImplBuilderEnd<SELF>> extends ImplBuilderTask<SELF, STaskEnd>
            implements BuilderEnd<SELF> {
        public ImplBuilderEnd(FlowBuilderImpl flowBuilder, STaskEnd task) {
            super(flowBuilder, task);
        }
    }

    public static class ImplBuilderStart<SELF extends ImplBuilderStart<SELF>> implements BuilderStart<SELF> {

        private final SStart start;

        public ImplBuilderStart(SStart start) {
            this.start = start;
        }

        @Override
        public SStart getStart() {
            return start;
        }
    }

    @SuppressWarnings({"rawtypes"})
    public static class ImplBuilderTransition<SELF extends ImplBuilderTransition<SELF>> implements
            BuilderTransition<SELF> {
        private final FlowBuilder flowBuilder;

        private final STransition transition;

        public ImplBuilderTransition(FlowBuilder flowBuilder, STransition transition) {
            this.flowBuilder = flowBuilder;
            this.transition = transition;
        }

        @Override
        public STransition getTransition() {
            return transition;
        }

        @Override
        public FlowBuilder getFlowBuilder() {
            return flowBuilder;
        }
    }

    public static class ImplBuilderTransitionPredicate<SELF extends ImplBuilderTransitionPredicate<SELF>> extends ImplBuilderTransition<SELF> implements
            BuilderTransitionPredicate<SELF> {

        public ImplBuilderTransitionPredicate(FlowBuilder flowBuilder, STransition transition) {
            super(flowBuilder, transition);
        }

        @Override
        public BuilderTransition<SELF> setCustomSchedule(IScheduleData scheduleData) {
            for (IConditionalTaskAction automaticAction : getTransition().getOrigin().getAutomaticActions()) {
                if (this.getTransition().getDestination().getAbbreviation().equals(automaticAction.getDestinationTaskAbbreviation())) {
                    automaticAction.setScheduleData(scheduleData);
                    break;
                }
            }
            return this;
        }
    }

    public static class ImplBuilderBusinessRole<SELF extends ImplBuilderBusinessRole<SELF>> implements
            BuilderBusinessRole<SELF> {

        private final SBusinessRole businessRole;

        public ImplBuilderBusinessRole(SBusinessRole businessRole) {
            Objects.requireNonNull(businessRole);
            this.businessRole = businessRole;
        }

        @Override
        public SBusinessRole getBusinessRole() {
            return businessRole;
        }
    }

}
