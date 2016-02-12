package br.net.mirante.singular.showcase.component.layout;

import br.net.mirante.singular.form.mform.SIComposite;
import br.net.mirante.singular.form.mform.SPackage;
import br.net.mirante.singular.form.mform.STypeComposite;
import br.net.mirante.singular.form.mform.STypeLista;
import br.net.mirante.singular.form.mform.PacoteBuilder;
import br.net.mirante.singular.form.mform.basic.ui.AtrBasic;
import br.net.mirante.singular.form.mform.basic.ui.AtrBootstrap;
import br.net.mirante.singular.form.mform.basic.view.MListMasterDetailView;
import br.net.mirante.singular.form.mform.core.STypeInteger;
import br.net.mirante.singular.form.mform.core.STypeString;
import br.net.mirante.singular.form.mform.util.comuns.STypeAnoMes;

public class CaseMasterDetailNestedPackage extends SPackage {

    @Override
    protected void carregarDefinicoes(PacoteBuilder pb) {

        STypeComposite<?> testForm = pb.createTipoComposto("testForm");

        final STypeLista<STypeComposite<SIComposite>, SIComposite> experiencias = testForm.addCampoListaOfComposto("experienciasProfissionais", "experiencia");
        final STypeComposite<?> experiencia = experiencias.getTipoElementos();
        final STypeAnoMes dtInicioExperiencia = experiencia.addCampo("inicio", STypeAnoMes.class, true);
        final STypeAnoMes dtFimExperiencia = experiencia.addCampo("fim", STypeAnoMes.class);
        final STypeString empresa = experiencia.addCampoString("empresa", true);
        final STypeString atividades = experiencia.addCampoString("atividades");

        final STypeLista<STypeComposite<SIComposite>, SIComposite> cargos = experiencia.addCampoListaOfComposto("cargos", "cargo");
        final STypeComposite<?> cargo = cargos.getTipoElementos();
        final STypeString nome = cargo.addCampoString("nome", true);
        final STypeAnoMes dtInicioCargo = cargo.addCampo("inicio", STypeAnoMes.class, true);
        final STypeAnoMes dtFimCargo = cargo.addCampo("fim", STypeAnoMes.class);


        final STypeLista<STypeComposite<SIComposite>, SIComposite> pets = cargo.addCampoListaOfComposto("pets", "pet");
        final STypeComposite pet = pets.getTipoElementos();
        final STypeString nomeDoPet = pet.addCampoString("nome", true);
        final STypeString tipoDoPet = pet.addCampoString("tipo", true)
                .withSelectionOf("Gatinho", "Cachorrinho", "Papagaio");
        final STypeInteger idadePet = pet.addCampoInteger("idade");

        {
            //@destacar:bloco
            experiencias
                    .withView(MListMasterDetailView::new)
                    .as(AtrBasic::new).label("Experiências profissionais");
            //@destacar:fim
            dtInicioExperiencia
                    .as(AtrBasic::new).label("Data inicial")
                    .as(AtrBootstrap::new).colPreference(2);
            dtFimExperiencia
                    .as(AtrBasic::new).label("Data final")
                    .as(AtrBootstrap::new).colPreference(2);
            empresa
                    .as(AtrBasic::new).label("Empresa")
                    .as(AtrBootstrap::new).colPreference(8);
            //@destacar:bloco
            cargos
                    .withView(MListMasterDetailView::new)
                    .as(AtrBasic::new).label("Cargos na empresa");
            dtInicioCargo
                    .as(AtrBasic::new).label("Data inicial")
                    .as(AtrBootstrap::new).colPreference(4);
            dtFimCargo
                    .as(AtrBasic::new).label("Data final")
                    .as(AtrBootstrap::new).colPreference(4);
            nome
                    .as(AtrBasic::new).label("Nome")
                    .as(AtrBootstrap::new).colPreference(4);
            pets
                    .withView(new MListMasterDetailView()
                            .col(nomeDoPet)
                            .col(tipoDoPet))
                    .as(AtrBasic::new).label("Animais de estimação no trabalho");
            nomeDoPet
                    .as(AtrBasic::new).label("Nome")
                    .as(AtrBootstrap::new).colPreference(4);
            tipoDoPet
                    .withSelectView()
                    .as(AtrBasic::new).label("Tipo")
                    .as(AtrBootstrap::new).colPreference(4);
            idadePet
                    .as(AtrBasic::new).label("Idade")
                    .as(AtrBootstrap::new).colPreference(4);
            //@destacar:fim
            atividades
                    .withTextAreaView()
                    .as(AtrBasic::new).label("Atividades Desenvolvidas");
        }

    }
}