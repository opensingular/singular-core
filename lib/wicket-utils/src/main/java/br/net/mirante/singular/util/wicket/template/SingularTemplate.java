/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.util.wicket.template;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.google.common.collect.ImmutableList;

public abstract class SingularTemplate extends WebPage {

    public static final String JAVASCRIPT_CONTAINER = "javascript-container";

    private static final List<HeaderItem> DEFAULT_CSS;
    private static final List<HeaderItem> DEFAULT_JS;
    
    public static List<HeaderItem> getDefaultCSSUrls() {
        return DEFAULT_CSS;
    }

    public static List<HeaderItem> getDefaultJavaScriptsUrls() {
        return DEFAULT_JS;
    }
    
    public final SkinOptions skinOptions = new SkinOptions();

    public SingularTemplate() {
        initSkins();
    }

    public SingularTemplate(IModel<?> model) {
        super(model);
        initSkins();
    }

    public SingularTemplate(PageParameters parameters) {
        super(parameters);
        initSkins();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        getApplication()
                .setHeaderResponseDecorator(r -> new JavaScriptFilteredIntoFooterHeaderResponse(r, SingularTemplate.JAVASCRIPT_CONTAINER));
        getApplication()
                .getJavaScriptLibrarySettings()
                .setJQueryReference(new PackageResourceReference(SingularTemplate.class, "empty.js"));

        add(new Label("pageTitle", new ResourceModel(getPageTitleLocalKey())));
        add(new HeaderResponseContainer(JAVASCRIPT_CONTAINER, JAVASCRIPT_CONTAINER));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        getDefaultCSSUrls().forEach(response::render);
        getDefaultJavaScriptsUrls().forEach(response::render);
        response.render(skinOptions.currentSkin().getRef());
    }

    protected String getPageTitleLocalKey() {
        return "label.page.title.local";
    }

    protected void initSkins() {
        skinOptions.addDefaulSkin("Default", CssHeaderItem.forUrl("/singular-static/resources/metronic/layout4/css/themes/default.css"));
        skinOptions.addSkin("Anvisa", CssHeaderItem.forUrl("/singular-static/resources/singular/themes/anvisa.css"));
        skinOptions.addSkin("Montreal", CssHeaderItem.forUrl("/singular-static/resources/singular/themes/montreal.css"));
    }


    public SkinOptions getSkinOptions() {
        return skinOptions;
    }
    
