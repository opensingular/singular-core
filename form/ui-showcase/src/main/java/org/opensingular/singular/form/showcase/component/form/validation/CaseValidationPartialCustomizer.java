/*
 * Copyright (c) 2016, Singular and/or its affiliates. All rights reserved.
 * Singular PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.singular.form.showcase.component.form.validation;

import static org.opensingular.lib.wicket.util.util.WicketUtils.$b;

import org.apache.wicket.ajax.markup.html.form.AjaxButton;

import org.opensingular.singular.form.showcase.component.CaseCustomizer;
import org.opensingular.singular.form.showcase.component.CaseBase;

public class CaseValidationPartialCustomizer implements CaseCustomizer {

    @Override
    public void customize(CaseBase caseBase) {
        caseBase.getBotoes().add((id, currentInstance) -> {
            final AjaxButton aj = new PartialValidationButton(id, currentInstance);

            aj.add($b.attr("value", "Validação Parcial"));

            return aj;
        });
    }

}