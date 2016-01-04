package br.net.mirante.singular.showcase.view.page.peticao;

import br.net.mirante.singular.form.mform.*;
import br.net.mirante.singular.form.mform.basic.ui.AtrBasic;
import br.net.mirante.singular.form.mform.basic.view.*;
import br.net.mirante.singular.form.mform.core.MIString;
import br.net.mirante.singular.form.mform.core.MTipoData;
import br.net.mirante.singular.form.mform.core.MTipoInteger;
import br.net.mirante.singular.form.mform.core.MTipoString;
import br.net.mirante.singular.form.mform.core.attachment.MTipoAttachment;
import br.net.mirante.singular.form.mform.util.comuns.MTipoCNPJ;
import br.net.mirante.singular.form.wicket.AtrBootstrap;

public class MPacotePeticaoGGTOX extends MPacote {

    public static final String PACOTE        = "mform.peticao";
    public static final String TIPO          = "PeticionamentoGGTOX";
    public static final String NOME_COMPLETO = PACOTE + "." + TIPO;
    private DadosResponsavel dadosResponsavel;
    private Componente componentes;


    public MPacotePeticaoGGTOX() {
        super(PACOTE);
    }

    @Override
    protected void carregarDefinicoes(PacoteBuilder pb) {

        final MTipoComposto<?> peticionamento = pb.createTipoComposto(TIPO);

        //TODO deveria ser possivel passar uma coleção para o withSelectionOf

        //TODO solicitar criacao de validacao para esse exemplo:
        /*
        MTipoComposto<MIComposto> sinonimiaComponente = peticionamento.addCampoComposto("sinonimiaComponente");
        
        sinonimiaComponente.as(AtrBasic::new).label("Sinonímia");
        
        sinonimiaComponente.addCampoString("sinonimiaAssociada", true)
                .withSelectionOf("Sinonímia teste",
                        "Sinonímia teste 2",
                        "Sinonímia teste 3")
                .withView(MSelecaoMultiplaPorSelectView::new)
                .as(AtrBasic::new)
                .label("Sinonímias já associadas a esta substância/mistura")
                .enabled(false);
        */

        dadosResponsavel = new DadosResponsavel(pb, peticionamento);
        componentes = new Componente(pb, peticionamento);

        MTabView tabbed = new MTabView();
        tabbed.addTab("tudo", "Tudo").add(dadosResponsavel.root).add(componentes.root);
        tabbed.addTab(dadosResponsavel.root);
        tabbed.addTab(componentes.root);
        peticionamento.withView(tabbed);

    }

    class DadosResponsavel {
        public final MTipoComposto<MIComposto> root;
        public final MTipoString responsavelTecnico, representanteLegal, concordo;

        DadosResponsavel(PacoteBuilder pb, MTipoComposto<?> peticionamento) {
            root = peticionamento.addCampoComposto("dadosResponsavel");

            root.as(AtrBasic::new).label("Dados do Responsável");
            responsavelTecnico = createResponsavelTecnicoField();
            representanteLegal = createRepresentanteLegalField();
            concordo = createConcordoField();


        }

        private MTipoString createResponsavelTecnicoField() {
            //TODO Como fazer a seleção para um objeto composto/enum ?
            //TODO a recuperação de valores deve ser dinamica
            MTipoString field = root.addCampoString("responsavelTecnico", true);
            field.withSelectionOf(getResponsaveis())
                    .withView(MSelecaoPorSelectView::new)
                    .as(AtrBasic::new).label("Responsável Técnico")
                    .as(AtrBootstrap::new).colPreference(3);
            return field;
        }

        private MTipoString createRepresentanteLegalField() {
            MTipoString field = root.addCampoString("representanteLegal", true);
            field.withSelectionOf(getResponsaveis())
                    .withView(MSelecaoPorSelectView::new)
                    .as(AtrBasic::new).label("Representante Legal")
                    .as(AtrBootstrap::new).colPreference(3);
            return field;
        }

