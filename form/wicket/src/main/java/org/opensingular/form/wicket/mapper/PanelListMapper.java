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

package org.opensingular.form.wicket.mapper;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.opensingular.form.SIList;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.decorator.action.ISInstanceActionCapable;
import org.opensingular.form.decorator.action.ISInstanceActionsProvider;
import org.opensingular.form.view.list.AbstractSViewListWithControls;
import org.opensingular.form.view.list.ButtonAction;
import org.opensingular.form.view.list.SViewListByForm;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.mapper.buttons.ElementsView;
import org.opensingular.form.wicket.mapper.buttons.RemoverButton;
import org.opensingular.form.wicket.mapper.components.ConfirmationModal;
import org.opensingular.form.wicket.mapper.components.MetronicPanel;
import org.opensingular.form.wicket.mapper.decorator.SInstanceActionsPanel;
import org.opensingular.form.wicket.mapper.decorator.SInstanceActionsProviders;
import org.opensingular.form.wicket.model.ReadOnlyCurrentInstanceModel;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.commons.lambda.ISupplier;
import org.opensingular.lib.commons.ui.Icon;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSCol;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSContainer;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSGrid;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSRow;
import org.opensingular.lib.wicket.util.bootstrap.layout.TemplatePanel;
import org.opensingular.lib.wicket.util.resource.DefaultIcons;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.opensingular.form.wicket.mapper.components.MetronicPanel.dependsOnModifier;
import static org.opensingular.lib.wicket.util.util.Shortcuts.$b;
import static org.opensingular.lib.wicket.util.util.Shortcuts.$m;

public class PanelListMapper extends AbstractListMapper implements ISInstanceActionCapable {

    private SInstanceActionsProviders instanceActionsProviders = new SInstanceActionsProviders(this);

    @Override
    public void addSInstanceActionsProvider(int sortPosition, ISInstanceActionsProvider provider) {
        this.instanceActionsProviders.addSInstanceActionsProvider(sortPosition, provider);
    }

    @Override
    public void buildView(WicketBuildContext ctx) {
        final BSContainer<?> parentCol = ctx.getContainer();
        parentCol.appendComponent((id) -> this.newPanel(id, ctx));
    }

    private MetronicPanel newPanel(String id, WicketBuildContext ctx) {
        final IModel<SIList<SInstance>> listModel = new ReadOnlyCurrentInstanceModel<>(ctx);
        final SIList<?> iList = listModel.getObject();
        final IModel<String> label = $m.ofValue(trimToEmpty(iList.asAtr().getLabel()));
        final ISupplier<SViewListByForm> viewSupplier = ctx.getViewSupplier(SViewListByForm.class);

        final SType<?> currentType = ctx.getCurrentInstance().getType();

        addInitialNumberOfLines(currentType, iList, viewSupplier);

        ctx.configureContainer(label);

        return MetronicPanel.MetronicPanelBuilder.build(id,
            (heading, form) -> {
                heading.appendTag("span", new Label("_title", label));

                IFunction<AjaxRequestTarget, List<?>> internalContextListProvider = target -> Arrays.asList(
                    this,
                    RequestCycle.get().find(AjaxRequestTarget.class),
                    listModel,
                    listModel.getObject(),
                    ctx,
                    ctx.getContainer());

                SInstanceActionsPanel.addPrimarySecondaryPanelsTo(
                    heading,
                    this.instanceActionsProviders,
                    listModel,
                    false,
                    internalContextListProvider, ctx.getActionClassifier());

                heading.add($b.visibleIf(() -> !ctx.getHint(HIDE_LABEL)
                    || !this.instanceActionsProviders.actionList(listModel, ctx.getActionClassifier()).isEmpty()));
            },
            (content, form) -> {

                TemplatePanel list = content.newTemplateTag(t -> ""
                    + "    <ul wicket:id='_u' class='list-group list-by-form'>"
                    + "      <li wicket:id='_e' class='list-group-item' style='margin-bottom:15px'>"
                    + "         <div wicket:id='_r'></div>"
                    + "      </li>"
                    + "      <div wicket:id='_empty' class='list-by-form-empty-state'>"
                    + "         <span>Nenhum item foi adicionado</span>"
                    + "      </div>"
                    + "    </ul>");

                final WebMarkupContainer container = new WebMarkupContainer("_u");
                final PanelElementsView elements = new PanelElementsView("_e", listModel, ctx, form, container);
                final WebMarkupContainer empty = new WebMarkupContainer("_empty");

                list
                    .add(container
                        .add(elements
                            .add($b.onConfigure(c -> c.setVisible(!listModel.getObject().isEmpty()))))
                        .add(empty
                            .add($b.onConfigure(c -> c.setVisible(listModel.getObject().isEmpty())))));
                content.getParent()
                    .add(dependsOnModifier(listModel));
            },
            (f, form) -> buildFooter(f, form, ctx));

    }

