package br.net.mirante.singular.form.mform;

import java.util.Objects;

public class SISimple<TIPO_NATIVO> extends SInstance {

    private TIPO_NATIVO valor;

    protected SISimple() {}

    @Override
    public TIPO_NATIVO getValue() {
        return valor;
    }

    @Override
    public void clearInstance() {
        setValue(null);
    }

    @Override
    public TIPO_NATIVO getValorWithDefault() {
        TIPO_NATIVO v = getValue();
        if (v == null) {
            return getMTipo().converter(getMTipo().getValorAtributoOrDefaultValueIfNull());
        }
        return v;
    }

    @Override
    final <T extends Object> T getValorWithDefaultIfNull(PathReader leitor, Class<T> classeDestino) {
        if (!leitor.isEmpty()) {
            throw new RuntimeException("Não ser aplica path a um tipo simples");
        }
        return getValorWithDefault(classeDestino);
    }

    @Override
    protected void resetValue() {
        setValue(null);
    }
    
    /** Indica que o valor da instância atual é null. */
    public boolean isNull() {
        return getValue() == null;
    }

    @Override
    public boolean isEmptyOfData() {
        return getValue() == null;
    }


    @Override
    public final void setValue(Object valor) {
        TIPO_NATIVO oldValue = this.getValue();
        TIPO_NATIVO newValue = getMTipo().converter(valor);
        this.valor = onSetValor(oldValue, newValue);
        if (getDocument() != null && !Objects.equals(oldValue, newValue)) {
            if (isAttribute()) {
                getDocument().getInstanceListeners().fireInstanceAttributeChanged(getAttributeOwner(), this, oldValue, newValue);
            } else {
                getDocument().getInstanceListeners().fireInstanceValueChanged(this, oldValue, newValue);
            }
        }
    }

    protected TIPO_NATIVO onSetValor(TIPO_NATIVO oldValue, TIPO_NATIVO newValue) {
        return newValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public STypeSimple<?, TIPO_NATIVO> getMTipo() {
        return (STypeSimple<?, TIPO_NATIVO>) super.getMTipo();
    }

    @Override
    public String getDisplayString() {
        return getMTipo().toStringDisplay(getValue());
    }

    public String toStringPersistencia() {
        if (getValue() == null) {
            return null;
        }
        return getMTipo().toStringPersistencia(getValue());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMTipo() == null) ? 0 : getMTipo().hashCode());
        result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SISimple<?> other = (SISimple<?>) obj;
        if (!getMTipo().equals(other.getMTipo())
                && !getMTipo().getName().equals(other.getMTipo().getName())) {
            return false;
        }
        if (getValue() == null) {
            if (other.getValue() != null)
                return false;
        } else if (!getValue().equals(other.getValue()))
            return false;
        return true;
    }

}
