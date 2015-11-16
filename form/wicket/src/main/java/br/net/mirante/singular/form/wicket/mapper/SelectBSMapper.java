package br.net.mirante.singular.form.wicket.mapper;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import br.net.mirante.singular.form.mform.MInstancia;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSControls;

public class SelectBSMapper extends SelectMapper {

    @Override
    protected Component formGroupAppender(BSControls formGroup, IModel<? extends MInstancia> model,
            final List<String> opcoesValue) {
        final DropDownChoice<String> choices = (DropDownChoice<String>) retrieveChoices(model, opcoesValue);
        formGroup.appendSelect(choices.setNullValid(true));
        return choices;
    }
}
