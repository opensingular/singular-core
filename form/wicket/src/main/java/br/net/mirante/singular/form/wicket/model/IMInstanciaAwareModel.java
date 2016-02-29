package br.net.mirante.singular.form.wicket.model;

import java.util.Optional;

import org.apache.wicket.model.IModel;

import br.net.mirante.singular.form.mform.SInstance;

public interface IMInstanciaAwareModel<T> extends IModel<T> {
    SInstance getMInstancia();

    static <X> Optional<IMInstanciaAwareModel<X>> optionalCast(IModel<X> model){
        if(model != null && IMInstanciaAwareModel.class.isAssignableFrom(model.getClass())){
            return Optional.of((IMInstanciaAwareModel<X>) model);
        } else {
            return Optional.empty();
        }
    }

    static IModel<SInstance> getInstanceModel(IMInstanciaAwareModel<?> model) {
        return new IMInstanciaAwareModel<SInstance>() {
            public SInstance getObject() {
                return getMInstancia();
            }
            public SInstance getMInstancia() {
                return model.getMInstancia();
            }
            @Override
            public void setObject(SInstance object) {}
            @Override
            public void detach() {}
        };
    }
}