/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.form.wicket.mapper.attachment;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.string.StringValue;
import org.opensingular.form.type.core.attachment.IAttachmentRef;
import org.opensingular.lib.commons.util.Loggable;

/**
 * Shared Resource bound to application.
 *
 * @see DownloadSupportedBehavior
 */
public class AttachmentPublicResource extends AbstractResource implements Loggable {

    private Map<String, Attachment> attachments = new HashMap<>();
    public static final String APPLICATION_KEY = "public";

    public AttachmentPublicResource() { /*Blank constructor*/}

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse resourceResponse = new ResourceResponse();
        StringValue attachmentKey = attributes.getParameters().get("attachmentKey");
        if (attachmentKey.isNull() || attachmentKey.isEmpty()) {
            return resourceResponse.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
        }
        Attachment attachment = attachments.get(attachmentKey.toString());
        if (attachment == null) {
            return resourceResponse.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
        }
        IAttachmentRef attachmentRef = attachment.attachmentRef;
        if (attachmentRef.getSize() > 0) {
            resourceResponse.setContentLength(attachmentRef.getSize());
        }

        resourceResponse.setFileName(attachment.filename);

        try {
            resourceResponse.setContentDisposition(attachment.contentDisposition);
            resourceResponse.setContentType(attachmentRef.getContentType());
            resourceResponse.setWriteCallback(new AttachmentResourceWriteCallback(resourceResponse, attachmentRef));
        } catch (Exception e) {
            getLogger().error("Erro ao recuperar arquivo.", e);
            resourceResponse.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
        }

        return resourceResponse;
    }

    /**
     * @param name        the file name
     * @param disposition the disposition
     * @param ref         the reference
     * @return the URL for download
     */
    public String addAttachment(String name, ContentDisposition disposition, IAttachmentRef ref) {
        WebApplication app = WebApplication.get();
        attachments.put(ref.getId(), new Attachment(name, disposition, ref));
        String path = app.getServletContext().getContextPath() + '/' + app.getWicketFilter().getFilterPath() + getDownloadURL(ref.getId());
        return path.replaceAll("\\*", "").replaceAll("//", "/");
    }

    public static String getMountPathPublic() {
        return getDownloadURL("${attachmentKey}");
    }

    public static String getDownloadURL(String path) {
        return '/' + APPLICATION_KEY + "/download/" + path;
    }


}