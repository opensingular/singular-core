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

package org.opensingular.lib.wicket.util.maps;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.opensingular.lib.commons.base.SingularProperties;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSContainer;
import org.opensingular.lib.wicket.util.bootstrap.layout.TemplatePanel;
import org.opensingular.lib.wicket.util.util.WicketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MarkableGoogleMapsPanel<T> extends BSContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkableGoogleMapsPanel.class);
    private static final String PANEL_SCRIPT = "MarkableGoogleMapsPanel.js";
    private static final String METADATA_JSON = "MarkableGoogleMapsPanelMetadata.json";
    private static final Integer DEFAULT_ZOOM = 4;
    public static final String SINGULAR_GOOGLEMAPS_KEY = "singular.googlemaps.key";
    public static final String MAP_ID = "map";
    public static final String MAP_STATIC_ID = "mapStatic";

    private final IModel<String> metadadosModel = new Model<>();
    private final IModel<Boolean> readOnly = Model.of(Boolean.FALSE);

    private final WebMarkupContainer map = new WebMarkupContainer(MAP_ID);
    private final WebMarkupContainer mapStatic = new WebMarkupContainer(MAP_STATIC_ID);
    private final HiddenField<String> metadados = new HiddenField<>("metadados", metadadosModel);

    private final String lat;
    private final String lng;
    private final String cleanButtonId;
    private final Button verNoMaps;

    @Override
    public void renderHead(IHeaderResponse response) {
        String property = SingularProperties.get().getProperty(SINGULAR_GOOGLEMAPS_KEY);
        if(property == null){
            property = "AIzaSyALU10ekJ7BQ8jBbMyiCfBK4Yw3giSRmqk";
        }

        final PackageResourceReference customJS = new PackageResourceReference(getClass(), PANEL_SCRIPT);

        response.render(JavaScriptReferenceHeaderItem.forReference(customJS));
        response.render(OnDomReadyHeaderItem.forScript("createSingularMap(" + stringfyId(metadados) + ", '" + property + "');"));

        super.renderHead(response);
    }

    public MarkableGoogleMapsPanel(String id, String lat, String lng, String cleanButtonId, Button verNoMaps) {
        super(id);
        this.lat = lat;
        this.lng = lng;
        this.cleanButtonId = cleanButtonId;
        this.verNoMaps = verNoMaps;
    }

    private void popularMetadados() {

        final Map<String, Object> properties = new HashMap<>();
        try (final PackageTextTemplate metadataJSON = new PackageTextTemplate(getClass(), METADATA_JSON)){
            properties.put("idButton", cleanButtonId);
            properties.put("idMap", map.getMarkupId(true));
            properties.put("idLat", lat);
            properties.put("idLng", lng);
            properties.put("zoom", DEFAULT_ZOOM);
            properties.put("readOnly", isReadOnly());
            metadataJSON.interpolate(properties);
            metadadosModel.setObject(metadataJSON.getString());
            metadataJSON.close();
        } catch (IOException e) {
            LOGGER.error("Erro ao fechar stream", e);
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        popularMetadados();

        TemplatePanel templatePanel = newTemplateTag(tt -> {
            final StringBuilder templateBuilder = new StringBuilder();
            templateBuilder.append(" <div wicket:id=\"map\" style=\"height: 100%;\"> </div> ");
            templateBuilder.append(" <input type=\"hidden\" wicket:id=\"metadados\"> ");
            templateBuilder.append(createHtmlStaticMap());
            return templateBuilder.toString();
        });
        templatePanel.add(map, metadados, mapStatic);
    }

    private String createHtmlStaticMap() {
        StringBuilder parameters = new StringBuilder();
        parameters.append("key=AIzaSyDda6eqjAVOfU4HeV1j9ET-FRxZkagjnRQ");
        parameters.append("&center=-15.7922, -47.4609");
        parameters.append("&zoom=4");
        parameters.append("&size=1000x"+getHeight());


        return " <div class=\"form-group\">" +
                "<img src=\"https://maps.googleapis.com/maps/api/staticmap?"+parameters+"\" wicket:id=\"mapStatic\" > " +
                " </div>";
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        visitChildren(FormComponent.class, (comp, visit) ->comp.setEnabled( !isReadOnly()));
        this.add(WicketUtils.$b.attrAppender("style", "height: " + getHeight() + "px;", ""));

        visitChildren(WebMarkupContainer.class, (comp, visit)->{
            if(comp.getId().equals(MAP_ID)){
                comp.setVisible(!isReadOnly());
            }
            if(comp.getId().equals(MAP_STATIC_ID)){
                comp.setVisible(isReadOnly());
                verNoMaps.setVisible(isReadOnly());
            }
        });
    }

    protected Integer getHeight() {
        return 500;
    }

    private String stringfyId(Component c) {
        return "'" + c.getMarkupId(true) + "'";
    }

    public MarkableGoogleMapsPanel<T> setReadOnly(boolean readOnly){
        this.readOnly.setObject(readOnly);
        return this;
    }

    protected boolean isReadOnly(){
        return readOnly.getObject();
    }
}
