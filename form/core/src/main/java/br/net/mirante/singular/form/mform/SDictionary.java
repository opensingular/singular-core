package br.net.mirante.singular.form.mform;

import java.util.Collection;

import br.net.mirante.singular.form.mform.basic.view.ViewResolver;
import br.net.mirante.singular.form.mform.core.SPackageCore;
import br.net.mirante.singular.form.mform.document.SDocument;

public class SDictionary implements IContextoTipo {

    private MapaNomeClasseValor<SPackage> pacotes = new MapaNomeClasseValor<>(p -> p.getNome());

    private MapaNomeClasseValor<SType<?>> tipos = new MapaNomeClasseValor<>(t -> t.getNome());

    private final SDocument internalDocument = new SDocument();

    private ViewResolver viewResolver;

    private SDictionary() {
    }

    /**
     * Apenas para uso interno do dicionario de modo que os atributos dos tipos
     * tenha um documento de referencia.
     */
    final SDocument getInternalDicionaryDocument() {
        return internalDocument;
    }

    public Collection<SPackage> getPacotes() {
        return pacotes.getValores();
    }

    /**
     * Retorna o registro e resolvedor (calculador) de views para as instâncias.
     * Permite registra view e decidir qual a view mais pertinente para a
     * instância alvo.
     */
    public ViewResolver getViewResolver() {
        if (viewResolver == null) {
            viewResolver = new ViewResolver();
        }
        return viewResolver;
    }

    public static SDictionary create() {
        SDictionary dicionario = new SDictionary();
        dicionario.carregarPacote(SPackageCore.class);
        return dicionario;
    }

    public <T extends SPackage> T carregarPacote(Class<T> classePacote) {
        if (classePacote == null){
            throw new SingularFormException("Classe pacote não pode ser nula");
        }
        T novo = pacotes.get(classePacote);
        if (novo == null) {
            pacotes.vericaNaoDeveEstarPresente(classePacote);
            novo = MapaNomeClasseValor.instanciar(classePacote);
            pacotes.vericaNaoDeveEstarPresente(novo);
            carregarInterno(novo);
        }
        return novo;
    }

    public PacoteBuilder criarNovoPacote(String nome) {
        pacotes.vericaNaoDeveEstarPresente(nome);
        SPackage novo = new SPackage(nome);
        novo.setDicionario(this);
        pacotes.add(novo);
        return new PacoteBuilder(novo);
    }

    final static MInfoTipo getAnotacaoMFormTipo(Class<?> classeAlvo) {
        MInfoTipo mFormTipo = classeAlvo.getAnnotation(MInfoTipo.class);
        if (mFormTipo == null) {
            throw new SingularFormException("O tipo '" + classeAlvo.getName() + " não possui a anotação @" + MInfoTipo.class.getSimpleName()
                    + " em sua definição.");
        }
        return mFormTipo;
    }

    private static Class<? extends SPackage> getAnotacaoPacote(Class<?> classeAlvo) {
        Class<? extends SPackage> pacote = getAnotacaoMFormTipo(classeAlvo).pacote();
        if (pacote == null) {
            throw new SingularFormException(
                    "O tipo '" + classeAlvo.getName() + "' não define o atributo 'pacote' na anotação @"
                    + MInfoTipo.class.getSimpleName());
        }
        return pacote;
    }

    @Override
    public <T extends SType<?>> T getTipoOpcional(Class<T> classeTipo) {
        T tipoRef = tipos.get(classeTipo);
        if (tipoRef == null) {
            Class<? extends SPackage> classPacote = getAnotacaoPacote(classeTipo);
            carregarPacote(classPacote);

            tipoRef = tipos.get(classeTipo);
        }
        return tipoRef;
    }

    public <I extends SInstance, T extends SType<I>> I novaInstancia(Class<T> classeTipo) {
        return getTipo(classeTipo).novaInstancia();
    }

    final MapaNomeClasseValor<SType<?>> getTiposInterno() {
        return tipos;
    }

    @SuppressWarnings("unchecked")
    final <T extends SType<?>> T registrarTipo(MEscopo escopo, T novo, Class<T> classeDeRegistro) {
        if (classeDeRegistro != null) {
            Class<? extends SPackage> classePacoteAnotado = getAnotacaoPacote(classeDeRegistro);
            SPackage pacoteAnotado = pacotes.getOrInstanciar(classePacoteAnotado);
            SPackage pacoteDestino = findPacote(escopo);
            if (!pacoteDestino.getNome().equals(pacoteAnotado.getNome())) {
                throw new SingularFormException("Tentativa de carregar o tipo '" + novo.getNomeSimples() + "' anotado para o pacote '"
                    + pacoteAnotado.getNome() + "' como sendo do pacote '" + pacoteDestino.getNome() + "'");
            }
        }
        novo.setEscopo(escopo);
        novo.resolverSuperTipo(this);
        tipos.vericaNaoDeveEstarPresente(novo);
        ((MEscopoBase) escopo).registrar(novo);
        tipos.add(novo, (Class<SType<?>>) classeDeRegistro);
        return novo;
    }

    private static SPackage findPacote(MEscopo escopo) {
        while (escopo != null && !(escopo instanceof SPackage)) {
            escopo = escopo.getEscopoPai();
        }
        return (SPackage) escopo;
    }

    @Override
    public SType<?> getTipoOpcional(String pathNomeCompleto) {
        return tipos.get(pathNomeCompleto);
    }

    private void carregarInterno(SPackage novo) {
        PacoteBuilder pb = new PacoteBuilder(novo);
        novo.setDicionario(this);
        pacotes.add(novo);
        novo.carregarDefinicoes(pb);
    }

    public void debug() {
        System.out.println("=======================================================");
        pacotes.forEach(p -> p.debug());
        System.out.println("=======================================================");
    }
}
