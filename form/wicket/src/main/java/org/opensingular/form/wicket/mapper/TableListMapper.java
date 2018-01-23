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

import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SIList;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.SingularFormException;
import org.opensingular.form.decorator.action.ISInstanceActionCapable;
import org.opensingular.form.decorator.action.ISInstanceActionsProvider;
import org.opensingular.form.view.SView;
import org.opensingular.form.view.SViewListByTable;
import org.opensingular.form.wicket.ISValidationFeedbackHandlerListener;
import org.opensingular.form.wicket.SValidationFeedbackHandler;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.enums.ViewMode;
import org.opensingular.form.wicket.feedback.FeedbackFence;
import org.opensingular.form.wicket.mapper.components.MetronicPanel;
import org.opensingular.form.wicket.mapper.decorator.SInstanceActionsPanel;
import org.opensingular.form.wicket.mapper.decorator.SInstanceActionsProviders;
import org.opensingular.form.wicket.model.ReadOnlyCurrentInstanceModel;
import org.opensingular.form.wicket.model.SInstanceFieldModel;
import org.opensingular.lib.commons.lambda.IBiConsumer;
import org.opensingular.lib.commons.lambda.IConsumer;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSContainer;
import org.opensingular.lib.wicket.util.bootstrap.layout.IBSGridCol.BSGridSize;
import org.opensingular.lib.wicket.util.bootstrap.layout.TemplatePanel;
import org.opensingular.lib.wicket.util.bootstrap.layout.table.BSTDataCell;
import org.opensingular.lib.wicket.util.bootstrap.layout.table.BSTRow;
import org.opensingular.lib.wicket.util.bootstrap.layout.table.BSTSection;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.opensingular.form.wicket.mapper.components.MetronicPanel.dependsOnModifier;
import static org.opensingular.lib.wicket.util.util.Shortcuts.$b;
import static org.opensingular.lib.wicket.util.util.Shortcuts.$m;

public class TableListMapper extends AbstractListMapper implements ISInstanceActionCapable {

    private SInstanceActionsProviders instanceActionsProviders = new SInstanceActionsProviders(this);

    @Override
    public void addSInstanceActionsProvider(int sortPosition, ISInstanceActionsProvider provider) {
        this.instanceActionsProviders.addSInstanceActionsProvider(sortPosition, provider);
    }

    @Override
    public void buildView(WicketBuildContext ctx) {

        if (!(ctx.getView() instanceof SViewListByTable)) {
            throw new SingularFormException("TableListMapper deve ser utilizado com SViewListByTable", ctx.getCurrentInstance());
        }

        if (!(ctx.getCurrentInstance() instanceof SIList)) {
            return;
        }

        ctx.setHint(AbstractControlsFieldComponentMapper.NO_DECORATION, Boolean.TRUE);
        ctx.getContainer().appendComponent((String id) -> buildPanel(ctx, id));
    }

    private TableListPanel buildPanel(WicketBuildContext ctx, String id) {

        final IModel<SIList<SInstance>> list = new ReadOnlyCurrentInstanceModel<>(ctx);
        final SViewListByTable view = (SViewListByTable) ctx.getView();
        final ViewMode viewMode = ctx.getViewMode();
        final Boolean isEdition = viewMode == null || viewMode.isEdition();
        final SIList<SInstance> iList = list.getObject();
        final SType<?> currentType = ctx.getCurrentInstance().getType();

        addInitialNumberOfLines(currentType, iList, view);

        return TableListPanel.TableListPanelBuilder.build(id,
            (h, form) -> buildHeader(h, form, list, ctx, view, isEdition),
            (c, form) -> builContent(c, form, list, ctx, view, isEdition),
            (f, form) -> buildFooter(f, form, ctx));
    }

    private void buildHeader(BSContainer<?> header, Form<?> form, IModel<SIList<SInstance>> list,
        WicketBuildContext ctx, SViewListByTable view, boolean isEdition) {

        final IModel<String> label = $m.ofValue(ctx.getCurrentInstance().getType().asAtr().getLabel());
        final Label title = new Label("_title", label);

        ctx.configureContainer(label);

        header.appendTag("span", title);

        IFunction<AjaxRequestTarget, List<?>> internalContextListProvider = target -> Arrays.asList(
            this,
            RequestCycle.get().find(AjaxRequestTarget.class),
            list,
            list.getObject(),
            ctx,
            ctx.getContainer());

        SInstanceActionsPanel.addPrimarySecondaryPanelsTo(
            header,
            this.instanceActionsProviders,
            list,
            false,
            internalContextListProvider);

        final SType<SInstance> elementsType = list.getObject().getElementsType();

        if (!elementsType.isComposite() && elementsType.asAtr().isRequired()) {
            title.add($b.classAppender("singular-form-required"));
        }

    }

