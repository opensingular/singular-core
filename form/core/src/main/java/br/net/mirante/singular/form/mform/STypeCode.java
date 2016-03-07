package br.net.mirante.singular.form.mform;

import br.net.mirante.singular.form.mform.core.SPackageCore;

@MInfoTipo(nome = "MTipoCode", pacote = SPackageCore.class)
public class STypeCode<I extends SICode<V>, V> extends SType<I> {

    private Class<V> codeClass;

    public STypeCode() {}

    public STypeCode(Class<I> instanceClass, Class<V> valueClass) {
        super(instanceClass);
        this.codeClass = valueClass;
    }

    public Class<V> getCodeClass() {
        return codeClass;
    }
    @SuppressWarnings("unchecked")
    @Override
    public <C> C converter(Object valor, Class<C> classeDestino) {
        return (C) valor;
    }
}