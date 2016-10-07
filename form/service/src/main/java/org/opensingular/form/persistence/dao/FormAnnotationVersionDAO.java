package org.opensingular.form.persistence.dao;

import org.opensingular.form.persistence.entity.FormAnnotationVersionEntity;
import org.opensingular.lib.support.persistence.BaseDAO;

public class FormAnnotationVersionDAO extends BaseDAO<FormAnnotationVersionEntity, Long> {

    public FormAnnotationVersionDAO() {
        super(FormAnnotationVersionEntity.class);
    }

}