package br.net.mirante.singular.exemplos.notificacaosimplificada.form.vocabulario;

import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.FormaFarmaceuticaBasica;
import br.net.mirante.singular.exemplos.notificacaosimplificada.service.DominioService;
import br.net.mirante.singular.form.SIComposite;
import br.net.mirante.singular.form.SInfoType;
import br.net.mirante.singular.form.STypeComposite;
import br.net.mirante.singular.form.TypeBuilder;
import br.net.mirante.singular.form.provider.STextQueryProvider;
import br.net.mirante.singular.form.type.core.STypeInteger;
import br.net.mirante.singular.form.type.core.STypeString;
import br.net.mirante.singular.form.view.SViewAutoComplete;

@SInfoType(spackage = SPackageVocabularioControlado.class)
public class STypeFormaFarmaceutica extends STypeComposite<SIComposite> {

    public STypeString descricao;
    public STypeInteger id;

    @Override
    protected void onLoadType(TypeBuilder tb) {
        super.onLoadType(tb);
        id = this.addFieldInteger("id");
        descricao = this.addFieldString("descricao");
        {

            this
                    .asAtr()
                    .required()
                    .label("Forma farmacêutica")
                    .asAtrBootstrap()
                    .colPreference(4);
            this.setView(SViewAutoComplete::new);

            this.autocomplete()
                    .id(id)
                    .display(descricao)
                    .filteredProvider((STextQueryProvider) (builder, query) -> {
                        builder
                                .getCurrentInstance()
                                .getDocument()
                                .lookupService(DominioService.class)
                                .buscarVocabulario(FormaFarmaceuticaBasica.class, query)
                                .forEach(vc -> builder.add().set(id, vc.getId()).set(descricao, vc.getDescricao()));
                    });

        }
    }


}