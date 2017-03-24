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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opensingular.flow.core.TestProcessInicializationDefault.ProcessWithDefaultInitialization.StepsDI;
import org.opensingular.flow.core.builder.FlowBuilderImpl;

/**
 * @author Daniel C. Bordin on 18/03/2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestProcessInicializationDefault extends TestFlowExecutionSupport {

    public static final String FLAG = "flag";

    @Test
    public void simpleStart() {
        ProcessInstance pi = new ProcessWithDefaultInitialization().prepareStartCall().createAndStart();

        assertions(pi).isAtTask(StepsDI.First).isVariableValue(FLAG, null);

        pi = reload(pi);
        assertions(pi).isAtTask(StepsDI.First).isVariableValue(FLAG, null);
    }

    @DefinitionInfo("InicializationDefault")
    public static class ProcessWithDefaultInitialization extends ProcessDefinition<ProcessInstance> {

        public enum StepsDI implements ITaskDefinition {
            First, End;

            @Override
            public String getName() {
                return toString();
            }
        }

        public ProcessWithDefaultInitialization() {
            super(ProcessInstance.class);
            getVariables().addVariableBoolean(FLAG);
        }

        @Override
        protected FlowMap createFlowMap() {
            FlowBuilderImpl f = new FlowBuilderImpl(this);

            f.addWaitTask(StepsDI.First);
            f.addEnd(StepsDI.End);

            f.setStart(StepsDI.First);
            f.from(StepsDI.First).go(StepsDI.End);

            return f.build();
        }
    }
}

