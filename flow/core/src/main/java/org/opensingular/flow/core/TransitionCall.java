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

import org.opensingular.flow.core.variable.ValidationResult;
import org.opensingular.flow.core.variable.VarInstanceMap;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Representa a montagem (preparação) para execução de uma transanção a partir
 * de uma Task específica.
 *
 * @author Daniel C. Bordin
 */
public final class TransitionCall extends CallWithParameters<TransitionCall> {

    private final RefTransition transition;

    TransitionCall(@Nonnull RefTransition transition) {
        this.transition = Objects.requireNonNull(transition);
    }

    /**
     *  Verifica se os parâmetros atuais da chamada atende os requisitos da chamada.
     *  @see STransition#setParametersValidator(STransition.ITransitionParametersValidator)
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        if (! result.hasErros()) {
            result = transition.getTransition().validate(transition, this);
        }
        return result;
    }

    @Override
    protected VarInstanceMap<?,?> newParameters() {
        return transition.getTransition().newTransitionParameters(transition);
    }

    /**
     * Executa a transição sobre a task sendo referenciada.
     */
    public void go() {
        FlowEngine.executeTransition(transition.getOriginTaskInstance(), transition.getTransition(), getParameters());
    }

}
