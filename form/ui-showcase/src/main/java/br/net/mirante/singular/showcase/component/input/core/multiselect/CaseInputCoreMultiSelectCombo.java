/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.showcase.component.input.core.multiselect;

import br.net.mirante.singular.showcase.component.CaseBase;

import java.io.Serializable;

public class CaseInputCoreMultiSelectCombo extends CaseBase implements Serializable {

    public CaseInputCoreMultiSelectCombo() {
        super("Multi Select", "Combo");
        setDescriptionHtml("Permite a seleção múltipla no formato de um combo. É funcional para listas curtas.");
    }

}