    static {
        DEFAULT_CSS = Arrays.asList(
            "/singular-static/resources/metronic/global/plugins/font-awesome/css/font-awesome.min.css",
            "/singular-static/resources/metronic/global/plugins/simple-line-icons/simple-line-icons.min.css",
            "/singular-static/resources/metronic/global/plugins/bootstrap/css/bootstrap.min.css",
            "/singular-static/resources/metronic/global/plugins/uniform/css/uniform.default.css",
            "/singular-static/resources/metronic/global/plugins/bootstrap-datepicker/css/bootstrap-datepicker.min.css",
            "/singular-static/resources/metronic/global/plugins/bootstrap-timepicker/css/bootstrap-timepicker.min.css",
            "/singular-static/resources/metronic/global/plugins/bootstrap-select/css/bootstrap-select.min.css",
            "/singular-static/resources/metronic/global/plugins/bootstrap-switch/css/bootstrap-switch.min.css",
            "/singular-static/resources/metronic/global/plugins/jquery-multi-select/css/multi-select.css",
            "/singular-static/resources/metronic/global/plugins/ion.rangeslider/css/normalize.css",
            "/singular-static/resources/metronic/global/plugins/ion.rangeslider/css/ion.rangeSlider.css",
            "/singular-static/resources/metronic/global/plugins/ion.rangeslider/css/ion.rangeSlider.skinHTML5.css",
            "/singular-static/resources/metronic/global/plugins/datatables/plugins/bootstrap/datatables.bootstrap.css",
            "/singular-static/resources/metronic/global/plugins/morris/morris.css",
            "/singular-static/resources/metronic/global/css/components-md.css",
            "/singular-static/resources/metronic/global/css/plugins-md.css",
            "/singular-static/resources/metronic/layout4/css/layout.css",
            "/singular-static/resources/metronic/global/plugins/jquery-file-upload/css/jquery.fileupload.css",
            "/singular-static/resources/singular/plugins/syntaxHighlighter/css/shCore.css",
            "/singular-static/resources/singular/plugins/syntaxHighlighter/css/shThemeDefault.css",
            "/singular-static/resources/metronic/global/plugins/bootstrap-toastr/toastr.min.css",
            "/singular-static/resources/metronic/global/plugins/typeahead/typeahead.css",
            "/singular-static/resources/singular/css/custom.css",
            "resources/custom/css/custom.css")
            .stream().map(CssHeaderItem::forUrl).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
        
        DEFAULT_JS = Stream.concat(
            Arrays.asList("/singular-static/resources/metronic/global/plugins/respond.min.js",
                "/singular-static/resources/metronic/global/plugins/excanvas.min.js")
                .stream().map(url -> JavaScriptHeaderItem.forUrl(url, null, false, "UTF-8", "lt IE 9")),
            Arrays.asList("/singular-static/resources/metronic/global/plugins/jquery-migrate.min.js",
                "/singular-static/resources/metronic/global/plugins/jquery-ui/jquery-ui.min.js",
                "/singular-static/resources/metronic/global/plugins/bootstrap/js/bootstrap.js",
                "/singular-static/resources/metronic/global/plugins/bootstrap-hover-dropdown/bootstrap-hover-dropdown.min.js",
                "/singular-static/resources/metronic/global/plugins/jquery-slimscroll/jquery.slimscroll.min.js",
                "/singular-static/resources/metronic/global/plugins/jquery.blockui.min.js",
                "/singular-static/resources/metronic/global/plugins/jquery.cokie.min.js",
                "/singular-static/resources/metronic/global/plugins/uniform/jquery.uniform.min.js",
                "/singular-static/resources/metronic/global/plugins/bootstrap-datepicker/js/bootstrap-datepicker.min.js",
                "/singular-static/resources/metronic/global/plugins/bootstrap-datepicker/locales/bootstrap-datepicker.pt-BR.min.js",
                "/singular-static/resources/metronic/global/plugins/bootstrap-timepicker/js/bootstrap-timepicker.min.js",
                "/singular-static/resources/metronic/global/plugins/bootstrap-select/js/bootstrap-select.min.js",
                "/singular-static/resources/metronic/global/plugins/bootstrap-switch/js/bootstrap-switch.min.js",
                "/singular-static/resources/metronic/global/plugins/jquery-multi-select/js/jquery.multi-select.js",
                "/singular-static/resources/metronic/global/plugins/jquery-inputmask/jquery.inputmask.bundle.min.js",
                "/singular-static/resources/metronic/global/plugins/datatables/datatables.min.js",
                "/singular-static/resources/metronic/global/plugins/datatables/plugins/bootstrap/datatables.bootstrap.js",
                "/singular-static/resources/metronic/global/plugins/morris/morris.min.js",
                "/singular-static/resources/metronic/global/plugins/morris/raphael-min.js",
                "/singular-static/resources/metronic/global/plugins/jquery.sparkline.min.js",
                "/singular-static/resources/metronic/global/plugins/amcharts/amcharts/amcharts.js",
                "/singular-static/resources/metronic/global/plugins/amcharts/amcharts/serial.js",
                "/singular-static/resources/metronic/global/plugins/amcharts/amcharts/pie.js",
                "/singular-static/resources/metronic/global/plugins/amcharts/amcharts/themes/light.js",
                "/singular-static/resources/metronic/global/plugins/bootstrap-maxlength/bootstrap-maxlength.min.js",
                "/singular-static/resources/metronic/global/plugins/ion.rangeslider/js/ion.rangeSlider.min.js",
                "/singular-static/resources/metronic/global/plugins/bootbox/bootbox.min.js",
                "/singular-static/resources/metronic/global/plugins/jquery-file-upload/js/jquery.iframe-transport.js",
                "/singular-static/resources/metronic/global/plugins/jquery-file-upload/js/jquery.fileupload.js",
                "/singular-static/resources/singular/plugins/jquery-maskmoney/dist/jquery.maskMoney.min.js",
                "/singular-static/resources/singular/plugins/syntaxHighlighter/js/shCore.js",
                "/singular-static/resources/singular/plugins/syntaxHighlighter/js/shBrushJava.js",
                "/singular-static/resources/singular/plugins/syntaxHighlighter/js/shBrushJScript.js",
                "/singular-static/resources/singular/plugins/syntaxHighlighter/js/shBrushXml.js",
                "/singular-static/resources/metronic/global/scripts/app.js",
                "/singular-static/resources/metronic/layout4/scripts/layout.js",
                "/singular-static/resources/metronic/global/plugins/bootstrap-toastr/toastr.min.js",
                "/singular-static/resources/metronic/global/plugins/typeahead/typeahead.bundle.min.js",
                "/singular-static/resources/singular/plugins/stringjs/string.min.js")
                .stream().map(JavaScriptHeaderItem::forUrl))
        .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }
    
}