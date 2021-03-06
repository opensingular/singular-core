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

package org.opensingular.form.wicket.mapper.maps;

import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.json.JSONObject;
import org.opensingular.form.SInstance;
import org.opensingular.form.type.util.SILatitudeLongitudeMultipleMarkable;
import org.opensingular.form.type.util.STypeLatitudeLongitude;
import org.opensingular.form.type.util.STypeLatitudeLongitudeGMaps;
import org.opensingular.form.view.SViewCurrentLocation;
import org.opensingular.form.wicket.model.SInstanceFieldModel;
import org.opensingular.form.wicket.model.SInstanceValueModel;
import org.opensingular.lib.commons.base.SingularProperties;
import org.opensingular.lib.commons.lambda.ISupplier;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSContainer;
import org.opensingular.lib.wicket.util.bootstrap.layout.TemplatePanel;
import org.opensingular.lib.wicket.util.util.WicketUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opensingular.lib.wicket.util.util.Shortcuts.$m;

/**
 * Panel for the Google maps.
 *
 * <code>https://developers.google.com/maps/documentation/javascript/kmllayer#kml_feature_details</code>
 * <code>https://developers.google.com/maps/documentation/javascript/reference/3/kml</code>
 */

public class MarkableGoogleMapsPanel<T> extends BSContainer<MarkableGoogleMapsPanel<T>> {


    private static final String PANEL_SCRIPT = "MarkableGoogleMapsPanel.js";

    public static final String MAP_ID = "map";
    public static final String MAP_STATIC_ID = "mapStatic";
    public static final int HEIGHT = 500;
    private final LatLongMarkupIds ids;

    private final String singularKeyMaps = SingularProperties.getOpt(SingularProperties.SINGULAR_GOOGLEMAPS_JS_KEY).orElse(null);
    private final String singularKeyMapStatic = SingularProperties.getOpt(SingularProperties.SINGULAR_GOOGLEMAPS_STATIC_KEY).orElse(null);

    private final IModel<String> metaDataModel = new Model<>();
    private final boolean visualization;
    private final ISupplier<? extends SViewCurrentLocation> viewSupplier;


    private boolean multipleMarkers; //Map for multiple markables, this will change the logic of the creation map.
    private String callbackUrl;
    private String tableContainerId;
    private String kmlUrl;

    private final Button clearButton;
    private final Button currentLocationButton;
    private WebMarkupContainer verNoMaps; //Enable just if not multiple markers.
    private ImgMap mapStatic;
    private final WebMarkupContainer map = new WebMarkupContainer(MAP_ID);
    private final HiddenField<String> metaData = new HiddenField<>("metadados", metaDataModel);

