package br.net.mirante.singular.dsl.exemplo2;

import br.net.mirante.singular.dsl.Builder;
import br.net.mirante.singular.dsl.exemplo2.dsl.Builder2;
import br.net.mirante.singular.flow.core.FlowMap;
import br.net.mirante.singular.flow.core.MTask;
import br.net.mirante.singular.flow.core.MTaskJava;
import br.net.mirante.singular.flow.core.MTaskPeople;
import br.net.mirante.singular.flow.core.ProcessInstance;

public class Exemplo2 {

    public FlowMap getFlowMap() {
        // @formatter:off
        return new Builder2()
                .javaTask("ENVIAR_EMAIL")
                    .config(this::configEmail)
                    .transition()
                .peopleTask("APROVAR_PARECER")
                    .config(this::configAprovar)
                    .transitionTo("AGUARDAR_PAGAMENTO", "aprovado")
                    .transitionTo("REVISAR", "rejeitado")
                .waitTask("AGUARDAR_PAGAMENTO")
                    .config(this::configAguardarPagamento)
                    .transition()
                .endTask("FIM")
                .peopleTask("REVISAR")
                    .config(this::configRevisar)
                    .transitionTo("EMAIL", "reconsiderar")
                    .transition("aprovar.parecer")
                .build();
        // @formatter:on
    }

    private void configAguardarPagamento(MTask mTask) {

    }

    private void configEmail(MTask java) {
        //faz o que quiser
    }


    private void configAprovar(MTask java) {
        //faz o que quiser
    }

    private void configRevisar(MTask java) {
        //faz o que quiser
    }


}