/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.studio.view.page.form.crud;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.util.string.StringValue;
import org.wicketstuff.annotation.mount.MountPath;

import br.net.mirante.singular.studio.view.template.Content;
import br.net.mirante.singular.studio.view.template.Template;

@MountPath("form/edit")
@SuppressWarnings("serial")
public class FormPage extends Template {
    protected static final String TYPE_NAME = "type",
            MODEL_ID                        = "id",
            VIEW_MODE                       = "viewMode",
            ANNOTATION                      = "annotation";

    @Override
    protected Content getContent(String id) {
        StringValue type = getPageParameters().get(TYPE_NAME),
                idExampleData = getPageParameters().get(MODEL_ID),
                viewMode = getPageParameters().get(VIEW_MODE),
                annotation = getPageParameters().get(ANNOTATION);

        return new FormContent(id, type, idExampleData, viewMode, annotation);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(OnDomReadyHeaderItem.forScript("$('#_menuItemDemo').addClass('active');"));
    }

}