    public MarkableGoogleMapsPanel(LatLongMarkupIds ids, IModel<? extends SInstance> model,
                                   ISupplier<? extends SViewCurrentLocation> viewSupplier, boolean visualization, boolean multipleMarkers) {
        super(model.getObject().getName());
        this.visualization = visualization;
        this.ids = ids;
        this.viewSupplier = viewSupplier;
        this.clearButton = new Button("clearButton", $m.ofValue("Limpar"));
        this.currentLocationButton = new Button("currentLocationButton", $m.ofValue("Marcar Minha Posição"));
        this.multipleMarkers = multipleMarkers;

        if (multipleMarkers) {
            createMapWithMultipleMarkers(model);
        } else {
            createMapWithoutMultipleMarkers(model);
        }

        clearButton.setDefaultFormProcessing(false);
        currentLocationButton.setDefaultFormProcessing(false);

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        final PackageResourceReference customJS = new PackageResourceReference(getClass(), PANEL_SCRIPT);
        response.render(JavaScriptReferenceHeaderItem.forReference(customJS));
        if (StringUtils.isNotBlank(singularKeyMapStatic) && StringUtils.isNotBlank(singularKeyMaps)) {
            response.render(OnDomReadyHeaderItem.forScript("Singular.createSingularMap(" + stringfyId(metaData) + ", '" + singularKeyMaps + "');"));
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        TemplatePanel panelErrorMsg = newTemplateTag(tt -> {
            final StringBuilder templateBuilder = new StringBuilder();
            templateBuilder.append("<div> <label class=\"text-danger\" wicket:id='errorMapStatic'></label> </div>");
            templateBuilder.append("<div> <label class=\"text-danger\" wicket:id='errorMapJS'></label> </div>");
            return templateBuilder.toString();
        });

        TemplatePanel templatePanel = newTemplateTag(tt -> {
            final StringBuilder templateBuilder = new StringBuilder();
            templateBuilder.append(" <div class=\"form-group\"> ");
            templateBuilder.append("    <input type=\"button\" class=\"btn btn-default\" wicket:id=\"clearButton\"> ");
            templateBuilder.append("    <input type=\"button\" class=\"btn btn-default\" wicket:id=\"currentLocationButton\"> ");
            templateBuilder.append("    <a class=\"btn btn-default\" wicket:id=\"verNoMaps\"></a> ");
            templateBuilder.append(" </div>");
            templateBuilder.append(" <div wicket:id=\"map\" style=\"height: 90%;\"> </div> ");
            templateBuilder.append(" <input type=\"hidden\" wicket:id=\"metadados\"> ");
            templateBuilder.append("<div class=\"form-group\">");
            templateBuilder.append("    <img wicket:id=\"mapStatic\" > ");
            templateBuilder.append(" </div>");
            return templateBuilder.toString();
        });

        createErrorPanel(panelErrorMsg, templatePanel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        visitChildren(FormComponent.class, (comp, visit) -> comp.setEnabled(!isVisualization()));
        this.add(WicketUtils.$b.attrAppender("style", "height: " + getHeight() + "px;", ""));


        map.setVisible(!isVisualization() || isKmlUrlVisible());
        currentLocationButton.setVisible(SViewCurrentLocation.class.isInstance(viewSupplier.get()) && !isVisualization());

        boolean notMultipleAndNotVisualization = !multipleMarkers && !isVisualization();
        clearButton.setVisible(notMultipleAndNotVisualization);
        mapStatic.setVisible(isVisualization() && !isKmlUrlVisible());
        verNoMaps.setVisible(!multipleMarkers && isVisualization());
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        populateMetaData();
    }

    public void updateJS(AjaxRequestTarget target) {
        if (StringUtils.isNotBlank(singularKeyMapStatic) && StringUtils.isNotBlank(singularKeyMaps)) {
            target.appendJavaScript("window.setTimeout(function () {Singular.createSingularMap(" + stringfyId(metaData) + ", '" + singularKeyMaps + "');}, 500);");
        }
    }

    /**
     * Method that contains the logic of the creation of the maps with multiple marks.
     *
     * @param model The model of the google maps. This model should contains the zoom, latitude and longitude.
     */
    private void createMapWithMultipleMarkers(IModel<? extends SInstance> model) {
        IModel<SInstance> zoomModel = new SInstanceValueModel<>(new SInstanceFieldModel<>(model, STypeLatitudeLongitudeGMaps.FIELD_ZOOM));
        verNoMaps = new WebMarkupContainer("verNoMaps");
        mapStatic = new ImgMap(MAP_STATIC_ID, $m.loadable(() -> {
            List<String> makerList = new ArrayList<>();
            List<LatLng> latLngList = new ArrayList<>();
            ((SILatitudeLongitudeMultipleMarkable) model.getObject()).getPoints().forEach(m -> {
                IModel<?> latitude = Model.of(m.getLatitude());
                IModel<?> longitude = Model.of(m.getLongitude());

                if (m.getLatitude() != null && m.getLongitude() != null) {
                    LatLng latLng = new LatLng(m.getLatitude().doubleValue(), m.getLongitude().doubleValue());
                    latLngList.add(latLng);
                    String latLngText = generateLatLngMaker(latitude, longitude);
                    makerList.add(latLngText);
                }
            });

            return configureMapToView(makerList, zoomModel, latLngList);

        }));
    }

    private String generateLatLngMaker(IModel<?> latitudeModel, IModel<?> longitudeModel) {
        if (latitudeModel.getObject() != null && longitudeModel.getObject() != null) {
            return latitudeModel.getObject() + "," + longitudeModel.getObject();
        }
        return null;
    }

    /**
     * Method that contains the logic of the creation of the maps with just one mark.
     *
     * @param model The model of the google maps. This model should contains the zoom, latitude and longitude.
     */
    private void createMapWithoutMultipleMarkers(IModel<? extends SInstance> model) {

        IModel<SInstance> zoomModel = new SInstanceValueModel<>(new SInstanceFieldModel<>(model, STypeLatitudeLongitudeGMaps.FIELD_ZOOM));
        IModel<SInstance> latitudeModel = new SInstanceValueModel<>(new SInstanceFieldModel<>(model, STypeLatitudeLongitude.FIELD_LATITUDE));
        IModel<SInstance> longitudeModel = new SInstanceValueModel<>(new SInstanceFieldModel<>(model, STypeLatitudeLongitude.FIELD_LONGITUDE));
        LoadableDetachableModel<String> googleMapsLinkModel = $m.loadable(() -> {
            if (latitudeModel.getObject() != null && longitudeModel.getObject() != null) {
                String localization = latitudeModel.getObject() + "," + longitudeModel.getObject() + "/@" + latitudeModel.getObject() + "," + longitudeModel.getObject();
                return "https://www.google.com.br/maps/place/" + localization + "," + zoomModel.getObject() + "z";
            } else {
                return "https://www.google.com.br/maps/search/-15.7481632,-47.8872134,15";
            }
        });

        verNoMaps = new ExternalLink("verNoMaps", googleMapsLinkModel, $m.ofValue("Visualizar no Google Maps")) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("target", "_blank");
            }
        };
        mapStatic = new ImgMap(MAP_STATIC_ID, $m.loadable(() -> {
            String latLng = generateLatLngMaker(latitudeModel, longitudeModel);
            return configureMapToView(Collections.singletonList(latLng), zoomModel, null);
        }));
    }

