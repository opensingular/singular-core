/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.exemplos.notificacaosimplificada.form.vegetal;

import br.net.mirante.singular.exemplos.notificacaosimplificada.form.SPackageNotificacaoSimplificada;
import br.net.mirante.singular.exemplos.notificacaosimplificada.form.STypeFarmacopeiaReferencia;
import br.net.mirante.singular.form.mform.SIComposite;
import br.net.mirante.singular.form.mform.SIList;
import br.net.mirante.singular.form.mform.SInfoType;
import br.net.mirante.singular.form.mform.SType;
import br.net.mirante.singular.form.mform.STypeAttachmentList;
import br.net.mirante.singular.form.mform.STypeComposite;
import br.net.mirante.singular.form.mform.STypeSimple;
import br.net.mirante.singular.form.mform.TypeBuilder;
import br.net.mirante.singular.form.mform.basic.view.SViewTextArea;
import br.net.mirante.singular.form.mform.core.STypeInteger;
import br.net.mirante.singular.form.mform.core.STypeString;
import br.net.mirante.singular.form.mform.core.attachment.STypeAttachment;
import br.net.mirante.singular.form.mform.util.transformer.Value;

@SInfoType(spackage = SPackageNotificacaoSimplificada.class)
public class STypeEnsaioControleQualidade extends STypeComposite<SIComposite> {

    public STypeString descricaoTipoEnsaio;
    public STypeString descricaoTipoReferencia;
    public STypeInteger idTipoEnsaio;

    @Override
    protected void onLoadType(TypeBuilder tb) {
        super.onLoadType(tb);

        STypeComposite<SIComposite> tipoEnsaio = this.addFieldComposite("tipoEnsaio");
        idTipoEnsaio = tipoEnsaio.addFieldInteger("id");
        descricaoTipoEnsaio = tipoEnsaio.addFieldString("descricao");
        tipoEnsaio.withSelectView().withSelectionFromProvider(descricaoTipoEnsaio, (ins, filter) -> {
            final SIList<?> list = ins.getType().newList();
            for (TipoEnsaioControleQualidade tipo : TipoEnsaioControleQualidade.values()) {
                final SIComposite c = (SIComposite) list.addNew();
                c.setValue(idTipoEnsaio, tipo.getId());
                c.setValue(descricaoTipoEnsaio, tipo.getDescricao());
            }
            return list;
        });

        tipoEnsaio.asAtrBasic().label("Ensaio de Controle de Qualidade").enabled(false);

        STypeComposite<SIComposite> tipoReferencia = this.addFieldComposite("tipoReferencia");
        STypeInteger idTipoReferencia = tipoReferencia.addFieldInteger("id");
        descricaoTipoReferencia = tipoReferencia.addFieldString("descricao");
        tipoReferencia.withRadioView().withSelectionFromProvider(descricaoTipoReferencia, (ins, filter) -> {
            final SIList<?> list = ins.getType().newList();
            for (TipoReferencia tipo : TipoReferencia.values()) {
                final SIComposite c = (SIComposite) list.addNew();
                c.setValue(idTipoReferencia, tipo.getId());
                c.setValue(descricaoTipoReferencia, tipo.getDescricao());
            }
            return list;
        }).asAtrBasic().label("Tipo de referência")
                .required()
                .dependsOn(tipoEnsaio).visible(i -> !TipoEnsaioControleQualidade.RESULTADOS.getId().equals(Value.of(i, idTipoEnsaio)));

        {
            STypeComposite<SIComposite> informacoesFarmacopeicas = this.addFieldComposite("informacoesFarmacopeicas");
            informacoesFarmacopeicas
                    .asAtrBasic()
                    .dependsOn(tipoReferencia)
                    .visible(i -> TipoReferencia.FARMACOPEICO.getId().equals(Value.of(i, idTipoReferencia)));

            STypeFarmacopeiaReferencia farmacopeia = informacoesFarmacopeicas.addField("farmacopeia", STypeFarmacopeiaReferencia.class);

        }

        {
            STypeAttachmentList resultadosLote = this.addFieldListOfAttachment("resultadosLote", "resuladoLote");
            resultadosLote.asAtrBasic().label("Metodologia/Especificação/Resultado para um lote")
                    .dependsOn(tipoReferencia)
                    .visible(i -> TipoReferencia.NAO_FARMACOPEICO.getId().equals(Value.of(i, idTipoReferencia)));

            STypeAttachment f = resultadosLote.getElementsType();
            SType<?> nomeArquivo = (STypeSimple) f.getField(f.FIELD_NAME);
            nomeArquivo.asAtrBasic().label("Nome do Arquivo");
        }

        STypeString justificativa = this.addFieldString("justificativa");
        justificativa
                .asAtrBasic().dependsOn(tipoReferencia)
                .label("Justificativa").visible(i -> TipoReferencia.NAO_SE_APLICA.getId().equals(Value.of(i, idTipoReferencia)))
                .tamanhoMaximo(600)
                .getTipo().withView(SViewTextArea::new);

        this.addFieldListOfAttachment("resultadosControleQualidade", "resultado")
        .asAtrBasic()
        .label("Resultados do controle da qualidade")
        .visible(i -> TipoEnsaioControleQualidade.RESULTADOS.getId().equals(Value.of(i, idTipoEnsaio)))
        .required();

    }

    enum TipoEnsaioControleQualidade {
        PROSPECCAO_FITOQUIMICA_CCD(1, "Perfil cromatográfico"),
        LAUDO_BOTANICO(2, "Teor"),
        RESULTADOS(3, "Resultados do controle da qualidade");

        private Integer id;
        private String descricao;

        TipoEnsaioControleQualidade(Integer id, String descricao) {
            this.id = id;
            this.descricao = descricao;
        }

        public Integer getId() {
            return id;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    enum TipoReferencia {
        FARMACOPEICO(1, "Farmacopêico"),
        NAO_FARMACOPEICO(2, "Não farmacopêico"),
        NAO_SE_APLICA(3, "Não se aplica");

        private Integer id;
        private String descricao;

        TipoReferencia(Integer id, String descricao) {
            this.id = id;
            this.descricao = descricao;
        }

        public Integer getId() {
            return id;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}