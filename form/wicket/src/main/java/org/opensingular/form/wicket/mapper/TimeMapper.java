package org.opensingular.form.wicket.mapper;

import org.opensingular.form.SInstance;
import org.opensingular.form.type.core.STypeTime;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.model.SIDateTimeModel;
import org.opensingular.form.wicket.behavior.InputMaskBehavior;
import org.opensingular.form.wicket.model.SInstanceValueModel;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSControls;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Mapper for data type responsible for storing time (hour and minutes).
 */
public class TimeMapper extends AbstractControlsFieldComponentMapper {

    @Override
    public Component appendInput(WicketBuildContext ctx, BSControls formGroup, IModel<String> labelModel) {
        final TextField<String> time = new TextField<>("time",
                new SIDateTimeModel.TimeModel(new SInstanceValueModel<>(ctx.getModel())));
        time.add(new Behavior() {
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                final String script = String.format("$('#%s').timepicker(%s)", component.getMarkupId(true), getJSONParams());
                response.render(OnDomReadyHeaderItem.forScript(script));
            }
        });
        time.add(new InputMaskBehavior(InputMaskBehavior.Masks.TIME));
        formGroup.appendInputText(time);
        return time;
    }

    @Override
    public String getReadOnlyFormattedText(IModel<? extends SInstance> model) {
        final SimpleDateFormat format = new SimpleDateFormat(STypeTime.FORMAT);
        if (model.getObject().getValue() instanceof Date) {
            return format.format(model.getObject().getValue());
        }
        return StringUtils.EMPTY;
    }

    private String getJSONParams() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("defaultTime", false);
        jsonObject.put("showMeridian", false);
        return jsonObject.toString();
    }
}