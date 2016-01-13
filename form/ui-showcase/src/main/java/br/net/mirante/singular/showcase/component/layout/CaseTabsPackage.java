package br.net.mirante.singular.showcase.component.layout;

import br.net.mirante.singular.form.mform.MIComposto;
import br.net.mirante.singular.form.mform.MPacote;
import br.net.mirante.singular.form.mform.MTipoComposto;
import br.net.mirante.singular.form.mform.MTipoLista;
import br.net.mirante.singular.form.mform.PacoteBuilder;
import br.net.mirante.singular.form.mform.basic.ui.AtrBasic;
import br.net.mirante.singular.form.mform.basic.view.MListMasterDetailView;
import br.net.mirante.singular.form.mform.basic.view.MTabView;
import br.net.mirante.singular.form.mform.core.MTipoInteger;
import br.net.mirante.singular.form.mform.core.MTipoString;
import br.net.mirante.singular.form.mform.util.comuns.MTipoAnoMes;
import br.net.mirante.singular.form.mform.util.comuns.MTipoEMail;
import br.net.mirante.singular.form.wicket.AtrBootstrap;

public class CaseTabsPackage extends MPacote {

    @Override
    protected void carregarDefinicoes(PacoteBuilder pb) {
        MTipoComposto<?> testForm = pb.createTipoComposto("testForm");

        MTipoString nome;
        MTipoInteger idade;
        MTipoEMail email;
        (nome = testForm.addCampoString("nome"))
                .as(AtrBasic.class).label("Nome");
        (idade = testForm.addCampoInteger("idade"))
                .as(AtrBasic.class).label("Idade");
        (email = testForm.addCampoEmail("email"))
                .as(AtrBasic.class).label("E-mail");

        final MTipoLista<MTipoComposto<MIComposto>, MIComposto> experiencias = testForm.addCampoListaOfComposto("experienciasProfissionais", "experiencia");
        final MTipoComposto<?> experiencia = experiencias.getTipoElementos();
        final MTipoAnoMes dtInicioExperiencia = experiencia.addCampo("inicio", MTipoAnoMes.class, true);
        final MTipoAnoMes dtFimExperiencia = experiencia.addCampo("fim", MTipoAnoMes.class);
        final MTipoString empresa = experiencia.addCampoString("empresa", true);
        final MTipoString cargo = experiencia.addCampoString("cargo", true);
        final MTipoString atividades = experiencia.addCampoString("atividades");

        {
            experiencias.withView(MListMasterDetailView::new)
                    .as(AtrBasic::new).label("Experiências profissionais");
            dtInicioExperiencia
                    .as(AtrBasic::new).label("Data inicial")
                    .as(AtrBootstrap::new).colPreference(2);
            dtFimExperiencia
                    .as(AtrBasic::new).label("Data final")
                    .as(AtrBootstrap::new).colPreference(2);
            empresa
                    .as(AtrBasic::new).label("Empresa")
                    .as(AtrBootstrap::new).colPreference(8);
            cargo
                    .as(AtrBasic::new).label("Cargo");
            atividades
                    .withTextAreaView()
                    .as(AtrBasic::new).label("Atividades Desenvolvidas");
        }

        //@destacar:bloco
        MTabView tabbed = new MTabView();
        tabbed.addTab("informacoes", "Informações pessoais")
                .add(nome)
                .add(email)
                .add(idade);
        tabbed.addTab(experiencias);
        testForm.withView(tabbed);
        //@destacar:fim
    }
}
