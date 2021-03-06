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

package org.opensingular.form.persistence.dao;

import org.opensingular.form.persistence.entity.FormTypeEntity;
import org.opensingular.lib.support.persistence.BaseDAO;
import org.hibernate.criterion.Restrictions;

public class FormTypeDAO extends BaseDAO<FormTypeEntity, Integer> {

    public FormTypeDAO() {
        super(FormTypeEntity.class);
    }

    public FormTypeEntity findFormTypeByAbbreviation(String abbreviation) {
        return (FormTypeEntity) getSession()
                .createCriteria(FormTypeEntity.class)
                .add(Restrictions.eq("abbreviation", abbreviation))
                .setMaxResults(1)
                .uniqueResult();
    }

}
