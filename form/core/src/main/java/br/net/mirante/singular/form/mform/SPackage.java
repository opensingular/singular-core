package br.net.mirante.singular.form.mform;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.NotImplementedException;

public class SPackage extends MEscopoBase {

    private static final Logger LOGGER = Logger.getLogger(SType.class.getName());

    private final String nome;

    private SDictionary dictionary;

    public SPackage() {
        this.nome = getClass().getName();
        MFormUtil.checkNomePacoteValido(nome);
        if (getClass() == SPackage.class) {
            throw new SingularFormException("Deve ser utilizado o construtor " + SPackage.class.getSimpleName() + "(String) ou "
                    + SPackage.class.getSimpleName() + " deve ser derivado");
        }
    }

    protected SPackage(String nome) {
        MFormUtil.checkNomePacoteValido(nome);
        this.nome = nome;
    }

    @Override
    public String getName() {
        return nome;
    }

    protected void carregarDefinicoes(PackageBuilder pb) {
    }

    @Override
    public MEscopo getEscopoPai() {
        return null;
    }

    public <T extends SType<?>> T createType(String nomeSimplesNovoTipo, Class<T> tipoBase) {
        // TODO implementar
        throw new NotImplementedException("TODO implementar");
    }

    @Override
    protected void debug(Appendable appendable, int nivel) {
        try {
            pad(appendable, nivel).append(getName()).append("\n");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        super.debug(appendable, nivel + 1);
    }

    protected static boolean isNull(SISimple<?> campo) {
        return campo == null || campo.isNull();
    }

    protected static boolean isNotNull(SISimple<?> campo) {
        return campo != null && !campo.isNull();
    }

    protected static boolean isTrue(SISimple<?> campo) {
        if (campo != null) {
            return campo.getValorWithDefault(Boolean.class);
        }
        return false;
    }

    @Override
    public SDictionary getDictionary() {
        return dictionary;
    }

    final void setDictionary(SDictionary dictionary) {
        this.dictionary = dictionary;
    }

}