    /**
     * Create a static map for visualization.
     *
     * @param latLngMakerList The list of latitude and longitude markers.
     * @param zoomModel       The selected zoom
     * @param latLngList      The list of lat and long object.
     * @return
     */
    private String configureMapToView(List<String> latLngMakerList, IModel<?> zoomModel, List<LatLng> latLngList) {
        String latLng = "-15.7922, -47.4609";
        StringBuilder marker = new StringBuilder();
        if (CollectionUtils.isNotEmpty(latLngMakerList)) {
            latLngMakerList.stream()
                    .filter(l -> l != null && !l.isEmpty())
                    .forEach(l -> marker.append("&markers=").append(l));
            if (StringUtils.isNotEmpty(latLngMakerList.get(0))) {
                latLng = latLngMakerList.get(0);
            }
        }

        StringBuilder parameters = new StringBuilder();
        parameters.append("key=").append(singularKeyMapStatic);
        parameters.append("&size=1000x").append(getHeight() - 35);


        parameters.append("&center=").append(latLng);
        if (multipleMarkers && CollectionUtils.isNotEmpty(latLngMakerList) && latLngMakerList.size() > 1) {
            /*Caso tenha apenas um ponto selecionado não é recomendado deixar o Zoom automatico,
            pois ele vai para o Zoom mais proximo deixando uma visualização ruim.*/
            parameters.append("&path=color:0x0ea001AA|weight:0|fillcolor:0xFFB6C1BB");
            parameters.append("|enc:").append(PolylineEncoding.encode(latLngList));
        } else {
            parameters.append("&zoom=").append(zoomModel.getObject());
        }
        parameters.append(marker);
        return "https://maps.googleapis.com/maps/api/staticmap?" + parameters.toString();
    }

    /**
     * Configuration the parameters of the Google maps.
     */
    void populateMetaData(boolean forceKmlUrlVisible) {
        JSONObject json = new JSONObject();
        json.put("idClearButton", clearButton.getMarkupId(true));
        json.put("idCurrentLocationButton", currentLocationButton.getMarkupId(true));
        json.put("idMap", map.getMarkupId(true));
        json.put("idLat", ids.latitudeId);
        json.put("idLng", ids.longitudeId);
        json.put("idZoom", ids.zoomId);
        json.put("readOnly", isReadOnly());
        json.put("tableContainerId", tableContainerId);
        json.put("callbackUrl", callbackUrl);
        json.put("multipleMarkers", multipleMarkers);
        json.put("urlKml", getKmlUrl());
        json.put("isKmlUrlVisible", forceKmlUrlVisible);
        metaDataModel.setObject(json.toString());
        metaData.inputChanged();
    }

    private void populateMetaData() {
        populateMetaData(false);
    }


    protected Integer getHeight() {
        return HEIGHT;
    }

    private String stringfyId(Component c) {
        return "'" + c.getMarkupId(true) + "'";
    }

    private boolean isVisualization() {
        return visualization;
    }

    private boolean isReadOnly() {
        SViewCurrentLocation view = viewSupplier.get();
        if (view != null)
            return visualization || view.isDisableUserLocationSelection();
        return visualization;
    }

    public void enableMultipleMarkers(String callbackUrl, String tableContainerId) {
        this.multipleMarkers = true;
        this.callbackUrl = callbackUrl;
        this.tableContainerId = tableContainerId;
    }

    /**
     * Create panel with erros about the keys of the google maps.
     * If the google maps keys are not configured, will be added some errors label.
     *
     * @param panelErrorMsg The panel that will be added.
     * @param templatePanel The panel with the google maps. [Will appear just if the keys of google maps is configured].
     */
    private void createErrorPanel(TemplatePanel panelErrorMsg, TemplatePanel templatePanel) {
        Component errorMapStatic = new Label("errorMapStatic", "Não foi encontrada a Key do Google Maps Static no arquivo singular.properties").setVisible(false);
        Component errorMapJS = new Label("errorMapJS", "Não foi encontrada a Key do Google Maps JS no arquivo singular.properties").setVisible(false);

        panelErrorMsg.add(errorMapJS, errorMapStatic);
        templatePanel.add(verNoMaps, clearButton, currentLocationButton, map, metaData, mapStatic);

        if (StringUtils.isBlank(singularKeyMapStatic)) {
            templatePanel.setVisible(false);
            errorMapStatic.setVisible(true);
        }
        if (StringUtils.isBlank(singularKeyMaps)) {
            templatePanel.setVisible(false);
            errorMapJS.setVisible(true);
        }
    }

    private static class ImgMap extends WebComponent {
        ImgMap(String id, IModel<?> model) {
            super(id, model);
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            checkComponentTag(tag, "img");
            tag.put("src", StringEscapeUtils.unescapeHtml4(getDefaultModelObjectAsString()));
        }
    }

    public boolean isKmlUrlVisible() {
        return StringUtils.isNotBlank(getKmlUrl());
    }

    public String getKmlUrl() {
        return kmlUrl;
    }

    public void setKmlUrl(String kmlUrl) {
        this.kmlUrl = kmlUrl;
    }

}
