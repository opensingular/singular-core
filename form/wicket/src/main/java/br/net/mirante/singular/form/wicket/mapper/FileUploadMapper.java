package br.net.mirante.singular.form.wicket.mapper;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;

import br.net.mirante.singular.form.mform.MInstancia;
import br.net.mirante.singular.form.wicket.model.MInstanciaValorModel;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSControls;

public class FileUploadMapper implements ControlsFieldComponentMapper {


    @Override
    public Component appendInput(BSControls formGroup, IModel<? extends MInstancia> model, IModel<String> labelModel) {
	MInstancia object = model.getObject();
	FileUploadField field = new FileUploadField(object.getNome(), 
					new MInstanciaValorModel<>(model));
	formGroup.appendInputFile(field);
	return field;
    }

}