        private MTipoString createConcordoField() {
            // TODO preciso de um campo boolean mas as labels devem ser as descritas abaixo
            //TODO deve ser possivel alinhar o texto: text-left text-right text-justify text-nowrap
            MTipoString field = root.addCampoString("concordo", true);
            field.withSelectionOf("Concordo", "Não Concordo")
                    .withView(MSelecaoPorRadioView::new);
            return field;
        }

        private String[] getResponsaveis() {
            return new String[] { "Daniel", "Delfino", "Fabrício", "Lucas", "Tetsuo", "Vinícius" };
        }
    }


    class Componente {
        final MTipoLista<MTipoComposto<MIComposto>, MIComposto> root;
        final MTipoComposto<MIComposto> rootType;
        final Identificacao identificacao;
        final Restricao restricao;
        final Sinonimia sinonimia;
        final Finalidade finalidade;
        final UsoPretendido usoPretendido;
        final NomeComercial nomeComercial;
        final Embalagem embalagem;


        Componente(PacoteBuilder pb, MTipoComposto<?> peticionamento) {
            root = peticionamento.addCampoListaOfComposto("componentes", "componente");
            root.as(AtrBasic::new).label("Componente");
            rootType = root.getTipoElementos();
            rootType.as(AtrBasic::new).label("Registro de Componente");

            identificacao = new Identificacao(pb);
            restricao = new Restricao(pb);
            sinonimia = new Sinonimia(pb);
            finalidade = new Finalidade(pb);
            usoPretendido =  new UsoPretendido(pb);
            nomeComercial = new NomeComercial(pb);
            embalagem = new Embalagem(pb);

            final MTipoLista<MTipoComposto<MIComposto>, MIComposto> anexos = rootType.addCampoListaOfComposto("anexos", "anexo");
            MTipoComposto<MIComposto> anexo = anexos.getTipoElementos();

            anexos
                    .withView(MPanelListaView::new)
                    .as(AtrBasic::new).label("Anexos");

            MTipoAttachment arquivo = anexo.addCampo("arquivo", MTipoAttachment.class);
            arquivo.as(AtrBasic.class).label("Informe o caminho do arquivo para o anexo")
                    .as(AtrBootstrap::new).colPreference(3);

            anexo.addCampoString("tipoArquivo")
                    .withSelectionOf("Ficha de emergência", "Ficha de segurança", "Outros")
                    .withView(MSelecaoPorSelectView::new)
                    .as(AtrBasic::new).label("Tipo do arquivo a ser anexado")
                    .as(AtrBootstrap::new).colPreference(3);

            addTestes(pb, rootType);


            root.withView(new MListMasterDetailView()
                    .col(identificacao.tipoComponente)
                    .col(sinonimia.sugerida)
            );
        }

        private void addTestes(PacoteBuilder pb, MTipoComposto<?> componente) {
            //TODO deve ser encontrado uma maneira de vincular o teste ao componente
            addTesteCaracteristicasFisicoQuimicas(pb, componente);
            addTesteIrritacaoOcular(pb, componente);
        }

