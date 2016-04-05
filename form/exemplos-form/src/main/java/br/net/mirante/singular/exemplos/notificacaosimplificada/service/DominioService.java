package br.net.mirante.singular.exemplos.notificacaosimplificada.service;

import br.net.mirante.singular.exemplos.notificacaosimplificada.dao.EnderecoEmpresaInternacionalDAO;
import br.net.mirante.singular.exemplos.notificacaosimplificada.dao.GenericDAO;
import br.net.mirante.singular.exemplos.notificacaosimplificada.dao.VocabularioControladoDAO;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.CategoriaRegulatoriaMedicamento;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.EmbalagemPrimariaBasica;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.EmbalagemSecundaria;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.EtapaFabricacao;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.Farmacopeia;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.FormaFarmaceuticaBasica;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.LinhaCbpf;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.Substancia;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.UnidadeMedida;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.corporativo.PessoaJuridicaNS;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.geral.EnderecoEmpresaInternacional;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DominioService {

    @Inject
    private VocabularioControladoDAO vocabularioControladoDAO;

    @Inject
    private GenericDAO genericDAO;

    @Inject
    private EnderecoEmpresaInternacionalDAO enderecoEmpresaInternacionalDAO;

    public List<LinhaCbpf> linhasProducao(String filtro) {
        return vocabularioControladoDAO.findByDescricao(LinhaCbpf.class, filtro);
    }

    public List<Triple> configuracoesLinhaProducaoDinamizado(Integer id) {
        List<Triple> list = new ArrayList<>();
        list.add(Triple.of(1, 1, "Comprimidos em Camadas"));
        list.add(Triple.of(2, 1, "Comprimidos Placebo"));
        list.add(Triple.of(3, 1, "Comprimidos Simples"));

        list.add(Triple.of(4, 2, "Drágeas Simples"));
        list.add(Triple.of(5, 2, "Drágeas Saborosas"));
        list.add(Triple.of(6, 2, "Drágeas Coloridas"));

        list.add(Triple.of(7, 3, "Cápsula Rídiga"));
        list.add(Triple.of(8, 3, "Cápsula Gelatinosa"));
        list.add(Triple.of(9, 3, "Cápsula de Amido"));
        return list;
    }


    public List<Triple> configuracoesLinhaProducao(Integer idLinhaProducao) {
        List<Triple> list = new ArrayList<>();

        if (idLinhaProducao == null) {
            return list;
        }

        list.add(Triple.of(1, 1, "Comprimidos em Camadas"));
        list.add(Triple.of(2, 1, "Comprimidos Placebo"));
        list.add(Triple.of(3, 1, "Comprimidos Simples"));

        list.add(Triple.of(4, 2, "Drágeas Simples"));
        list.add(Triple.of(5, 2, "Drágeas Saborosas"));
        list.add(Triple.of(6, 2, "Drágeas Coloridas"));

        list.add(Triple.of(7, 3, "Cápsula Rídiga"));
        list.add(Triple.of(8, 3, "Cápsula Gelatinosa"));
        list.add(Triple.of(9, 3, "Cápsula de Amido"));

        return list.stream().filter(t -> t.getMiddle().equals(idLinhaProducao % 3 + 1)).collect(Collectors.toList());
    }


    public List<Substancia> substancias(Integer idConfiguracaoLinhaProducao, String filter) {
        return vocabularioControladoDAO.findByDescricao(Substancia.class, filter);
    }


    public List<Triple> descricoesHomeopaticas(Integer idConfiguracaoLinhaProducao) {
        List<Triple> list = new ArrayList<>();
        list.add(Triple.of(1, 1, "Echinacea angustifolia"));
        list.add(Triple.of(2, 1, "Malva officinalis"));
        list.add(Triple.of(3, 1, "Calendula officinalis"));

        list.add(Triple.of(4, 2, "Gelsemium sempervivum"));
        list.add(Triple.of(5, 2, "Belladonna atropa"));
        list.add(Triple.of(6, 2, "Aconitum napellus"));

        list.add(Triple.of(7, 3, "Eucalyptus glóbulos"));
        list.add(Triple.of(8, 3, "Aconitum napellus"));
        list.add(Triple.of(9, 3, "Gelsemium sempervivum"));

        list.add(Triple.of(10, 4, "Bryonia alba"));
        list.add(Triple.of(11, 4, "Agnus castus"));
        list.add(Triple.of(12, 4, "Juniperus brasiliensis"));

        list.add(Triple.of(13, 5, "Paullinea sorbilis"));
        list.add(Triple.of(14, 5, "Sterculia acuminata"));
        list.add(Triple.of(15, 5, "Polygonum punctatum"));

        list.add(Triple.of(16, 6, "Hamamelis virginianum"));
        list.add(Triple.of(17, 6, "Etinilestradiol"));
        list.add(Triple.of(18, 6, "Ciproterona"));

        list.add(Triple.of(19, 7, "Chelidonium majus"));
        list.add(Triple.of(20, 7, "Lobelia inflata"));
        list.add(Triple.of(21, 7, "Grindelia robusta"));

        list.add(Triple.of(22, 8, "Castane vesca"));
        list.add(Triple.of(23, 8, "Antimonium tartaricum"));
        list.add(Triple.of(24, 8, "Drosera rotundifolia"));

        list.add(Triple.of(25, 9, "Crataegus oxycanta "));
        list.add(Triple.of(26, 9, "Cactus grandiflorus"));
        list.add(Triple.of(27, 9, "Avena sativa"));

        if (idConfiguracaoLinhaProducao == null) {
            return Collections.emptyList();
        }

        return list.stream().filter(t -> t.getMiddle().equals(idConfiguracaoLinhaProducao % 9 + 1)).collect(Collectors.toList());
    }

    public List<FormaFarmaceuticaBasica> formasFarmaceuticas(String filtro) {
        return vocabularioControladoDAO.findByDescricao(FormaFarmaceuticaBasica.class, filtro);
    }

    public List<FormaFarmaceuticaBasica> formasFarmaceuticasDinamizadas(List<Integer> configuracoesDinamizado, String filtro) {
        return vocabularioControladoDAO.formasFarmaceuticasDinamizadas(configuracoesDinamizado, filtro);
    }

    public List<Triple> concentracoes(Integer idSubstancia) {

        List<Triple> list = new ArrayList<>();

        if (idSubstancia == null) {
            return list;
        }

        Integer idSubstanciaFake = idSubstancia % 9 + 1;


        list.add(Triple.of(1, 1, "10 mg"));
        list.add(Triple.of(2, 1, "15 mg"));
        list.add(Triple.of(3, 1, "20 mg"));

        list.add(Triple.of(4, 2, "25 mg"));
        list.add(Triple.of(5, 2, "35 mg"));
        list.add(Triple.of(6, 2, "40 mg"));

        list.add(Triple.of(7, 3, "50 mg"));
        list.add(Triple.of(8, 3, "60 mg"));
        list.add(Triple.of(9, 3, "70 mg"));

        list.add(Triple.of(10, 4, "80 mg"));
        list.add(Triple.of(11, 4, "90 mg"));
        list.add(Triple.of(12, 4, "100 mg"));

        list.add(Triple.of(13, 5, "1 g"));
        list.add(Triple.of(14, 5, "2 g"));
        list.add(Triple.of(15, 5, "4 kg"));

        list.add(Triple.of(16, 6, "30 mg + 40 mg"));
        list.add(Triple.of(17, 6, "22 mg"));
        list.add(Triple.of(18, 6, "7 mg"));

        list.add(Triple.of(19, 7, "1 mg"));
        list.add(Triple.of(20, 7, "2 mg"));
        list.add(Triple.of(21, 7, "3 mg"));

        list.add(Triple.of(22, 8, "20 mg + 15 mg"));
        list.add(Triple.of(23, 8, "3 mg"));
        list.add(Triple.of(24, 8, "15 mg + 235 mg"));

        list.add(Triple.of(25, 9, "200 mg"));
        list.add(Triple.of(26, 9, "7 mg"));
        list.add(Triple.of(27, 9, "75 mg"));

        return list.stream().filter(t -> t.getMiddle().equals(idSubstanciaFake)).collect(Collectors.toList());
    }


    public List<Triple> diluicoes(Integer idDescricaoDinamizada) {
        List<Triple> list = new ArrayList<>();

        if (idDescricaoDinamizada == null) {
            return list;
        }

        Integer idDescricaoDinamizadaFake = idDescricaoDinamizada % 9 + 1;


        list.add(Triple.of(1, 1, "15 DH 0,008 ml"));
        list.add(Triple.of(2, 1, "5 CH"));
        list.add(Triple.of(3, 1, "30 CH"));

        list.add(Triple.of(4, 2, "15 DH 0,009 ml"));
        list.add(Triple.of(5, 2, "15 DH 0,00098 ml"));
        list.add(Triple.of(6, 2, "55 CH"));

        list.add(Triple.of(7, 3, "15D 0,008 ml"));
        list.add(Triple.of(8, 3, "1 LM 0,02 ml"));
        list.add(Triple.of(9, 3, "129 LM 0,02 ml"));

        list.add(Triple.of(10, 4, "75 DH 0,02 ml"));
        list.add(Triple.of(11, 4, "5 DH 0,031 ml"));
        list.add(Triple.of(12, 4, "9 LM 0,073 ml"));

        list.add(Triple.of(13, 5, "5 DH 0,0221 ml"));
        list.add(Triple.of(14, 5, "19 CH 1L"));
        list.add(Triple.of(15, 5, "31 CH 500 ml"));

        list.add(Triple.of(16, 6, "31 CH 0,456 ml"));
        list.add(Triple.of(17, 6, "31 CH 0,56 ml"));
        list.add(Triple.of(18, 6, "31 CH 0,123 ml"));

        list.add(Triple.of(19, 7, "11 CH 0,03 ml"));
        list.add(Triple.of(20, 7, "121 CH 0,0000001 ml"));
        list.add(Triple.of(21, 7, "2 CH 0,005 ml"));

        list.add(Triple.of(22, 8, "35 DH 0,025 ml"));
        list.add(Triple.of(23, 8, "45 DH 0,034 ml"));
        list.add(Triple.of(24, 8, "5 DH 0,012 ml"));

        list.add(Triple.of(25, 9, "65 LM 0,302 ml"));
        list.add(Triple.of(26, 9, "75 CH 0,042 ml"));
        list.add(Triple.of(27, 9, "85 DH 0,072 ml"));

        return list.stream().filter(t -> t.getMiddle().equals(idDescricaoDinamizadaFake)).collect(Collectors.toList());
    }

    public List<EmbalagemPrimariaBasica> findEmbalagensBasicas(String filtro) {
        return vocabularioControladoDAO.findByDescricao(EmbalagemPrimariaBasica.class, filtro);
    }

    public List<EmbalagemSecundaria> embalagensSecundarias(String filtro) {
        return vocabularioControladoDAO.findByDescricao(EmbalagemSecundaria.class, filtro);
    }

    public List<UnidadeMedida> unidadesMedida(String filtro) {
        return vocabularioControladoDAO.findUnidadeMedida(filtro);
    }

    public List<EnderecoEmpresaInternacional> empresaInternacional(String filtro) {
        return enderecoEmpresaInternacionalDAO.buscarEnderecos(filtro, 5);
    }

    public List<PessoaJuridicaNS> empresaTerceirizada(String filtro) {
        return genericDAO.findByProperty(PessoaJuridicaNS.class, "razaoSocial", filtro, 5);
    }

    public List<PessoaJuridicaNS> outroLocalFabricacao(String filtro) {
        return genericDAO.findByProperty(PessoaJuridicaNS.class, "razaoSocial", filtro, 5);
    }

    public List<EtapaFabricacao> etapaFabricacao(String filtro) {
        return vocabularioControladoDAO.findByDescricao(EtapaFabricacao.class, filtro);
    }

    public List<CategoriaRegulatoriaMedicamento> listCategoriasRegulatorias() {
        return vocabularioControladoDAO.listAll(CategoriaRegulatoriaMedicamento.class);
    }

    public List<CategoriaRegulatoriaMedicamento> listCategoriasRegulatoriasMedicamentoDinamizado(String filtro) {
        return vocabularioControladoDAO.listCategoriasRegulatoriasMedicamentoDinamizado(filtro);
    }

    public List<Pair> nomenclaturaBotanica(String filtro) {
        List<Pair> list = new ArrayList<>();

        list.add(Pair.of(1L, "Planta1 + Planta2"));
        list.add(Pair.of(2L, "Planta2 + Planta3"));
        list.add(Pair.of(3L, "Planta4 + Planta5"));
        list.add(Pair.of(4L, "Planta6 + Planta7"));

        return list;
    }

    public List<Pair> concentracao(String filtro) {
        List<Pair> list = new ArrayList<>();

        list.add(Pair.of(1L, "30mg + 90mg"));
        list.add(Pair.of(2L, "50mg + 60mg"));
        list.add(Pair.of(3L, "20mg + 10mg"));
        list.add(Pair.of(4L, "5mg + 12mg"));

        return list;
    }

    public List<Farmacopeia> listFarmacopeias() {
        return vocabularioControladoDAO.listAll(Farmacopeia.class);
    }

    public List<LinhaCbpf> listarLinhasProducaoDinamizado(String filtro) {
        return vocabularioControladoDAO.listarLinhasProducaoDinamizado(filtro);
    }
}