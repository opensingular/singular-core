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


import org.opensingular.form.persistence.entity.FormAnnotationEntity;
import org.opensingular.form.persistence.entity.FormAnnotationPK;
import org.opensingular.lib.support.persistence.BaseDAO;

public class FormAnnotationDAO extends BaseDAO<FormAnnotationEntity, FormAnnotationPK> {

    public FormAnnotationDAO() {
        super(FormAnnotationEntity.class);
    }

    @Override
    public void delete(FormAnnotationEntity obj) {
        super.delete(obj);
        getSession().flush();
    }

    @Override
    public void saveOrUpdate(FormAnnotationEntity novoObj) {
        super.saveOrUpdate(novoObj);
        getSession().flush();

    }
}