package org.opensingular.form.wicket.mapper.attachment;

import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.opensingular.form.document.SDocument;
import org.opensingular.form.type.core.attachment.IAttachmentPersistenceHandler;
import org.opensingular.form.type.core.attachment.IAttachmentRef;
import org.opensingular.lib.commons.util.Loggable;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class AttachmentResource extends AbstractResource implements Loggable {

    private final String url;
    private final String id;
    private final String filename;
    private final ContentDisposition contentDisposition;
    private final SDocument sDocument;
    private final SharedResourceReference ref;

    public AttachmentResource(String url, String id, String filename, ContentDisposition contentDisposition,
                              SDocument sDocument, SharedResourceReference ref) {
        this.url = url;
        this.id = id;
        this.filename = filename;
        this.contentDisposition = contentDisposition;
        this.sDocument = sDocument;
        this.ref = ref;
    }

    private IAttachmentRef findAttachmentRef() {
        IAttachmentRef ref = null;
        for (IAttachmentPersistenceHandler<?> service : getHandlers()) {
            ref = service.getAttachment(id);
            if (ref != null) {
                break;
            }
        }
        return ref;
    }

    private List<IAttachmentPersistenceHandler<?>> getHandlers() {
        List<IAttachmentPersistenceHandler<?>> services = new ArrayList<>();
        if (sDocument.isAttachmentPersistenceTemporaryHandlerSupported()) {
            services.add(sDocument.getAttachmentPersistenceTemporaryHandler());
        }
        sDocument.getAttachmentPersistencePermanentHandler().ifPresent(services::add);
        return services;
    }

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        IAttachmentRef fileRef = findAttachmentRef();
        ResourceResponse resourceResponse = new ResourceResponse();
        if (fileRef == null) {
            return resourceResponse.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
        }
        if (fileRef.getSize() > 0) {
            resourceResponse.setContentLength(fileRef.getSize());
        }
        resourceResponse.setFileName(filename);
        try {
            resourceResponse.setContentDisposition(contentDisposition);
            resourceResponse.setContentType(fileRef.getContentType());
            resourceResponse.setWriteCallback(new AttachmentResourceWriteCallback(resourceResponse, fileRef, url, ref));
        } catch (Exception e) {
            getLogger().error("Erro ao recuperar arquivo.", e);
            resourceResponse.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
        }
        return resourceResponse;
    }
}