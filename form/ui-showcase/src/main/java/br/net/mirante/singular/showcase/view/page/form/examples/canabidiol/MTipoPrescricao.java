package br.net.mirante.singular.showcase.view.page.form.examples.canabidiol;

import br.net.mirante.singular.form.mform.MIComposto;
import br.net.mirante.singular.form.mform.MInfoTipo;
import br.net.mirante.singular.form.mform.MTipoComposto;
import br.net.mirante.singular.form.mform.TipoBuilder;
import br.net.mirante.singular.form.mform.basic.ui.AtrBasic;
import br.net.mirante.singular.form.mform.core.MTipoString;
import br.net.mirante.singular.form.wicket.AtrBootstrap;

@MInfoTipo(nome = "MTipoPrescricao", pacote = MPacotePeticaoCanabidiol.class)
public class MTipoPrescricao extends MTipoComposto<MIComposto> implements CanabidiolUtil {


    @Override
    protected void onCargaTipo(TipoBuilder tb) {
        super.onCargaTipo(tb);

        MTipoMedico medico = this.addCampo("medico", MTipoMedico.class);

    }


}
