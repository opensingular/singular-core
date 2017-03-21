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

import org.opensingular.flow.core.variable.VarDefinition;
import org.opensingular.flow.core.variable.VarDefinitionMap;

/**
 * Classe Base para estrutura de definição do flow que trabalhar com parâmetros para sua execução.
 *
 * @author Daniel C. Bordin on 19/03/2017.
 */
public abstract class MParametersEnabled {

    private VarDefinitionMap<?> parameters;

    abstract FlowMap getFlowMap();

    public final VarDefinitionMap<?> getParameters() {
        if (parameters == null) {
            parameters = getFlowMap().getVarService().newVarDefinitionMap();
        }
        return parameters;
    }

    /**
     * Adiciona um parâmetro que automaticamente atualiza a variável do processo. O parâmetro têm as mesmas
     * definições da variável.
     */
    public MParametersEnabled addParamBindedToProcessVariable(String ref, boolean required) {
        VarDefinition defVar = getFlowMap().getProcessDefinition().getVariables().getDefinition(ref);
        if (defVar == null) {
            throw new SingularFlowException(
                    getFlowMap().createErrorMsg("Variable '" + ref + "' is not defined in process definition."),
                    getFlowMap());
        }
        getParameters().addVariable(defVar.copy()).setRequired(required);
        return this;
    }

}
