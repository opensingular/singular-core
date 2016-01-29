package br.net.mirante.singular.form.mform;

import br.net.mirante.singular.form.mform.core.STypeData;
import br.net.mirante.singular.form.mform.options.MOptionsCompositeProvider;
import br.net.mirante.singular.form.mform.util.transformer.MListaBuilder;
import br.net.mirante.singular.form.mform.util.transformer.Value;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class TestMoptionsConfigTipoLista {


    private static final Date DT_1 = new Date();
    private static final Date DT_2 = new Date();
    private static final Date DT_3 = new Date();
    private static final Date DT_4 = new Date();
    private static final Date DT_5 = new Date();
    private static final Date DT_6 = new Date();
    private SDictionary _dicionario;
    private STypeComposto<? extends SIComposite> _raiz;
    private STypeLista<STypeComposto<SIComposite>, SIComposite> _alertas;
    private STypeComposto<SIComposite> _alerta;
    private STypeData _alerta_data;
    private SIComposite evento;
    private SList<SIComposite> alertas;
    private SList listaAlertas;
    private SInstance opcaoAlerta1;
    private SInstance opcaoAlerta2;
    private SInstance opcaoAlerta3;


    @Before
    public void setup() {
        _dicionario = SDictionary.create();
        PacoteBuilder pb = _dicionario.criarNovoPacote("teste");

        _raiz = pb.createTipoComposto("_raiz");
        

        _alertas = _raiz.addCampoListaOfComposto("alertas", "alerta");
        _alerta = _alertas.getTipoElementos();
        _alerta_data = _alerta.addCampo("data", STypeData.class);

        _raiz.asAtrBasic().label("Evento");
        _alertas.asAtrBasic().label("Alertas");
        _alerta.asAtrBasic().label("Alerta");
        _alerta_data.asAtrBasic().label("Data");

        evento = _raiz.novaInstancia();

        //alertas
        listaAlertas = (SList) evento.getCampo(_alertas.getNomeSimples());

        _alerta.withSelectionFromProvider(_alerta_data, new MOptionsCompositeProvider() {
            @Override
            public void listOptions(SInstance instancia, MListaBuilder<STypeComposto> lb) {
                lb
                        .add()
                        .set(_alerta_data, DT_1)
                        .add()
                        .set(_alerta_data, DT_2)
                        .add()
                        .set(_alerta_data, DT_3)
                        .add()
                        .set(_alerta_data, DT_4)
                        .add()
                        .set(_alerta_data, DT_5)
                        .add()
                        .set(_alerta_data, DT_6);

            }
        });

        SList listaOpcoes = _alerta.getProviderOpcoes().listAvailableOptions(listaAlertas);
        opcaoAlerta1 = listaOpcoes.get(0);
        opcaoAlerta2 = listaOpcoes.get(1);
        opcaoAlerta3 = listaOpcoes.get(2);
        SInstance m1 = listaAlertas.addNovo();
        SInstance m2 = listaAlertas.addNovo();
        SInstance m3 = listaAlertas.addNovo();
        Value.hydrate(m1, Value.dehydrate(opcaoAlerta1));
        Value.hydrate(m2, Value.dehydrate(opcaoAlerta2));
        Value.hydrate(m3, Value.dehydrate(opcaoAlerta3));


    }


    @Test
    public void testValueFromKey() {
        String keyFromOption1 = listaAlertas.getOptionsConfig().getKeyFromOptions(opcaoAlerta1);
        String keyFromOption2 = listaAlertas.getOptionsConfig().getKeyFromOptions(opcaoAlerta2);
        String keyFromOption3 = listaAlertas.getOptionsConfig().getKeyFromOptions(opcaoAlerta3);
        Assert.assertNotNull(keyFromOption1);
        Assert.assertNotNull(keyFromOption2);
        Assert.assertNotNull(keyFromOption3);
        Assert.assertEquals(opcaoAlerta1, listaAlertas.getOptionsConfig().getValueFromKey(keyFromOption1));
        Assert.assertEquals(opcaoAlerta2, listaAlertas.getOptionsConfig().getValueFromKey(keyFromOption2));
        Assert.assertEquals(opcaoAlerta3, listaAlertas.getOptionsConfig().getValueFromKey(keyFromOption3));
        Assert.assertEquals(listaAlertas.size(), 3);
        Assert.assertEquals(listaAlertas.get(0), listaAlertas.getOptionsConfig().getValueFromKey(keyFromOption1));
        Assert.assertEquals(listaAlertas.get(1), listaAlertas.getOptionsConfig().getValueFromKey(keyFromOption2));
        Assert.assertEquals(listaAlertas.get(2), listaAlertas.getOptionsConfig().getValueFromKey(keyFromOption3));
    }

    @Test
    public void testeLabelFromKey() {
        String keyFromOption1 = listaAlertas.getOptionsConfig().getKeyFromOptions(opcaoAlerta1);
        String keyFromOption2 = listaAlertas.getOptionsConfig().getKeyFromOptions(opcaoAlerta2);
        String keyFromOption3 = listaAlertas.getOptionsConfig().getKeyFromOptions(opcaoAlerta3);
        String label1 = listaAlertas.getOptionsConfig().getLabelFromKey(keyFromOption1);
        String label2 = listaAlertas.getOptionsConfig().getLabelFromKey(keyFromOption2);
        String label3 = listaAlertas.getOptionsConfig().getLabelFromKey(keyFromOption3);
        Assert.assertEquals(opcaoAlerta1.getSelectLabel(), label1);
        Assert.assertEquals(opcaoAlerta2.getSelectLabel(), label2);
        Assert.assertEquals(opcaoAlerta3.getSelectLabel(), label3);
    }

    @Test
    public void testMTipoOpcoes(){
        for(SInstance instancia : _alerta.getProviderOpcoes().listAvailableOptions(listaAlertas)){
            Assert.assertEquals(_alerta, instancia.getMTipo());
        }
    }

    @Test
    public void testKeyValueMapping(){
        for(SInstance instancia : _alerta.getProviderOpcoes().listAvailableOptions(listaAlertas)){
            String key = listaAlertas.getOptionsConfig().getKeyFromOptions(instancia);
            Assert.assertEquals(instancia, listaAlertas.getOptionsConfig().getValueFromKey(key));
            Assert.assertEquals(listaAlertas.getOptionsConfig().getLabelFromKey(key), instancia.getSelectLabel());
        }
    }

    @Test
    public void testSelectLabel() {
        for(SInstance instancia : _alerta.getProviderOpcoes().listAvailableOptions(listaAlertas)){
            Assert.assertEquals(String.valueOf(Value.of(instancia, _alerta_data)), instancia.getSelectLabel());
        }
        Assert.assertEquals(_alerta_data.getNomeSimples(), _alerta.getSelectLabel());
    }
}
