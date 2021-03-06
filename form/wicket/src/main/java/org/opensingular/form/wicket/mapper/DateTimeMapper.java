/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.form.wicket.mapper;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SInstance;
import org.opensingular.form.type.core.STypeDateTime;
import org.opensingular.form.view.date.SViewDate;
import org.opensingular.form.view.date.SViewDateTime;
import org.opensingular.form.wicket.IAjaxUpdateListener;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.mapper.datetime.DateTimeContainer;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSControls;

public class DateTimeMapper extends AbstractControlsFieldComponentMapper {

    private DateTimeContainer dateTimeContainer;

    @Override
    public Component appendInput(WicketBuildContext ctx, BSControls formGroup, IModel<String> labelModel) {
        final IModel<? extends SInstance> model = ctx.getModel();
        dateTimeContainer = new DateTimeContainer(model.getObject().getName(), ctx);
        formGroup.appendDiv(dateTimeContainer);
        return dateTimeContainer;
    }

    @Override
    public String getReadOnlyFormattedText(WicketBuildContext ctx, IModel<? extends SInstance> model) {
        final SimpleDateFormat format = new SimpleDateFormat(STypeDateTime.FORMAT);
        if (model.getObject().getValue() instanceof Date) {
            return format.format(model.getObject().getValue());
        }
        return StringUtils.EMPTY;
    }


    @Override
    public void addAjaxUpdate(WicketBuildContext ctx, Component component, IModel<SInstance> model, IAjaxUpdateListener listener) {
        TextField timePicker = dateTimeContainer.getTimeTextField();
        TimeMapper.addAjaxEvent(model, listener, timePicker, ctx.getViewSupplier(SViewDateTime.class).get());

        TextField datePicker = dateTimeContainer.getDateTextField();
        DateMapper.addAjaxEvent(model, listener, datePicker, ctx.getViewSupplier(SViewDate.class).get());
    }
}
