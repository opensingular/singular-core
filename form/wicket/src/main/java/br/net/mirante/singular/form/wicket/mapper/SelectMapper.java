package br.net.mirante.singular.form.wicket.mapper;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.AbstractSingleSelectChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IObjectClassAwareModel;

import br.net.mirante.singular.form.mform.MILista;
import br.net.mirante.singular.form.mform.MISimples;
import br.net.mirante.singular.form.mform.MInstancia;
import br.net.mirante.singular.form.mform.MTipo;
import br.net.mirante.singular.form.mform.basic.view.MView;
import br.net.mirante.singular.form.mform.core.MTipoString;
import br.net.mirante.singular.form.mform.options.MISelectItem;
import br.net.mirante.singular.form.mform.options.MOptionsProvider;
import br.net.mirante.singular.form.mform.options.MTipoSelectItem;
import br.net.mirante.singular.form.wicket.model.IMInstanciaAwareModel;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSContainer;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSControls;

@SuppressWarnings("serial")
public class SelectMapper implements ControlsFieldComponentMapper {

    @Override
    @SuppressWarnings("rawtypes")
    public Component appendInput(MView view, BSContainer bodyContainer, BSControls formGroup, IModel<? extends MInstancia> model, IModel<String> labelModel) {
        List<SelectOption<String>> opcoesValue = newArrayList();
        MTipo<?> type = model.getObject().getMTipo();
        if (type instanceof MTipoString && ((MTipoString) model.getObject().getMTipo()).getProviderOpcoes() != null) {
            opcoesValue = addStringOptions(model);
        } else if (type instanceof MTipoSelectItem) {
            MTipoSelectItem selectType = (MTipoSelectItem) type;
            if (selectType.getProviderOpcoes() != null) {
                MOptionsProvider opcoes = selectType.getProviderOpcoes();
                MILista<MISelectItem> rawOptions = (MILista<MISelectItem>) opcoes.listAvailableOptions(model.getObject());
                opcoesValue = rawOptions.getValores().stream().map((x) -> new SelectOption<>(x.getFieldId(), x.getFieldValue())).collect(Collectors.toList());
            }
        }

        return formGroupAppender(formGroup, model, opcoesValue);
    }

    private List<SelectOption<String>> addStringOptions(IModel<? extends MInstancia> model) {
        MOptionsProvider opcoes = ((MTipoString) model.getObject().getMTipo()).getProviderOpcoes();
        List<SelectOption<String>> opcoesValue = newArrayList();
        opcoesValue.addAll(opcoes.listAvailableOptions(model.getObject()).getValor().stream().map((x) -> new SelectOption(x.toString(), x)).collect(Collectors.toList()));
        return opcoesValue;
    }

    protected Component formGroupAppender(BSControls formGroup, IModel<? extends MInstancia> model, final List<SelectOption<String>> opcoesValue) {
        final DropDownChoice<SelectOption> choices = (DropDownChoice<SelectOption>) retrieveChoices(model, opcoesValue);
        formGroup.appendSelect(choices.setNullValid(true), false, false);
        return choices;
    }

    protected AbstractSingleSelectChoice<SelectOption> retrieveChoices(IModel<? extends MInstancia> model, final List<SelectOption<String>> opcoesValue) {
        String id = model.getObject().getNome();
//        return new DropDownChoice<>(id, new MInstanciaValorModel<>(model), opcoesValue, choiceRenderer);
        return new DropDownChoice<SelectOption>(id, 
                new MSelectionInstanceModel(model), opcoesValue, rendererer());
    }

    protected ChoiceRenderer rendererer() {
        ChoiceRenderer choiceRenderer = new ChoiceRenderer("value", "key");
        return choiceRenderer;
    }
}

@SuppressWarnings("serial")
class MSelectionInstanceModel implements IModel<SelectOption>,
    IMInstanciaAwareModel<SelectOption>{

    private IModel<? extends MInstancia> model;
    
    public MSelectionInstanceModel(IModel<? extends MInstancia> instanciaModel) {
        this.model = instanciaModel;
    }
    
    @Override
    public SelectOption getObject() {
        if(model.getObject() instanceof MISimples){
            Object value = ((MISimples) model.getObject()).getValor();
            String v = value != null ? value.toString() : null;
            return new SelectOption<String>(v, v);
        }else if (model instanceof MISelectItem){
            MISelectItem item = (MISelectItem) model.getObject();
            return new SelectOption<String>(item.getFieldId(), item.getFieldValue());
        }
        return null;
    }

    @Override
    public void setObject(SelectOption object) {
        MInstancia instance = model.getObject();
        if(instance instanceof MISimples){
            Object value = null;
            if(object != null) value = object.getValue();
            instance.setValor(value);
        }
        else if(instance instanceof MISelectItem) {
            MISelectItem item = (MISelectItem) instance;
            item.setValorItem(object.getKey(), object.getValue()); 
        }
    }

    @Override
    public void detach() {
        model.detach();
    }

    @Override
    public MInstancia getMInstancia() {
        return model.getObject();
    }
    
}
