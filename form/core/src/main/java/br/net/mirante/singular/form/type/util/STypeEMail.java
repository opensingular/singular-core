/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.form.type.util;

import br.net.mirante.singular.form.SInfoType;
import br.net.mirante.singular.form.TypeBuilder;
import br.net.mirante.singular.form.type.core.STypeString;
import br.net.mirante.singular.form.validation.ValidationErrorLevel;
import br.net.mirante.singular.form.validation.validator.InstanceValidators;

@SInfoType(name = "EMail", spackage = SPackageUtil.class)
public class STypeEMail extends STypeString {

    @Override
    protected void onLoadType(TypeBuilder tb) {
        asAtr().label("E-mail");
        addInstanceValidator(ValidationErrorLevel.ERROR, InstanceValidators.email());
    }
}
