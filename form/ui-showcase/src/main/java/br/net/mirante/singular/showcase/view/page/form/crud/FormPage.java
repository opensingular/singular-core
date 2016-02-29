package br.net.mirante.singular.showcase.view.page.form.crud;

import br.net.mirante.singular.showcase.view.template.Content;
import br.net.mirante.singular.showcase.view.template.Template;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.util.string.StringValue;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath("form/edit")
@SuppressWarnings("serial")
public class FormPage extends Template {
    protected static final String TYPE_NAME = "type",
                                  MODEL_KEY = "key",
                                  VIEW_MODE = "viewMode",
                                  ANNOTATION = "annotation";

    @Override
    protected Content getContent(String id) {
        StringValue type = getPageParameters().get(TYPE_NAME),
                    key = getPageParameters().get(MODEL_KEY),
                    viewMode = getPageParameters().get(VIEW_MODE),
                    annotation = getPageParameters().get(ANNOTATION);
        ;
        return new FormContent(id,type, key, viewMode, annotation);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(OnDomReadyHeaderItem.forScript("$('#_menuItemDemo').addClass('active');"));
    }

}