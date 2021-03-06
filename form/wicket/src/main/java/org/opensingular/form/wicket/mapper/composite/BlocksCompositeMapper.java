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

package org.opensingular.form.wicket.mapper.composite;

import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.StyleAttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.opensingular.form.ICompositeInstance;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.type.basic.AtrBasic;
import org.opensingular.form.type.core.SPackageBootstrap;
import org.opensingular.form.view.Block;
import org.opensingular.form.view.SViewByBlock;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.feedback.SValidationFeedbackPanel;
import org.opensingular.form.wicket.mapper.behavior.RequiredBehaviorUtil;
import org.opensingular.form.wicket.mapper.decorator.SInstanceActionsPanel;
import org.opensingular.form.wicket.model.SInstanceFieldModel;
import org.opensingular.form.wicket.util.SingularFormProcessingPayload;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSCol;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSContainer;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSGrid;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSRow;
import org.opensingular.lib.wicket.util.bootstrap.layout.IBSComponentFactory;
import org.opensingular.lib.wicket.util.bootstrap.layout.TemplatePanel;
import org.opensingular.lib.wicket.util.model.IMappingModel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.opensingular.lib.wicket.util.util.Shortcuts.$b;
import static org.opensingular.lib.wicket.util.util.Shortcuts.$m;

public class BlocksCompositeMapper extends AbstractCompositeMapper {

    @Override
    protected ICompositeViewBuilder getViewBuilder(WicketBuildContext ctx) {
        return new BlocksCompositeViewBuilder(ctx, this);
    }

    private static boolean isBlockHandlesTitleFromChild(WicketBuildContext ctx, Block block) {
        SInstance currentInstance = ctx.getCurrentInstance();
        if (block.isSingleType() && currentInstance instanceof ICompositeInstance) {
            final boolean blockTitleBlank = isBlank(block.getName());
            final boolean singleTypeTitleBlank = isSingleTypeTitleBlank(block, (ICompositeInstance) currentInstance);
            final boolean sameTitle = isSameTitle(block, currentInstance);

            return blockTitleBlank ^ singleTypeTitleBlank ^ sameTitle;
        }
        return false;
    }

    private static boolean isSameTitle(final Block block, SInstance currentInstance) {
        return getSingleBlockLabel(block, currentInstance).equalsIgnoreCase(block.getName());
    }

    private static boolean isSingleTypeTitleBlank(final Block block, ICompositeInstance currentInstance) {
        return isBlank(getSingleBlockLabel(block, (SInstance) currentInstance));
    }

    @Nonnull
    private static String getSingleBlockLabel(Block block, SInstance currentInstance) {
        return block.getSingleType(currentInstance).map(SInstance::asAtr).map(AtrBasic::getLabel).orElse("");
    }

    private static class BlocksCompositeViewBuilder extends AbstractCompositeViewBuilder {

        BlocksCompositeViewBuilder(WicketBuildContext ctx, AbstractCompositeMapper mapper) {
            super(ctx, mapper);
        }

        @Override
        protected void buildFields(WicketBuildContext ctx, BSGrid grid) {

            final List<String> remainingTypes = new ArrayList<>();
            final List<String> addedTypes = new ArrayList<>();

            final WicketBuildContext rootContext = ctx.getRootContext();
            final IBSComponentFactory<Component> factory = rootContext.getPreFormPanelFactory();

            BSGrid targetGrid = grid;
            if (factory != null) {
                targetGrid.newComponent(factory);
                targetGrid = targetGrid.newGrid();
                rootContext.setPreFormPanelFactory(null);
            }

            SValidationFeedbackPanel feedback = ctx.createFeedbackPanel("feedback").setShowBox(true);
            targetGrid.appendTag("div", feedback);

            targetGrid = targetGrid.newGrid();

            final List<Block> blocks = ((SViewByBlock) ctx.getView()).getBlocks();
            for (int i = 0; i < blocks.size(); i++) {
                final Block block = blocks.get(i);
                final PortletPanel portlet = new PortletPanel("_portlet" + i, block, ctx, (BlocksCompositeMapper) mapper);
                addedTypes.addAll(block.getTypes());
                appendBlock(targetGrid, block, portlet);
            }

            for (SType<?> f : getInstanceType().getFields()) {
                String nameSimple = f.getNameSimple();
                if (!addedTypes.contains(nameSimple)) {
                    remainingTypes.add(nameSimple);
                }
            }

            if (!remainingTypes.isEmpty()) {
                final Block block = new Block();
                final PortletPanel portlet = new PortletPanel("_portletForRemaining", block, ctx, (BlocksCompositeMapper) mapper);
                block.setTypes(remainingTypes);
                appendBlock(targetGrid, block, portlet);
            }
        }

        private void appendBlock(BSGrid grid, Block block, PortletPanel portlet) {
            final BSGrid newGrid = portlet.getNewGrid();
            BSRow row = newGrid.newRow();

            grid.appendTag("div", portlet);

            for (String typeName : block.getTypes()) {
                row = buildBlockAndGetCurrentRow(getInstanceType().getField(typeName), newGrid, row, block);
            }
        }

        private BSRow buildBlockAndGetCurrentRow(SType<?> field, BSGrid grid, BSRow row, Block block) {
            Boolean newRow = field.getAttributeValue(SPackageBootstrap.ATR_COL_ON_NEW_ROW);
            SInstanceFieldModel<SInstance> im = fieldModel(field);
            BSRow target = (newRow != null && newRow) ? grid.newRow() : row;
            buildField(target, im, block);
            return target;
        }

