package br.net.mirante.singular.exemplos.ggtox.primariasimplificada.common;

import static br.net.mirante.singular.form.util.SingularPredicates.*;

import br.net.mirante.singular.exemplos.SelectBuilder;
import br.net.mirante.singular.exemplos.ggtox.primariasimplificada.form.SPackagePPSCommon;
import br.net.mirante.singular.form.SIComposite;
import br.net.mirante.singular.form.SInfoType;
import br.net.mirante.singular.form.STypeComposite;
import br.net.mirante.singular.form.STypeList;
import br.net.mirante.singular.form.STypeSimple;
import br.net.mirante.singular.form.TypeBuilder;
import br.net.mirante.singular.form.type.core.STypeBoolean;
import br.net.mirante.singular.form.type.core.STypeDecimal;
import br.net.mirante.singular.form.type.core.STypeInteger;
import br.net.mirante.singular.form.type.core.STypeString;
import br.net.mirante.singular.form.type.core.attachment.STypeAttachment;
import br.net.mirante.singular.form.util.transformer.Value;
import br.net.mirante.singular.form.view.SViewListByMasterDetail;
import br.net.mirante.singular.form.view.SViewListByTable;

import java.util.Optional;


@SInfoType(spackage = SPackagePPSCommon.class)
public class STypeEstudosResiduos extends STypeComposite<SIComposite> {

    private EstudoResiduo estudoResiduo;
    private STypeBoolean culturaConformeMatriz;

    @Override
    protected void onLoadType(TypeBuilder tb) {
        super.onLoadType(tb);

        this.asAtr()
                .label("Estudo de Resíduos");

        estudoResiduo = new EstudoResiduo(this);

        estudoResiduo.
                amostra
                .root
                .asAtr().dependsOn(estudoResiduo.tipoEstudo)
                .exists(typeValIsEqualsTo(estudoResiduo.tipoEstudo, EstudoResiduo.ESTUDO_NOVO));
    }

    class EstudoResiduo {

        public static final String ESTUDO_PUBLICADO = "Publicado pela ANVISA";
        public static final String ESTUDO_MATRIZ = "Conforme matriz";
        public static final String ESTUDO_NOVO = "Novo";
        public static final String NOME_CULTURA_FIELD_NAME = "nomeCultura";
        public static final String NOME_OUTRA_CULTURA_FIELD_NAME = "nomeOutraCultura";

        private final STypeList<STypeComposite<SIComposite>, SIComposite> root;
        private final STypeComposite<SIComposite> rootType;
        final STypeString tipoEstudo;
        public final Amostra amostra;

