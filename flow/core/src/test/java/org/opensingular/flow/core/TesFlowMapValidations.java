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

package org.opensingular.flow.core;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opensingular.flow.core.TesFlowMapValidations.FlowWithFlowValidation.StepsDI;
import org.opensingular.flow.core.builder.BuilderHuman;
import org.opensingular.flow.core.builder.FlowBuilderImpl;
import org.opensingular.flow.core.property.MetaDataRef;
import org.opensingular.flow.schedule.ScheduleDataBuilder;
import org.opensingular.internal.lib.commons.test.RunnableEx;
import org.opensingular.internal.lib.commons.test.SingularTestUtil;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Daniel C. Bordin on 18/03/2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TesFlowMapValidations {

    private static final MetaDataRef<Boolean> TAG = new MetaDataRef<>("tag", Boolean.class);
    private static final MetaDataRef<Boolean> FLAG = new MetaDataRef<>("flag", Boolean.class);

    private static ValidationConditions condicions = new ValidationConditions();

    @After
    @Before
    public void correctCondicions() {
        condicions = new ValidationConditions();
    }

    @Test
    public void basic() {
        condicions = new ValidationConditions();
        FlowWithFlowValidation definition = new FlowWithFlowValidation();

        assertException(() -> definition.getFlowMap().getTaskByAbbreviationOrException("wrong"), "not found");
        definition.getFlowMap().getTaskByAbbreviationOrException(StepsDI.StepPeople.getKey());
        assertException(() -> definition.getFlowMap().getHumanTaskByAbbreviationOrException("wrong"), "not found");
        definition.getFlowMap().getHumanTaskByAbbreviationOrException(StepsDI.StepPeople.getKey());
        assertException(() -> definition.getFlowMap().getHumanTaskByAbbreviationOrException(StepsDI.StepWait.getKey()), "found, but it is of type");
        assertException(() -> definition.getFlowMap().getTask(StepsDI.NoAndded), "não encontrada");

        List<STask<?>> result = definition.getFlowMap().getTasksWithMetadata(TAG);
        assertEquals(1, result.size());
        assertTrue(result.get(0).is(StepsDI.StepWait));

        assertTrue(definition.getFlowMap().getTask(StepsDI.StepWait).getMetaData().get(FLAG));
    }

    @Test
    public void dontSetStart() {
        condicions = new ValidationConditions();
        condicions.configStart = false;
        assertException(() -> new FlowWithFlowValidation().getFlowMap(), "There is no initial task set");
    }

    @Test
    public void dontConfigHumanTask() {
        condicions = new ValidationConditions();
        condicions.configPeopleAccessStrategy = false;
        assertException(() -> new FlowWithFlowValidation().getFlowMap(), "Não foi definida a estrategia de verificação de acesso da tarefa");

        condicions = new ValidationConditions();
        condicions.configPeopleExecutionPage = false;
        assertException(() -> new FlowWithFlowValidation().getFlowMap(), "Não foi definida a estratégia da página para execução da tarefa");
    }

    @Test
    public void taskWithoutPathToEnd() {
        condicions = new ValidationConditions();
        condicions.createTaskWithoutPathToEnd = true;
        assertException(() -> new FlowWithFlowValidation().getFlowMap(), "no way to reach the end");
    }

    @Test
    public void flowMetaData() {
        condicions = new ValidationConditions();
        FlowWithFlowValidation p = new FlowWithFlowValidation();
        p.getFlowMap().setMetaDataValue(TAG, Boolean.TRUE);
        assertTrue(p.getMetaDataValue(TAG));
        p.getFlowMap().setMetaDataValue(TAG, Boolean.FALSE);
        assertFalse(p.getMetaDataValue(TAG));
    }

    @Test
    public void taskJavaWithoutCall() {
        condicions = new ValidationConditions();
        condicions.javaTaskSetCode = false;
        assertException(() -> new FlowWithFlowValidation().getFlowMap(), "Não foi configurado o código de execução da tarefa");
    }

    @Test
    public void taskJavaWithBatchCall() {
        condicions = new ValidationConditions();
        condicions.javaTaskSetCode = false;
        condicions.javaTaskSetCodeByBlock = true;
        new FlowWithFlowValidation().getFlowMap();
    }

    @DefinitionInfo("WithFlowValidation")
    public static class FlowWithFlowValidation extends FlowDefinition<FlowInstance> {

        public enum StepsDI implements ITaskDefinition {
            StepWait("F1"), StepWait2("F1"), StepPeople("S1"), StepJava("J1"), End("E1"), NoAndded("X");

            private final String name;

            StepsDI(String name) {this.name = name;}

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getKey() {
                return toString();
            }
        }

        public FlowWithFlowValidation() {
            super(FlowInstance.class);
        }

        @Override
        protected FlowMap createFlowMap() {
            FlowBuilderImpl f = new FlowBuilderImpl(this);

            BuilderHuman<?> humanTask = f.addHumanTask(StepsDI.StepPeople);
            if (condicions.configPeopleExecutionPage) {
                humanTask.withExecutionPage((t, u) -> null);
            }
            if (condicions.configPeopleAccessStrategy) {
                humanTask.uiAccess(new DummyAccessStrategy());
            }

            f.addWaitTask(StepsDI.StepWait).setMetaDataValue(TAG, Boolean.TRUE);

            assertException(() -> f.addWaitTask(StepsDI.StepWait), "Task with abbreviation");
            assertException(() -> f.addWaitTask(StepsDI.StepWait2), "Task with name");

            if (condicions.javaTaskSetCodeByBlock) {
                f.addJavaTask(StepsDI.StepJava).batchCall((TaskJavaBatchCall) (task) -> null,
                        ScheduleDataBuilder.buildMinutely(60));
            } else if (condicions.javaTaskSetCode) {
                f.addJavaTask(StepsDI.StepJava).call( (task) -> {});
            } else {
                f.addJavaTask(StepsDI.StepJava);
            }

            f.addEndTask(StepsDI.End);
            assertException(() -> f.addEndTask(StepsDI.End), "already defined");

            assertException(() -> f.build().getStart(), "Task inicial não definida no processo");

            if (condicions.configStart) {
                f.setStartTask(StepsDI.StepWait);
                assertException(() -> f.setStartTask(StepsDI.StepWait), "The start point is already setted");
            }

            f.from(StepsDI.StepWait).go(StepsDI.StepPeople).thenGo(StepsDI.StepJava);

            if (! condicions.createTaskWithoutPathToEnd) {
                f.from(StepsDI.StepJava).go(StepsDI.End);
            }

            f.forEach(builder -> builder.setMetaDataValue(FLAG, Boolean.TRUE));
            return f.build();
        }
    }

    public static void assertException(RunnableEx code, String expectedExceptionMsgPart) {
        SingularTestUtil.assertException(code, SingularFlowException.class, expectedExceptionMsgPart, null);
    }

    private static class ValidationConditions {
        public boolean configStart = true;
        public boolean configPeopleExecutionPage = true;
        public boolean configPeopleAccessStrategy = true;
        public boolean createTaskWithoutPathToEnd = false;
        public boolean javaTaskSetCode = true;
        public boolean javaTaskSetCodeByBlock = false;
    }


    public static class DummyAccessStrategy extends TaskAccessStrategy<FlowInstance> {

        @Override
        public boolean canExecute(FlowInstance instance, SUser user) {
            return false;
        }

        @Override
        public Set<Integer> getFirstLevelUsersCodWithAccess(FlowInstance instance) {
            return null;
        }

        @Override
        public List<? extends SUser> listAllocableUsers(FlowInstance instance) {
            return null;
        }

        @Override
        public List<String> getExecuteRoleNames(FlowDefinition<?> definition, STask<?> task) {
            return null;
        }
    }
}

