package br.net.mirante.singular.view.template;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;

import static br.net.mirante.singular.util.wicket.util.WicketUtils.$b;
import static br.net.mirante.singular.util.wicket.util.WicketUtils.$m;
import br.net.mirante.singular.wicket.UIAdminSession;

//@AuthorizeAction(action = Action.RENDER, roles = Roles.ADMIN)
public abstract class Template extends WebPage {

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new Label("pageTitle", new ResourceModel(getPageTitleLocalKey())));
        add(new WebMarkupContainer("pageBody")
                .add($b.attrAppender("class", "page-full-width", " ", $m.ofValue(!withMenu()))));
        queue(new Header("_Header", withMenu(), withTopAction(), withSideBar()));
        queue(withMenu() ? new Menu("_Menu") : new WebMarkupContainer("_Menu"));
        queue(configureContent("_Content"));
        queue(new Footer("_Footer"));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new PackageResourceReference(Template.class, "Template.css")));
        if (withSideBar()) {
            addQuickSidebar(response);
        }
    }

    protected String getUserId() {
        return UIAdminSession.get().getUserId();
    }
    
    protected boolean withTopAction() {
        return false;
    }

    protected boolean withSideBar() {
        return false;
    }

    protected boolean withMenu() {
        return true;
    }

    protected String getPageTitleLocalKey() {
        return "label.page.title.local";
    }

    protected abstract Content getContent(String id);

    private Content configureContent(String contentId) {
        if (withSideBar()) {
            return getContent(contentId).addSideBar();
        } else {
            return getContent(contentId);
        }
    }

    private void addQuickSidebar(IHeaderResponse response) {
        response.render(JavaScriptReferenceHeaderItem.forUrl("/singular-static/resources/metronic/layout4/scripts/quick-sidebar.js"));
        StringBuilder script = new StringBuilder();
        script.append("jQuery(document).ready(function () {\n")
                .append("    QuickSidebar.init(); // init quick sidebar\n")
                .append("});");
        response.render(OnDomReadyHeaderItem.forScript(script));
    }

}