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

package org.opensingular.form.wicket.mapper.decorator;

import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.opensingular.lib.wicket.util.util.Shortcuts.$b;
import static org.opensingular.lib.wicket.util.util.WicketUtils.$m;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.opensingular.form.SInstance;
import org.opensingular.form.decorator.action.ActionClassifier;
import org.opensingular.form.decorator.action.SInstanceAction;
import org.opensingular.form.decorator.action.SInstanceAction.ActionHandler;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.commons.lambda.IPredicate;
import org.opensingular.lib.commons.lambda.ISupplier;
import org.opensingular.lib.wicket.util.ajax.ActionAjaxLink;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSContainer;
import org.opensingular.lib.wicket.util.bootstrap.layout.TemplatePanel;
import org.opensingular.lib.wicket.util.jquery.JQuery;

import com.google.common.collect.ImmutableMap;

/**
 * Componente que encapsula o layout de ícones para ações sobre campos.
 */
public class SInstanceActionsPanel extends TemplatePanel {

    public enum Mode {
            HBAR, VBAR, MENU;
        boolean isMenu() {
            return this == MENU;
        }
    }

    private static String template(SInstanceActionsPanel c) {

        switch (c.mode) {
            case MENU:
                return ""
                    + "\n<div class='singular-form-action-menu dropdown md-skip btn-group" + (c.large ? " btn-group-lg actions-lg" : "") + "'>"
                    + "\n  <button type='button' class='md-skip btn btn-link dropdown-toggle singular-form-action-menu-button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>"
                    + "\n    <i class='fa fa-ellipsis-h'></i>"
                    + "\n  </button>"
                    + "\n  <ul class='dropdown-menu pull-right'>"
                    + "\n    <li wicket:id='actions' class='text-right singular-form-action dropdown'>"
                    + "\n      <a wicket:id='link' class='singular-form-action-link' style='text-align:right'><span wicket:id='label'></span> <i wicket:id='icon'></i></a>"
                    + "\n      <div wicket:id='preview'></div>"
                    + "\n    </li>"
                    + "\n  </ul>"
                    + "\n</div>";
            case VBAR:
                return ""
                    + "\n<div class='singular-form-action-bar md-skip btn-group" + (c.large ? " btn-group-lg actions-lg" : "") + "' style='margin-left:-1px !important; display: inline-flex; flex-flow: column nowrap;'>"
                    + "\n  <div wicket:id='actions' class='singular-form-action dropdown'>"
                    + "\n    <a wicket:id='link' href='javascript:void;' class='singular-form-action-link' style='padding:0px;'><span wicket:id='label'></span> <i wicket:id='icon'></i></a>"
                    + "\n    <div wicket:id='preview'></div>"
                    + "\n  </div>"
                    + "\n</div>";
            case HBAR:
            default:
                return ""
                    + "\n<div class='singular-form-action-bar md-skip btn-group" + (c.large ? " btn-group-lg actions-lg" : "") + "' style='margin-left:-1px !important;'>"
                    + "\n  <div wicket:id='actions' class='singular-form-action dropdown'>"
                    + "\n    <a wicket:id='link' href='javascript:void;' class='singular-form-action-link' style='padding:0px;'><span wicket:id='label'></span> <i wicket:id='icon'></i></a>"
                    + "\n    <div wicket:id='preview'></div>"
                    + "\n  </div>"
                    + "\n</div>";
        }
    }

    private final Mode                                       mode;
    private final ISupplier<? extends List<SInstanceAction>> actionsSupplier;
    private boolean                                          large = false;

    private final ActionsView                                actionsView;

    public SInstanceActionsPanel(
            String id,
            IModel<? extends SInstance> instanceModel,
            IFunction<AjaxRequestTarget, List<?>> internalContextListProvider,
            Mode mode,
            ISupplier<? extends List<SInstanceAction>> actionsSupplier) {
        super(id, instanceModel, c -> template((SInstanceActionsPanel) c));
        this.mode = mode;
        this.actionsSupplier = actionsSupplier;

        add($b.classAppender("decorator-actions"));
        add($b.onReadyScript(c -> JQuery.$(c, ".singular-form-action-menu-button") + ".dropdown()"));

        add(actionsView = new ActionsView("actions", mode, instanceModel, internalContextListProvider, actionsSupplier));
    }

    public SInstanceActionsPanel setLarge(boolean large) {
        this.large = large;
        return this;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(Optional.ofNullable(actionsSupplier)
            .map(it -> it.get())
            .filter(it -> !it.isEmpty())
            .isPresent());
    }

    public static void addLeftSecondaryRightPanelsTo(
            BSContainer<?> container,
            SInstanceActionsProviders instanceActionsProviders,
            IModel<? extends SInstance> model,
            boolean large,
            IFunction<AjaxRequestTarget, List<?>> internalContextListProvider,
            ActionClassifier actionClassifier) {

        addLeftSecondaryRightPanelsTo(
            container,
            instanceActionsProviders,
            model,
            large,
            internalContextListProvider,
            filter -> instanceActionsProviders.actionList(model, filter, actionClassifier));
    }

