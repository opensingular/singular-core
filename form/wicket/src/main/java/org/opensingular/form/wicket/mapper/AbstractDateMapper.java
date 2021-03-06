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

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SInstance;
import org.opensingular.form.view.date.ISViewDate;
import org.opensingular.form.view.date.SViewDate;
import org.opensingular.form.wicket.IAjaxUpdateListener;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.behavior.AjaxUpdateInputBehavior;
import org.opensingular.form.wicket.behavior.InputMaskBehavior;
import org.opensingular.lib.commons.lambda.IConsumer;
import org.opensingular.lib.wicket.util.behavior.DatePickerSettings;
import org.opensingular.lib.wicket.util.behavior.SingularDatePickerSettings;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSControls;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSInputGroup;
import org.opensingular.lib.wicket.util.resource.DefaultIcons;

import static org.opensingular.form.wicket.mapper.SingularEventsHandlers.OPTS_ORIGINAL_PROCESS_EVENT;
import static org.opensingular.form.wicket.mapper.SingularEventsHandlers.OPTS_ORIGINAL_VALIDATE_EVENT;
import static org.opensingular.lib.wicket.util.behavior.DatePickerInitScriptBuilder.JS_CHANGE_EVENT;


@SuppressWarnings("serial")
public abstract class AbstractDateMapper extends AbstractControlsFieldComponentMapper {

    private IConsumer<? extends Component> textFieldConfigurer = IConsumer.noop();

    private TextField inputText;
    private boolean createButton = true;

    @SuppressWarnings("unchecked")
    @Override
    public Component appendInput(WicketBuildContext ctx, BSControls formGroup, IModel<String> labelModel) {
        inputText = createInputText(ctx.getModel(), labelModel);
        BSInputGroup bsInputGroup = (BSInputGroup) formGroup.appendDatepicker(inputText
                , getOptions(ctx.getModel()), getDatePickerSettings(ctx));
        ISViewDate viewDate = ctx.getViewSupplier(SViewDate.class).get();
        if (isCreateButton() && checkModalPickerWillHide(viewDate)) {
            bsInputGroup.newButtonAddon(DefaultIcons.CALENDAR);
        }
        return inputText;
    }

    /**
     * Method responsible for create the input.
     * This method could be used for create a input data configured.
     *
     * @param model      The model of the input.
     * @param labelModel The label of the input.
     */
    public TextField createInputText(IModel<? extends SInstance> model, IModel<String> labelModel) {
        TextField comp = getInputData(model);
        configureInputDateText(labelModel, comp);
        return comp;
    }

    /**
     * Settings for the DatePicker.
     * This settings can be configured using the <code>SViewDate</code>.
     *
     * @param ctx The ctx that contanins model and view.
     * @return Return a DatePickerSettings.
     * @see SingularDatePickerSettings
     */
    protected DatePickerSettings getDatePickerSettings(WicketBuildContext ctx) {
        return null;
    }

    /**
     * The mask of the input, by default will use the FULL_DATE mask.
     *
     * @return
     */
    protected InputMaskBehavior getInputMaskBehavior() {
        return new InputMaskBehavior(InputMaskBehavior.Masks.FULL_DATE);
    }

    /**
     * The input data with the configuration necessary, some converter for example.
     *
     * @param model The model of the instance
     * @return TextField of the date.
     */
    protected abstract TextField getInputData(IModel<? extends SInstance> model);

    /**
     * Method for configure the options of the Bootstrap DatePicker.
     *
     * @param model The model of the instance.
     * @return return a Map with the key (datePicker option) and the value.
     */
    protected Map<String, ? extends Serializable> getOptions(IModel<? extends SInstance> model) {
        return null;
    }

    /**
     * Method responsible for enable or disabled the creation of the button addon.
     * By default the button will be created [true].
     *
     * @return True for create;
     */
    protected boolean isCreateButton() {
        return createButton;
    }

    public void setTextFieldConfigurer(IConsumer<? extends Component> textFieldConfigurer) {
        this.textFieldConfigurer = textFieldConfigurer;
    }

    @Override
    public void addAjaxUpdate(WicketBuildContext ctx, Component component, IModel<SInstance> model, IAjaxUpdateListener listener) {
        addAjaxEvent(model, listener, inputText, ctx.getViewSupplier(SViewDate.class).get());
    }

    /**
     * Method to add AjaxEvent's to the Date Mapper. This event's should be add to works fine with dependsON.
     * If this ajaxEvent don't have, can have a error if have a dependsOn with exists = false.
     *
     * @param model     The model for process and validate.
     * @param listener  The listener for process and validate.
     * @param component The component that will be the ajax Event's adding.
     * @param viewDate  View of the date.
     */
    static void addAjaxEvent(IModel<SInstance> model, IAjaxUpdateListener listener, TextField component, ISViewDate viewDate) {

        SingularEventsHandlers eventsHandlers = new SingularEventsHandlers(SingularEventsHandlers.FUNCTION.ADD_TEXT_FIELD_HANDLERS);

        if (checkModalPickerWillHide(viewDate)) {
            eventsHandlers.setOption(OPTS_ORIGINAL_VALIDATE_EVENT, JS_CHANGE_EVENT)
                    .setOption(OPTS_ORIGINAL_PROCESS_EVENT, JS_CHANGE_EVENT);
        }

        component.add(eventsHandlers)
                .add(AjaxUpdateInputBehavior.forProcess(model, listener))
                .add(AjaxUpdateInputBehavior.forValidate(model, listener));
    }

    /**
     * Method responsible for configure the input of the date Text.
     *
     * @param labelModel The label of the input.
     * @param comp       The textFieldComponent that will be configured.
     */
    private void configureInputDateText(IModel<String> labelModel, TextField comp) {
        if (labelModel != null) {
            comp.setLabel(labelModel);
        }

        comp.add(AttributeAppender.append("autocomplete", "off"))
                .setOutputMarkupId(true)
                .add(getInputMaskBehavior());

        if (textFieldConfigurer != null) {
            ((IConsumer) textFieldConfigurer).accept(comp);
        }
    }

    /**
     * Method responsible for containing the rules of the visible modal picker.
     *
     * @param viewDate The view that containing the logic.
     * @return True if the modal picker will be hide.
     */
    private static boolean checkModalPickerWillHide(ISViewDate viewDate) {
        return viewDate == null || !viewDate.isModalHide();
    }


}
