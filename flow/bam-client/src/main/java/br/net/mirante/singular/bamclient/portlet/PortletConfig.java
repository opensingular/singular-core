package br.net.mirante.singular.bamclient.portlet;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.net.mirante.singular.bamclient.util.SelfReference;


public abstract class PortletConfig<T extends PortletConfig> implements Serializable, SelfReference<T> {

    private PortletSize portletSize = PortletSize.MEDIUM;
    private String title;
    private String subtitle;
    private List<PortletQuickFilter> quickFilter = new ArrayList<>();

    public T setPortletSize(PortletSize portletSize) {
        this.portletSize = portletSize;
        return self();
    }

    public T setTitle(String title) {
        this.title = title;
        return self();
    }

    public T setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return self();
    }

    public T setQuickFilter(List<PortletQuickFilter> quickFilter) {
        this.quickFilter = quickFilter;
        return self();
    }

    public PortletSize getPortletSize() {
        return portletSize;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public List<PortletQuickFilter> getQuickFilter() {
        return quickFilter;
    }

}
