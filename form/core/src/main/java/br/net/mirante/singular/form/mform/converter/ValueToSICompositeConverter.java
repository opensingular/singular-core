package br.net.mirante.singular.form.mform.converter;

import br.net.mirante.singular.form.mform.SIComposite;
import br.net.mirante.singular.form.mform.SInstance;
import br.net.mirante.singular.form.mform.SingularFormException;

import java.io.Serializable;

@FunctionalInterface
public interface ValueToSICompositeConverter<T extends Serializable> extends SInstanceConverter<T, SInstance> {

    @Override
    default void fillInstance(SInstance ins, T obj) {
        toInstance((SIComposite) ins, obj);
    }

    void toInstance(SIComposite ins, T obj);

    @Override
    default T toObject(SInstance ins) {
        throw new SingularFormException(ValueToSICompositeConverter.class.getName() + " não é capaz de converter para objeto");
    }

}