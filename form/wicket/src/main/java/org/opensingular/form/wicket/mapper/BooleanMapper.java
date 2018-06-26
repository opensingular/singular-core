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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.opensingular.form.SInstance;
import org.opensingular.form.decorator.action.ISInstanceActionCapable;
import org.opensingular.form.decorator.action.ISInstanceActionsProvider;
import org.opensingular.form.type.basic.SPackageBasic;
import org.opensingular.form.view.SViewCheckBoxLabelAbove;
import org.opensingular.form.wicket.IWicketComponentMapper;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.behavior.DisabledClassBehavior;
import org.opensingular.form.wicket.mapper.behavior.RequiredBehaviorUtil;
import org.opensingular.form.wicket.mapper.decorator.SInstanceActionsPanel;
import org.opensingular.form.wicket.mapper.decorator.SInstanceActionsProviders;
import org.opensingular.form.wicket.model.AttributeModel;
import org.opensingular.form.wicket.model.SInstanceValueModel;
import org.opensingular.form.wicket.util.ClasspathHtmlLoader;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.commons.table.Alignment;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSContainer;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSControls;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSWellBorder;
import org.opensingular.lib.wicket.util.bootstrap.layout.TemplatePanel;

import static org.opensingular.form.wicket.mapper.SingularEventsHandlers.FUNCTION.ADD_TEXT_FIELD_HANDLERS;

public class BooleanMapper implements IWicketComponentMapper, ISInstanceActionCapable {

    public static final String BS_WELL = "_well";
    private final SInstanceActionsProviders instanceActionsProviders = new SInstanceActionsProviders(this);

    @Override
    public void buildView(WicketBuildContext ctx) {
        switch (ctx.getViewMode()) {
            case READ_ONLY:
                buildForVisualization(ctx);
                break;
            case EDIT:
                buildForEdition(ctx);
                break;
        }
    }

    protected void buildForEdition(WicketBuildContext ctx) {
        final BSControls formGroup = ctx.getContainer().newFormGroup();
        final IModel<? extends SInstance> model = ctx.getModel();
        final CheckBox input = new CheckBox(model.getObject().getName(), new SInstanceValueModel<>(model));
        configureCheckBoxWithLabelByView(ctx, formGroup, input);

        final BSContainer<?> checkboxDiv = input.getMetaData(BSControls.CHECKBOX_DIV);
        if (checkboxDiv != null) { //
            IFunction<AjaxRequestTarget, List<?>> internalContextListProvider = target -> Arrays.asList(
                    BooleanMapper.this,
                    RequestCycle.get().find(AjaxRequestTarget.class),
                    model,
                    model.getObject(),
                    ctx,
                    ctx.getContainer());

            SInstanceActionsPanel.addPrimarySecondaryPanelsTo(
                    checkboxDiv,
                    instanceActionsProviders,
                    model,
                    false,
                    internalContextListProvider);
        }

        input.add(DisabledClassBehavior.getInstance());
        formGroup.appendFeedback(ctx.createFeedbackCompactPanel("feedback"));
        ctx.configure(this, input);

    }

    /**
     * Configure the checkBox Input and the Label according with View.
     *
     * @param ctx       Ctx containing the View.
     * @param formGroup The formGroup to add the checkBox.
     * @param input     The CheckBox Input that should be add in the FormGroup.
     */
    private void configureCheckBoxWithLabelByView(WicketBuildContext ctx, BSControls formGroup,
            CheckBox input) {
        final IModel<? extends SInstance> model = ctx.getModel();
        Label label;
        if (ctx.getView() instanceof SViewCheckBoxLabelAbove) {
            label = createLabel(ctx);
            BSControls labelBar = createLabelBar(label);
            formGroup.appendLabel(labelBar);

            final AttributeModel<String> subtitle = new AttributeModel<>(model, SPackageBasic.ATR_SUBTITLE);
            createSubTitle(formGroup, subtitle);

            formGroup.appendCheckboxWithoutLabel(input, ((SViewCheckBoxLabelAbove) ctx.getView()).getAlignment());
        } else {
            final AttributeModel<String> labelModel = new AttributeModel<>(model, SPackageBasic.ATR_LABEL);
            label = buildLabel("_", labelModel);
            formGroup.appendCheckbox(input, label, null);
        }


        adjustJSEvents(ctx, label);
        label.add(new ClassAttributeModifier() {
            @Override
            protected Set<String> update(Set<String> oldClasses) {
                return RequiredBehaviorUtil.updateRequiredClasses(oldClasses, model.getObject());
            }
        });
    }