        public EstudoResiduo(STypeComposite<SIComposite> parentType) {
            root = parentType.addFieldListOfComposite("culturas", "cultura");
            rootType = root.getElementsType();

            final STypeString nomeCultura = rootType.addFieldString(NOME_CULTURA_FIELD_NAME);
            final STypeString nomeOutraCultura = rootType.addFieldString(NOME_OUTRA_CULTURA_FIELD_NAME);
            final STypeString emprego = rootType.addFieldString("emprego");
            final STypeBoolean outraCultura = rootType.addFieldBoolean("outraCultura");
            final STypeBoolean parteComestivel = rootType.addFieldBoolean("parteComestivel");
            final STypeInteger intervaloPretendido = rootType.addFieldInteger("intervaloPretendido");
            final STypeComposite<SIComposite> norma = rootType.addFieldComposite("norma");
            final STypeInteger idNorma = norma.addFieldInteger("idNorma");
            final STypeString descricaoNorma = norma.addFieldString("descricaoNorma");
            final STypeString observacoes = rootType.addFieldString("observacoes");

            tipoEstudo = rootType.addFieldString("tipoEstudo");
            final STypeString estudoPublicado = rootType.addFieldString("estudoPublicado");
            final STypeString numeroEstudo = rootType.addFieldString("numeroEstudo");
            final STypeComposite<SIComposite> dosagemAmostra = rootType.addFieldComposite("dosagemAmostra");
            final STypeInteger idDosagem = dosagemAmostra.addFieldInteger("idDosagem");
            final STypeString siglaDosagem = dosagemAmostra.addFieldString("siglaDosagem");
            final STypeBoolean adjuvante = rootType.addFieldBoolean("adjuvante");
            amostra = new Amostra(rootType);
            final STypeAttachment estudoResiduo = rootType.addFieldAttachment("estudoResiduo");

            root
                    .withView(new SViewListByMasterDetail()
                            .col("Cultura", si -> (String)Optional.ofNullable(Value.of(si, NOME_CULTURA_FIELD_NAME)).orElse(Value.of(si, NOME_OUTRA_CULTURA_FIELD_NAME)))
                            .col(emprego)
                            .col(tipoEstudo)
                            .largeSize()
                    )
                    .asAtr().exists(typeValIsNotEqualsTo(culturaConformeMatriz, Boolean.TRUE));

            nomeCultura
                    .selectionOf(culturas())
                    .asAtr().label("Nome da Cultura")
                    .required()
                    .dependsOn(outraCultura, tipoEstudo)
                    .exists(allMatches(typeValIsNotEqualsTo(outraCultura, Boolean.TRUE)));

            nomeOutraCultura
                    .asAtr().label("Nome da Cultura")
                    .required()
                    .dependsOn(outraCultura)
                    .exists(typeValIsTrue(outraCultura));

            emprego
                    .selectionOf(empregos())
                    .asAtr().label("Emprego");

            outraCultura
                    .asAtr().label("Outra cultura")
                    .asAtrBootstrap()
                    .colPreference(6);

            parteComestivel
                    .asAtr()
                    .label("Parte Comestível?")
                    .asAtrBootstrap()
                    .colPreference(6);

            intervaloPretendido
                    .asAtr()
                    .label("Intervalo de Segurança Pretendido (em dias)")
                    .asAtrBootstrap()
                    .colPreference(6);

            norma
                    .asAtr()
                    .label("Norma")
                    .asAtrBootstrap()
                    .colPreference(4);

            norma
                    .selection()
                    .id(idNorma)
                    .display(descricaoNorma)
                    .simpleProvider( builder -> {
                        builder.add().set(idNorma, 1).set(descricaoNorma, "RDC - 216 - 4 Estudos");
                        builder.add().set(idNorma, 2).set(descricaoNorma, "3 Estudos (X e 2X)");
                        builder.add().set(idNorma, 3).set(descricaoNorma, "2 Estudos (X e 2X)");
                        builder.add().set(idNorma, 4).set(descricaoNorma, "Sem informações");
                        builder.add().set(idNorma, 5).set(descricaoNorma, "Produto de ocorrência natural na planta");
                    });

            observacoes
                    .asAtr()
                    .label("Observações")
                    .asAtrBootstrap()
                    .colPreference(12);

            observacoes
                    .withTextAreaView();



            tipoEstudo
                    .withRadioView()
                    .selectionOf(ESTUDO_MATRIZ, ESTUDO_PUBLICADO, ESTUDO_NOVO)
                    .asAtr()
                    .label("Estudo")
                    .asAtrBootstrap()
                    .newRow();

            estudoPublicado
                    .asAtr().label("Código do Estudo Publicado pela ANVISA")
                    .dependsOn(tipoEstudo)
                    .exists(typeValIsEqualsTo(tipoEstudo, ESTUDO_PUBLICADO))
                    .asAtrBootstrap().newRow();


            numeroEstudo
                    .asAtr().label("Número do Estudo")
                    .dependsOn(tipoEstudo)
                    .exists(typeValIsEqualsTo(tipoEstudo, ESTUDO_NOVO))
                    .asAtrBootstrap()
                    .colPreference(4);

            dosagemAmostra
                    .asAtr()
                    .dependsOn(tipoEstudo)
                    .exists(typeValIsEqualsTo(tipoEstudo, ESTUDO_NOVO))
                    .label("Dosagem das Amostras")
                    .asAtrBootstrap()
                    .colPreference(4);

            dosagemAmostra
                    .selection()
                    .id(idDosagem)
                    .display(siglaDosagem)
                    .simpleProvider( builder ->{
                        builder.add().set(idDosagem, 1).set(siglaDosagem, "g/hectare");
                        builder.add().set(idDosagem, 2).set(siglaDosagem, "g/m3");
                    });

            adjuvante
                    .withSelectView()
                    .selectionOf(Boolean.class)
                    .selfId()
                    .display( bool -> bool ? "Sim" : "Não")
                    .simpleConverter();

            adjuvante
                    .asAtr().label("Adjuvante")
                    .required()
                    .dependsOn(tipoEstudo)
                    .exists(typeValIsEqualsTo(tipoEstudo, ESTUDO_NOVO))
                    .asAtrBootstrap()
                    .colPreference(4);


            estudoResiduo
                    .asAtr().label("Estudo de Resíduo")
                    .dependsOn(tipoEstudo)
                    .exists(typeValIsEqualsTo(tipoEstudo, ESTUDO_NOVO));
        }

    }

