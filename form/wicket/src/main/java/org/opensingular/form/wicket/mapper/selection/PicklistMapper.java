/*
 * Copyright (c) 2016, Singular and/or its affiliates. All rights reserved.
 * Singular PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.form.wicket.mapper.selection;

import org.opensingular.form.SInstance;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSControls;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import java.util.List;

@SuppressWarnings("serial")
public class PicklistMapper extends MultipleSelectMapper {

    @Override
    protected Component formGroupAppender(BSControls formGroup,
                                          IModel<? extends SInstance> model,
                                          final List<?> opcoesValue) {
        return formGroup.appendPicklist(retrieveChoices(model, opcoesValue));
    }
}