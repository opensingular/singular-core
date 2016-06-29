/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.form.wicket;

import br.net.mirante.singular.form.SType;
import br.net.mirante.singular.form.SingularFormException;
import br.net.mirante.singular.form.context.SingularFormConfigImpl;

import java.util.Map;


public class SingularFormConfigWicketImpl extends SingularFormConfigImpl implements SingularFormConfigWicket {

    private UIBuilderWicket buildContext = new UIBuilderWicket();

    public void setCustomMappers(Map<Class<? extends SType>, Class<IWicketComponentMapper>> customMappers) {
        if (customMappers != null) {
            for (Map.Entry<Class<? extends SType>, Class<IWicketComponentMapper>> entry : customMappers.entrySet()) {
                Class<IWicketComponentMapper> c = entry.getValue();
                buildContext.getViewMapperRegistry().register(entry.getKey(), () -> {
                    try {
                        return c.newInstance();
                    } catch (Exception e) {
                        throw new SingularFormException("Não é possível instanciar o mapper: " + entry.getValue(), e);
                    }
                });
            }
        }
    }

    @Override
    public SingularFormContextWicket createContext() {
        return new SingularFormContextWicketImpl(this);
    }
}
