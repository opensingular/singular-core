/*
 * Copyright (c) 2016, Singular and/or its affiliates. All rights reserved.
 * Singular PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.singular.form.showcase.component.form.core;

import org.opensingular.form.PackageBuilder;
import org.opensingular.form.SPackage;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.type.core.STypeTime;
import org.opensingular.singular.form.showcase.component.CaseItem;
import org.opensingular.singular.form.showcase.component.Group;

/**
 * Componente para inserção de data
 */
@CaseItem(componentName = "Date", subCaseName = "Simples", group = Group.INPUT)
public class CaseInputCoreDatePackage extends SPackage {

    @Override
    protected void onLoadPackage(PackageBuilder pb) {
        STypeComposite<?> tipoMyForm = pb.createCompositeType("testForm");
        tipoMyForm.addFieldDate("inicioDia")
                  .asAtr().label("Data Início");
        tipoMyForm.addField("inicioHora", STypeTime.class)
                .asAtr().label("Hora Início");
    }

}