    public static void addLeftSecondaryRightPanelsTo(
            BSContainer<?> container,
            SInstanceActionsProviders instanceActionsProviders,
            IModel<? extends SInstance> model,
            boolean large,
            IFunction<AjaxRequestTarget, List<?>> internalContextListProvider,
            IFunction<IPredicate<SInstanceAction>, List<SInstanceAction>> actionsFunc) {

        ISupplier<? extends List<SInstanceAction>> filterLeft = () -> actionsFunc.apply(it -> !it.isSecondary() && it.getPosition() < 0);
        ISupplier<? extends List<SInstanceAction>> filterRight = () -> actionsFunc.apply(it -> !it.isSecondary() && it.getPosition() >= 0);
        ISupplier<? extends List<SInstanceAction>> filterSecondary = () -> actionsFunc.apply(it -> it.isSecondary());
        container
            .appendTag("div", new SInstanceActionsPanel("actionsLeft", model, internalContextListProvider, SInstanceActionsPanel.Mode.HBAR, filterLeft)
                .setLarge(large)
                .add($b.classAppender("align-left")))
            .appendTag("div", new SInstanceActionsPanel("actionsSecondary", model, internalContextListProvider, SInstanceActionsPanel.Mode.MENU, filterSecondary)
                .setLarge(large)
                .add($b.classAppender("align-right")))
            .appendTag("div", new SInstanceActionsPanel("actionsRight", model, internalContextListProvider, SInstanceActionsPanel.Mode.HBAR, filterRight)
                .setLarge(large)
                .add($b.classAppender("align-right")));
    }

    public static void addPrimarySecondaryPanelsTo(
            BSContainer<?> container,
            SInstanceActionsProviders instanceActionsProviders,
            IModel<? extends SInstance> model,
            boolean large,
            IFunction<AjaxRequestTarget, List<?>> internalContextListProvider,
            ActionClassifier actionClassifier) {

        addPrimarySecondaryPanelsTo(
            container,
            model,
            large,
            internalContextListProvider,
            filter -> instanceActionsProviders.actionList(model, filter, actionClassifier));
    }

    public static void addPrimarySecondaryPanelsTo(
            BSContainer<?> container,
            IModel<? extends SInstance> model,
            boolean large,
            IFunction<AjaxRequestTarget, List<?>> internalContextListProvider,
            IFunction<IPredicate<SInstanceAction>, List<SInstanceAction>> actionsFunc) {

        container
            .appendTag("div", new SInstanceActionsPanel("actionsPrimary", model, internalContextListProvider, SInstanceActionsPanel.Mode.HBAR, () -> actionsFunc.apply(it -> !it.isSecondary()))
                .setLarge(large)
                .add($b.classAppender("align-left")))
            .appendTag("div", new SInstanceActionsPanel("actionsSecondary", model, internalContextListProvider, SInstanceActionsPanel.Mode.MENU, () -> actionsFunc.apply(it -> it.isSecondary()))
                .setLarge(large)
                .add($b.classAppender("align-right")));
    }

    public static Component addImportantAndSecondaryVerticalPanelTo(
            BSContainer<?> container,
            SInstanceActionsProviders instanceActionsProviders,
            IModel<? extends SInstance> model,
            boolean large,
            IFunction<AjaxRequestTarget, List<?>> internalContextListProvider,
            ActionClassifier actionClassifier) {

        return addImportantAndSecondaryVerticalPanelTo(
            container,
            model,
            large,
            internalContextListProvider,
            filter -> instanceActionsProviders.actionList(model, filter, actionClassifier));
    }

    public static Component addImportantAndSecondaryVerticalPanelTo(
            BSContainer<?> container,
            IModel<? extends SInstance> model,
            boolean large,
            IFunction<AjaxRequestTarget, List<?>> internalContextListProvider,
            IFunction<IPredicate<SInstanceAction>, List<SInstanceAction>> actionsFunc) {

        ISupplier<? extends List<SInstanceAction>> actionsImp = () -> actionsFunc.apply(it -> it.isImportant()).stream()
            .collect(toList());
        ISupplier<? extends List<SInstanceAction>> actionsSup = () -> actionsFunc.apply(it -> !it.isImportant()).stream()
            .sorted(Comparator.comparing(it -> it.isSecondary() ? 1 : 0))
            .collect(toList());

        return container.newTag("div", true, "", new BSContainer<>("actionsContainer"))

            .appendTag("div",
                new SInstanceActionsPanel("actionsImportant", model, internalContextListProvider, SInstanceActionsPanel.Mode.VBAR, actionsImp)
                    .setLarge(large))

            .appendTag("div",
                new SInstanceActionsPanel("actionsSecondary", model, internalContextListProvider, SInstanceActionsPanel.Mode.MENU, actionsSup)
                    .setLarge(large)
                    .add($b.classAppender("align-right")))

            .add($b.styleAppender(ImmutableMap.of("display", "flex", "flex-flow", "column nowrap")));
    }