        private void buildField(final BSRow row, final SInstanceFieldModel<SInstance> mField, Block block) {
            SInstance iField = mField.getObject();
            BSCol col = row.newCol();
            configureColspan(ctx, iField, col);

            WicketBuildContext childCtx = ctx.createChild(col, ctx.getExternalContainer(), mField);
            childCtx.setHint(AbstractCompositeMapper.HIDE_LABEL, isBlockHandlesTitleFromChild(ctx, block));
            childCtx.build();
        }
    }

    private static class PortletPanel extends TemplatePanel {

        private static final String         TITLE_ID       = "title";
        private static final String         GRID_ID        = "grid";

        private static final String         PORTLET_MARKUP = ""
            + "<div class='portlet light'>                                     "
            + "  <div class='portlet-title' wicket:id='" + TITLE_ID + "'></div>"
            + "  <div class='portlet-body'>                                    "
            + "    <div wicket:id='" + GRID_ID + "' />                         "
            + "  </div>                                                        "
            + "</div>                                                          ";

        private final Block                 block;
        private final BSGrid                newGrid;
        private final WicketBuildContext    ctx;
        private final BlocksCompositeMapper mapper;

        private boolean                     visible;

        PortletPanel(String id, Block block, WicketBuildContext ctx, BlocksCompositeMapper mapper) {
            super(id, PORTLET_MARKUP);
            this.block = block;
            this.ctx = ctx;
            this.mapper = mapper;
            this.newGrid = new BSGrid(GRID_ID);
            add(newGrid, buildPortletTitle(block, ctx));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(new StyleAttributeModifier() {
                @Override
                protected Map<String, String> update(Map<String, String> oldStyles) {
                    final Map<String, String> newStyles = new HashMap<>(oldStyles);
                    if (isAnyChildrenVisible()) {
                        newStyles.put("display", "block");
                        visible = true;
                    } else {
                        newStyles.put("display", "none");
                        visible = false;
                    }
                    return newStyles;
                }
            });
        }

        private boolean isAnyChildrenVisible() {
            SInstance instance = ctx.getCurrentInstance();
            if ((instance instanceof SIComposite) && instance.asAtr().exists() && instance.asAtr().isVisible()) {
                for (String typeName : block.getTypes()) {
                    SInstance field = instance.getField(typeName);
                    if (field.asAtr().exists() && field.asAtr().isVisible()) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void onEvent(IEvent<?> event) {
            super.onEvent(event);
            if (!SingularFormProcessingPayload.class.isAssignableFrom(event.getPayload().getClass())) {
                return;
            }

            AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
            boolean newVisible = isAnyChildrenVisible();
            if (newVisible != visible) {
                if (newVisible) {
                    target.appendJavaScript("$('#" + this.getMarkupId() + "').css('display', 'block');");
                } else {
                    target.appendJavaScript("$('#" + this.getMarkupId() + "').css('display', 'none');");
                }
                visible = newVisible;
            }
        }

        private TemplatePanel buildPortletTitle(Block block, WicketBuildContext ctx) {
            final String titleMarkup = "<div wicket:id='caption' class='caption'></div>";
            final IModel<String> titleLabelModel = newBlockLabelModel(block, ctx);

            final TemplatePanel portletTitle = new TemplatePanel(TITLE_ID, titleMarkup);
            final Label titleLabel = new Label("title", titleLabelModel);
            final BSContainer<?> caption = new BSContainer<>("caption");

            portletTitle.setVisible(isNotBlank(titleLabelModel.getObject()));
            portletTitle.add(caption);
            caption
                .appendTag("span", titleLabel.add($b.classAppender("caption-subject")))
                .add($b.styleAppender("width", "100%", $m.ofValue(Boolean.TRUE)));

            if (isBlockHandlesTitleFromChild(ctx, block)) {
                IModel<? extends SInstance> model = IMappingModel.of(ctx.getModel())
                    .map(it -> block.getSingleType(it).orElse(null));
                IFunction<AjaxRequestTarget, List<?>> internalContextListProvider = target -> Arrays.asList(
                    mapper,
                    RequestCycle.get().find(AjaxRequestTarget.class),
                    model,
                    model.getObject(),
                    ctx,
                    ctx.getContainer());

                SInstanceActionsPanel.addLeftSecondaryRightPanelsTo(
                    caption,
                    mapper.getInstanceActionsProviders(),
                    model,
                    false,
                    internalContextListProvider,
                    ctx.getActionClassifier());
            }

            titleLabel.add(new ClassAttributeModifier() {
                @Override
                protected Set<String> update(Set<String> oldClasses) {
                    if (block.getTypes().size() == 1 && ctx.getViewMode().isEdition()) {
                        final SIComposite sic = ctx.getCurrentInstance();
                        final SInstance firstChild = sic.getField(block.getTypes().get(0));
                        return RequiredBehaviorUtil.updateRequiredClasses(oldClasses, firstChild);
                    }
                    return oldClasses;
                }
            });

            return portletTitle;
        }

        private IModel<String> newBlockLabelModel(Block block, WicketBuildContext ctx) {
            String label;
            if (isNotBlank(block.getName()))
                label = block.getName();
            else {
                SInstance parent = ctx.getCurrentInstance();
                label = block.getSingleType(parent)
                    .map(it -> it.asAtr().getLabel())
                    .orElse(null);
            }

            return Model.of(label);
        }

        BSGrid getNewGrid() {
            return newGrid;
        }
    }

}