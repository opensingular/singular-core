/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.singular.form.wicket.mapper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.json.JsonFunction;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import org.opensingular.form.SInstance;
import org.opensingular.form.type.basic.SPackageBasic;
import org.opensingular.singular.form.wicket.WicketBuildContext;
import org.opensingular.singular.form.wicket.behavior.InputMaskBehavior;
import org.opensingular.singular.form.wicket.model.SInstanceValueModel;
import org.opensingular.singular.util.wicket.bootstrap.layout.BSControls;

public class DecimalMapper extends StringMapper {

    private static final int DEFAULT_INTEGER_DIGITS = 9;
    private static final int DEFAULT_DIGITS         = 2;

    @Override
    public Component appendInput(WicketBuildContext ctx, BSControls formGroup, IModel<String> labelModel) {
        final IModel<? extends SInstance> model = ctx.getModel();

        Integer decimalMaximo = getDecimalMaximo(model);
        TextField<String> comp = new TextField<String>(model.getObject().getName(),
            new SInstanceValueModel<>(model), String.class) {
            @Override
            public IConverter getConverter(Class type) {
                return new BigDecimalConverter(decimalMaximo);
            }
        };
        formGroup.appendInputText(comp.setLabel(labelModel).setOutputMarkupId(true)
            .add(new InputMaskBehavior(withOptionsOf(model), true)));
        return comp;
    }

    private Map<String, Object> withOptionsOf(IModel<? extends SInstance> model) {
        Optional<Integer> inteiroMaximo = Optional.ofNullable(
            model.getObject().getAttributeValue(SPackageBasic.ATR_INTEGER_MAX_LENGTH));
        Integer decimal = getDecimalMaximo(model);
        Map<String, Object> options = defaultOptions();
        options.put("integerDigits", inteiroMaximo.orElse(DEFAULT_INTEGER_DIGITS));
        options.put("digits", decimal);
        return options;
    }

    private Integer getDecimalMaximo(IModel<? extends SInstance> model) {
        Optional<Integer> decimalMaximo = Optional.ofNullable(
            model.getObject().getAttributeValue(SPackageBasic.ATR_FRACTIONAL_MAX_LENGTH));
        return (Integer) decimalMaximo.orElse(DEFAULT_DIGITS);
    }

    private Map<String, Object> defaultOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("alias", "decimal");
        options.put("placeholder", "0");
        options.put("radixPoint", ",");
        options.put("groupSeparator", ".");
        options.put("autoGroup", true);
        options.put("digitsOptional", true);
        options.put("onBeforePaste", new JsonFunction("function (pastedValue, opts) {" +
            "                return pastedValue.replace(/[^0-9,]/g, '');" +
            "            }"));

        return options;
    }

    @Override
    public String getReadOnlyFormattedText(IModel<? extends SInstance> model) {
        final SInstance mi = model.getObject();

        if ((mi != null) && (mi.getValue() != null)) {

            final BigDecimal valor = (BigDecimal) mi.getValue();
            return formatDecimal(valor, true);
        }

        return StringUtils.EMPTY;
    }

    private String formatDecimal(BigDecimal bigDecimal, boolean groupingUsed) {
        DecimalFormat nf = (DecimalFormat) DecimalFormat.getInstance(new Locale("pt", "BR"));
        nf.setParseBigDecimal(true);
        nf.setGroupingUsed(groupingUsed);
        nf.setMinimumFractionDigits(0);
        return nf.format(bigDecimal);
    }

    @SuppressWarnings("rawtypes")
    private class BigDecimalConverter implements IConverter {
        private Integer maximoCasasDecimais;

        public BigDecimalConverter(Integer maximoCasasDecimais) {
            this.maximoCasasDecimais = maximoCasasDecimais;
        }

        @Override
        public Object convertToObject(String value, Locale locale) {
            if (!StringUtils.isEmpty(value)) {
                return new BigDecimal(value.replaceAll("\\.", "").replaceAll(",", "."));
            }

            return null;
        }

        @Override
        public String convertToString(Object value, Locale locale) {
            if (value == null) {
                return "";
            } else if (value instanceof String) {
                value = convertToObject((String) value, locale);
            }

            BigDecimal bigDecimal = (BigDecimal) value;
            int casasValue = bigDecimal.scale();
            int casasDecimais = casasValue < this.maximoCasasDecimais ? casasValue : this.maximoCasasDecimais;
            return formatDecimal(bigDecimal.setScale(casasDecimais, BigDecimal.ROUND_HALF_UP), false);
        }

    }
}