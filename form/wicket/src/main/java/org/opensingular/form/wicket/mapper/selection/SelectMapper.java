/*
 * Copyright (c) 2016, Singular and/or its affiliates. All rights reserved.
 * Singular PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.form.wicket.mapper.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;

import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.form.SInstance;
import org.opensingular.form.converter.SInstanceConverter;
import org.opensingular.form.provider.AtrProvider;
import org.opensingular.form.provider.Provider;
import org.opensingular.form.provider.ProviderContext;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.mapper.AbstractControlsFieldComponentMapper;
import org.opensingular.form.wicket.model.SelectSInstanceAwareModel;
import org.opensingular.form.wicket.renderer.SingularChoiceRenderer;
import org.opensingular.lib.wicket.util.bootstrap.layout.BSControls;

public class SelectMapper extends AbstractControlsFieldComponentMapper {

    private static final long serialVersionUID = 3837032981059048504L;

    @Override
    public Component appendInput(WicketBuildContext ctx, BSControls formGroup, IModel<String> labelModel) {
        final IModel<? extends SInstance> model = ctx.getModel();
        
        final DropDownChoice<Serializable> dropDownChoice = new DropDownChoice<Serializable>(ctx.getCurrentInstance().getName(),
                new SelectSInstanceAwareModel(model),
                getChoicesDetachableModel(model),
                new SingularChoiceRenderer(model)) {
            @Override
            protected String getNullValidDisplayValue() {
                return "Selecione";
            }

            @Override
            protected String getNullKeyDisplayValue() {
                return null;
            }

            @Override
            public boolean isNullValid() {
                return true;
            }
        };
        formGroup.appendSelect(dropDownChoice);
        return dropDownChoice;
    }

    public String getReadOnlyFormattedText(IModel<? extends SInstance> model) {
        final SInstance mi = model.getObject();
        if (mi != null && mi.getValue() != null) {
            Serializable instanceObject = mi.getType().asAtrProvider().getConverter().toObject(mi);
            if (instanceObject != null) {
                return mi.getType().asAtrProvider().getDisplayFunction().apply(instanceObject);
            }
        }
        return StringUtils.EMPTY;
    }

    protected LoadableDetachableModel<List<Serializable>> getChoicesDetachableModel(IModel<? extends SInstance> model){
        return new DefaultOptionsProviderLoadableDetachableModel(model);
    }


    public static class DefaultOptionsProviderLoadableDetachableModel extends LoadableDetachableModel<List<Serializable>> {

        private static final long serialVersionUID = -3852358882003412437L;

        private final IModel<? extends SInstance> model;

        public DefaultOptionsProviderLoadableDetachableModel(IModel<? extends SInstance> model) {
            this.model = model;
        }

        @Override
        protected List<Serializable> load() {

            final AtrProvider        atrProvider = model.getObject().asAtrProvider();
            final Provider           provider    = atrProvider.getProvider();
            final List<Serializable> values      = new ArrayList<>();

            if (provider != null) {
                final List<Serializable> result = provider.load(ProviderContext.of(model.getObject()));
                if (result != null) {
                    values.addAll(result);
                }
            }

            if (!model.getObject().isEmptyOfData()) {

                final SInstanceConverter        converter    = atrProvider.getConverter();
                final Serializable              converted    = converter.toObject(model.getObject());
                final RequestCycle              requestCycle = RequestCycle.get();
                final List<Object>              ids          = new ArrayList<>();
                final IFunction<Object, Object> idFunction   = atrProvider.getIdFunction();

                /**
                 * Collect All Ids
                 */
                values.forEach(v -> ids.add(idFunction.apply(v)));

                if (!ids.contains(idFunction.apply(converted))) {

                    /**
                     * Se for requisição Ajax, limpa o campo caso o valor não for encontrado,
                     * caso contrario mantem o valor.
                     */

                    if (requestCycle != null && requestCycle.find(AjaxRequestTarget.class) != null) {
                        model.getObject().clearInstance();
                    } else {
                        values.add(0, converted);
                    }
                }
            }

            return values;
        }
    }

}