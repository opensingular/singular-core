package br.net.mirante.singular.form.wicket.mapper.attachment;

import br.net.mirante.singular.form.SIList;
import br.net.mirante.singular.form.SInstance;
import br.net.mirante.singular.form.type.core.attachment.IAttachmentPersistenceHandler;
import br.net.mirante.singular.form.type.core.attachment.SIAttachment;
import br.net.mirante.singular.form.wicket.WicketBuildContext;
import br.net.mirante.singular.form.wicket.mapper.SingularEventsHandlers;
import br.net.mirante.singular.form.wicket.model.IMInstanciaAwareModel;
import br.net.mirante.singular.form.wicket.model.MInstanceRootModel;
import br.net.mirante.singular.util.wicket.resource.Icone;
import br.net.mirante.singular.util.wicket.util.WicketUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.string.StringValue;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static br.net.mirante.singular.form.wicket.mapper.attachment.FileUploadPanel.PARAM_NAME;
import static java.lang.Integer.*;
import static org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem.*;

/**
 * Created by nuk on 10/06/16.
 */
public class FileListUploadPanel extends Panel {

    private final FileUploadField          fileField;
    private final WebMarkupContainer       fileList;
    private final AddFileBehavior          adder;
    private final RemoveFileBehavior       remover;
    private final WicketBuildContext       ctx;
    private final DownloadBehavior downloader;
    private       List<MInstanceRootModel> listOfFileModels;

