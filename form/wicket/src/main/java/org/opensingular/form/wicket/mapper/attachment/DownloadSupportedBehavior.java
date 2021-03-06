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
package org.opensingular.form.wicket.mapper.attachment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.Session;
import org.apache.wicket.SharedResources;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.opensingular.form.SInstance;
import org.opensingular.form.document.SDocument;
import org.opensingular.form.type.core.attachment.IAttachmentPersistenceHandler;
import org.opensingular.form.type.core.attachment.IAttachmentRef;
import org.opensingular.lib.commons.util.Loggable;

import static org.apache.wicket.markup.head.JavaScriptHeaderItem.forReference;

/**
 * Behavior a ser adicionado ao componente de upload/download para permitir download dos arquivos
 * Busca o arquivo por meio do hash e do nome e retorna uma url com um link temporário para download
 * o link retornado funciona apenas uma vez.
 * <p>
 * A busca é feita primeiro no armazenamento temporárioe  em seguida no permanente.
 *
 * @author vinicius
 */
public class DownloadSupportedBehavior extends Behavior implements IResourceListener, Loggable {

    private Component component;
    private IModel<? extends SInstance> model;
    private ContentDisposition contentDisposition;

    public DownloadSupportedBehavior(IModel<? extends SInstance> model, ContentDisposition contentDisposition) {
        this.model = model;
        this.contentDisposition = contentDisposition;
    }

    public DownloadSupportedBehavior(IModel<? extends SInstance> model) {
        this(model, ContentDisposition.INLINE);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        response.render(forReference(new PackageResourceReference(getClass(), "DownloadSupportedBehavior.js")));
    }

    @Override
    public void bind(Component component) {
        this.component = component;
    }

    @Override
    public void onResourceRequested() {
        try {
            handleRequest();
        } catch (IOException e) {
            getLogger().debug(null, e);
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleRequest() throws IOException {
        WebRequest request = (WebRequest) RequestCycle.get().getRequest();
        IRequestParameters parameters = request.getRequestParameters();
        StringValue id = parameters.getParameterValue("fileId");
        StringValue name = parameters.getParameterValue("fileName");
        writeResponse(getDownloadURL(id.toString(), name.toString()));
    }

    private void writeResponse(String url) throws IOException {
        JSONObject jsonFile = new JSONObject();
        jsonFile.put("url", url);
        WebResponse response = (WebResponse) RequestCycle.get().getResponse();
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.getOutputStream().write(jsonFile.toString().getBytes(StandardCharsets.UTF_8));
        response.flush();
    }

    public String getUrl() {
        return component.urlFor(this, IResourceListener.INTERFACE, new PageParameters()).toString();
    }

    private AttachmentResource addAttachmentSharedResourceToCurrentSession() {
        String sessionKey = getSessionKey();
        getWebApplication().mountResource(AttachmentResource.getMountPath(sessionKey), new SharedResourceReference(sessionKey));
        AttachmentResource attachmentResource = new AttachmentResource(sessionKey);
        getSharedResources().add(sessionKey, attachmentResource);
        return attachmentResource;
    }

    private String getSessionKey() {
        return Session.get().getId();
    }

    protected WebApplication getWebApplication() {
        return WebApplication.get();
    }

    public AttachmentResource lookupAttachmentSharedResourceOnCurrentSession() {
        ResourceReference resourceReference = getSharedResources().get(Session.get().getId());
        if (resourceReference == null) {
            return addAttachmentSharedResourceToCurrentSession();
        } else {
            return (AttachmentResource) resourceReference.getResource();
        }
    }

    protected SharedResources getSharedResources() {
        return getWebApplication().getSharedResources();
    }


    /**
     * Registra um recurso compartilhado do wicket para permitir o download
     * sem bloquear a fila de ajax do wicket.
     * O recurso compartilhado é removido tão logo o download é executado
     * Esse procedimento visa garantir que somente quem tem acesso à página pode fazer
     * download dos arquivos.
     *
     * @param filename
     * @return
     */
    String getDownloadURL(String attachmentKey, String filename) {
        AttachmentResource attachmentResource = lookupAttachmentSharedResourceOnCurrentSession();
        return attachmentResource.addAttachment(filename, contentDisposition, findAttachmentRef(attachmentKey));
    }

    public IAttachmentRef findAttachmentRef(String attachmentKey) {
        IAttachmentRef ref = null;
        for (IAttachmentPersistenceHandler<?> service : getHandlers()) {
            ref = service.getAttachment(attachmentKey);
            if (ref != null) {
                break;
            }
        }
        return ref;
    }

    private List<IAttachmentPersistenceHandler<?>> getHandlers() {
        List<IAttachmentPersistenceHandler<?>> services = new ArrayList<>();
        SDocument sDocument = model.getObject().getDocument();
        if (sDocument.isAttachmentPersistenceTemporaryHandlerSupported()) {
            services.add(sDocument.getAttachmentPersistenceTemporaryHandler());
        }
        sDocument.getAttachmentPersistencePermanentHandler().ifPresent(services::add);
        return services;
    }

}