package br.net.mirante.singular.form.wicket.mapper;

import br.net.mirante.singular.form.mform.MInstancia;
import br.net.mirante.singular.form.mform.basic.ui.MPacoteBasic;
import br.net.mirante.singular.form.mform.basic.view.MView;
import br.net.mirante.singular.form.wicket.behavior.InputMaskBehavior;
import br.net.mirante.singular.form.wicket.model.MInstanciaValorModel;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSContainer;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSControls;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MonetarioMapper implements ControlsFieldComponentMapper {

    private static final int DEFAULT_INTEGER_DIGITS = 9;
    private static final int DEFAULT_DIGITS = 2;

    private static final String INTEGER_DIGITS = "integerDigits";
    private static final String DIGITS = "digits";

    @Override
    public Component appendInput(MView view, BSContainer bodyContainer, BSControls formGroup,
                                 IModel<? extends MInstancia> model, IModel<String> labelModel)
    {
        TextField<BigDecimal> comp = new TextField<>(model.getObject().getNome(),
                new MInstanciaValorModel<>(model), BigDecimal.class);
        formGroup.appendInputText(comp.setLabel(labelModel).setOutputMarkupId(true)
                .add(new Behavior() {
                    @Override
                    public void beforeRender(Component component) {
                        component.getResponse().write("<div class=\"input-group\">");
                        component.getResponse().write("<div class=\"input-group-addon\">R$</div>");
                    }

                    @Override
                    public void afterRender(Component component) {
                        component.getResponse().write("</div>");
                    }
                })
                .add(new InputMaskBehavior(withOptionsOf(model), false)));
        return comp;
    }

    @Override
    public String getReadOnlyFormatedText(IModel<? extends MInstancia> model) {
        if (model.getObject() != null && model.getObject().getValor() != null) {
            BigDecimal b = (BigDecimal) model.getObject().getValor();
            Integer digitos = (int) withOptionsOf(model).get(DIGITS);
            BigDecimal divisor = BigDecimal.valueOf(Math.pow(10, digitos));
            return String.format("R$ %."+digitos+"f", b.divide(divisor));
        }
        return StringUtils.EMPTY;
    }

    private Map<String, Object> withOptionsOf(IModel<? extends MInstancia> model) {
        Optional<Integer> inteiroMaximo = Optional.ofNullable(
                model.getObject().getValorAtributo(MPacoteBasic.ATR_TAMANHO_INTEIRO_MAXIMO));
        Optional<Integer> decimalMaximo = Optional.ofNullable(
                model.getObject().getValorAtributo(MPacoteBasic.ATR_TAMANHO_DECIMAL_MAXIMO));
        Map<String, Object> options = defaultOptions();
        options.put(INTEGER_DIGITS, inteiroMaximo.orElse(DEFAULT_INTEGER_DIGITS));
        options.put(DIGITS, decimalMaximo.orElse(DEFAULT_DIGITS));
        return options;
    }

    private Map<String, Object> defaultOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("alias", "decimal");
        options.put("radixPoint", ",");
        options.put("groupSeparator", ".");
        options.put("placeholder", "0");
        options.put("autoGroup", true);
        options.put("digitsOptional", false);
        options.put("showMaskOnHover", false);

        return options;
    }
}
