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

package org.opensingular.lib.wicket.util.bootstrap.datepicker;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.convert.IConverter;
import org.opensingular.lib.commons.lambda.IConsumer;
import org.opensingular.lib.wicket.util.behavior.DatePickerInitBehaviour;
import org.opensingular.lib.wicket.util.behavior.DatePickerInitBehaviour.DatePickerSettings;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSInputGroup;
import org.opensingular.lib.wicket.util.resource.DefaultIcons;

import java.util.Date;

import static org.opensingular.lib.wicket.util.util.WicketUtils.$b;
import static org.opensingular.lib.wicket.util.util.WicketUtils.$m;

public class BSDatepickerInputGroup extends BSInputGroup {

    public enum ViewMode {
        DAYS, MONTH, YEAR, DECADE, CENTURY, MILLENIUM;

        public String toString() {
            return name().toLowerCase();
        }
    }

    public static final String DEFAULT_DATE_FORMAT     = "dd/mm/yyyy";
    public static final String DEFAULT_START_DATE      = "01/01/1900";
    public static final String DEFAULT_END_DATE        = "31/12/2999";
    public static final String DEFAULT_DATE_VIEW_START = "days";

    private Component textfield;
    private Component button;

    private String   dateFormat = DEFAULT_DATE_FORMAT;
    private String   startDate  = DEFAULT_START_DATE;
    private String   endDate    = DEFAULT_END_DATE;
    private ViewMode startView  = ViewMode.DAYS;
    private ViewMode minView    = ViewMode.DAYS;
    private ViewMode maxView    = ViewMode.MILLENIUM;

    private DatePickerSettings datePickerSettings;
    private IConsumer<? extends Component> textFieldConfigurer = IConsumer.noop();
    private IConverter<Date> converter;

    public BSDatepickerInputGroup(String id, DatePickerSettings datePickerSettings) {
        super(id);
        this.datePickerSettings = datePickerSettings;
    }

    public BSDatepickerInputGroup(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        initialize();
    }

    private void initialize() {
        if (textfield == null) {
            textfield = newTextField(this.getId());
            textfield.setMetaData(BSDatepickerConstants.KEY_CONTAINER, this);
            add($b.attr("data-date-format", $m.get(this::getDateFormat)));
            add($b.attr("data-date-start-date", $m.get(this::getStartDate)));
            add($b.attr("data-date-end-date", $m.get(this::getEndDate)));
            add($b.attr("data-date-start-view", $m.get(this::getStartView)));
            add($b.attr("data-date-min-view-mode", $m.get(this::getMinView)));
            add($b.attr("data-date-max-view-mode", $m.get(this::getMaxView)));

            appendInputText(textfield);
            button = newButtonAddon(DefaultIcons.CALENDAR);
            add(new DatePickerInitBehaviour(datePickerSettings));
            add($b.classAppender("date"));
        }
    }

    public static BSDatepickerInputGroup getFromTextfield(Component textfield) {
        return (BSDatepickerInputGroup) textfield.getMetaData(BSDatepickerConstants.KEY_CONTAINER);
    }

    public final Component getTextField() {
        initialize();
        return textfield;
    }

    public final Component getButton() {
        initialize();
        return button;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Component newTextField(String id) {
        TextField<Date> comp = new TextField<Date>(id, Date.class) {
            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                return (IConverter<C>) BSDatepickerInputGroup.this.getConverter();
            }
        };
        ((IConsumer) textFieldConfigurer).accept(comp);
        return comp;
    }

    public IConverter<Date> getConverter() {
        return (converter != null)
                ? converter
                : super.getConverter(Date.class);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public ViewMode getStartView() {
        return startView;
    }

    public ViewMode getMinView() {
        return minView;
    }

    public ViewMode getMaxView() {
        return maxView;
    }

    public BSDatepickerInputGroup setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    public BSDatepickerInputGroup setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public BSDatepickerInputGroup setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public BSDatepickerInputGroup setStartView(ViewMode startView) {
        this.startView = startView;
        return this;
    }

    public BSDatepickerInputGroup setMinView(ViewMode minView) {
        this.minView = minView;
        return this;
    }

    public BSDatepickerInputGroup setMaxView(ViewMode maxView) {
        this.maxView = maxView;
        return this;
    }

    public BSDatepickerInputGroup setConverter(IConverter<Date> converter) {
        this.converter = converter;
        return this;
    }

    public BSDatepickerInputGroup setTextFieldConfigurer(IConsumer<? extends Component> configurer) {
        this.textFieldConfigurer = configurer;
        return this;
    }
}
