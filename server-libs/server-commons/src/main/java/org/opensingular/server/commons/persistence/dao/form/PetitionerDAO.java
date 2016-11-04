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

package org.opensingular.server.commons.persistence.dao.form;

import org.hibernate.criterion.Restrictions;
import org.opensingular.lib.support.persistence.BaseDAO;
import org.opensingular.server.commons.persistence.entity.form.PetitionerEntity;

public class PetitionerDAO<T extends PetitionerEntity> extends BaseDAO<T, Long> {

    public PetitionerDAO() {
        super((Class<T>) PetitionerEntity.class);
    }

    public PetitionerEntity findPetitionerByExternalId(String externalId) {
        return (PetitionerEntity) getSession()
                .createCriteria(PetitionerEntity.class)
                .add(Restrictions.eq("idPessoa", externalId))
                .setMaxResults(1)
                .uniqueResult();
    }

}