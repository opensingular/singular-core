/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.showcase.component.layout;

import br.net.mirante.singular.form.mform.PackageBuilder;
import br.net.mirante.singular.form.mform.SPackage;
import br.net.mirante.singular.form.mform.STypeComposite;
import br.net.mirante.singular.form.mform.basic.ui.AtrBasic;

public class CaseRowControlGridPackage extends SPackage {

    @Override
    protected void carregarDefinicoes(PackageBuilder pb) {
        super.carregarDefinicoes(pb);

        final STypeComposite<?> testForm = pb.createCompositeType("testForm");

        testForm.addFieldString("nome")
                .as(AtrBasic.class).label("Nome")
                .asAtrBootstrap().colPreference(4);
        testForm.addFieldInteger("idade")
                .as(AtrBasic.class).label("Idade")
                .asAtrBootstrap().colPreference(1);
        testForm.addFieldEmail("email")
                .as(AtrBasic.class).label("E-mail")
                .asAtrBootstrap().newRow()
                .asAtrBootstrap().colPreference(5);
    }

}