        private void addTesteCaracteristicasFisicoQuimicas(PacoteBuilder pb, MTipoComposto<?> componente) {
            final MTipoLista<MTipoComposto<MIComposto>, MIComposto> testes = componente.addCampoListaOfComposto("testesCaracteristicasFisicoQuimicas", "caracteristicasFisicoQuimicas");
            MTipoComposto<MIComposto> teste = testes.getTipoElementos();

            testes
//            .withView(MPanelListaView::new)
                    .as(AtrBasic::new).label("Testes Características fisíco-químicas");

            teste.as(AtrBasic::new).label("Características fisíco-químicas");

            MTipoString estadoFisico = teste.addCampoString("estadoFisico", true);
            estadoFisico.withSelectionOf("Líquido", "Sólido", "Gasoso")
                    .withView(MSelecaoPorSelectView::new)
                    .as(AtrBasic::new).label("Estado físico")
                    .as(AtrBootstrap::new).colPreference(2);

            MTipoString aspecto = teste.addCampoString("aspecto", true);
            aspecto.as(AtrBasic::new).label("Aspecto")
                    .tamanhoMaximo(50)
                    .as(AtrBootstrap::new).colPreference(4);

            MTipoString cor = teste.addCampoString("cor", true);
            cor.as(AtrBasic::new).label("Cor")
                    .tamanhoMaximo(40)
                    .as(AtrBootstrap::new).colPreference(3);

            MTipoString odor = teste.addCampoString("odor");
            odor.as(AtrBasic::new).label("Odor")
                    .tamanhoMaximo(40)
                    .as(AtrBootstrap::new).colPreference(3);

            testes.withView(new MListMasterDetailView()
                    .col(estadoFisico)
                    .col(aspecto)
                    .col(cor)
                    .col(odor)
            );

            MTipoComposto<MIComposto> faixaFusao = teste.addCampoComposto("faixaFusao");
            faixaFusao.as(AtrBootstrap::new).colPreference(6);

            //TODO cade as mascaras para campos decimais
            faixaFusao.as(AtrBasic::new).label("Fusão");

            faixaFusao.addCampoDecimal("pontoFusao")
                    .as(AtrBasic::new).label("Ponto de fusão").subtitle("ºC")
                    .as(AtrBootstrap::new).colPreference(4);

            //TODO o campo faixa de fusao precisa de um tipo intervalo
            // Exemplo: Faixa De 10 a 20
            faixaFusao.addCampoDecimal("faixaFusaoDe")
                    .as(AtrBasic::new).label("Início").subtitle("da Faixa")
                    .as(AtrBootstrap::new).colPreference(4);

            faixaFusao.addCampoDecimal("faixaFusaoA")
                    .as(AtrBasic::new).label("Fim").subtitle("da Faixa")
                    .as(AtrBootstrap::new).colPreference(4);

            MTipoComposto<MIComposto> faixaEbulicao = teste.addCampoComposto("faixaEbulicao");

            faixaEbulicao.as(AtrBasic::new).label("Ebulição")
                    .as(AtrBootstrap::new).colPreference(6);

            faixaEbulicao.addCampoDecimal("pontoEbulicao")
                    .as(AtrBasic::new).label("Ebulição").subtitle("ºC")
                    .as(AtrBootstrap::new).colPreference(4);

            faixaEbulicao.addCampoDecimal("faixaEbulicaoDe")
                    .as(AtrBasic::new).label("Início").subtitle("da Faixa")
                    .as(AtrBootstrap::new).colPreference(4);

            faixaEbulicao.addCampoDecimal("faixaEbulicaoA")
                    .as(AtrBasic::new).label("Fim").subtitle("da Faixa")
                    .as(AtrBootstrap::new).colPreference(4);

            MTipoComposto<MIComposto> pressaoVapor = teste.addCampoComposto("pressaoVapor");
            pressaoVapor.as(AtrBasic::new).label("Pressão do vapor")
                    .as(AtrBootstrap::new).colPreference(6);

            pressaoVapor.addCampoDecimal("valor")
                    .as(AtrBasic::new).label("Valor")
                    .as(AtrBootstrap::new).colPreference(6);

            pressaoVapor.addCampoString("unidade")
                    .withSelectionOf("mmHg", "Pa", "mPa")
                    .withView(MSelecaoPorSelectView::new)
                    .as(AtrBasic::new).label("Unidade")
                    .as(AtrBootstrap::new).colPreference(6);

            MTipoComposto<MIComposto> solubilidade = teste.addCampoComposto("solubilidade");
            solubilidade.as(AtrBasic::new).label("Solubilidade")
                    .as(AtrBootstrap::new).colPreference(6);

            solubilidade.addCampoDecimal("solubilidadeAgua")
                    .as(AtrBasic::new).label("em água").subtitle("mg/L a 20 ou 25 ºC")
                    .as(AtrBootstrap::new).colPreference(6);

            solubilidade.addCampoDecimal("solubilidadeOutrosSolventes")
                    .as(AtrBasic::new).label("em outros solventes").subtitle("mg/L a 20 ou 25 ºC")
                    .as(AtrBootstrap::new).colPreference(6);

            teste.addCampoString("hidrolise")
                    .as(AtrBasic::new).label("Hidrólise")
                    .as(AtrBootstrap::new).colPreference(6);

            teste.addCampoString("estabilidade")
                    .as(AtrBasic::new).label("Estabilidade às temperaturas normal e elevada")
                    .as(AtrBootstrap::new).colPreference(6);

            teste.addCampoDecimal("pontoFulgor")
                    .as(AtrBasic::new).label("Ponto de fulgor").subtitle("ºC")
                    .as(AtrBootstrap::new).colPreference(3);

            teste.addCampoDecimal("constanteDissociacao")
                    .as(AtrBasic::new).label("Constante de dissociação")
                    .as(AtrBootstrap::new).colPreference(3);

            final MTipoLista<MTipoComposto<MIComposto>, MIComposto> phs = teste.addCampoListaOfComposto("phs", "ph");
            MTipoComposto<MIComposto> ph = phs.getTipoElementos();

            ph.addCampoDecimal("valorPh", true)
                    .as(AtrBasic::new).label("pH").subtitle(".")
                    .as(AtrBootstrap::new).colPreference(4);

            ph.addCampoDecimal("solucao", true)
                    .as(AtrBasic::new).label("Solução").subtitle("%")
                    .as(AtrBootstrap::new).colPreference(4);

            ph.addCampoDecimal("temperatura", true)
                    .as(AtrBasic::new).label("Temperatura").subtitle("ºC")
                    .as(AtrBootstrap::new).colPreference(4);

            phs
                    .withView(MPanelListaView::new)
                    .as(AtrBasic::new).label("Lista de pH");

            teste.addCampoDecimal("coeficienteParticao")
                    .as(AtrBasic::new).label("Coeficiente de partição octanol/Água").subtitle("a 20-25 ºC")
                    .as(AtrBootstrap::new).colPreference(4);

            teste.addCampoDecimal("densidade")
                    .as(AtrBasic::new).label("Densidade").subtitle("g/cm³ a 20ºC")
                    .as(AtrBootstrap::new).colPreference(4);

            teste.addCampoString("observacoes")
                    .withView(MTextAreaView::new)
                    .as(AtrBasic::new).label("Observações");
        }

