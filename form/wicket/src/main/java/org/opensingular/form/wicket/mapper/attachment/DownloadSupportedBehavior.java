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

import static org.apache.wicket.markup.head.JavaScriptHeaderItem.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.tika.io.IOUtils;
import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
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
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.opensingular.form.SInstance;
import org.opensingular.form.type.core.attachment.IAttachmentPersistenceHandler;
import org.opensingular.form.type.core.attachment.IAttachmentRef;
import org.opensingular.lib.commons.util.Loggable;

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
    
    private static final String         DOWNLOAD_PATH        = "/download";
    private Component                   component;
    private IModel<? extends SInstance> model;

    public DownloadSupportedBehavior(IModel<? extends SInstance> model) {
        this.model = model;
    }

    private List<IAttachmentPersistenceHandler<?>> getHandlers() {
        List<IAttachmentPersistenceHandler<?>> services = new ArrayList<>();
        if (model.getObject().getDocument().isAttachmentPersistenceTemporaryHandlerSupported()) {
            services.add(model.getObject().getDocument().getAttachmentPersistenceTemporaryHandler());
        }
        if (model.getObject().getDocument().isAttachmentPersistencePermanentHandlerSupported()) {
            services.add(model.getObject().getDocument().getAttachmentPersistencePermanentHandler());
        }
        return services;
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
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private IAttachmentRef findAttachmentRef(String id) {
        IAttachmentRef ref = null;
        for (IAttachmentPersistenceHandler<?> service : getHandlers()) {
            ref = service.getAttachment(id);
            if (ref != null) {
                break;
            }
        }
        return ref;
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
        response.getOutputStream().write(jsonFile.toString().getBytes());
        response.flush();
    }

    public String getUrl() {
        return component.urlFor(this, IResourceListener.INTERFACE, new PageParameters()).toString();
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
    private String getDownloadURL(String id, String filename) {
        String url = DOWNLOAD_PATH + "/" + id + "/" + new Date().getTime();
        SharedResourceReference ref = new SharedResourceReference(String.valueOf(id));
        AbstractResource resource = new AbstractResource() {
            @Override
            protected ResourceResponse newResourceResponse(Attributes attributes) {
                IAttachmentRef fileRef = findAttachmentRef(id);
                if (fileRef == null) {
                    return new ResourceResponse().setStatusCode(HttpServletResponse.SC_NOT_FOUND);
                }
                ResourceResponse resourceResponse = new ResourceResponse();
                if (fileRef.getSize() > 0) {
                    resourceResponse.setContentLength(fileRef.getSize());
                }
                resourceResponse.setFileName(filename);
                try {
                    resourceResponse.setContentType(fileRef.getContentType());
                    resourceResponse.setWriteCallback(new WriteCallback() {
                        @Override
                        public void writeData(Attributes attributes) throws IOException {
                            try (InputStream inputStream = fileRef.getInputStream()) {
                                IOUtils.copy(inputStream, attributes.getResponse().getOutputStream());
                                /*Desregistrando recurso compartilhado*/
                                WebApplication.get().unmount(url);
                                WebApplication.get().getSharedResources().remove(ref.getKey());
                            } catch (Exception e) {
                                getLogger().error("Erro ao recuperar arquivo.", e);
                                ((WebResponse) attributes.getResponse()).setStatus(HttpServletResponse.SC_NOT_FOUND);
                                resourceResponse.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
                            }
                        }
                    });
                } catch (Exception e) {
                    getLogger().error("Erro ao recuperar arquivo.", e);
                    resourceResponse.setStatusCode(HttpServletResponse.SC_NOT_FOUND);
                }
                return resourceResponse;
            }
        };
        /*registrando recurso compartilhado*/
        WebApplication.get().getSharedResources().add(String.valueOf(id), resource);
        WebApplication.get().mountResource(url, ref);
        String path = WebApplication.get().getServletContext().getContextPath() + "/" + WebApplication.get().getWicketFilter().getFilterPath() + url;
        path = path.replaceAll("\\*", "").replaceAll("//", "/");
        return path;
    }
}