    private void builContent(BSContainer<?> content, Form<?> form, IModel<SIList<SInstance>> list,
                             WicketBuildContext ctx, SViewListByTable view, boolean isEdition) {

        final String markup = ""
            + " <div class='list-table-empty' wicket:id='empty-content'>                                             "
            + "     <p class='list-table-empty-message'>Nenhum item foi adicionado. </p>                             "
            + " </div>                                                                                               "
            + " <div wicket:id='not-empty-content'>                                                                  "
            + "     <table class='table table-condensed table-unstyled' style='margin-bottom:0px'>                   "
            + "          <thead wicket:id='_h'></thead>                                                              "
            + "          <tbody wicket:id='_b'><wicket:container wicket:id='_e'><tr wicket:id='_r'></tr></wicket:container></tbody> "
            + "          <tfoot wicket:id='_ft'>                                                                     "
            + "              <tr><td colspan='99' wicket:id='_fb'></td></tr>                                         "
            + "          </tfoot>                                                                                    "
            + "     </table>                                                                                         "
            + " </div>                                                                                               ";

        final TemplatePanel template = content.newTemplateTag(tp -> markup);

        final WebMarkupContainer emptyContent = new WebMarkupContainer("empty-content");
        emptyContent.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);
                component.setVisible(list.getObject().isEmpty());
            }
        });
        template.add(emptyContent);

        final WebMarkupContainer notEmptyContent = new WebMarkupContainer("not-empty-content");

        final BSTSection tableHeader = new BSTSection("_h").setTagName("thead");
        final WebMarkupContainer tableBody = new WebMarkupContainer("_b");
        final ElementsView tableRows = new TableElementsView("_e", list, ctx, form, tableBody);
        final WebMarkupContainer tableFooter = new WebMarkupContainer("_ft");
        final BSContainer<?> footerBody = new BSContainer<>("_fb");
        final SType<SInstance> elementsType = list.getObject().getElementsType();

        notEmptyContent.add($b.onConfigure(c -> c.setVisible(!list.getObject().isEmpty())));

        //        content.add($b.attrAppender("style", "padding: 15px 15px 10px 15px", ";"));

        if (elementsType.isComposite()) {

            final STypeComposite<?> compositeElementsType = (STypeComposite<?>) elementsType;
            final BSTRow row = tableHeader.newRow();

            if (view.isInsertEnabled() && isEdition) {
                row.newTHeaderCell($m.ofValue(""));
            }

            Collection<SType<?>> fields = compositeElementsType.getFields();
            int sumWidthPref = fields.stream().mapToInt((x) -> x.asAtrBootstrap().getColPreference(1)).sum();

            IConsumer<SType<?>> columnCallback = field -> {
                final Integer preferentialWidth = field.asAtrBootstrap().getColPreference(1);
                final IModel<String> headerModel = $m.ofValue(field.asAtr().getLabel());
                final BSTDataCell cell = row.newTHeaderCell(headerModel);
                final String width = String.format("width:%.0f%%;", (100.0 * preferentialWidth) / sumWidthPref);
                final boolean requiredField = field.asAtr().isRequired();

                ctx.configureContainer(headerModel);

                cell.setInnerStyle(width);
                cell.add(new ClassAttributeModifier() {
                    @Override
                    protected Set<String> update(Set<String> oldClasses) {
                        if (requiredField && isEdition) {
                            oldClasses.add("singular-form-required");
                        } else {
                            oldClasses.remove("singular-form-required");
                        }
                        return oldClasses;
                    }
                });
            };

            if (view.isRenderCompositeFieldsAsColumns()) {
                for (SType<?> field : fields)
                    columnCallback.accept(field);
            } else {
                columnCallback.accept(compositeElementsType);
            }
        }

        tableFooter.setVisible(!(view.isNewEnabled() && isEdition));

        template
            .add(notEmptyContent
                .add(tableHeader)
                .add(tableBody
                    .add(tableRows))
                .add(tableFooter
                    .add(footerBody)));

        content.getParent().add(dependsOnModifier(list));
    }

    private static final class TableElementsView extends ElementsView {

        private final WicketBuildContext ctx;
        private final SView              view;
        private final Form<?>            form;

        private TableElementsView(String id, IModel<SIList<SInstance>> model, WicketBuildContext ctx, Form<?> form,
                                  WebMarkupContainer parentContainer) {
            super(id, model, parentContainer);
            super.setRenderedChildFunction(c -> ((MarkupContainer) c).get("_r"));
            this.ctx = ctx;
            this.view = ctx.getView();
            this.form = form;
        }

        @Override
        protected void populateItem(Item<SInstance> item) {

            final BSTRow row = new BSTRow("_r", BSGridSize.MD);
            final IModel<SInstance> itemModel = item.getModel();
            final SInstance instance = itemModel.getObject();

            SValidationFeedbackHandler feedbackHandler = SValidationFeedbackHandler.bindTo(new FeedbackFence(row))
                .addInstanceModel(itemModel)
                .addListener(ISValidationFeedbackHandlerListener.withTarget(t -> t.add(row)));

            row.setDefaultModel(itemModel);
            row.add($b.classAppender("singular-form-table-row can-have-error"));
            row.add($b.classAppender("has-errors", $m.ofValue(feedbackHandler).map(SValidationFeedbackHandler::containsNestedErrors)));

            if (!(view instanceof SViewListByTable)) {
                return;
            }

            final SViewListByTable viewListByTable = (SViewListByTable) view;

            if (viewListByTable.isInsertEnabled() && ctx.getViewMode().isEdition()) {
                final BSTDataCell actionColumn = row.newCol();
                actionColumn.add($b.attrAppender("style", "width:20px", ";"));
                appendInserirButton(this, form, item, actionColumn);
            }

            if ((instance instanceof SIComposite) && viewListByTable.isRenderCompositeFieldsAsColumns()) {
                final SIComposite ci = (SIComposite) instance;
                final STypeComposite<?> ct = ci.getType();

                for (SType<?> ft : ct.getFields()) {
                    IModel<SInstance> fm = new SInstanceFieldModel<>(item.getModel(), ft.getNameSimple());
                    ctx.createChild(row.newCol(), fm, ctx.getConfirmationModal()).setHint(HIDE_LABEL, Boolean.TRUE).build();
                }
            } else {
                ctx.createChild(row.newCol(), itemModel, ctx.getConfirmationModal()).setHint(HIDE_LABEL, Boolean.FALSE).build();
            }

            if (viewListByTable.isDeleteEnabled() && ctx.getViewMode().isEdition()) {
                final BSTDataCell actionColumn = row.newCol();
                actionColumn.add($b.attrAppender("style", "width:20px", ";"));
                appendRemoverButton(this, form, item, actionColumn, ctx.getConfirmationModal());
            }

            item.add(row);
        }
    }

    private static abstract class TableListPanel extends MetronicPanel {

        public TableListPanel(String id) {
            super(id);
        }

        public TableListPanel(String id, boolean withForm) {
            super(id, withForm);
        }

        @Override
        public IFunction<TemplatePanel, String> getTemplateFunction() {
            String wrapper = withForm ? "<form wicket:id='_fo'>%s</form>" : "%s";
            return (tp) -> String.format(wrapper, ""
                + "  <div class='list-table-input'>"
                + "    <div wicket:id='_hd' class='list-table-heading'></div>"
                + "    <div class='list-table-body' wicket:id='_co' >"
                + "    </div>"
                + "    <div wicket:id='_ft' class='list-table-footer'></div>"
                + "  </div>"
                + "");
        }

        public static final class TableListPanelBuilder {

            private TableListPanelBuilder() {}

            public static TableListPanel build(String id,
                IBiConsumer<BSContainer<?>, Form<?>> buildHeading,
                IBiConsumer<BSContainer<?>, Form<?>> buildContent,
                IBiConsumer<BSContainer<?>, Form<?>> buildFooter) {
                return build(id, true, buildHeading, buildContent, buildFooter);
            }

            public static TableListPanel build(String id,
                boolean withForm,
                IBiConsumer<BSContainer<?>, Form<?>> buildHeading,
                IBiConsumer<BSContainer<?>, Form<?>> buildContent,
                IBiConsumer<BSContainer<?>, Form<?>> buildFooter) {

                return new TableListPanel(id, withForm) {
                    @Override
                    protected void buildHeading(BSContainer<?> heading, Form<?> form) {
                        buildHeading.accept(heading, form);
                    }

                    @Override
                    protected void buildFooter(BSContainer<?> footer, Form<?> form) {
                        buildFooter.accept(footer, form);
                    }

                    @Override
                    protected void buildContent(BSContainer<?> content, Form<?> form) {
                        buildContent.accept(content, form);
                    }
                };
            }

        }
    }
}
