/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.form.mform.basic.view;

import java.io.Serializable;

import br.net.mirante.singular.form.mform.SType;

@SuppressWarnings("serial")
public class SView implements Serializable {

    public static final SView DEFAULT = new SView();

    public boolean isApplicableFor(SType<?> type) {
        return true;
    }
}