        private void addTesteIrritacaoOcular(PacoteBuilder pb, MTipoComposto<?> componente) {

            final MTipoLista<MTipoComposto<MIComposto>, MIComposto> testes = componente.addCampoListaOfComposto("testesIrritacaoOcular", "irritacaoOcular");
            MTipoComposto<MIComposto> teste = testes.getTipoElementos();

            //TODO criar regra para pelo menos um campo preenchido

            testes
//            .withView(MPanelListaView::new)
                    .as(AtrBasic::new).label("Testes Irritação / Corrosão ocular");

            teste.as(AtrBasic::new).label("Irritação / Corrosão ocular")
                    .as(AtrBootstrap::new).colPreference(4);

            MTipoString laboratorio = teste.addCampoString("laboratorio");
            laboratorio.as(AtrBasic::new).label("Laboratório")
                    .tamanhoMaximo(50);

            MTipoString protocolo = teste.addCampoString("protocoloReferencia");
            protocolo.as(AtrBasic::new).label("Protocolo de referência")
                    .tamanhoMaximo(50);

            MTipoData inicio = teste.addCampoData("dataInicioEstudo");
            inicio.as(AtrBasic::new).label("Data de início do estudo")
                    .as(AtrBootstrap::new).colPreference(3);

            MTipoData fim = teste.addCampoData("dataFimEstudo");
            fim.as(AtrBasic::new).label("Data final do estudo")
                    .as(AtrBootstrap::new).colPreference(3);

            testes.withView(new MListMasterDetailView()
                    .col(laboratorio)
                    .col(protocolo)
                    .col(inicio)
                    .col(fim)
            );

            teste.addCampoString("purezaProdutoTestado")
                    .as(AtrBasic::new).label("Pureza do produto testado")
                    .as(AtrBootstrap::new).colPreference(6);

            teste.addCampoString("unidadeMedida")
                    .withSelectionOf("g/Kg", "g/L")
                    .as(AtrBasic::new).label("Unidade de medida")
                    .as(AtrBootstrap::new).colPreference(2);

            teste.addCampoString("especies")
                    .withSelectionOf("Càes",
                            "Camundongos",
                            "Cobaia",
                            "Coelho",
                            "Galinha",
                            "Informação não disponível",
                            "Peixe",
                            "Primatas",
                            "Rato")
                    .as(AtrBasic::new).label("Espécies")
                    .as(AtrBootstrap::new).colPreference(4);

            teste.addCampoString("linhagem")
                    .as(AtrBasic::new).label("Linhagem")
                    .as(AtrBootstrap::new).colPreference(6);

            teste.addCampoDecimal("numeroAnimais")
                    .as(AtrBasic::new).label("Número de animais")
                    .as(AtrBootstrap::new).colPreference(3);

            teste.addCampoString("veiculo")
                    .as(AtrBasic::new).label("Veículo")
                    .as(AtrBootstrap::new).colPreference(3);

            teste.addCampoString("fluoresceina")
                    .withSelectionOf("Sim", "Não")
                    .as(AtrBasic::new).label("Fluoresceína")
                    .as(AtrBootstrap::new).colPreference(3);

            teste.addCampoString("testeRealizado")
                    .withSelectionOf("Com lavagem", "Sem lavagem")
                    .as(AtrBasic::new).label("Teste realizado")
                    .as(AtrBootstrap::new).colPreference(3);

            MTipoComposto<MIComposto> alteracoes = teste.addCampoComposto("alteracoes");

            alteracoes.as(AtrBasic::new).label("Alterações")
                    .as(AtrBootstrap::new);

            alteracoes.addCampoString("cornea")
                    .withSelectionOf("Sem alterações", "Opacidade persistente", "Opacidade reversível em...")
                    .as(AtrBasic::new).label("Córnea")
                    .as(AtrBootstrap::new).colPreference(6);

            alteracoes.addCampoString("tempoReversibilidadeCornea")
                    .as(AtrBasic::new).label("Tempo de reversibilidade")
                    .as(AtrBootstrap::new).colPreference(6);

            alteracoes.addCampoString("conjuntiva")
                    .withSelectionOf("Sem alterações", "Opacidade persistente", "Opacidade reversível em...")
                    .as(AtrBasic::new).label("Conjuntiva")
                    .as(AtrBootstrap::new).colPreference(6);

            alteracoes.addCampoString("tempoReversibilidadeConjuntiva")
                    .as(AtrBasic::new).label("Tempo de reversibilidade")
                    .as(AtrBootstrap::new).colPreference(6);

            alteracoes.addCampoString("iris")
                    .withSelectionOf("Sem alterações", "Opacidade persistente", "Opacidade reversível em...")
                    .as(AtrBasic::new).label("Íris")
                    .as(AtrBootstrap::new).colPreference(6);

            alteracoes.addCampoString("tempoReversibilidadeIris")
                    .as(AtrBasic::new).label("Tempo de reversibilidade")
                    .as(AtrBootstrap::new).colPreference(6);

            teste.addCampoString("observacoes")
                    .withView(MTextAreaView::new)
                    .as(AtrBasic::new).label("Observações");

        }

