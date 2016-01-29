package br.net.mirante.singular.pet.server.wicket.view.content;

import br.net.mirante.singular.pet.module.wicket.view.template.Content;
import org.apache.wicket.model.IModel;

import static br.net.mirante.singular.util.wicket.util.WicketUtils.$m;

public class HomeContent extends Content {

    public HomeContent(String id) {
        super(id);
    }

    public HomeContent(String id, boolean withInfoLink, boolean withBreadcrumb) {
        super(id, withInfoLink, withBreadcrumb);
    }

    @Override
    protected IModel<?> getContentTitlelModel() {
        return $m.ofValue("Página inicial");
    }

    @Override
    protected IModel<?> getContentSubtitlelModel() {
        return $m.ofValue("Página inicial");
    }
}
