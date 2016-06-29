/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.form.type.core;

import br.net.mirante.singular.commons.lambda.IConsumer;
import br.net.mirante.singular.form.SInfoType;
import br.net.mirante.singular.form.STypeSimple;
import br.net.mirante.singular.form.type.basic.SPackageBasic;
import br.net.mirante.singular.form.view.SViewTextArea;
import org.apache.commons.lang3.StringUtils;

@SInfoType(name = "String", spackage = SPackageCore.class)
public class STypeString extends STypeSimple<SIString, String> {

    public STypeString() {
        super(SIString.class, String.class);
    }

    protected STypeString(Class<? extends SIString> classeInstancia) {
        super(classeInstancia, String.class);
    }

    public boolean getValorAtributoTrim() {
        return getAttributeValue(SPackageBasic.ATR_TRIM);
    }

    public boolean getValorAtributoEmptyToNull() {
        return getAttributeValue(SPackageBasic.ATR_EMPTY_TO_NULL);
    }

    public STypeString withValorAtributoTrim(boolean valor) {
        return (STypeString) with(SPackageBasic.ATR_TRIM, valor);
    }

    /**
     * Configura o tipo para utilizar a view {@link SViewTextArea} e invoca o initializer
     */
    @SafeVarargs
    public final STypeString withTextAreaView(IConsumer<SViewTextArea>... initializers) {
        withView(new SViewTextArea(), initializers);
        return this;
    }

    @Override
    public String convert(Object valor) {
        String s = super.convert(valor);
        if (s != null) {
            if (getValorAtributoEmptyToNull()) {
                if (getValorAtributoTrim()) {
                    s = StringUtils.trimToNull(s);
                } else if (StringUtils.isEmpty(s)) {
                    s = null;
                }
            } else if (getValorAtributoTrim()) {
                s = StringUtils.trim(s);
            }
        }
        return s;
    }

    @Override
    public String convertNotNativeNotString(Object valor) {
        return valor.toString();
    }

}