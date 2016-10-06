/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.singular.form.wicket.mapper;

import static org.opensingular.singular.form.wicket.mapper.SingularEventsHandlers.FUNCTION.*;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.ErrorLevelFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;

import org.opensingular.singular.commons.lambda.IConsumer;
import org.opensingular.form.SInstance;
import org.opensingular.form.type.basic.SPackageBasic;
import org.opensingular.singular.form.wicket.IWicketComponentMapper;
import org.opensingular.singular.form.wicket.WicketBuildContext;
import org.opensingular.singular.form.wicket.behavior.DisabledClassBehavior;
import org.opensingular.singular.form.wicket.model.AttributeModel;
import org.opensingular.singular.form.wicket.model.SInstanceValueModel;
import org.opensingular.singular.util.wicket.bootstrap.layout.BSControls;
import org.opensingular.singular.util.wicket.bootstrap.layout.BSWellBorder;
import org.opensingular.singular.util.wicket.bootstrap.layout.TemplatePanel;

public class BooleanMapper implements IWicketComponentMapper {

    public void buildView(WicketBuildContext ctx) {

        final IModel<? extends SInstance> model = ctx.getModel();
        final BSControls formGroup = ctx.getContainer().newFormGroup();
        final AttributeModel<String> labelModel = new AttributeModel<>(model, SPackageBasic.ATR_LABEL);

        switch (ctx.getViewMode()) {
            case READ_ONLY:
                buildForVisualization(model, formGroup, labelModel);
                break;
            case EDIT:
                buildForEdition(ctx, model, formGroup, labelModel);
                break;
        }
    }

    private void buildForEdition(WicketBuildContext ctx, IModel<? extends SInstance> model, BSControls formGroup,
                                 AttributeModel<String> labelModel) {

        final CheckBox input = new CheckBox(model.getObject().getName(), new SInstanceValueModel<>(model));
        final Label label = buildLabel("_", labelModel);
        adjustJSEvents(label);
        formGroup.appendCheckbox(input, label);
        input.add(DisabledClassBehavior.getInstance());
        formGroup.appendFeedback(formGroup, new ErrorLevelFeedbackMessageFilter(FeedbackMessage.WARNING), IConsumer.noop());
        ctx.configure(this, input);

        label.add(new ClassAttributeModifier() {
            @Override
            protected Set<String> update(Set<String> oldClasses) {
                if (model.getObject().isRequired()) {
                    oldClasses.add("singular-form-required");
                } else {
                    oldClasses.remove("singular-form-required");
                }
                return oldClasses;
            }
        });
    }

    private void buildForVisualization(IModel<? extends SInstance> model, BSControls formGroup,
                                       AttributeModel<String> labelModel) {
        final Boolean checked;

        final SInstance mi = model.getObject();
        if ((mi != null) && (mi.getValue() != null)) {
            checked = (Boolean) mi.getValue();
        } else {
            checked = false;
        }

        String clazz = checked ? "fa fa-check-square" : "fa fa-square-o";
        String idSuffix = (mi != null) ? mi.getName() : StringUtils.EMPTY;
        TemplatePanel tp = formGroup.newTemplateTag(t -> ""
            + "<div wicket:id='" + "_well" + idSuffix + "'>"
            + "   <i class='" + clazz + "'></i> <span wicket:id='label'></span> "
            + " </div>");
        final BSWellBorder wellBorder = BSWellBorder.small("_well" + idSuffix);
        tp.add(wellBorder.add(buildLabel("label", labelModel)));
    }

    protected Label buildLabel(String id, AttributeModel<String> labelModel) {
        return (Label) new Label(id, labelModel.getObject())
            .setEscapeModelStrings(false);
    }

    @Override
    public void adjustJSEvents(Component comp) {
        comp.add(new SingularEventsHandlers(ADD_TEXT_FIELD_HANDLERS, ADD_MOUSEDOWN_HANDLERS));
    }

}