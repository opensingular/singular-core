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

package org.opensingular.form.wicket.feedback;

import static org.opensingular.lib.wicket.util.util.WicketUtils.*;
import static java.util.stream.Collectors.*;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.opensingular.form.validation.ValidationError;
import org.opensingular.form.validation.ValidationErrorLevel;
import org.opensingular.form.wicket.SValidationFeedbackHandler;
import org.opensingular.lib.wicket.util.jquery.JQuery;
import org.opensingular.lib.wicket.util.util.JavaScriptUtils;

public class SValidationFeedbackIndicator extends WebMarkupContainer implements IFeedback {

    private final Component fence;

    public SValidationFeedbackIndicator(String id, Component fence) {
        super(id);
        this.fence = fence;
        add($b.classAppender("singular-feedback-indicator fa fa-exclamation-triangle text-danger"));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(anyMessage());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        if (this.anyMessage(ValidationErrorLevel.ERROR)) {
            response.render(OnDomReadyHeaderItem.forScript(
                JQuery.$(this) + ".closest('.can-have-error').addClass('has-error');"));
        } else if (this.anyMessage(ValidationErrorLevel.WARNING)) {
            response.render(OnDomReadyHeaderItem.forScript(
                JQuery.$(this) + ".closest('.can-have-error').addClass('has-warning');"));
        } else {
            response.render(OnDomReadyHeaderItem.forScript(
                JQuery.$(this) + ".closest('.can-have-error').removeClass('has-error').removeClass('has-warning');"));
        }

        String errors = getValidationFeedbackHandler().collectNestedErrors().stream()
            .map(it -> it.getMessage())
            .collect(joining("</li><li>", "<ul><li>", "</li></ul>"));
        response.render(OnDomReadyHeaderItem.forScript(""
            + JQuery.$(this)
            + "  .data('content', '" + JavaScriptUtils.javaScriptEscape(errors) + "')"
            + "  .popover({"
            + "    'html':true,"
            + "    'placement':'top',"
            + "    'trigger':'hover'"
            + "  });"));
    }

    public boolean anyMessage() {
        return getValidationFeedbackHandler().containsNestedErrors();
    }

    public boolean anyMessage(ValidationErrorLevel level) {
        return getValidationFeedbackHandler().containsNestedErrors(level);
    }

    protected SValidationFeedbackHandler getValidationFeedbackHandler() {
        return SValidationFeedbackHandler.get(getFence());
    }

    public Component getFence() {
        return fence;
    }

    /**
     * Gets the css class for the given message.
     * 
     * @param message
     *            the message
     * @return the css class; by default, this returns feedbackPanel + the message level, eg
     *         'feedbackPanelERROR', but you can override this method to provide your own
     */
    protected String getCSSClass(final ValidationError message) {
        String cssClass;
        switch (message.getErrorLevel()) {
            case WARNING:
                cssClass = getString(FeedbackMessage.WARNING_CSS_CLASS_KEY);
                break;
            case ERROR:
                cssClass = getString(FeedbackMessage.ERROR_CSS_CLASS_KEY);
                break;
            default:
                cssClass = "feedbackPanel" + message.getErrorLevel();
        }
        return cssClass;
    }
}
