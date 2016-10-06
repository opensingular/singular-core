/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.form.wicket.mapper;

import java.time.YearMonth;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.opensingular.form.SInstance;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.behavior.InputMaskBehavior;
import org.opensingular.form.wicket.model.SInstanceValueModel;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSControls;
import org.opensingular.lib.wicket.util.form.YearMonthField;

public class YearMonthMapper extends AbstractControlsFieldComponentMapper {

    public Component appendInput(WicketBuildContext ctx, BSControls formGroup, IModel<String> labelModel) {
        final IModel<? extends SInstance> model = ctx.getModel();

        YearMonthField comp = new YearMonthField(model.getObject().getName(), new SInstanceValueModel<>(model));
        formGroup.appendDatepicker(comp.setLabel(labelModel)
            .setOutputMarkupId(true).add(new InputMaskBehavior(InputMaskBehavior.Masks.SHORT_DATE)),
            new HashMap<String, String>() {
                {
                    put("data-date-format", "mm/yyyy");
                    put("data-date-start-view", "months");
                    put("data-date-min-view-mode", "months");
                    put("data-date-start-date", "01/1900");
                    put("data-date-end-date", "12/2999");
                }
            });
        return comp;
    }

    @Override
    public String getReadOnlyFormattedText(IModel<? extends SInstance> model) {
        if ((model != null) && (model.getObject() != null)) {
            SInstance instancia = model.getObject();
            if (instancia.getValue() instanceof YearMonth) {
                YearMonth ym = (YearMonth) instancia.getValue();
                return String.format("%02d/%04d", ym.getMonthValue(), ym.getYear());
            }
        }
        return StringUtils.EMPTY;
    }
}