        class Identificacao {
            final MTipoComposto<MIComposto> root;
            final MTipoString tipoComponente;
            Identificacao(PacoteBuilder pb){
                root = rootType.addCampoComposto("identificacaoComponente");
                root.as(AtrBasic::new).label("Identificação de Componente")
                        .as(AtrBootstrap::new).colPreference(4);

                tipoComponente = root.addCampoString("tipoComponente", true);
                tipoComponente.withSelectionOf("Substância", "Mistura")
                        .withView(MSelecaoPorRadioView::new)
                        .as(AtrBasic::new).label("Tipo componente");
            }
        }

        class Restricao {
            final MTipoComposto<MIComposto> root;
            Restricao(PacoteBuilder pb){
                root = rootType.addCampoComposto("restricoesComponente");
                root.as(AtrBasic::new).label("Restrições")
                        .as(AtrBootstrap::new).colPreference(4);

                //TODO caso eu marque sem restrições os outros campos devem ser desabilitados
                root.addCampoListaOf("restricoes", pb.createTipo("restricao", MTipoString.class)
                        .withSelectionOf("Impureza relevante presente",
                                "Controle de impureza determinado",
                                "Restrição de uso em algum país",
                                "Restrição de uso em alimentos",
                                "Sem restrições"))
                        .withView(MSelecaoMultiplaPorCheckView::new)
                        .as(AtrBasic::new).label("Restrições");

            }
        }

