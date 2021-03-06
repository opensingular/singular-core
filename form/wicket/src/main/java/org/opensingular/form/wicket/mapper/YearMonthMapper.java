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

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SInstance;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.behavior.InputMaskBehavior;
import org.opensingular.form.wicket.model.SInstanceValueModel;
import org.opensingular.lib.wicket.util.form.YearMonthField;

public class YearMonthMapper extends AbstractDateMapper {

    @Override
    protected Map<String, String> getOptions(IModel<? extends SInstance> model) {
        HashMap<String, String> options = new HashMap<>();
        options.put("data-date-format", "mm/yyyy");
        options.put("data-date-start-view", "months");
        options.put("data-date-min-view-mode", "months");
        options.put("data-date-start-date", "01/1900");
        options.put("data-date-end-date", "12/2999");
        return options;
    }

    @Override
    protected TextField getInputData(IModel<? extends SInstance> model) {
        return new YearMonthField(model.getObject().getName(), new SInstanceValueModel<>(model));
    }

    @Override
    protected InputMaskBehavior getInputMaskBehavior() {
        return new InputMaskBehavior(InputMaskBehavior.Masks.SHORT_DATE);
    }

    @Override
    public String getReadOnlyFormattedText(WicketBuildContext ctx, IModel<? extends SInstance> model) {
        if ((model != null) && (model.getObject() != null)) {
            SInstance instance = model.getObject();
            if (instance.getValue() instanceof YearMonth) {
                YearMonth ym = (YearMonth) instance.getValue();
                return String.format("%02d/%04d", ym.getMonthValue(), ym.getYear());
            }
        }
        return StringUtils.EMPTY;
    }
}
