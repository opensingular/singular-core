/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.form.wicket.mapper.selection;

import br.net.mirante.singular.form.SInstance;
import br.net.mirante.singular.form.view.SViewBooleanByRadio;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.IModel;

public class BooleanRadioMapper extends RadioMapper {

    /*@SuppressWarnings("rawtypes")
    @Override
    public IReadOnlyModel<List<SelectOption>> getOpcoesValue(MView view, IModel<? extends SInstance> model) {
        if (!(view instanceof MBooleanRadioView)){
            throw new SingularFormException("Radio mapper requires a MBooleanRadioView configuration.");
        }
        MBooleanRadioView booleanRadioView = (MBooleanRadioView) view;
        return new IReadOnlyModel<List<SelectOption>>() {
            @Override
            public List<SelectOption> getObject() {
                List<SelectOption> opcoesValue = new ArrayList<>(2);
                opcoesValue.add(new SelectOption(booleanRadioView.labelTrue(), true));
                opcoesValue.add(new SelectOption(booleanRadioView.labelFalse(), false));
                return opcoesValue;
            }
        };
    }*/
    
    public String getReadOnlyFormattedText(IModel<? extends SInstance> model) {
        final SInstance mi = model.getObject();
        Boolean valor = mi.getValue(Boolean.class);
        if(valor != null) {
            SViewBooleanByRadio booleanRadioView = (SViewBooleanByRadio) mi.getType().getView();
            if(valor){
                return booleanRadioView.labelTrue();
            } else {
                return booleanRadioView.labelFalse();
            }
        }
        return StringUtils.EMPTY;
    }
}