        class Sinonimia {
            final MTipoComposto<MIComposto> root;
            final MTipoLista<MTipoString, MIString> lista;
            final MTipoString sugerida;

            Sinonimia(PacoteBuilder pb){
                root = rootType.addCampoComposto("sinonimiaComponente");
                root.as(AtrBasic::new).label("Sinonímia").as(AtrBootstrap::new).colPreference(4);

                lista = createListaField(pb);
                sugerida = createSugeridaField();

            }

            private MTipoLista<MTipoString, MIString> createListaField(PacoteBuilder pb) {
                MTipoLista<MTipoString, MIString> field = root.addCampoListaOf("sinonimiaAssociada",
                        pb.createTipo("sinonimia", MTipoString.class)
                                .withSelectionOf("Sinonímia teste", "Sinonímia teste 2", "Sinonímia teste 3"));
                field.withView(MSelecaoMultiplaPorSelectView::new)
                        .as(AtrBasic::new)
                        .label("Sinonímias já associadas a esta substância/mistura")
                        .enabled(false);
                return field;
            }

            private MTipoString createSugeridaField() {
                final MTipoLista<MTipoComposto<MIComposto>, MIComposto> sinonimias = root.addCampoListaOfComposto("sinonimias", "sinonimia");
                final MTipoComposto<?> sinonimia = sinonimias.getTipoElementos();
                MTipoString field = sinonimia.addCampoString("nomeSinonimia", true);

                field.as(AtrBasic::new).label("Sinonímia sugerida").tamanhoMaximo(100);

                field.withView(MTableListaView::new)
                        .as(AtrBasic::new)
                        .label("Lista de sinonímias sugeridas para esta substância/mistura");
                return field;
            }
        }

        class Finalidade{
            final MTipoComposto<MIComposto> root;
            Finalidade(PacoteBuilder pb){
                root = rootType.addCampoComposto("finalidadesComponente");

                root.as(AtrBasic::new).label("Finalidades")
                        .as(AtrBootstrap::new).colPreference(4);

                root.addCampoListaOf("finalidades", pb.createTipo("finalidade", MTipoString.class)
                        .withSelectionOf("Produção","Importação","Exportação","Comercialização","Utilização"))
                        .withView(MSelecaoMultiplaPorCheckView::new);
            }
        }

        class UsoPretendido {
            final MTipoComposto<MIComposto> root;
            UsoPretendido(PacoteBuilder pb){
                //TODO falta criar modal para cadastrar novo uso pretendido
                root = rootType.addCampoComposto("usosPretendidosComponente");

                root.as(AtrBasic::new).label("Uso pretendido").as(AtrBootstrap::new).colPreference(4);

                root.addCampoListaOf("usosPretendidos",
                        pb.createTipo("usoPretendido", MTipoString.class)
                                .withSelectionOf("Uso 1","Uso 2","Uso 3"))
                        .withView(MSelecaoMultiplaPorPicklistView::new)
                        .as(AtrBasic::new).label("Lista de uso pretendido/mistura");
            }
        }

        class NomeComercial {
            final MTipoLista<MTipoComposto<MIComposto>, MIComposto> root;
            final MTipoComposto<MIComposto> type;
            final MTipoString nome;
            final Fabricante fabricante;

            NomeComercial(PacoteBuilder pb){
                root = rootType.addCampoListaOfComposto("nomesComerciais", "nomeComercial");
                root.withView(MPanelListaView::new).as(AtrBasic::new).label("Nome comercial");

                type = root.getTipoElementos();

                nome = type.addCampoString("nome", true);
                nome.as(AtrBasic::new).label("Nome comercial").tamanhoMaximo(80);

                fabricante = new Fabricante(pb);
            }

