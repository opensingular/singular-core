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

import org.hibernate.Session;
import org.junit.Before;
import org.opensingular.flow.test.support.TestFlowSupport;

import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;

/**
 * Classe base para implementação de teste que executarão um definição de fluxo.
 *
 * @author Daniel C. Bordin on 19/03/2017.
 */
public abstract class TestFlowExecutionSupport extends TestFlowSupport {

    @Before
    public final void setUp() {
        assertNotNull(singularFlowConfigurationBean);
        Flow.setConf(singularFlowConfigurationBean, true);
    }

    /**
     * Executa o código de assertivas duas vezes. A primeira direto no fluxo e novamente depois de recarregar o
     * fluxo a partir do banco.
     */
    protected void assertReloadAssert(FlowInstance pi, Consumer<FlowInstance> assertionsCode) {
        assertionsCode.accept(pi);
        assertionsCode.accept(reload(pi));
    }

    /** Recarrega a instância de fluxo a partir do BD. */
    protected FlowInstance reload(FlowInstance pi) {
        Session session = sessionFactory.getCurrentSession();
        session.flush();
        session.clear();
        return singularFlowConfigurationBean.getFlowInstance(pi.getFullId());
    }

}
