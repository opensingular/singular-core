package br.net.mirante.singular.form.mform.io;

import br.net.mirante.singular.form.mform.ServiceRef;

/**
 * Faz referência para um serviço que não deverá ser serializado, ou seja, o
 * valor será descartado em caso de serialização da referência. Tipicamente é
 * utilizado para referência do tipo cache ou que pode ser recalculada depois.
 *
 * @author Daniel C. Bordin
 */
public class ServiceRefTransientValue<T> implements ServiceRef<T> {

    private final transient T value;

    public ServiceRefTransientValue(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

}
