package org.opensingular.form.wicket.panel;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.opensingular.lib.commons.lambda.IConsumer;
import org.opensingular.lib.wicket.util.ajax.ActionAjaxButton;
import org.opensingular.lib.wicket.util.modal.BSModalBorder.ButtonStyle;

public interface IOpenModalEvent extends Serializable {

    String getModalTitle();

    Component getBodyContent(String id);

    AjaxRequestTarget getTarget();

    Iterator<ButtonDef> getFooterButtons(IConsumer<AjaxRequestTarget> closeCallback);

    public static class ButtonDef {
        public final ButtonStyle      style;
        public final IModel<String>   label;
        public final ActionAjaxButton button;
        public ButtonDef(ButtonStyle style, IModel<String> label, ActionAjaxButton button) {
            this.style = style;
            this.label = label;
            this.button = button;
        }
    }
}
