package br.net.mirante.singular.form.wicket.model;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IObjectClassAwareModel;

import br.net.mirante.singular.form.mform.SList;
import br.net.mirante.singular.form.mform.SInstance;
import br.net.mirante.singular.form.mform.SType;
import br.net.mirante.singular.form.mform.STypeSimple;

@SuppressWarnings("serial")
public class MInstanciaValorModel<T>
    implements
    IModel<T>,
    IObjectClassAwareModel<T>,
    IMInstanciaAwareModel<T> {

    private IModel<? extends SInstance> instanciaModel;

    public MInstanciaValorModel(IModel<? extends SInstance> instanciaModel) {
        this.instanciaModel = instanciaModel;
    }

    public SInstance getTarget() {
        return instanciaModel.getObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        return (T) getTarget().getValor();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setObject(T object) {
        SInstance target = getTarget();
        if (target instanceof SList) {
            ((SList) target).clear();
            ((List) object).forEach(((SList) target)::addValor);
        } else {
            target.setValor(object);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getObjectClass() {
        SType<?> mtipo = getTarget().getMTipo();
        if (mtipo instanceof STypeSimple<?, ?>) {
            return (Class<T>) ((STypeSimple<?, ?>) mtipo).getClasseTipoNativo();
        }
        return (Class<T>) mtipo.getClasseInstancia();
    }

    @Override
    public SInstance getMInstancia() {
        return instanciaModel.getObject();
    }

    @Override
    public void detach() {
        this.instanciaModel.detach();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((instanciaModel == null) ? 0 : instanciaModel.hashCode());
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
        MInstanciaValorModel<?> other = (MInstanciaValorModel<?>) obj;
        if (instanciaModel == null) {
            if (other.instanciaModel != null)
                return false;
        } else if (!instanciaModel.equals(other.instanciaModel))
            return false;
        return true;
    }
}