    class Amostra {

        private final STypeList<STypeComposite<SIComposite>, SIComposite> root;
        private final STypeComposite<SIComposite> rootType;

        public Amostra(STypeComposite<SIComposite> parentType) {
            root = parentType.addFieldListOfComposite("amostras", "amostra");
            rootType = root.getElementsType();
            final STypeString id = rootType.addFieldString("id");
            final STypeDecimal dose = rootType.addFieldDecimal("dose");
            final STypeInteger aplicacoes = rootType.addFieldInteger("aplicacoes");
            final STypeInteger dat = rootType.addFieldInteger("dat");
            final STypeDecimal loq = rootType.addFieldDecimal("loq");
            final STypeDecimal residuo = rootType.addFieldDecimal("residuo");
            STypeAtivoAmostra ativo = rootType.addField("ativos", STypeAtivoAmostra.class);
            final STypeComposite<?> estado = rootType.addFieldComposite("estado");
            final STypeString siglaUF = estado.addFieldString("sigla");
            estado.addFieldString("nome");
            final STypeComposite<?> cidade = rootType.addFieldComposite("cidade");
            cidade.addFieldInteger("id");
            cidade.addFieldString("nome");
            final STypeString uf = cidade.addFieldString("UF");
            final STypeBoolean tempoMaior30Dias = rootType.addFieldBoolean("tempoMaior30Dias");
            final STypeAttachment estudoEstabilidade = rootType.addFieldAttachment("estudoEstabilidade");
            final STypeBoolean metabolito = rootType.addFieldBoolean("metabolito");
            final STypeList<STypeComposite<SIComposite>, SIComposite> metabolitos = rootType.addFieldListOfComposite("metabolitos", "metabolito");
            final STypeString descricaoMetabolito = metabolitos.getElementsType().addFieldString("descricao");
            final STypeDecimal loqMetabolito = metabolitos.getElementsType().addFieldDecimal("loqMetabolito");
            final STypeDecimal residuoMetabolito = metabolitos.getElementsType().addFieldDecimal("residuoMetabolito");

            root
                    .withView(new SViewListByMasterDetail()
                            .col(id, "Id")
                            .col(dose, "Dose")
                            .col(aplicacoes, "Aplicações")
                            .col(ativo.nomeComumPortugues, "Ingrediente Ativo")
                            .col(residuo, "Residuo")
                            .col(dat, "DAT")
                    )
                    .asAtr().label("Amostras");

            id
                    .asAtr()
                    .label("ID da Amostra")
                    .required()
                    .asAtrBootstrap()
                    .colPreference(4);


            dose
                    .asAtrBootstrap()
                    .colPreference(4)
                    .asAtr()
                    .label("Dose")
                    .required();

            aplicacoes
                    .asAtrBootstrap()
                    .colPreference(4)
                    .asAtr()
                    .label("Número de Aplicações")
                    .required();



            dat
                    .asAtr()
                    .label("DAT")
                    .asAtrBootstrap()
                    .colPreference(4);


            loq
                    .asAtr().label("LoQ (mg/KG)")
                    .fractionalMaxLength(4);

            residuo
                    .asAtr().label("Resíduo")
                    .fractionalMaxLength(4);

            ativo
                    .asAtr()
                    .required()
                    .label("Ingrediente Ativo da Amostra (informados na seção de ativos)")
                    .asAtrBootstrap()
                    .colPreference(6);


            estado
                    .asAtr()
                    .required()
                    .asAtr()
                    .label("Estado")
                    .asAtrBootstrap()
                    .colPreference(3)
                    .newRow();

            estado.selectionOf(SelectBuilder.EstadoDTO.class)
                    .id(SelectBuilder.EstadoDTO::getSigla)
                    .display("${nome} - ${sigla}")
                    .autoConverterOf(SelectBuilder.EstadoDTO.class)
                    .simpleProvider(ins ->  SelectBuilder.buildEstados());

            cidade
                    .asAtr()
                    .required(inst -> Value.notNull(inst, (STypeSimple) estado.getField(siglaUF)))
                    .asAtr()
                    .label("Cidade")
                    .enabled(inst -> Value.notNull(inst, (STypeSimple) estado.getField(siglaUF)))
                    .dependsOn(estado)
                    .asAtrBootstrap()
                    .colPreference(3);

            cidade.selectionOf(SelectBuilder.CidadeDTO.class)
                    .id(SelectBuilder.CidadeDTO::getId)
                    .display(SelectBuilder.CidadeDTO::getNome)
                    .autoConverterOf(SelectBuilder.CidadeDTO.class)
                    .simpleProvider(i -> SelectBuilder.buildMunicipiosFiltrado((String) Value.of(i, (STypeSimple) estado.getField(siglaUF.getNameSimple()))));

            tempoMaior30Dias
                    .asAtr().label("Tempo Entre Análise e Colheita Maior que 30 Dias")
                    .asAtrBootstrap().colPreference(6)
                    .newRow();

            estudoEstabilidade
                    .asAtr().label("Estudo de Estabilidade")
                    .dependsOn(tempoMaior30Dias)
                    .exists(typeValIsTrue(tempoMaior30Dias));

            metabolito
                    .withRadioView()
                    .asAtr().label("Metabólito")
                    .required();

            metabolitos
                    .withView(SViewListByTable::new)
                    .asAtr()
                    .label("Metabólitos")
                    .dependsOn(metabolito)
                    .exists(typeValIsTrue(metabolito));

            descricaoMetabolito
                    .asAtr().label("Descrição");

            loqMetabolito
                    .asAtr().label("LoQ (mg/KG)")
                    .fractionalMaxLength(4);

            residuoMetabolito
                    .asAtr().label("Resíduo")
                    .fractionalMaxLength(4);
        }
    }

    private String[] culturas() {
        return new String[] {
                "Algodão herbáceo",
                "Amendoim",
                "Arroz",
                "Aveia",
                "Centeio",
                "Cevada",
                "Feijão",
                "Girassol",
                "Mamona",
                "Milho",
                "Soja",
                "Sorgo",
                "Trigo",
                "Triticale",
                "Abacaxi",
                "Alho",
                "Banana",
                "Cacau",
                "Café arábica",
                "Café canephora",
                "Cana-de-açúcar",
                "Castanha-de-caju",
                "Cebola",
                "Coco-da-baía",
                "Fumo",
                "Guaraná",
                "Juta",
                "Laranja",
                "Maçã",
                "Malva",
                "Mandioca",
                "Pimenta-do-reino",
                "Sisal ou agave",
                "Tomate",
                "Uva"
        };
    }

    private String[] empregos() {
        return new String[] {
                "Caule",
                "Foliar",
                "Solo",
                "Tronco"
        };
    }
}
