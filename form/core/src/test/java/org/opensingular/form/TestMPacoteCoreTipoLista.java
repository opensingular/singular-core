/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.form;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opensingular.form.TestMPacoteCoreTipoLista.TestPacoteListaA.Pedido;
import org.opensingular.form.TestMPacoteCoreTipoLista.TestPacoteListaA.TestTipoListaComCargaInterna;
import org.opensingular.form.TestMPacoteCoreTipoLista.TestPacoteListaA.TipoPedido;
import org.opensingular.form.type.basic.SPackageBasic;
import org.opensingular.form.type.core.SIInteger;
import org.opensingular.form.type.core.SIString;
import org.opensingular.form.type.core.STypeBoolean;
import org.opensingular.form.type.core.STypeInteger;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.internal.lib.commons.test.SingularTestUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class TestMPacoteCoreTipoLista extends TestCaseForm {

    public static final String INDICE_INVALIDO = "índice inválido";

    public TestMPacoteCoreTipoLista(TestFormConfig testFormConfig) {
        super(testFormConfig);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTipoListaCriacaoOfTipoSimples() {
        PackageBuilder pb = createTestPackage();

        STypeList<STypeString, SIString> nomes = pb.createListTypeOf("nomes", STypeString.class);
        assertType(nomes).isParent(null);
        assertType(nomes.getElementsType()).isParent(nomes);

        SIList<SIString> lista = (SIList<SIString>) nomes.newInstance();
        lista.addValue("Paulo");
        assertLista(lista, new String[]{"Paulo"});
        lista.addValue("Joao");
        assertLista(lista, new String[]{"Paulo", "Joao"});
        lista.addValue("Maria");
        assertLista(lista, new String[]{"Paulo", "Joao", "Maria"});

        testCaminho(lista, null, null);
        assertEquals(lista.getValue("[1]"), "Joao");
        assertEquals(lista.indexOf(lista.get(1)), 1);
        testCaminho(lista, "[1]", "[1]");
        testCaminho(lista.getField("[1]"), null, "[1]");

        lista.remove(1);
        assertLista(lista, new String[]{"Paulo", "Maria"});
        assertException(() -> lista.remove(10), INDICE_INVALIDO);

        SIList<SIInteger> listaInt = (SIList<SIInteger>) pb.getDictionary().getType(STypeInteger.class).newList();
        listaInt.addValue(10);
        assertLista(listaInt, new Integer[]{10});
        listaInt.addValue("20");
        assertLista(listaInt, new Integer[]{10, 20});
        assertException(() -> listaInt.addValue("XX"), "não consegue converter");

        assertEquals(lista.getValue("[0]"), "Paulo");
        assertEquals((Object) listaInt.getValue("[1]"), 20);
        assertException(() -> listaInt.getValue("[20]"), INDICE_INVALIDO);

    }

    private static void assertLista(SIList<?> lista, Object[] valoresEsperados) {
        assertEqualsList(lista.getValue(), valoresEsperados);
    }

    @Test
    public void testTipoListaCriacaoOfTipoComposto() {
        PackageBuilder pb = createTestPackage();

        STypeList<STypeComposite<SIComposite>, SIComposite> tipoPedidos = pb.createListOfNewCompositeType("pedidos",
                "pedido");
        tipoPedidos.getElementsType().addFieldString("descricao");
        STypeInteger qtd = tipoPedidos.getElementsType().addFieldInteger("qtd");

        assertThat(tipoPedidos.getPathFromRoot()).isNull();
        assertThat(tipoPedidos.getPathFull()).isEqualTo("pedidos");
        assertThat(qtd.getPathFromRoot()).isEqualTo("pedido.qtd");
        assertThat(qtd.getPathFull()).isEqualTo("pedidos.pedido.qtd");
        assertType(tipoPedidos).field(qtd.getPathFromRoot()).isSameAs(qtd);
        assertType(tipoPedidos).field("pedido.qtd").isSameAs(qtd);


        SIList<SIComposite> pedidos = (SIList<SIComposite>) tipoPedidos.newInstance();
        SIComposite pedido;
        assertException(() -> pedidos.addValue("Paulo"), "SIComposite só suporta valores de mesmo tipo");
        assertChildren(pedidos, 0);
        pedido = pedidos.addNew();
        assertChildren(pedidos, 1);
        assertNotNull(pedido);
        assertEquals(1, pedidos.size());
        assertTrue((pedidos.get(0)) instanceof SIComposite);
        assertTrue((pedidos.getValueAt(0)) instanceof Collection);

        assertException(() -> pedidos.get(10), INDICE_INVALIDO);
        assertException(() -> pedidos.getValueAt(10), INDICE_INVALIDO);

        pedido.setValue("descricao", "bola");
        pedido.setValue("qtd", 20);
        assertChildren(pedidos, 3);

        pedido = pedidos.addNew();
        pedido.setValue("descricao", "rede");
        pedido.setValue("qtd", -10);

        assertException(() -> pedidos.getValueAt(10), INDICE_INVALIDO);

        assertEquals(pedidos.getValue("[0].descricao"), "bola");
        assertEquals((Object) pedidos.getValue("[0].qtd"), 20);

        testAtribuicao(pedidos, "[1].descricao", "rede2", 6);
        testAtribuicao(pedidos, "[1].qtd", 20, 6);
        assertException(() -> pedidos.setValue("[1].marca", 10), "Não é um campo definido");

        testCaminho(pedidos, null, null);
        testCaminho(pedidos, "[0]", "[0]");
        testCaminho(pedidos, "[0].descricao", "[0].descricao");
        testCaminho(pedidos.getField("[0]"), null, "[0]");
        testCaminho(pedidos.getField("[0]"), "qtd", "[0].qtd");
        testCaminho(pedidos.getField("[0].qtd"), null, "[0].qtd");
    }

    @Test
    public void testTipoListaCriacaoOfTipoCompostoTipado() {
        PackageBuilder pb = createTestPackage();

        STypeList<TipoPedido, Pedido> tipoPedidos = pb.createListTypeOf("formulas", TipoPedido.class);
        TipoPedido tipoPedidoLista = tipoPedidos.getElementsType();
        assertType(tipoPedidoLista).isDirectExtensionOf(TipoPedido.class);

        assertType(tipoPedidoLista.id).isNotNull();
        assertType(tipoPedidoLista.nome).isNotNull();
        assertType(tipoPedidoLista.embalagem).isNotNull().isComposite(2);
        assertType(tipoPedidoLista).isComposite(3);

        assertType(tipoPedidoLista.embalagem.descricao).isNotNull();
        assertType(tipoPedidoLista.embalagem.especial).isNotNull();

        assertType(tipoPedidoLista.id).isDirectExtensionOf(TipoPedido.class, "id");
        assertType(tipoPedidoLista.nome).isDirectExtensionOf(TipoPedido.class, "nome");
        assertType(tipoPedidoLista.embalagem).isExtensionOfParentCompositeFieldReference();
        assertType(tipoPedidoLista.embalagem.descricao).isExtensionOfParentCompositeFieldReference();
        assertType(tipoPedidoLista.embalagem.especial).isExtensionOfParentCompositeFieldReference();

        SIList<Pedido> pedidos = (SIList<Pedido>) tipoPedidos.newInstance();

        Pedido pedido = pedidos.addNew();
        pedido.setValue("id", "1");
        pedido.setValue("nome", "arroz");

        assertEquals("1", pedido.getValue("id"));
        assertEquals("1", pedidos.getValueString("[0].id"));
        assertEquals("arroz", pedidos.getValueString("[0].nome"));
    }

    @Test
    public void testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalCompositeType() {
        //-----------------------------------------------------------------
        //Teste 1 - Criando a lista direto no package e usando um composite criado
        PackageBuilder pb = createTestPackage();
        STypeComposite<SIComposite> original = pb.createCompositeType("original");
        original.addFieldString("s1");
        STypeList<?, ?> list = pb.createListTypeOf("list", original);
        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalCompositeType(original, list);

        //-----------------------------------------------------------------
        //Teste 2 - Criando a lista direto no package e usando SType
        pb = createTestPackage();
        original = pb.getType(STypeComposite.class);
        list = pb.createListOfNewCompositeType("list", "item");
        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalCompositeType(original, list);

        //-----------------------------------------------------------------
        //Teste 3 - Criando a lista direto no package e usando SType pela referencia de classe
        pb = createTestPackage();
        original = pb.getType(STypeComposite.class);
        list = pb.createListTypeOf("list", STypeComposite.class);
        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalCompositeType(original, list);

        //-----------------------------------------------------------------
        //Teste 4 - Criando a lista de um tipo e usando um composite criado
        pb = createTestPackage();
        original = pb.createCompositeType("original");
        original.addFieldString("s1");
        STypeComposite<SIComposite> parent = pb.createCompositeType("parent");
        list = parent.addFieldListOf("list", original);
        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalCompositeType(original, list);

        //-----------------------------------------------------------------
        //Teste 5 - Criando a lista de um tipo e usando SType
        pb = createTestPackage();
        original = pb.getType(STypeComposite.class);
        parent = pb.createCompositeType("parent");
        list = parent.addFieldListOfComposite("list", "item");
        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalCompositeType(original, list);

        //-----------------------------------------------------------------
        //Teste 6 - Criando a lista de um tipo e usando SType pela referencia de classe
        pb = createTestPackage();
        original = pb.getType(STypeComposite.class);
        parent = pb.createCompositeType("parent");
        list = parent.addFieldListOf("list", STypeComposite.class);
        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalCompositeType(original, list);
    }

    public void testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalCompositeType(
            STypeComposite<?> original, STypeList<?, ?> list) {
        ((STypeComposite) list.getElementsType()).addFieldString("s2");

        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalType(original, list);
        assertType(list.getElementsType()).isComposite(original.getFields().size() + 1);
    }

    private void testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalType(SType<?> original,
            STypeList<?, ?> list) {
        assertType(list.getElementsType()).isExtensionCorrect(original);

        assertType(original).isAttribute(SPackageBasic.ATR_LABEL, null);
        assertType(list.getElementsType()).isAttribute(SPackageBasic.ATR_LABEL, null);
        list.getElementsType().asAtr().label("xxx");
        assertType(original).isAttribute(SPackageBasic.ATR_LABEL, null);
        assertType(list.getElementsType()).isAttribute(SPackageBasic.ATR_LABEL, "xxx");

        assertType(original).isAttribute(SPackageBasic.ATR_SUBTITLE, null);
        assertType(list.getElementsType()).isAttribute(SPackageBasic.ATR_SUBTITLE, null);
        original.asAtr().subtitle("yyy");
        assertType(original).isAttribute(SPackageBasic.ATR_SUBTITLE, "yyy");
        assertType(list.getElementsType()).isAttribute(SPackageBasic.ATR_SUBTITLE, "yyy");
    }

    @Test
    //@Ignore("Desativado devido a problema estrutural. Voltar quando tiver sido resolvido")
    public void testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalTypeByClassPedido() {
        PackageBuilder pb = createTestPackage();
        TipoPedido pedido = pb.getType(TipoPedido.class);
        STypeList<TipoPedido, Pedido> list = pb.createListTypeOf("list", pedido);
        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalType(pedido, list);
        verificarVariaveisPedidoCriada(pedido);
        verificarVariaveisPedidoCriada(list.getElementsType());

        pb = createTestPackage();
        pedido = pb.getType(TipoPedido.class);
        list = pb.createListTypeOf("list", TipoPedido.class);
        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalType(pedido, list);
        verificarVariaveisPedidoCriada(pedido);
        verificarVariaveisPedidoCriada(list.getElementsType());
    }

    private void verificarVariaveisPedidoCriada(STypeList<?, ?> list) {
        verificarVariaveisPedidoCriada((TipoPedido) list.getElementsType());
    }

    private void verificarVariaveisPedidoCriada(TipoPedido pedido) {
        assertNotNull(pedido.id);
        assertNotNull(pedido.embalagem);
        assertNotNull(pedido.embalagem.descricao);
    }

    @Test
    //@Ignore("Desativado devido a problema estrutural. Voltar quando tiver sido resolvido")
    public void testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalTypeByClassPedidoTwoTimes() {
        PackageBuilder pb = createTestPackage();
        TipoPedido pedido = pb.getType(TipoPedido.class);
        STypeComposite<SIComposite> composite = pb.createCompositeType("block");
        STypeList<TipoPedido, Pedido> list1 = composite.addFieldListOf("list1", pedido);
        STypeList<TipoPedido, Pedido> list2 = composite.addFieldListOf("list2", pedido);
        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalTypeByClassPedido(pedido, list1, list2);
        verificarVariaveisPedidoCriada(pedido);
        verificarVariaveisPedidoCriada(list1.getElementsType());
        verificarVariaveisPedidoCriada(list2.getElementsType());

        pb = createTestPackage();
        pedido = pb.getType(TipoPedido.class);
        composite = pb.createCompositeType("block");
        list1 = composite.addFieldListOf("list1", TipoPedido.class);
        list2 = composite.addFieldListOf("list2", TipoPedido.class);
        testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalTypeByClassPedido(pedido, list1, list2);
        verificarVariaveisPedidoCriada(pedido);
        verificarVariaveisPedidoCriada(list1.getElementsType());
        verificarVariaveisPedidoCriada(list2.getElementsType());
    }

    private void testChangeInAttributeOfTheListElementTypeMustNotChangeTheOriginalTypeByClassPedido(TipoPedido pedido,
            STypeList<TipoPedido, Pedido> list1, STypeList<TipoPedido, Pedido> list2) {
        assertType(list1.getElementsType()).isExtensionCorrect(pedido);
        assertType(list2.getElementsType()).isExtensionCorrect(pedido).isNotSameAs(list1.getElementsType());

        assertType(pedido).isAttrLabel(null);
        assertType(list1.getElementsType()).isAttrLabel(null);
        assertType(list2.getElementsType()).isAttrLabel(null);
        list1.getElementsType().asAtr().label("xxx");
        assertType(pedido).isAttrLabel(null);
        assertType(list1.getElementsType()).isAttrLabel("xxx");
        assertType(list2.getElementsType()).isAttrLabel(null);

        assertType(pedido).isAttrSubTitle(null);
        assertType(list1.getElementsType()).isAttrSubTitle(null);
        assertType(list2.getElementsType()).isAttrSubTitle(null);
        pedido.asAtr().subtitle("yyy");
        assertType(pedido).isAttrSubTitle("yyy");
        assertType(list1.getElementsType()).isAttrSubTitle("yyy");
        assertType(list2.getElementsType()).isAttrSubTitle("yyy");
    }

    @Test
    public void testeOnCargaTipoDireto() {
        TestTipoListaComCargaInterna tipo = createTestDictionary().getType(TestTipoListaComCargaInterna.class);
        assertEquals("xxx", tipo.asAtr().getLabel());
        assertEquals((Boolean) true, tipo.isRequired());
    }

    @Test
    public void testeOnCargaTipoChamadaSubTipo() {
        PackageBuilder pb = createTestPackage();
        TestTipoListaComCargaInterna tipo = pb.createType("arquivo", TestTipoListaComCargaInterna.class);

        assertEquals("xxx", tipo.asAtr().getLabel());
        assertEquals((Boolean) true, tipo.isRequired());
    }

    @SInfoPackage(name = "teste.pacoteListaA")
    public static final class TestPacoteListaA extends SPackage {

        @Override
        protected void onLoadPackage(PackageBuilder pb) {
            pb.createType(TestTipoListaComCargaInterna.class);
            pb.createType(TipoEmbalagem.class);
            pb.createType(TipoPedido.class);
        }

        @SInfoType(spackage = TestPacoteListaA.class)
        public static final class TestTipoListaComCargaInterna extends STypeList<STypeString, SIString> {
            @Override
            protected void onLoadType(TypeBuilder tb) {
                asAtr().required(true);
                asAtr().label("xxx");
            }
        }

        @SInfoType(spackage = TestPacoteListaA.class)
        public static final class TipoEmbalagem extends STypeComposite<SIComposite> {

            public STypeString descricao;
            public STypeBoolean especial;

            @Override
            protected void onLoadType(TypeBuilder tb) {
                descricao = addFieldString("descricao");
                especial = addFieldBoolean("especial");
            }
        }

        @SInfoType(spackage = TestPacoteListaA.class)
        public static final class TipoPedido extends STypeComposite<Pedido> {

            public STypeString   id;
            public STypeString   nome;
            public TipoEmbalagem embalagem;

            public TipoPedido() {
                super(Pedido.class);
            }

            @Override
            protected void onLoadType(TypeBuilder tb) {
                id = addFieldString("id");
                nome = addFieldString("nome");
                embalagem = addField("embalagem", TipoEmbalagem.class);
            }
        }

        public static final class Pedido extends SIComposite {
        }

    }

    @Test
    public void testWrongIndexHandling() {
        testWrongIndexHandling(0);
        testWrongIndexHandling(1);
        testWrongIndexHandling(2);
    }

    private void testWrongIndexHandling(int size) {
        SIList<SIInteger> list = createIntList(size);
        assertEquals((size == 0), list.isEmpty());
        assertEquals(size, list.size());

        assertException(() -> list.getValueAt(-1), INDICE_INVALIDO);
        assertException(() -> list.get(-1), INDICE_INVALIDO);
        assertException(() -> list.getField("[-1]"), " inválido");
        assertException(() -> list.getFieldOpt("[-1]"), " inválido");
        for (Integer i = 0; i < size; i++) {
            assertNotNull(list.get(i));
            assertEquals(i, list.get(i).getValue());
            assertEquals(i, list.getField("[" + i + "]").getValue());
            assertEquals(i, list.getFieldOpt("[" + i + "]").get().getValue());
            assertEquals(i, list.getValueAt(i));
        }
        assertException(() -> list.getValueAt(size), INDICE_INVALIDO);
        assertException(() -> list.get(size), INDICE_INVALIDO);
        assertException(() -> list.getField("[" + size + "]"), INDICE_INVALIDO);
        assertFalse(list.getFieldOpt("[" + size + "]").isPresent());


        assertEquals((size == 0), list.isEmpty());
        assertEquals(size, list.size());
    }

    private SIList<SIInteger> createIntList(int size) {
        PackageBuilder    pb   = createTestPackage();
        SIList<SIInteger> list = pb.createListTypeOf("numbers", STypeInteger.class).newInstance();

        for (int i = 0; i < size; i++) {
            list.addValue(i);
        }
        return list;
    }

    @Test
    public void testListInstantionWithoutElementTypeDefined() {
        SDictionary dictionary = createTestDictionary();

        SIList<SIString> siList = (SIList<SIString>) dictionary.newInstance(STypeList.class);

        SingularTestUtil.assertException(() -> siList.addNew(), SingularFormException.class,
                "doesn't have the type of its elements");
        assertInstance(siList).isList(0).assertCorrectStructure();

        SIString siString = dictionary.newInstance(STypeString.class);
        siString.setValue("value");
        SingularTestUtil.assertException(() -> siList.addElement(siString), SingularFormException.class,
                "doesn't have the type of its elements");
        assertInstance(siList).isList(0).assertCorrectStructure();
    }

    @Test
    public void testMixingDifferenteElementsFromDiferentDocuments1() {
        SIComposite instance = createTestDictionary().getType(MixedType.class).newInstance();

        assertInstance(instance).assertCorrectStructure();
    }

    @Test
    public void testMixingDifferenteElementsFromDiferentDocuments2() {
        SIComposite instance = createTestDictionary().getType(MixedType.class).newInstance();
        SIList<SIComposite> list = (SIList<SIComposite>) instance.getFieldList("itens");
        assertInstance(instance).assertCorrectStructure();

        SType<?> fromOtherDictionary = SDictionary.create().getType(MixedType.class).newInstance().getFieldList("itens")
                .getElementsType();
        assertNotSame(instance.getDictionary(), fromOtherDictionary.getDictionary());

        SIComposite newElement = (SIComposite) fromOtherDictionary.newInstance();
        newElement.getField("name").setValue("item3");
        list.addElement(newElement);
        assertInstance(instance).assertCorrectStructure();
    }

    @SInfoPackage
    public static class PackageMixingDocuments extends SPackage {

    }

    @SInfoType(spackage = PackageMixingDocuments.class)
    public static class MixedType extends STypeComposite<SIComposite> {

        public STypeList<STypeComposite<SIComposite>, SIComposite> itens;

        @Override
        protected void onLoadType(TypeBuilder tb) {

            itens = addFieldListOfComposite("itens", "item");
            STypeComposite<SIComposite> item = itens.getElementsType();
            item.addFieldString("name");
            item.addFieldInteger("qtd");

            itens.withInitListener((list) -> {
                List<String> periodosStr = Arrays.asList("item1", "item2");
                periodosStr.forEach(p -> {
                    SIComposite e = itens.getElementsType().newInstance();
                    e.getField("name").setValue(p);
                    list.addElement(e);
                });
            });
        }
    }
}
