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

package org.opensingular.form;


import org.opensingular.lib.commons.base.SingularException;
import org.opensingular.form.type.core.STypeString;

public class SingularFormException extends SingularException {

    public SingularFormException() {
    }

    public SingularFormException(String msg) {
        super(msg);
    }

    /**
     * Cria um erro incluindo informações de contexto para o objeto informado. Se o objeto não for um SType ou
     * SInstance, então simplesmente dá um toString() no mesmo.
     */
    public SingularFormException(String msg, Object target) {
        super(msg);
        add(target);
    }

    /**
     * Cria o erro incluindo informações de contexto da instancia sobre a qual
     * ocorreu o erro.
     */
    public SingularFormException(String msg, SInstance instance) {
        super(msg);
        add(instance);
    }

    /**
     * Cria o erro incluindo informações de contexto do tipo sobre o qual ocorreu o erro.
     */
    public SingularFormException(String msg, SType<?> type) {
        super(msg);
        add(type);
    }

    /**
     * Cria o erro incluindo informações de contexto da instancia sobre a qual
     * ocorreu o erro e permite acrescentar informação adicional.
     */
    public SingularFormException(String msg, SInstance instance, String additionalMsgContext) {
        super(msg);
        add(instance);
        add(additionalMsgContext);
    }

    public SingularFormException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Cria o erro incluindo informações de contexto da instancia sobre a qual
     * ocorreu o erro.
     */
    public SingularFormException(String msg, Throwable cause, SInstance instance) {
        super(msg, cause);
        add(instance);
    }

    public SingularFormException add(SInstance instance) {
        if (instance != null) {
            add("instancia path", instance.getPathFull());
            add("instancia classe", instance.getClass().getName());
            add("instancia type", instance.getType());
            if (! instance.getType().getClass().getPackage().equals(STypeString.class.getPackage())) {
                add("instancia type class", instance.getType().getClass().getName());
            }
        }
        return this;
    }

    public SingularFormException add(SType<?> type) {
        if (type != null) {
            add("type", type);
        }
        return this;
    }

    /**
     * Adiciona um nova linha de informação extra na exception a ser exibida junto com a mensagem da mesma.
     * @param value Valor da informação (pode ser null)
     */
    public SingularFormException add(Object value) {
        if (value instanceof SType) {
            add((SType<?>) value);
        } else if (value instanceof SInstance) {
            add((SInstance) value);
        } else {
            super.add(value);
        }
        return this;
    }

    /**
     * Adiciona um nova linha de informação extra na exception a ser exibida junto com a mensagem da mesma.
     * @param label Label da informação (pode ser null)
     * @param value Valor da informação (pode ser null)
     */
    public SingularFormException add(String label, Object value) {
        return (SingularFormException) super.add(label, value);
    }

    /**
     * Adiciona um nova linha de informação extra na exception a ser exibida junto com a mensagem da mesma.
     * @param level Nível de indentação da informação
     * @param label Label da informação (pode ser null)
     * @param value Valor da informação (pode ser null)
     */
    public SingularFormException add(int level, String label, Object value) {
        return (SingularFormException) super.add(level, label, value);
    }
}
