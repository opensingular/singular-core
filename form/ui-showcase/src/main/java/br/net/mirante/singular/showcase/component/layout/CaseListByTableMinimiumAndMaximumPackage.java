/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.showcase.component.layout;

import br.net.mirante.singular.form.*;
import br.net.mirante.singular.form.type.core.STypeDate;
import br.net.mirante.singular.form.type.core.STypeString;
import br.net.mirante.singular.form.type.util.STypeYearMonth;
import br.net.mirante.singular.form.view.SViewListByTable;

public class CaseListByTableMinimiumAndMaximumPackage extends SPackage {

    @Override
    protected void carregarDefinicoes(PackageBuilder pb) {

        STypeComposite<?> testForm = pb.createCompositeType("testForm");

        STypeList<STypeComposite<SIComposite>, SIComposite> certificacoes = testForm.addFieldListOfComposite("certificacoes", "certificacao");
        certificacoes.asAtr().label("Certificações");
        STypeComposite<?> certificacao = certificacoes.getElementsType();
        STypeYearMonth dataCertificacao = certificacao.addField("data", STypeYearMonth.class, true);
        STypeString entidadeCertificacao = certificacao.addFieldString("entidade", true);
        STypeDate validadeCertificacao = certificacao.addFieldDate("validade");
        STypeString nomeCertificacao = certificacao.addFieldString("nome", true);
        {
            certificacoes
                    //@destacar:bloco
                    .withMiniumSizeOf(2)
                    .withMaximumSizeOf(3)
                     //@destacar:fim
                    .withView(SViewListByTable::new)
                    .asAtr().label("Certificações");
            certificacao
                    .asAtr().label("Certificação");
            dataCertificacao
                    .asAtr().label("Data")
                    .asAtrBootstrap().colPreference(2);
            entidadeCertificacao
                    .asAtr().label("Entidade")
                    .asAtrBootstrap().colPreference(4);
            validadeCertificacao
                    .asAtr().label("Validade")
                    .asAtrBootstrap().colPreference(2);
            nomeCertificacao
                    .asAtr().label("Nome")
                    .asAtrBootstrap().colPreference(4);
        }
    }
}