    public FileListUploadPanel(String id, IModel<SIList<SIAttachment>> model,
                               WicketBuildContext ctx) {
        super(id, model);
        this.ctx = ctx;
        add(new Label("uploadLabel", Model.of(ObjectUtils.defaultIfNull(ctx.getCurrentInstance().asAtr().getLabel(), StringUtils.EMPTY))) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                this.setVisible(StringUtils.isNotEmpty(getDefaultModelObjectAsString()));
            }
        });
        add(new WebMarkupContainer("empty-box") {
            @Override
            public boolean isVisible() {
                return model.getObject().isEmpty();
            }
        }
                .add(new Label("empty-message", "Nenhum arquivo foi adicionado.")));
        add(fileField = new FileUploadField("fileUpload", dummyModel()));
        add(new LabelWithIcon("fileUploadLabel", Model.of("Carregar Arquivo"), Icone.UPLOAD, Model.of(fileField.getMarkupId())));
        add(fileList = new WebMarkupContainer("fileList"));
        updateListOfFileModels(model.getObject());
        fileList.add(new FilesListView(listOfFileModels, model, ctx));
        add(adder = new AddFileBehavior());
        add(remover = new RemoveFileBehavior());
        add(downloader = new DownloadBehavior(model.getObject().getDocument()));
        fileField.add(new SingularEventsHandlers(SingularEventsHandlers.FUNCTION.ADD_MOUSEDOWN_HANDLERS));
    }

    private List<MInstanceRootModel> updateListOfFileModels(SIList<SIAttachment> fileList) {
        return listOfFileModels = toModelList(fileList);
    }

    private List<MInstanceRootModel> toModelList(SIList<SIAttachment> fileList) {
        return fileList.stream()
                .map((f) -> new MInstanceRootModel(f))
                .collect(Collectors.toList());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(forReference(resourceRef("FileListUploadPanel.js")));
        response.render(OnDomReadyHeaderItem.forScript(generateInitJS()));
    }

    private String generateInitJS() {
        return " $(function () { \n" +
                "     var params = { \n" +
                "             file_field_id: '" + fileField.getMarkupId() + "', \n" +
                "             fileList_id: '" + fileList.getMarkupId() + "', \n" +
                "             component_id: '" + this.getMarkupId() + "', \n" +
                "  \n" +
                "             param_name : '" + PARAM_NAME + "', \n" +
                "             upload_url : '" + uploadUrl() + "', \n" +
                "             upload_id : '" + serviceId().toString() + "', \n" +
                "             download_url : '" + downloader.getUrl() + "', \n" +
                "             add_url : '" + adder.getUrl() + "', \n" +
                "             remove_url : '" + remover.getUrl() + "', \n" +
                "  \n" +
                "     }; \n" +
                "  \n" +
                "     window.FileListUploadPanel.setup(params); \n" +
                " });";
    }

    private PackageResourceReference resourceRef(String resourceName) {
        return new PackageResourceReference(getClass(), resourceName);
    }

    private String uploadUrl() {
        String contextPath = getWebApplication().getServletContext().getContextPath();
        return contextPath + FileUploadServlet.UPLOAD_URL;
    }

    private UUID serviceId() {
        IAttachmentPersistenceHandler service = ((SIList) getDefaultModel().getObject()).getDocument().getAttachmentPersistenceTemporaryHandler();
        HttpSession                   session = ((ServletWebRequest) getRequest()).getContainerRequest().getSession();

        return FileUploadServlet.registerService(session, service);
    }

    private IMInstanciaAwareModel dummyModel() {
        return new IMInstanciaAwareModel() {
            @Override
            public Object getObject() {
                return null;
            }

            @Override
            public void setObject(Object object) {
            }

            @Override
            public void detach() {
            }

            @Override
            public SInstance getMInstancia() {
                return (SInstance) getDefaultModel().getObject();
            }
        };
    }

    private static void removeFileFrom(SIList<SIAttachment> list, String fileId) {
        SIAttachment file = findFileByID(list, fileId);
        if (file != null) {
            list.remove(file);
        }
    }

    private static SIAttachment findFileByID(SIList<SIAttachment> list, String fileId) {
        SIAttachment file = null;
        for (SIAttachment a : list) {
            if (fileId.equals(a.getFileId())) {
                file = a;
                break;
            }
        }
        return file;
    }

    protected abstract class BaseJQueryFileUploadBehavior
            extends Behavior implements IResourceListener {
        transient protected WebWrapper w = new WebWrapper();
        private Component component;

        protected SIList<SIAttachment> currentInstance() {
            IModel<?> model = FileListUploadPanel.this.getDefaultModel();
            return (SIList<SIAttachment>) model.getObject();
        }

        protected StringValue getParamFileId(String fileId) {
            return params().getParameterValue(fileId);
        }

        protected IRequestParameters params() {
            ServletWebRequest request = w.request();
            return request.getRequestParameters();
        }

        @Override
        public void bind(Component component) {
            this.component = component;
        }

        public String getUrl() {
            return component.urlFor(this, IResourceListener.INTERFACE, new PageParameters()).toString();
        }
    }

    private class AddFileBehavior extends BaseJQueryFileUploadBehavior {
        @Override
        public void onResourceRequested() {
            try {
                populateFromParams(currentInstance().addNew());
                updateListOfFileModels(currentInstance());
            } catch (Exception e) {
                throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        private void populateFromParams(SIAttachment siAttachment) {
            siAttachment.setFileId(getParamFileId("fileId").toString());
            siAttachment.setFileName(getParamFileId("name").toString());
            siAttachment.setFileHashSHA1(getParamFileId("hashSHA1").toString());
            siAttachment.setFileSize(parseInt(getParamFileId("size").toString()));
        }

    }

    private class RemoveFileBehavior extends BaseJQueryFileUploadBehavior {

        @Override
        public void onResourceRequested() {
            try {
                String fileId = getParamFileId("fileId").toString();
                removeFileFrom(currentInstance(), fileId);
            } catch (Exception e) {
                throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private class FilesListView extends ListView {
        private final IModel<SIList<SIAttachment>> model;
        private final WicketBuildContext           ctx;

        public FilesListView(List<MInstanceRootModel> collect, IModel<SIList<SIAttachment>> model, WicketBuildContext ctx) {
            super("fileItem", collect);
            this.model = model;
            this.ctx = ctx;
        }

        protected void populateItem(ListItem item) {
            MInstanceRootModel itemModel = (MInstanceRootModel) item.getModelObject();
            SIAttachment       file      = (SIAttachment) itemModel.getObject();
            item.add(
                    DownloadResource.link("downloadLink", itemModel, downloader.getUrl())
                                .add(new Label("file_name", Model.of(file.getFileName())))
            );
            item.add(new RemoveButton(itemModel));
        }

        private class RemoveButton extends AjaxButton {
            private final MInstanceRootModel itemModel;

            public RemoveButton(MInstanceRootModel itemModel) {
                super("remove_btn");
                this.itemModel = itemModel;
            }

            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                SIAttachment file = (SIAttachment) itemModel.getObject();

                removeFromListOfModels(file);
                removeFileFrom(model.getObject(), file.getFileId());

                target.add(FileListUploadPanel.this);
                target.add(fileList);
            }

            private void removeFromListOfModels(SIAttachment file) {
                Iterator<MInstanceRootModel> it = listOfFileModels.iterator();
                while (it.hasNext()) {
                    MInstanceRootModel m = it.next();
                    SIAttachment       f = (SIAttachment) m.getObject();
                    if (file.getFileId().equals(f.getFileId())) {
                        it.remove();
                        break;
                    }
                }
            }

            @Override
            public boolean isVisible() {
                return ctx.getViewMode().isEdition();
            }
        }
    }

    public static class LabelWithIcon extends Label {

        private final Icone          icon;
        private final IModel<String> forAttrValue;

        public LabelWithIcon(String id, IModel<?> model, Icone icon, IModel<String> forAttrValue) {
            super(id, model);
            this.icon = icon;
            this.forAttrValue = forAttrValue;
        }

        public LabelWithIcon(String id, IModel<?> model, Icone icon) {
            this(id, model, icon, null);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            if (forAttrValue != null) {
                add(WicketUtils.$b.attr("for", forAttrValue.getObject()));
            }
        }

        @Override
        public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
            replaceComponentTagBody(markupStream, openTag, "<i class='" + icon.getCssClass() + "'></i>\n" + getDefaultModelObjectAsString());
        }

    }

}
