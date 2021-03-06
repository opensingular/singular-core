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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SInstance;
import org.opensingular.form.type.basic.SPackageBasic;
import org.opensingular.form.view.SViewPassword;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.behavior.CountDownBehaviour;
import org.opensingular.form.wicket.model.SInstanceValueModel;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSControls;

import static org.opensingular.lib.wicket.util.util.WicketUtils.$b;

public class PasswordMapper extends AbstractControlsFieldComponentMapper {

    @Override
    public Component appendInput(WicketBuildContext ctx, BSControls formGroup, IModel<String> labelModel) {
        final IModel<? extends SInstance> model = ctx.getModel();

        boolean haveToTrim = ctx.getCurrentInstance().getType().getAttributeValue(SPackageBasic.ATR_TRIM);
        FormComponent<?> comp = new PasswordTextField(model.getObject().getName(),
                new SInstanceValueModel<>(model)) {
            @Override
            protected boolean shouldTrimInput() {
                 return haveToTrim;
            }
        }.setLabel(labelModel);

        if (ctx.getView() instanceof SViewPassword) {
            ((PasswordTextField) comp).setResetPassword(((SViewPassword) ctx.getView()).getResetPassword());
        }

        comp.setRequired(false);

        formGroup.appendInputPassword(comp);

        Optional<Integer> maxSize = Optional.ofNullable(
                model.getObject().getAttributeValue(SPackageBasic.ATR_MAX_LENGTH));
        if (maxSize.isPresent()) {
            comp.add($b.attr("maxlength", maxSize.get()));
            comp.add(new CountDownBehaviour());
        }

        return comp;
    }

    @Override
    protected Component appendReadOnlyInput(WicketBuildContext ctx, BSControls formGroup, IModel<String> labelModel) {
        Component c = super.appendReadOnlyInput(ctx, formGroup, labelModel);
        formGroup.setVisible(false);
        return c;
    }

    @Override
    public String getReadOnlyFormattedText(WicketBuildContext ctx, IModel<? extends SInstance> model) {
        return StringUtils.EMPTY;
    }

}