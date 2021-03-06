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

package org.opensingular.lib.wicket.util.bootstrap.layout;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

public class BSDropDownMenu extends BSContainer<BSDropDownMenu> {

    private final AttributeAppender dropdownMenuRightBehavior = new AttributeAppender("class", "dropdown-menu-right", " ");

    public BSDropDownMenu(String id) {
        super(id);
        add(new AttributeAppender("class", "dropdown-menu", " "));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        // HACK ajuste para corrigir bug do metronic no alinhamento do menu dropdown
        response.render(CssHeaderItem.forCSS(
            ".dropdown-menu-right{left:auto !important;}",
            BSDropDownMenu.class.getSimpleName() + "_tweaks"));
    }

    public BSDropDownMenu setAlignRight(boolean alignRight) {
        if (alignRight)
            add(dropdownMenuRightBehavior);
        else if (getBehaviors(AttributeAppender.class).contains(dropdownMenuRightBehavior))
            remove(dropdownMenuRightBehavior);
        return this;
    }

    public BSDropDownMenu appendLink(IModel<?> labelModel, MarkupContainer link) {
        return this.appendLink(new Label("_", labelModel), link);
    }
    public BSDropDownMenu appendLink(Component label, MarkupContainer link) {
        return appendTag("li", true, null, itemId ->
            new TemplatePanel(itemId, () -> "<a wicket:id='" + link.getId() + "'><span wicket:id='" + label.getId() + "'></span></a>")
                .add(link
                    .add(label))
                .setRenderBodyOnly(false));
    }
    public BSDropDownMenu appendLink(Component link) {
        return appendTag("li", true, null, itemId ->
            new TemplatePanel(itemId, () -> "<a wicket:id='" + link.getId() + "'></a>")
                .add(link)
                .setRenderBodyOnly(false));
    }
    public BSDropDownMenu appendSeparator() {
        return appendTag("li", true, "role='separator' class='divider'", itemId -> new WebMarkupContainer(itemId));
    }
}
