package org.opensingular.studio.app.wicket.pages;


import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.opensingular.lib.wicket.util.resource.IconeView;
import org.opensingular.studio.app.menu.GroupMenuEntry;
import org.opensingular.studio.core.menu.MenuEntry;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.List;

import static org.opensingular.lib.wicket.util.util.WicketUtils.$m;

@MountPath("/portal/${path}")
public class StudioPortalPage extends StudioTemplate {

    @Override
    protected void onInitialize() {
        super.onInitialize();
        MenuEntry currentEntry = findCurrentMenuEntry();
        if (currentEntry == null) {
            buildPortal(getStudioMenu().getChildren());
        } else if (currentEntry instanceof GroupMenuEntry) {
            buildPortal(((GroupMenuEntry) currentEntry).getChildren());
        } else {
            throw new RestartResponseException(StudioCRUDPage.class, getPageParameters());
        }
    }

    private void buildPortal(List<MenuEntry> entries) {
        ListView<MenuEntry> listView = new ListView<MenuEntry>("entries", entries) {
            @Override
            protected void populateItem(ListItem<MenuEntry> listItem) {
                final MenuEntry entry = listItem.getModelObject();
                WebMarkupContainer anchor = new WebMarkupContainer("anchor") {
                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                        tag.put("href", entry.getEndpoint());
                    }
                };
                anchor.add(new IconeView("icon", $m.ofValue(entry.getIcon()), null, null));
                anchor.add(new Label("label", entry.getName()));
                listItem.add(anchor);
            }

        };
        add(listView);
    }

    @Override
    protected boolean isWithMenu() {
        return false;
    }
}