    private static final class PanelElementsView extends ElementsView {

        private final Form<?>            form;
        private final WicketBuildContext ctx;
        private final ConfirmationModal  confirmationModal;

        private PanelElementsView(String id,
            IModel<SIList<SInstance>> model,
            WicketBuildContext ctx,
            Form<?> form,
            WebMarkupContainer parentContainer) {
            super(id, model, parentContainer);
            this.ctx = ctx;
            this.form = form;
            this.confirmationModal = ctx.getExternalContainer().newComponent(ConfirmationModal::new);
        }

        @Override
        public void renderHead(IHeaderResponse response) {
            super.renderHead(response);
            PackageResourceReference cssFile =
                new PackageResourceReference(this.getClass(), "PanelElementsView.js");
            JavaScriptHeaderItem javascriptItem = JavaScriptHeaderItem.forReference(cssFile);

            response.render(javascriptItem);
            // response.render(OnDomReadyHeaderItem.forScript("appendListItemEvent();"));
        }

        @Override
        protected void populateItem(Item<SInstance> item) {
            BSGrid grid = new BSGrid("_r");

            buildHeader(item, grid);
            buildBody(item, grid);

            item.add(grid);
        }

        private void buildHeader(Item<SInstance> item, BSGrid grid) {

            ISupplier<SViewListByForm> viewSupplier = ctx.getViewSupplier(SViewListByForm.class);

            final BSRow header = grid.newRow();
            header.add($b.classAppender("list-item-header"));
            final BSCol titleGrid = header.newCol(11).newGrid().newColInRow();
            Model<Serializable> model = new Model<Serializable>() {
                @Override
                public Serializable getObject() {
                        return item.getModelObject().toStringDisplay();
                }
            };
            titleGrid.newTemplateTag(tp -> "<span wicket:id='_title' ></span>")
                .add(new Label("_title", model));

            final BSGrid btnGrid = header.newCol(1).newGrid();

            header.add($b.classAppender("list-icons"));

            if (isEdition(viewSupplier) && (viewSupplier.get().getButtonsConfig().isInsertEnabled(item.getModelObject()))) {
                ButtonAction editButton = viewSupplier.get().getButtonsConfig().getInsertButton();
                appendInserirButton(this, form, item, titleGrid, editButton)
                        .add($b.classAppender("pull-left"))
                        .add($b.attrAppender("style", " margin-right:10px", ";"));
            }

            final BSCol btnCell = btnGrid.newColInRow();

            if (isEdition(viewSupplier) && viewSupplier.get().getButtonsConfig().isDeleteEnabled(item.getModelObject())) {
                appendRemoverButton(this, form, item, btnCell, confirmationModal, viewSupplier)
                        .add($b.classAppender("pull-right"));
            }

        }

        private boolean isEdition(ISupplier<SViewListByForm> viewSupplier) {
            return viewSupplier.get() != null && ctx.getViewMode().isEdition();
        }

        private void buildBody(Item<SInstance> item, BSGrid grid) {
            final BSRow body = grid.newRow();
            body.add($b.classAppender("list-item-body"));
            ctx.createChild(body.newCol(12), ctx.getExternalContainer(), item.getModel()).build();
        }

        @Override
        protected RemoverButton appendRemoverButton(ElementsView elementsView, Form<?> form, Item<SInstance> item,
                BSContainer<?> cell, ConfirmationModal confirmationModal, ISupplier<? extends AbstractSViewListWithControls> viewSupplier) {
            ButtonAction deleteButton = viewSupplier.get().getButtonsConfig().getDeleteButton();
            final RemoverButton btn = new RemoverButton("_remover_", form, elementsView, item, confirmationModal, deleteButton);
            final Icon deleteIcon = Optional.ofNullable(deleteButton.getIcon()).orElse(DefaultIcons.REMOVE);
            cell.newTemplateTag(tp -> "<i  wicket:id='_remover_' class='singular-remove-btn " + deleteIcon + "' />")
                    .add(btn);
            return btn;
        }

    }



}