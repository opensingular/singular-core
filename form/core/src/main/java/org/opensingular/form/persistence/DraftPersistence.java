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

package org.opensingular.form.persistence;

import org.opensingular.form.SInstance;

/**
 * Controla a persistencia de rascunhos
 */
public interface DraftPersistence {

    /**
     * Insere um novo rascunho.
     *
     * @param instance para criação do form entity
     * @return a chave da nova entidade
     */
    Long insert(SInstance instance);

    /**
     * Atualiza um rascunho já salvo
     *
     * @param instance a instancia do form
     * @param draftCod o codigo da entidade
     */
    void update(SInstance instance, Long draftCod);

}