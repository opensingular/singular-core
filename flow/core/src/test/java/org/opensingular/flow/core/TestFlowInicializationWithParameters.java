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

import org.junit.Assert;
import org.junit.Test;
import org.opensingular.flow.core.TestFlowInicializationWithoutParameters.FlowWithInitialization.Steps;
import org.opensingular.flow.core.builder.BuilderStart;
import org.opensingular.flow.core.builder.FlowBuilderImpl;
import org.opensingular.internal.lib.commons.test.SingularTestUtil;
import org.opensingular.internal.lib.commons.util.SingularIOUtils;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Daniel C. Bordin on 18/03/2017.
 */
public class TestFlowInicializationWithParameters extends TestFlowExecutionSupport {

    private static final Date VALUE_DT = new Date();

    private static final String PARAM_FLAG = "paramFlag";
    private static final String PARAM_DT = "paramDt";
    private static final String PARAM_NOCOPY = "paramNoCopy";
    private static final String PARAM_BIG = "paramBig";
    private static final BigDecimal VALUE_BIG = new BigDecimal(10);

    private static boolean startInitializerCalled;

    @Test
    public void corretCall() {
        startInitializerCalled = false;
        StartCall<FlowInstance> startCall = new FlowWithInitializationAndParameters().prepareStartCall()
            .setValue(PARAM_FLAG, 1.5d)
            .setValue(PARAM_BIG, VALUE_BIG)
            .setValue(PARAM_DT, VALUE_DT);

        FlowInstance pi = startCall.createAndStart();

        assertTrue(startInitializerCalled);

        assertReloadAssert(pi, p-> assertions(p).isAtTask(Steps.Second)
                .isVariableValue(PARAM_FLAG, 7.5d)
                .isVariableValue(PARAM_BIG, VALUE_BIG)
                .isVariablesSize(3, 2));
    }

    @Test
    public void callWithoutRequeridParameter() {
        StartCall<FlowInstance> startCall = new FlowWithInitializationAndParameters().prepareStartCall();

        SingularTestUtil.assertException(startCall::createAndStart, SingularFlowInvalidParametersException.class,
                "paramFlag");
    }

    @Test
    public void callParameterValidation() {
        StartCall<FlowInstance> startCall = new FlowWithInitializationAndParameters().prepareStartCall()
                .setValue(PARAM_FLAG, 0.0)
                .setValue(PARAM_BIG, VALUE_BIG.pow(2));

        SingularTestUtil.assertException(startCall::createAndStart, SingularFlowInvalidParametersException.class,
                PARAM_BIG);
    }

    @Test
    public void notBindedParamatersShouldNotBeCopied() {
        StartCall<FlowInstance> startCall = new FlowWithInitializationAndParameters().prepareStartCall()
                .setValue(PARAM_FLAG, 1.5)
                .setValue(PARAM_BIG, VALUE_BIG)
                .setValue(PARAM_NOCOPY, "No");

        FlowInstance pi = startCall.createAndStart();

        assertReloadAssert(pi, p -> assertions(p).isAtTask(Steps.Second).isVariableValue(PARAM_NOCOPY, null)
                .isVariablesSize(3, 2));
    }

    @Test
    public void serialization() {
        StartCall<FlowInstance> startCall = new FlowWithInitializationAndParameters().prepareStartCall()
                .setValue(PARAM_FLAG, 1.5d)
                .setValue(PARAM_BIG, VALUE_BIG)
                .setValue(PARAM_DT, VALUE_DT);

        assertTrue(SParametersEnabled.isAutoBindedToFlowVariable(startCall.getVariable(PARAM_FLAG)));
        startCall = SingularIOUtils.serializeAndDeserialize(startCall, true);
        assertTrue(SParametersEnabled.isAutoBindedToFlowVariable(startCall.getVariable(PARAM_FLAG)));

        FlowInstance pi = startCall.createAndStart();

        assertions(pi).isAtTask(Steps.Second)
                .isVariableValue(PARAM_FLAG, 7.5d)
                .isVariableValue(PARAM_BIG, VALUE_BIG)
                .isVariablesSize(3, 2);

    }

    @DefinitionInfo("WithParameters")
    public static class FlowWithInitializationAndParameters extends FlowDefinition<FlowInstance> {

        public enum StepsIP implements ITaskDefinition {
            First, Second, End;

            @Override
            @Nonnull
            public String getName() {
                return toString();
            }
        }

        public FlowWithInitializationAndParameters() {
            super(FlowInstance.class);
            getVariables().addVariableDouble(PARAM_FLAG);
            getVariables().addVariableBigDecimal(PARAM_BIG);
            getVariables().addVariableStringMultipleLines(PARAM_NOCOPY, PARAM_NOCOPY);
        }

        @Override
        @Nonnull
        protected FlowMap createFlowMap() {
            FlowBuilderImpl f = new FlowBuilderImpl(this);

            f.addJavaTask(StepsIP.First).call(this::doFirst);
            f.addWaitTask(StepsIP.Second);
            f.addEndTask(StepsIP.End);

            f.setStartTask(StepsIP.First).setInitializer(this::flowInitializer).with(this::setupStartParameters);
            f.from(StepsIP.First).go(StepsIP.Second).thenGo(StepsIP.End);

            return f.build();
        }

        private void setupStartParameters(BuilderStart<?> start) {
            start.addParamBindedToFlowVariable(PARAM_FLAG, true);
            start.addParamBindedToFlowVariable(PARAM_BIG, true);
            start.addParamDate(PARAM_DT, false);
            start.addParamStringMultipleLines(PARAM_NOCOPY, PARAM_NOCOPY, false, 100);

            start.setValidator((startCall, validationResult) -> {
               if( startCall.getValueBigDecimal(PARAM_BIG).doubleValue() > 50) {
                   validationResult.addErro(startCall.getVariable(PARAM_BIG), "Valor > 50");
               }
                if( startCall.getValueDouble(PARAM_FLAG) <= 0.0) {
                    validationResult.addErro(startCall.getVariable(PARAM_FLAG), "Valor <= 0");
                }
            });
        }

        private void flowInitializer(FlowInstance instance, StartCall<FlowInstance> startCall) {
            startInitializerCalled = true;
            Double v = startCall.getValueDouble(PARAM_FLAG);
            assertEquals((Double) 1.5d, v);

            //Verifica se for feita a copia automática do auto bind
            assertTrue(SParametersEnabled.isAutoBindedToFlowVariable(startCall.getVariable(PARAM_FLAG)));
            assertEquals(v, instance.getVariableValue(PARAM_FLAG));
            assertEquals(VALUE_BIG, instance.getVariableValue(PARAM_BIG));

            instance.getVariables().setValue(PARAM_FLAG, v * 2);

            instance.start();
        }

        void doFirst(ExecutionContext<FlowInstance> flowInstanceExecutionContext) {
            Double v = flowInstanceExecutionContext.getFlowInstance().getVariables().getValueDouble(PARAM_FLAG, 0.0);
            Assert.assertEquals((Double) 3.0, v);
            Assert.assertEquals(VALUE_BIG, flowInstanceExecutionContext.getFlowInstance().getVariables().getValueBigDecimal(PARAM_BIG));
            flowInstanceExecutionContext.getFlowInstance().getVariables().setValue(PARAM_FLAG, v * 2.5);
        }
    }
}

