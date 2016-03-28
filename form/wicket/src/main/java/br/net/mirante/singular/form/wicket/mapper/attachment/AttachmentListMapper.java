package br.net.mirante.singular.form.wicket.mapper.attachment;

import br.net.mirante.singular.form.mform.SIList;
import br.net.mirante.singular.form.mform.SInstance;
import br.net.mirante.singular.form.mform.STypeAttachmentList;
import br.net.mirante.singular.form.mform.SingularFormException;
import br.net.mirante.singular.form.mform.basic.ui.SPackageBasic;
import br.net.mirante.singular.form.mform.basic.view.SView;
import br.net.mirante.singular.form.mform.basic.view.SViewListByForm;
import br.net.mirante.singular.form.mform.core.attachment.SIAttachment;
import br.net.mirante.singular.form.mform.core.attachment.STypeAttachment;
import br.net.mirante.singular.form.wicket.UIBuilderWicket;
import br.net.mirante.singular.form.wicket.WicketBuildContext;
import br.net.mirante.singular.form.wicket.enums.ViewMode;
import br.net.mirante.singular.form.wicket.mapper.AbstractListaMapper;
import br.net.mirante.singular.form.wicket.mapper.MapperCommons;
import br.net.mirante.singular.form.wicket.mapper.components.MetronicPanel;
import br.net.mirante.singular.form.wicket.model.MInstanceRootModel;
import br.net.mirante.singular.form.wicket.model.SInstanceItemListaModel;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSContainer;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSGrid;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSRow;
import br.net.mirante.singular.util.wicket.bootstrap.layout.TemplatePanel;
import br.net.mirante.singular.util.wicket.resource.Icone;
import br.net.mirante.singular.util.wicket.upload.SFileUploadField;
import com.google.common.base.Strings;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import java.util.List;

import static br.net.mirante.singular.util.wicket.util.Shortcuts.$b;
import static br.net.mirante.singular.util.wicket.util.Shortcuts.$m;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

public class AttachmentListMapper extends AbstractListaMapper {

    private final static String CLICK_DELEGATE_SCRIPT_TEMPLATE = "$('#%s').on('click', function(){$('#%s').click();});";

    @Override
    public void buildView(WicketBuildContext ctx) {

        final SInstance            instance    = ctx.getCurrentInstance();
        final SIList<SIAttachment> attachments = (SIList<SIAttachment>) ctx.getCurrentInstance();


        if (!STypeAttachmentList.class.isAssignableFrom(instance.getType().getClass())) {
            throw new SingularFormException("O tipo " + instance.getType() + " não é compativel com AttachmentListMapper.");
        }

        final FileUploadField multipleFileUploadHiddenField = buildFileUploadField(ctx.getContainer(),
                new MInstanceRootModel<>(attachments));

        ctx.getContainer().appendTag("input", true, "type='file' style='display:none' multiple", multipleFileUploadHiddenField);
        ctx.getContainer().appendTag("div", buildMetronicPanel(ctx, multipleFileUploadHiddenField));

    }

    private FileUploadField buildFileUploadField(BSContainer<?> container, IModel<SIList<SIAttachment>> attachments) {

        final FileUploadField uploadField = new SFileUploadField("uploadField");

        uploadField.add(new AjaxFormSubmitBehavior("change") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);
                final List<FileUpload> uploads = uploadField.getFileUploads();
                for (FileUpload upload : uploads) {
                    final SIAttachment attachment = attachments.getObject().addNew();
                    if (upload != null) {
                        attachment.setContent(upload.getBytes());
                        attachment.setFileName(upload.getClientFileName());
                        attachment.setTemporary();
                        target.add(container);
                    }
                }
            }
        });

        return uploadField;
    }

    private MetronicPanel buildMetronicPanel(final WicketBuildContext ctx, final FileUploadField multipleFileUploadHiddenField) {

        final IModel<SIList<SInstance>> listaModel = $m.get(ctx::getCurrentInstance);
        final SIList<?>                 iLista     = listaModel.getObject();
        final IModel<String>            label      = $m.ofValue(trimToEmpty(iLista.as(SPackageBasic.aspect()).getLabel()));
        final SView                     view       = ctx.getView();
        final ViewMode                  viewMode   = ctx.getViewMode();

        return new MetronicPanel("metronicPanel") {

            @Override
            protected void buildHeading(BSContainer<?> heading, Form<?> form) {
                heading.appendTag("span", new Label("_title", label));
                heading.add($b.visibleIf($m.get(() -> !Strings.isNullOrEmpty(label.getObject()))));

                if (viewMode.isEdition()) {
                    appendAddButton(heading, multipleFileUploadHiddenField);
                }
            }

            @Override
            protected void buildFooter(BSContainer<?> footer, Form<?> form) {
                footer.setVisible(false);
            }

            @Override
            protected void buildContent(BSContainer<?> content, Form<?> form) {

                final TemplatePanel list = content.newTemplateTag(t -> ""
                        + "      <div wicket:id='_e'>"
                        + "        <div wicket:id='_r'></div>"
                        + "      </div>");

                list.add($b.onConfigure(c -> c.setVisible(!listaModel.getObject().isEmpty())));
                list.add(new PanelElementsView("_e", listaModel, ctx.getUiBuilderWicket(), ctx));
                content.add($b.attrAppender("style", "padding: 15px 15px 10px 15px", ";"));
            }

        };
    }

    private void appendAddButton(BSContainer<?> container, FileUploadField multipleFileUploadHiddenField) {
        final WebMarkupContainer addButton = new WebMarkupContainer("_add");
        container.newTemplateTag(t -> ""
                + "<button"
                + " type='button'"
                + " wicket:id='_add'"
                + " class='btn blue btn-sm pull-right'"
                + " style='" + MapperCommons.BUTTON_STYLE + "'>"
                + " <i style='" + MapperCommons.ICON_STYLE + "' class='" + Icone.PLUS + "'></i>"
                + "</button>"
        ).add(addButton);

        addButton.add($b.onReadyScript(() -> {
            return String.format(CLICK_DELEGATE_SCRIPT_TEMPLATE,
                    addButton.getMarkupId(true), multipleFileUploadHiddenField.getMarkupId(true));
        }));
    }

    private static final class PanelElementsView extends ElementsView {

        private final WicketBuildContext ctx;
        private final UIBuilderWicket    wicketBuilder;

        private PanelElementsView(String id, IModel<SIList<SInstance>> model, UIBuilderWicket wicketBuilder,
                                  WicketBuildContext ctx) {
            super(id, model);
            this.wicketBuilder = wicketBuilder;
            this.ctx = ctx;

        }

        @Override
        protected void populateItem(Item<SInstance> item) {
            final BSGrid   grid     = new BSGrid("_r");
            final BSRow    row      = grid.newRow();
            final ViewMode viewMode = ctx.getViewMode();
            wicketBuilder.build(ctx.createChild(row.newCol(12), true, item.getModel()), viewMode);
            item.add(grid);
        }
    }
}