            class Fabricante{
                final MTipoLista<MTipoComposto<MIComposto>, MIComposto> root;
                Fabricante(PacoteBuilder pb){
                    root = type.addCampoListaOfComposto("fabricantes", "fabricante");

                    //TODO Fabricante deve ser uma pesquisa
                    MTipoComposto<MIComposto> fabricante = root.getTipoElementos();
                    //TODO como usar o tipo cnpj
                    fabricante.addCampo("cnpj", MTipoCNPJ.class).as(AtrBasic::new).label("CNPJ").as(AtrBootstrap::new).colPreference(4);
                    fabricante.addCampoString("razaoSocial").as(AtrBasic::new).label("Razão social").as(AtrBootstrap::new).colPreference(4);
                    fabricante.addCampoString("cidade").as(AtrBasic::new).label("Cidade").as(AtrBootstrap::new).colPreference(2);
                    fabricante.addCampoString("pais").as(AtrBasic::new).label("País").as(AtrBootstrap::new).colPreference(2);

                    root.withView(MListMasterDetailView::new)
                            .as(AtrBasic::new).label("Fabricante(s)");
                }
            }
        }
        class Embalagem {
            final MTipoLista<MTipoComposto<MIComposto>, MIComposto> root;
            final MTipoComposto<MIComposto> type;
            final MTipoString produtoExterior;
            final MTipoString tipo;
            MTipoString material;
            final MTipoString unidadeMedida;
            final MTipoInteger capacidade;
            private final String[]
                    tiposDisponiveis = new String[]{
                    "Balde", "Barrica", "Bombona", "Caixa", "Carro tanque", "Cilindro",
                    "Container", "Frasco", "Galão", "Garrafa", "Lata", "Saco", "Tambor"
            },
                    materiaisDisponiveis = new String[]{"Papel", "Alumínio", "Ferro", "Madeira"
                    };
            Embalagem(PacoteBuilder pb){
                root = rootType.addCampoListaOfComposto("embalagens", "embalagem");
                root.withView(MTableListaView::new).as(AtrBasic::new).label("Embalagem");
                type = root.getTipoElementos();
                produtoExterior = createFieldProdutoExterior();
                tipo = createFieldTipo();
                material = createFieldMaterial();
                capacidade = createFieldCapacidade();
                unidadeMedida = createFieldUnidadeDeMedida();
            }

            private MTipoString createFieldProdutoExterior() {
                //TODO converter sim nao para true false
                MTipoString field = type.addCampoString("produtoExterior", true);
                field.withSelectionOf("Sim", "Não")
                        .withView(MSelecaoPorRadioView::new)
                        .as(AtrBasic::new).label("Produto formulado no exterior?")
                        .as(AtrBootstrap::new).colPreference(12);
                return field;
            }

            private MTipoString createFieldTipo() {
                MTipoString field = type.addCampoString("tipo", true);
                field.withSelectionOf(tiposDisponiveis)
                        .withView(MSelecaoPorSelectView::new)
                        .as(AtrBasic::new).label("Tipo")
                        .as(AtrBootstrap::new).colPreference(4);
                return field;
            }

            private MTipoString createFieldMaterial() {
                MTipoString field = type.addCampoString("material", true);
                field.withSelectionOf(materiaisDisponiveis)
                        .withView(MSelecaoPorSelectView::new)
                        .as(AtrBasic::new).label("Material")
                        .as(AtrBootstrap::new).colPreference(4);
                return field;
            }

            private MTipoInteger createFieldCapacidade() {
                MTipoInteger field = type.addCampoInteger("capacidade", true);
                field.as(AtrBasic::new).label("Capacidade")
                        .tamanhoMaximo(15)
                        .as(AtrBootstrap::new).colPreference(4);
                return field;
            }

            private MTipoString createFieldUnidadeDeMedida() {
                //TODO caso o array tenha uma string vazia, ocorre um NPE
                MTipoString field = type.addCampoString("unidadeMedida", true);
                field.withSelectionOf(new String[]{"cm"}).withView(MSelecaoPorSelectView::new)
                        .as(AtrBasic::new).label("Unidade medida")
                        .as(AtrBootstrap::new).colPreference(1);
                return field;
            }

        }
    }
}

