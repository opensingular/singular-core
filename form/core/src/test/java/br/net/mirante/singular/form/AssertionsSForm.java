package br.net.mirante.singular.form;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Classe de apoio a a escrita de asserções referente ao Singular Form.
 *
 * @author Daniel C. Bordin
 */
public class AssertionsSForm {

    private AssertionsSForm() {
    }

    /** Cria assertivas para um {@link br.net.mirante.singular.form.SType}. */
    public static AssertionsSType assertType(SType<?> type) {
        return new AssertionsSType(type);
    }

    /** Cria assertivas para um {@link br.net.mirante.singular.form.SInstance}. */
    public static AssertionsSInstance assertInstance(SInstance instance) {
        return new AssertionsSInstance(instance);
    }

}