    protected void buildForVisualization(WicketBuildContext ctx) {
        final BSControls formGroup = ctx.getContainer().newFormGroup();
        final IModel<? extends SInstance> model = ctx.getModel();
        final Boolean checked;

        final SInstance mi = model.getObject();
        if ((mi != null) && (mi.getValue() != null)) {
            checked = (Boolean) mi.getValue();
        } else {
            checked = Boolean.FALSE;
        }

        String clazz = checked ? "fa fa-check-square" : "fa fa-square-o";
        String idSuffix = (mi != null) ? mi.getName() : StringUtils.EMPTY;

        if (ctx.getView() instanceof SViewCheckBoxLabelAbove) {
            formGroup.appendLabel(createLabel(ctx));

            final AttributeModel<String> subtitle = new AttributeModel<>(model, SPackageBasic.ATR_SUBTITLE);
            createSubTitle(formGroup, subtitle);

            TemplatePanel tp = createTagForViewCheckBox(formGroup, clazz, idSuffix, ((SViewCheckBoxLabelAbove) ctx.getView()).getAlignment());
            final BSWellBorder wellBorder = BSWellBorder.small(BS_WELL + idSuffix);
            tp.add(wellBorder.add(new WebMarkupContainer("label").setVisible(false)));
        } else {
            TemplatePanel tp = createTagForViewCheckBox(formGroup, clazz, idSuffix, null);
            final BSWellBorder wellBorder = BSWellBorder.small(BS_WELL + idSuffix);
            tp.add(wellBorder.add(buildLabel("label", new AttributeModel<>(model, SPackageBasic.ATR_LABEL))));
        }

    }

    private TemplatePanel createTagForViewCheckBox(BSControls formGroup, String clazz,
            String idSuffix, Alignment alignment) {

        return formGroup.newTemplateTag(t -> new ClasspathHtmlLoader("CheckBoxMapper.html", this.getClass(), false)
                .loadHtml()
                .replaceAll("#STYLE#", configureTextAlignStyle(alignment))
                .replaceAll("#clazz#", clazz)
                .replaceAll("#id#", BS_WELL + idSuffix));


//        return formGroup.newTemplateTag(t -> ""
//                + "<div wicket:id='" + BS_WELL + idSuffix + "' "
//                + configureTextAlignStyle(alignment) + ">"
//                + "   <i class='" + clazz + "'></i>"
//                + (withSpan ? "<span wicket:id='label'></span> " : "")
//                + " </div>");
//    }
    }


    private String configureTextAlignStyle(Alignment alignment) {
        String style = "";
        if (alignment != null) {
            style = "style= 'text-align:" + alignment.name().toLowerCase() + "'";
        }
        return style;
    }

    /**
     * This method is responsible for create the label of checkBox.
     * Be careful, this should be LABEL, can't be BSLabel because the changed of the behavior.
     *
     * @param id         The id of the label.
     * @param labelModel The model of the label.
     * @return Retuns label.
     */
    protected Label buildLabel(String id, AttributeModel<String> labelModel) {
        return (Label) new Label(id, labelModel.getObject())
                .setEscapeModelStrings(false);
    }

    @Override
    public void adjustJSEvents(WicketBuildContext ctx, Component comp) {
        comp.add(new SingularEventsHandlers(ADD_TEXT_FIELD_HANDLERS));
    }

    @Override
    public void addSInstanceActionsProvider(int sortPosition, ISInstanceActionsProvider provider) {
        this.instanceActionsProviders.addSInstanceActionsProvider(sortPosition, provider);
    }
}