    public SInstanceActionsPanel setActionClassFunction(IFunction<SInstanceAction, String> actionClassFunction) {
        this.actionsView.setActionClassFunction(actionClassFunction);
        return this;
    }
    public SInstanceActionsPanel setLinkClassFunction(IFunction<SInstanceAction, String> linkClassFunction) {
        this.actionsView.setLinkClassFunction(linkClassFunction);
        return this;
    }

    private static final class ActionsView extends RefreshingView<SInstanceAction> {
        private final Mode                                       mode;
        private final IFunction<AjaxRequestTarget, List<?>>      internalContextListProvider;
        private final IModel<? extends SInstance>                instanceModel;
        private final ISupplier<? extends List<SInstanceAction>> actionsSupplier;
        private IFunction<SInstanceAction, String>               actionClassFunction = it -> "";
        private IFunction<SInstanceAction, String>               linkClassFunction   = it -> "";
        private ActionsView(String id,
                Mode mode,
                IModel<? extends SInstance> instanceModel,
                IFunction<AjaxRequestTarget, List<?>> internalContextListProvider,
                ISupplier<? extends List<SInstanceAction>> actionsSupplier) {
            super(id);
            this.mode = mode;
            this.instanceModel = instanceModel;
            this.internalContextListProvider = internalContextListProvider;
            this.actionsSupplier = actionsSupplier;
        }
        @Override
        protected void populateItem(Item<SInstanceAction> item) {
            IModel<SInstanceAction> itemModel = item.getModel();
            SInstanceAction action = itemModel.getObject();

            MarkupContainer link;
            if (action.getPreview() != null) {
                link = new WebMarkupContainer("link");
                link.add($b.attr("data-toggle", "dropdown"));
                item.add(new SInstanceActionPreviewPanel("preview",
                    $m.map(itemModel, it -> it.getPreview()),
                    instanceModel,
                    internalContextListProvider));
            } else {
                link = new ActionAjaxLink<SInstanceAction>("link", itemModel) {

                    /**
                     * The link for the actions panel ignores the enabled hierarchy, because
                     * we must be able to disable some chunks of the form and the action
                     * should be indepent.
                     *
                     * @return
                     */
                    @Override
                    public boolean isEnabledInHierarchy() {
                        return ActionsView.this.isEnabled() && ActionsView.this.isEnableAllowed();
                    }

                    @Override
                    protected void onAction(AjaxRequestTarget target) {
                        SInstanceAction instanceAction = this.getModelObject();
                        ActionHandler actionHandler = instanceAction.getActionHandler();
                        if (actionHandler != null) {
                            final List<?> contextList = internalContextListProvider.apply(target);
                            final SInstanceAction.Delegate delegate = new WicketSIconActionDelegate(instanceModel, contextList);
                            actionHandler.onAction(instanceAction, instanceModel::getObject, delegate);
                        }
                    }
                };
                item.add(new WebMarkupContainer("preview")
                    .setVisible(false));
            }

            Label label = new Label("label", $m.get(() -> action.getText()));
            link.add(label
                .add($b.visibleIf(() -> mode.isMenu())));

            addIcon(link, itemModel);

            item
                .add(link
                    .add($b.classAppender($m.map(itemModel, linkClassFunction))))
                .add($b.classAppender($m.map(itemModel, actionClassFunction)));
        }
        private void addIcon(MarkupContainer container, IModel<SInstanceAction> itemModel) {
            final IModel<String> titleModel = $m.map(itemModel, action -> defaultString(action.getText(), action.getDescription()));
            container
                .add(new WebMarkupContainer("icon")
                    .add($b.classAppender($m.map(itemModel, it -> it.getIcon().getIconCssClassesString())))
                    .add($b.styleAppender($m.map(itemModel, it -> it.getIcon().getIconCssStyles()))))
                .add($b.classAppender($m.map(itemModel, it -> it.getIcon().getContainerCssClassesString())))
                .add($b.styleAppender($m.map(itemModel, it -> it.getIcon().getContainerCssStyles())))
                .add($b.visibleIf($m.map(itemModel, it -> it.getIcon() != null)))
                .add($b.attr("title", titleModel));
        }
        @Override
        protected Iterator<IModel<SInstanceAction>> getItemModels() {
            return actionsSupplier.get().stream()
                .sorted(Comparator.comparing(it -> it.getPosition()))
                .map(it -> (IModel<SInstanceAction>) Model.of(it))
                .iterator();
        }
        public ActionsView setActionClassFunction(IFunction<SInstanceAction, String> actionClassFunction) {
            this.actionClassFunction = actionClassFunction;
            return this;
        }
        public ActionsView setLinkClassFunction(IFunction<SInstanceAction, String> linkClassFunction) {
            this.linkClassFunction = linkClassFunction;
            return this;
        }
    }
}
