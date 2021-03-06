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
package org.opensingular.form.service;

import org.opensingular.form.SFormUtil;
import org.opensingular.form.SType;
import org.opensingular.form.persistence.dao.FormTypeDAO;
import org.opensingular.form.persistence.entity.FormTypeEntity;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Transactional
public class FormTypeService  {


    @Inject
    private FormTypeDAO formTypeDAO;

    public FormTypeEntity findFormTypeEntity(final SType<?> type){
        return getOrCreateNewFormTypeEntity(type);
    }


    @SuppressWarnings("unchecked")
    private FormTypeEntity getOrCreateNewFormTypeEntity(final SType<?> type) {
        String name = type.getName();
        FormTypeEntity formTypeEntity = formTypeDAO.findFormTypeByAbbreviation(name);
        if (formTypeEntity == null) {
            formTypeEntity = new FormTypeEntity();
            formTypeEntity.setAbbreviation(name);
            formTypeEntity.setLabel(SFormUtil.getTypeLabel((Class<? extends SType<?>>) type.getClass())
                    .orElse(type.getNameSimple()));
            formTypeEntity.setCacheVersionNumber(1L);//TODO VINICIUS.NUNES
            formTypeDAO.saveOrUpdate(formTypeEntity);
        }
        return formTypeEntity;
    }

    public List<FormTypeEntity> listAll() {
        return formTypeDAO.listAll();
    }

    public FormTypeEntity findFormTypeByAbbreviation(String abbreviation) {
        return formTypeDAO.findFormTypeByAbbreviation(abbreviation);
    }

}