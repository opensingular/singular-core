/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.showcase.component.layout;

import br.net.mirante.singular.form.mform.PackageBuilder;
import br.net.mirante.singular.form.mform.SIComposite;
import br.net.mirante.singular.form.mform.SPackage;
import br.net.mirante.singular.form.mform.STypeComposite;
import br.net.mirante.singular.form.mform.STypeList;
import br.net.mirante.singular.form.mform.basic.view.SViewListByMasterDetail;
import br.net.mirante.singular.form.mform.core.STypeString;
import br.net.mirante.singular.form.mform.util.comuns.STypeYearMonth;

public class CaseListByMasterDetailColumnsPackage extends SPackage {

    @Override
    protected void carregarDefinicoes(PackageBuilder pb) {

        STypeComposite<?> testForm = pb.createCompositeType("testForm");

        final STypeList<STypeComposite<SIComposite>, SIComposite> experiencias = testForm.addFieldListOfComposite("experienciasProfissionais", "experiencia");
        final STypeComposite<?> experiencia = experiencias.getElementsType();
        final STypeYearMonth dtInicioExperiencia = experiencia.addField("inicio", STypeYearMonth.class, true);
        final STypeYearMonth dtFimExperiencia = experiencia.addField("fim", STypeYearMonth.class);
        final STypeString empresa = experiencia.addFieldString("empresa", true);
        final STypeString cargo = experiencia.addFieldString("cargo", true);
        final STypeString atividades = experiencia.addFieldString("atividades");

        {
            //@destacar:bloco
            experiencias.withView(new SViewListByMasterDetail()
                    .col(empresa, "Empresa em que trabalhou") // Desta forma, será utilizado rótulo personalizado para esta coluna.
                    .col(dtInicioExperiencia) //Nos demais, a coluna terá o mesmo rótulo do tipo que a define.
                    .col(dtFimExperiencia))
            //@destacar:fim
                    .asAtrBasic().label("Experiências profissionais");
            dtInicioExperiencia
                    .asAtrBasic().label("Data inicial")
                    .asAtrBootstrap().colPreference(2);
            dtFimExperiencia
                    .asAtrBasic().label("Data final")
                    .asAtrBootstrap().colPreference(2);
            empresa
                    .asAtrBasic().label("Empresa")
                    .asAtrBootstrap().colPreference(8);
            cargo
                    .asAtrBasic().label("Cargo");
            atividades
                    .withTextAreaView()
                    .asAtrBasic().label("Atividades Desenvolvidas");
        }

    }
}
