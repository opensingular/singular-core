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

import org.opensingular.flow.core.property.MetaDataKey;
import org.opensingular.flow.core.variable.VarDefinition;
import org.opensingular.flow.core.variable.VarDefinitionMap;
import org.opensingular.flow.core.variable.VarInstance;

import javax.annotation.Nonnull;

/**
 * Classe Base para estrutura de definição do flow que trabalhar com parâmetros para sua execução.
 *
 * @author Daniel C. Bordin on 19/03/2017.
 */
public abstract class SParametersEnabled {

    private static MetaDataKey<Boolean> AUTO_BIND_VARIABLE = MetaDataKey.of("autoBindToFlowVariable",
            Boolean.class, Boolean.FALSE);

    private VarDefinitionMap<?> parameters;

    abstract FlowMap getFlowMap();

    /** Retorna as definições de variáveis associadas a esse tipo de transição. */
    public final VarDefinitionMap<?> getParameters() {
        if (parameters == null) {
            parameters = getFlowMap().getVarService().newVarDefinitionMap();
        }
        return parameters;
    }

    /**
     * Adiciona um parâmetro que automaticamente atualiza a variável do fluxo. O parâmetro têm as mesmas
     * definições da variável.
     */
    public SParametersEnabled addParamBindedToFlowVariable(String ref, boolean required) {
        VarDefinition defVar = getFlowMap().getFlowDefinition().getVariables().getDefinition(ref);
        if (defVar == null) {
            throw new SingularFlowException("Variable '" + ref + "' is not defined in flow definition.", getFlowMap());
        }
        VarDefinition newVarDef = getParameters().addVariable(defVar.copy());
        newVarDef.setRequired(required);
        newVarDef.setMetaDataValue(AUTO_BIND_VARIABLE, Boolean.TRUE);
        return this;
    }

    /** Verifica se a definição indica que a variável deve ser automaticamente copiada para as variável da instância. */
    final static boolean isAutoBindedToFlowVariable(@Nonnull VarDefinition varDef) {
        return varDef.getMetaDataValue(AUTO_BIND_VARIABLE);
    }

    /** Verifica se a definição indica que a variável deve ser automaticamente copiada para as variável da instância. */
    final static boolean isAutoBindedToFlowVariable(@Nonnull VarInstance var) {
        return isAutoBindedToFlowVariable(var.getDefinition());
    }

    @Nonnull
    final <V> V inject(@Nonnull V target) {
        return getFlowMap().getFlowDefinition().inject(target);
    }
}
