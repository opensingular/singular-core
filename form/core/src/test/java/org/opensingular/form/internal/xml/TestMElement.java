/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.form.internal.xml;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

/**
 * JUnit para test do da classe MElement.
 *
 * @author Daniel C. Bordin
 */
public class TestMElement {

    /** XML de base para teste de percorrimento e leitura. */
    private MElement raiz_;

    /**
     * Method chamado pelo JUnit antes de cada método testXXXX para que esse
     * estabeleça o ambiente do teste.
     */
    @Before
    public void setUp() throws Exception {
        //@formatter:off
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
                + "<pedido cliente=\"Paulo\">          \n"
                + "    <cd cod=\"1\">                  \n"
                + "       <grupo>Pato Fu</grupo>      \n"
                + "       <nome>Acustico</nome>        \n"
                + "       <ano>2002</ano>              \n"
                + "       <faixa>Minha Musica</faixa>  \n"
                + "       <faixa>Sua Musica</faixa>    \n"
                + "       <faixa>Nossa Musica</faixa>  \n"
                + "       <presente/>                  \n"
                + "       <preco>10.12</preco>         \n"
                + "    </cd>                           \n"
                + "    <cd cod=\"4\">                  \n"
                + "       <grupo>Paralamas</grupo>     \n"
                + "       <nome>9 Luas</nome>          \n"
                + "       <ano>1999</ano>              \n"
                + "       <faixa>9 Luas</faixa>        \n"
                + "       <faixa>8 Luas</faixa>        \n"
                + "       <faixa>7 Luas</faixa>        \n"
                + "       <preco>10.1</preco>          \n"
                + "    </cd>                           \n"
                + "    <cd cod=\"6\">                  \n"
                + "       <grupo>U2</grupo>            \n"
                + "       <nome>Zooropa</nome>         \n"
                + "       <ano>1997</ano>              \n"
                + "       <faixa>Babyface</faixa>      \n"
                + "       <faixa>Numb</faixa>          \n"
                + "       <faixa>Lemon</faixa>         \n"
                + "       <preco>10</preco>            \n"
                + "    </cd>                           \n"
                + "</pedido>                           \n"
                + "";
        //@formatter:on
        raiz_ = MParser.parse(xml);

    }

    /**
     * Verifica se obtem corretamente a lista de sub elementos
     */
    @Test
    public void testGetElements() {
        assertEquals(3, raiz_.getElements("cd").length);
        assertEquals(3, raiz_.getElements(null).length);
        assertEquals(1, raiz_.getElement("cd").getElements("presente").length);
        assertEquals(3, raiz_.getElement("cd").getElements("faixa").length);
    }

    /**
     * Verifica método MElement.getValores()
     */
    @Test
    public void testGetValores() {
        assertEquals(3, raiz_.getValores("cd").size());
        assertEquals(0, raiz_.getValores("none").size());
        assertEquals(0, raiz_.getValores("cd/none").size());
        assertEquals(9, raiz_.getValores("cd/faixa").size());
        assertEquals(3, raiz_.getValores("cd/@cod").size());
        assertEquals(3, raiz_.getValores("cd[2]/faixa").size());
        assertEquals(3, raiz_.getElement("cd").getValores("faixa").size());
        assertEquals(7, raiz_.getElement("cd").getValores(null).size());
        assertEquals(3, raiz_.getValores("cd/grupo").size());
        assertEquals(0, raiz_.getValores("cd/presente").size());
    }

    @Test
    public void testFormatNumber() {
        assertEquals("10,12", raiz_.formatNumber("cd[1]/preco", false));
        assertEquals("10,1", raiz_.formatNumber("cd[1]/preco", 1, false));
        assertEquals("10,1", raiz_.formatNumber("cd[2]/preco", false));
        assertEquals("10", raiz_.formatNumber("cd[3]/preco", false));
        assertEquals("10", raiz_.formatNumber("cd[3]/preco", -1, false));
        assertEquals("", raiz_.formatNumber("cd[4]/preco", false));
    }

    /**
     * Testa o funcionamento dos métodos copy
     */
    @Test
    public void testCopy() throws Exception {
        //Testa a copia sem namespace
        MElement raiz2 = MElement.newInstance("http://acme.com", "lista");
        MElement novo2 = raiz2.copy(raiz_.getElement("cd"), null);

        raiz_.print(System.err);
        novo2.print(System.err);
        isIgual(raiz_.getElement("cd"), novo2);

        //Testa a copia com namespace
        MElement raiz3 = MElement.newInstance("Pai-simples");
        MElement novo3 = raiz3.copy(raiz2, null);
        //raiz2.print(System.err);
        //novo3.print(System.err);
        isIgual(raiz2, novo3);

    }

    /**
     * Testa o uso de valroes default ao adicionar um no
     */
    @Test
    public void testAddNull() {
        assertNull(raiz_.addElement("x", (String) null, null));
        assertNull(raiz_.addElement("x", (Object) null, null));
        assertTrue(raiz_.isNull("x"));
        assertTrue(!raiz_.possuiNode("x"));
        assertEquals("a", raiz_.addElement("x", null, "a").getValor());
        assertEquals("a", raiz_.getValor("x"));
        assertEquals("b", raiz_.addElement("x", "b", "a").getValor());
        assertEquals(1.1, raiz_.addElement("x", null, new Double(1.1)).getDouble(), 0);
    }

    /**
     * Faz diverso testo na leitura de valor de atributo e elemento via xPath
     */
    @Test
    public void testLeituraValorXPath() {
        assertEquals(raiz_.getInt("cd/@cod"), 1);
        assertEquals(raiz_.getValor("cd/@cod"), "1");
        assertEquals(raiz_.getValor("/pedido/cd[2]/nome"), "9 Luas");
        assertEquals(raiz_.getValor("/cd/ano"), null);

        MElement cd = raiz_.getElement("cd");
        assertEquals(cd.getValor("/pedido/cd/ano"), "2002");
        assertEquals(cd.getLong("/pedido/cd/ano"), 2002);
        assertEquals(cd.getValor("/ano"), null);
        assertEquals(cd.getValor("ano"), "2002");
        assertEquals(cd.getValor("@cod"), "1");
        assertEquals(cd.getValor("@xpto"), null);

    }

    /**
     * Testa a adição de atributos no XML
     */
    @Test
    public void testSetAtributo() {
        MElement raiz = MElement.newInstance("pedido");

        raiz.addElement("cd/@cod", "1");
        assertEquals(raiz.getValorNotNull("cd/@cod"), "1");

        raiz.addElement("cd/@id", "XF19");
        assertEquals(raiz.getValorNotNull("cd/@id"), "XF19");

        //Escreve por cima
        raiz.addElement("cd/@cod", "2");
        assertEquals(raiz.getValorNotNull("cd/@cod"), "2");

        raiz.addElement("cd/grupo/@ativo", "sim");
        assertEquals(raiz.getValorNotNull("cd/grupo/@ativo"), "sim");

        raiz.addElement("/pedido/entrega/cep", "700");
        assertEquals(raiz.getValorNotNull("/pedido/entrega/cep"), "700");

        raiz.addElement("/pedido/entrega/@urgente", "sim");
        assertEquals(raiz.getValorNotNull("/pedido/entrega/@urgente"), "sim");

        raiz.addElement("@cliente", "Paulo");
        assertEquals(raiz.getValorNotNull("@cliente"), "Paulo");

        raiz.addElement("/pedido/@prioridade", "1");
        assertEquals(raiz.getValorNotNull("/pedido/@prioridade"), "1");

        MElement entrega = raiz.getElement("entrega");
        entrega.addElement("/pedido/transportadora/@cod", "20");
        assertEquals(raiz.getValorNotNull("/pedido/transportadora/@cod"), "20");
    }

    /**
     * Verifica ser os métodos de set e get para java.util.Date, java.sql.Date,
     * java.sql.Timestamp e GregorianCalendar funcionan.
     */
    @Test
    public void testSetGetDatas() {
        GregorianCalendar agoraGc = new GregorianCalendar(2001, 2, 31, 23, 59, 49);
        agoraGc.set(GregorianCalendar.MILLISECOND, 123);
        java.sql.Timestamp agoraTS = new java.sql.Timestamp(agoraGc.getTimeInMillis());
        agoraTS.setNanos(123456780);
        java.sql.Date agoraDateSQL = new java.sql.Date(agoraGc.getTimeInMillis());
        java.util.Date agoraDate = new java.util.Date(agoraGc.getTimeInMillis());

        MElement xml;

        //Teste ler e escrever timestampo
        xml = MElement.newInstance("T");
        xml.addElement("V1", agoraTS);

        assertEquals("string timestamp", xml.getValor("V1"), "2001-03-31T23:59:49.12345678");
        assertEquals("timestamp gravado lido", xml.getTimestamp("V1"), agoraTS);

        //Teste ler e escrever Calendar
        xml = MElement.newInstance("T");
        xml.addElement("V2", agoraGc);

        assertEquals("string calendar errada", xml.getValor("V2"), "2001-03-31T23:59:49.123");
        assertEquals("Calendar gravado lido", xml.getCalendar("V2"), agoraGc);

        //Teste ler e escrever java.util.Date
        xml = MElement.newInstance("T");
        xml.addElement("V", agoraDate);

        assertEquals("string util.date errada", xml.getValor("V"), "2001-03-31T23:59:49.123");
        assertEquals("util.date gravado lido", xml.getDate("V"), agoraDate);

        //Teste ler e escrever java.sql.Date
        xml = MElement.newInstance("T");
        xml.addElement("V", agoraDateSQL);

        assertEquals("string sql.date errada", xml.getValor("V"), "2001-03-31T23:59:49.123");
        assertEquals("sql.date gravado lido", xml.getDateSQL("V"), agoraDateSQL);

        //Teste ler e escrever java.sql.Date (apenas dia)
        agoraGc = new GregorianCalendar(2001, 0, 31);
        agoraDateSQL = new java.sql.Date(agoraGc.getTimeInMillis());
        xml = MElement.newInstance("T");
        xml.addElement("V", agoraDateSQL);

        assertEquals("string sql.date errada", xml.getValor("V"), "2001-01-31");
        assertEquals("sql.date gravado lido", xml.getDateSQL("V"), agoraDateSQL);

        //Teste ler e escrever java.sql.Date (apenas hora)
        agoraGc = new GregorianCalendar(2001, 0, 31, 10, 41, 32);
        agoraDateSQL = new java.sql.Date(agoraGc.getTimeInMillis());
        xml = MElement.newInstance("T");
        xml.addElement("V", agoraDateSQL);

        assertEquals("string sql.date errada", xml.getValor("V"), "2001-01-31T10:41:32");
        assertEquals("sql.date gravado lido", xml.getDateSQL("V"), agoraDateSQL);

        //Teste ler e escrever java.sql.Date (apenas hora)
        xml.addElement("V2", "2001-01-31 10:41:32");
        assertEquals("Leitura com espaço", xml.getDateSQL("V2"), agoraDateSQL);

        //Testa adicionar data como string
        xml.addDate("V3", "08/01/2003");
        assertEquals("data errada", xml.getValor("V3"), "2003-01-08");

        //Testa formatação
        assertEquals("formatação errada", xml.formatDate("V2"), "31/01/2001");
        assertEquals("formatação errada", xml.formatDate("V2", "short"), "31/01/01");
        assertEquals("formatação errada", xml.formatDate("V2", "medium"), "31/01/2001");

        assertEquals("formatação errada", xml.formatDate("V3"), "08/01/2003");
        assertEquals("formatação errada", xml.formatDate("V3", "short"), "08/01/03");
        assertEquals("formatação errada", xml.formatDate("V3", "medium"), "08/01/2003");

    }

    /**
     * Testa os métodos de leitura de null.
     */
    @Test
    public void testGetFormatado() {
        GregorianCalendar agoraGc = new GregorianCalendar(2001, 2, 31, 23, 59, 49);
        agoraGc.set(GregorianCalendar.MILLISECOND, 123);
        java.sql.Timestamp agoraTS = new java.sql.Timestamp(agoraGc.getTimeInMillis());
        agoraTS.setNanos(123456780);
        //java.sql.Date agoraDateSQL = new
        // java.sql.Date(agoraGc.getTimeInMillis());
        //java.util.Date agoraDate = new
        // java.util.Date(agoraGc.getTimeInMillis());

        MElement xml;

        //getDateFormatado
        xml = MElement.newInstance("T");
        xml.addElement("V", agoraTS);
        xml.addElement("V2", 12312);
        xml.addElement("V3", 12312.123);

        //System.out.println(xml.getValor("V"));
        //System.out.println(xml.getDate("V"));
        //System.out.println(xml.getFormatadoDate("V"));
        //System.out.println(xml.getFormatadoHora("V"));
        //System.out.println(xml.getFormatadoNumber("V2", 0));
        //System.out.println(xml.getFormatadoNumber("V2", 2));
        //System.out.println(xml.getFormatadoNumber("V3", 0));
        //System.out.println(xml.getFormatadoNumber("V3", 2));

        assertEquals(xml.formatDate("V"), "31/03/2001");
        assertEquals(xml.formatHour("V"), "23:59:49");
        assertEquals(xml.formatNumber("V2", 0), "12.312");
        assertEquals(xml.formatNumber("V2", 2), "12.312,00");
        assertEquals(xml.formatNumber("V3", 0), "12.312");
        assertEquals(xml.formatNumber("V3", 2), "12.312,12");

    }

    /**
     * Verifica se ocorre os erro de campo null ou vazio quando necessário.
     */
    @Test
    public void testGetValorNull() {
        assertNull(raiz_.getValor("carro"));

        try {
            raiz_.getValorNotNull("carro");
            fail("Deveria ter ocorrido erro ao ler um campo que não existe");
        } catch (NullPointerException e) {
            //ok
        }
        try {
            raiz_.getValorNotNull("cd/@prioridade");
            fail("Deveria ter ocorrido erro ao ler um campo que não existe");
        } catch (NullPointerException e) {
            //ok
        }
        try {
            raiz_.getValorNotNull("cd/presente");
            fail("Deveria ter ocorrido erro ao ler um campo vazio");
        } catch (NullPointerException e) {
            //ok
        }
        try {
            raiz_.getLong("cep");
            fail("Deveria ter ocorrido erro ao ler um campo que não existe");
        } catch (NullPointerException e) {
            //ok
        }
        try {
            raiz_.getInt("cep");
            fail("Deveria ter ocorrido erro ao ler um campo que não existe");
        } catch (NullPointerException e) {
            //ok
        }
        MElement presente = raiz_.getElement("cd/presente");
        try {
            presente.getInt();
            fail("Deveria ter ocorrido erro ao ler um campo vazio");
        } catch (NullPointerException e) {
            //ok
        }
        try {
            presente.getLong();
            fail("Deveria ter ocorrido erro ao ler um campo vazio");
        } catch (NullPointerException e) {
            //ok
        }
    }

    /**
     * Verifica se os métodos de adicionar acusam erro se o valor for null.
     */
    @Test
    public void testAddValorNull() {
        MElement raiz = MElement.newInstance("xxx");
        try {
            raiz.addElement("campo", (java.util.Date) null);
            fail("Deveria ter ocorrido um erro em campo com valor null");
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            raiz.addElement("campo", (String) null);
            fail("Deveria ter ocorrido um erro em campo com valor null");
        } catch (IllegalArgumentException e) {
            //ok
        }
        try {
            raiz.addElement("campo", (Timestamp) null);
            fail("Deveria ter ocorrido um erro em campo com valor null");
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    /**
     * Verifica se o método de count funciona adequadamente
     */
    @Test
    public void testCount() {
        assertEquals(3, raiz_.countFilhos());
        assertEquals(3, raiz_.count(null));
        assertEquals(3, raiz_.count("cd"));
        assertEquals(0, raiz_.count("xpto"));
        assertEquals(3, raiz_.getElement("cd").count("faixa"));
    }

    /**
     * Verifica se o método possuiElement()
     */
    @Test
    public void testPossuiNodeElement() {
        assertEquals(true, raiz_.possuiElement("cd"));
        assertEquals(true, raiz_.possuiElement("cd/faixa"));
        assertEquals(false, raiz_.possuiElement("cd/xpto"));
        assertEquals(false, raiz_.possuiElement("xpto"));
        try {
            assertEquals(false, raiz_.possuiElement("@cliente"));
            fail("Deveria ter ocorrido um erro, pois @cliente é um atributo");
        } catch (RuntimeException e) {
            //ok
        }
        assertEquals(true, raiz_.possuiNode("cd"));
        assertEquals(true, raiz_.possuiNode("cd/faixa"));
        assertEquals(false, raiz_.possuiNode("cd/xpto"));
        assertEquals(false, raiz_.possuiNode("xpto"));
        assertEquals(true, raiz_.possuiNode("@cliente"));
        assertEquals(true, raiz_.possuiNode("cd/@cod"));
    }

    /**
     * Verifica se consegue colocar um XML como texto e depois ler o mesmo
     * conteudo via uma parse.
     */
    @Test
    public void testXMLtoStringtoXMLsemNamespace() throws Exception {
        MElement raiz = MElement.newInstance("requisicao-tempo-atendimento");

        raiz.addElement("Filtro/operadoras/id-operadora", 2);
        raiz.addElement("grupo/usuario-grupo");
        raiz.addElement("orderby").addElement("loja");
        toStringToXML(raiz);
    }

    /**
     * Testa o gravar e ler XML com String.
     */
    @Ignore("Verificar por que não funciona")
    public void testXMLtoStringtoXMLAcento() throws Exception {
        MElement raiz = MElement.newInstance("requisicao-tempo-atendimento");

        raiz.addElement("texto", "ÁÃÀÄÉËÈÊ");
        toStringToXML(raiz);
    }

    /**
     * Verifica se consegue colocar um XML como texto e depois ler o mesmo
     * conteudo via uma parse.
     */
    @Test
    public void testXMLtoStringtoXMLComNamespaceComPrefixo() throws Exception {
        MElement raiz = MElement.newInstance("http://www.br/gerencia/Tempo", "x:req-tempo");

        raiz.addElement("Filtro/operadoras/id-operadora", 2);
        raiz.addElement("Filtro/operadoras/loja/id-loja", 8802);
        raiz.addElement("ordem/usuario-ordem");
        raiz.addElement("grupo/usuario-grupo");
        raiz.addElement("orderby").addElement("loja");

        toStringToXML(raiz);
    }

    /**
     * Verifica se consegue colocar um XML como texto e depois ler o mesmo
     * conteudo via uma parse.
     */
    @Test
    public void testXMLtoStringtoXMLComNamespaceSemPrefixo() throws Exception {
        MElement raiz = MElement.newInstance("http://www.br/gerencia/Tempo", "req-tempo");

        raiz.addElement("Filtro/operadoras/id-operadora", 2);
        raiz.addElement("Filtro/operadoras/loja/id-loja", 8802);
        raiz.addElement("ordem/usuario-ordem");
        raiz.addElement("grupo/usuario-grupo");
        raiz.addElement("orderby").addElement("loja");

        toStringToXML(raiz);
    }

    /**
     * Verifica se o parde dispara exception quando deve - caso simples.
     */
    @Test
    public void testParseValidatComErro() throws Exception {
        String sXML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + "<raiz><filho/></raiz>";

        try {
            MParser.parse(new ByteArrayInputStream(sXML.getBytes()), true, true);
            fail("Deveria ter ocorrido um erro no parse");
        } catch (SAXException e) {
            if (!e.getMessage().startsWith("Erro(s) efetuando")) {
                throw e;
            }
            //ok - chegou o erro esperado
        }
    }

    /**
     * Verifica se o parse dispara exception quando não encontra o DTD.
     */
    @Test
    public void testParseValidatDTDNotFound() throws Exception {
        String sXML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<!DOCTYPE apolices SYSTEM \"testeNaoExiste.dtd\">\n" + "<raiz><filho/></raiz>";

        try {
            MParser.parse(new ByteArrayInputStream(sXML.getBytes()), true, true);
            fail("Deveria ter ocorrido um erro no parse");
        } catch (FileNotFoundException e) {
            //ok - chegou o erro esperado
        } catch (SAXException e) {
            if (e.getMessage().indexOf("can not be resolved") == -1) {
                throw e;
            }
            //ok - chegou o erro esperado
        }
    }

    /**
     * Verifica se o parse dispara exception quando o XML não obdece ao DTD.
     */
    @Test
    public void testParseValidatDTDNaoSatisfeito() throws Exception {
        String sXML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<!DOCTYPE raiz SYSTEM \"http://eee/teste-dtd-existe.dtd\">\n" + "<raiz><filho3/></raiz>";
        String sDTD =
                "<!ELEMENT raiz (filho, filho2?)>\n" + "<!ELEMENT filho (#PCDATA)>\n" + "<!ELEMENT filho2 (#PCDATA)>\n";

        MParser p = new MParser();
        p.addInputSource("http://eee/teste-dtd-existe.dtd", sDTD);
        try {
            p.parseComResolver(new ByteArrayInputStream(sXML.getBytes()));
            fail("Deveria ter ocorrido um erro no parse");
        } catch (SAXException e) {
            if (e.getMessage().indexOf("does not allow \"filho3\"") == -1 && e.getMessage().indexOf(
                    "\"filho3\" must be declared") == -1) {
                throw e;
            }
            //ok - chegou o erro esperado
        }
    }

    /**
     * Verifica se o parse consegue ler um DTD a partir de uma InputStream
     */
    @Test
    public void testParseValidatDTDEmInputStream() throws Exception {
        String sXML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<!DOCTYPE raiz SYSTEM \"http://eee/teste-dtd-existe.dtd\">\n" + "<raiz><filho/></raiz>";
        String sDTD =
                "<!ELEMENT raiz (filho, filho2?)>\n" + "<!ELEMENT filho (#PCDATA)>\n" + "<!ELEMENT filho2 (#PCDATA)>\n";

        MParser p = new MParser();
        p.addInputSource("http://eee/teste-dtd-existe.dtd", new ByteArrayInputStream(sDTD.getBytes()));
        p.parseComResolver(sXML.getBytes());
    }

    /**
     * Verifica se o parse consegue ler um DTD em um arquivo relativo a uma
     * classe.
     */
    @Test
    public void testParseValidatDTDFromClasse() throws Exception {
        String sXML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "<!DOCTYPE raiz SYSTEM \"http://eee/teste-dtd-existe.dtd\">\n" + "<raiz><filho/></raiz>";

        MParser p = new MParser();
        p.addInputSource("http://eee/teste-dtd-existe.dtd", getClass(), "teste-dtd.dtd");
        p.parseComResolver(sXML.getBytes());
    }

    /**
     * Verifica se o método de copia funciona para o elemento informado.
     *
     * @param raiz Elemento a ser copiado
     */
    private void testCopy(MElement raiz) throws Exception {
        MElement paiNovo = MElement.newInstance("pai-a");
        MElement novo = paiNovo.copy(raiz, null);
        isIgual(raiz, novo);

        MElement paiNovo2 = MElement.newInstance("http://www.miranteinfo.com", "pai-b");
        MElement novo2 = paiNovo2.copy(raiz, null);
        isIgual(raiz, novo2);

        MElement paiNovo3 = MElement.newInstance("http://www.miranteinfo.com", "p:pai-c");
        MElement novo3 = paiNovo3.copy(raiz, null);
        isIgual(raiz, novo3);
    }

    /**
     * Converte para String depois para XML e verifica se o resultado é igual.
     *
     * @param raiz XML original
     * @return Retorna o MElement resultante da conversão de ida e volta
     * @throws Exception -
     */
    private MElement toStringToXML(MElement raiz) throws Exception {
        //Gera String a partir do XML
        //Faz o parse da String
        testCopy(raiz);

        String space = raiz.getNamespaceURI();
        String local = raiz.getLocalName();

        String sXML = raiz.toStringExato();
        MElement lido = MParser.parse(sXML);

        if ((space != null) && !space.equals(raiz.getNamespaceURI())) {
            fail("Erro bizarro: o namespace do elemento mudou depois do parse");
        }
        if ((local != null) && !local.equals(raiz.getLocalName())) {
            fail("Erro bizarro: o localName do elemento mudou depois do parse");
        }

        //Verifica igualdade
        isIgual(raiz, lido);

        testCopy(lido);

        MElement lido2 = MParser.parse(new ByteArrayInputStream(raiz.toByteArray()), true, false);
        isIgual(raiz, lido2);

        return lido;
    }

    /**
     * Verifica se ambos os nos são iguais fazendo uma comparação em
     * profundidade.
     *
     * @param n1 -
     * @param n2 -
     * @throws Exception Se nbão forem iguais
     */
    public static void isIgual(Node n1, Node n2) throws Exception {
        if (n1 == n2) {
            return;
        }

        isIgual(n1, n2, "NodeName", n1.getNodeName(), n2.getNodeName());
        isIgual(n1, n2, "NodeValue", n1.getNodeValue(), n2.getNodeValue());
        isIgual(n1, n2, "Namespace", n1.getNamespaceURI(), n2.getNamespaceURI());
        isIgual(n1, n2, "Prefix", n1.getPrefix(), n2.getPrefix());
        isIgual(n1, n2, "LocalName", n1.getLocalName(), n2.getLocalName());

        if (isMesmaClasse(Element.class, n1, n2)) {
            Element e1 = (Element) n1;
            Element e2 = (Element) n2;
            //Verifica se possuem os mesmos atributos
            NamedNodeMap nn1 = e1.getAttributes();
            NamedNodeMap nn2 = e2.getAttributes();
            if (nn1.getLength() != nn2.getLength()) {
                fail("O número atributos em " + XPathToolkit.getFullPath(n1) + " (qtd=" + nn1.getLength() +
                        " é diferente de n2 (qtd=" + nn2.getLength() + ")");
            }
            for (int i = 0; i < nn1.getLength(); i++) {
                isIgual((Attr) nn1.item(i), (Attr) nn2.item(i));
            }

            //Verifica se possuem os mesmos filhos
            Node filho1 = e1.getFirstChild();
            Node filho2 = e2.getFirstChild();
            int count = 0;
            while ((filho1 != null) && (filho2 != null)) {
                isIgual(filho1, filho2);
                filho1 = filho1.getNextSibling();
                filho2 = filho2.getNextSibling();
                count++;
            }
            if (filho1 != null) {
                fail("Há mais node [" + count + "] " + XPathToolkit.getNomeTipo(filho1) + " (" +
                        XPathToolkit.getFullPath(filho1) + ") em n1:" + XPathToolkit.getFullPath(n1));
            }
            if (filho2 != null) {
                fail("Há mais node [" + count + "] " + XPathToolkit.getNomeTipo(filho2) + " (" +
                        XPathToolkit.getFullPath(filho2) + ") em n2:" + XPathToolkit.getFullPath(n2));
            }

        } else if (isMesmaClasse(Attr.class, n1, n2)) {
            //Ok

        } else if (isMesmaClasse(Text.class, n1, n2)) {
            //Ok

        } else {
            fail("Tipo de nó " + n1.getClass() + " não tratado");
        }

    }

    /**
     * Verifica se os atributos são iguais. Existe pois a comparação de
     * atributos possui particularidades.
     *
     * @param n1 -
     * @param n2 -
     * @throws Exception Se não forem iguais
     */
    public static void isIgual(Attr n1, Attr n2) throws Exception {
        if (n1 == n2) {
            return;
        }
        isIgual(n1, n2, "NodeName", n1.getNodeName(), n2.getNodeName());
        isIgual(n1, n2, "NodeValue", n1.getNodeValue(), n2.getNodeValue());

        //Por algum motivo depois do parse Prefix passa de null para não null
        //isIgual(n1, n2, "Prefix", n1.getPrefix(), n2.getPrefix());
        //Por algum motivo depois do parse Localname passe de não null para
        // null
        //isIgual(n1, n2, "LocalName", n1.getLocalName(), n2.getLocalName());

        if (!(n1.getNodeName().startsWith("xmlns") && n2.getNodeName().startsWith("xmlns"))) {
            isIgual(n1, n2, "Namespace", n1.getNamespaceURI(), n2.getNamespaceURI());
        }
    }

    /*
     * private static boolean isAtributosIguais(Node n1, Node n2) { NamedNodeMap
     * n1Attrs = n1.getAttributes(); NamedNodeMap n2Attrs = n2.getAttributes();
     * if (domEle.hasAttributes()) { //passagem dos paramentros do element DOM
     * para o SOAP element NamedNodeMap domAttributes = domEle.getAttributes();
     * int noOfAttributes = domAttributes.getLength(); for (int i = 0; i <
     * noOfAttributes; i++) { Node attr = domAttributes.item(i); Name attrNome =
     * null; //System.out.println("["+i+"]LocalName=" + attr.getLocalName());
     * //System.out.println("["+i+"]Prefix =" + attr.getPrefix());
     * //System.out.println("["+i+"]Namespace=" + attr.getNamespaceURI()); if
     * (attr.getLocalName() == null) { if (!attr.getNodeName().equals("xmlns") &&
     * !attr.getNodeName().startsWith("xmlns:")) { attrNome =
     * env.createName(attr.getNodeName()); } } else if (attr.getPrefix() ==
     * null) { if (!attr.getLocalName().equals("xmlns")) { attrNome =
     * env.createName(attr.getNodeName()); } } else { if
     * (!"xmlns".equals(attr.getPrefix())) { env.createName(
     * attr.getLocalName(), attr.getPrefix(), attr.getNamespaceURI()); } } if
     * (attrNome != null) { soapEle.addAttribute(attrNome, attr.getNodeValue()); } } } }
     */

    /**
     * Verifica se ambos o nós são da classe informada. Se apenas um for, um
     * erro é disparado devido a incompatibilidade.
     *
     * @param c        Classe a ser verificada
     * @param original instância 1
     * @param novo     instância 2
     * @return true Se ambos forem instância de c
     */
    private static boolean isMesmaClasse(Class c, Node original, Node novo) {
        if (c.isInstance(original)) {
            if (c.isInstance(novo)) {
                return true;
            } else {
                fail(XPathToolkit.getFullPath(original) + " não é da mesma classe que " +
                        XPathToolkit.getFullPath(novo));
            }
        } else if (c.isInstance(novo)) {
            fail(XPathToolkit.getFullPath(original) + " não é da mesma classe que " + XPathToolkit.getFullPath(novo));
        }
        return false;
    }

    /**
     * Verifica a igualdade de um determiando para de objetos já considerando a
     * situação de um deles ser null.
     *
     * @param n1        -
     * @param n2        -
     * @param nomeParte -
     * @param v1        -
     * @param v2        -
     */
    private static void isIgual(Node n1, Node n2, String nomeParte, Object v1, Object v2) {
        if (((v1 == null) && (v2 != null)) || ((v1 != null) && !v1.equals(v2))) {

            fail("O(a) " + nomeParte + " em  " + XPathToolkit.getFullPath(n2) + " (" + escreverValor(v2) +
                    ") está diferente do original em " + XPathToolkit.getFullPath(n1) + " (" + escreverValor(v1) + ")");
        }
    }

    /**
     * Apenas para formar de forma visivel o caso null
     *
     * @param o -
     * @return Uma string entre '' se diferente null ou a string null.
     */
    private static String escreverValor(Object o) {
        if (o == null) {
            return "null";
        } else {
            return "'" + o + "'";
        }
    }

    /**
     * Verifica o método updateElement
     */
    @Test
    public void testUpdateElement() throws Exception {
        MElement original = MElement.newInstance("teste");
        original.addElement("nome", "Paulo");
        original.addElement("idade", "30");
        original.addElement("@cod", 10);
        original.addElement("@cpf", "676.123.123-23");
        MElement tels = original.addElement("telefones");
        tels.addElement("comercial", "9999");
        tels.addElement("celular", "8888");

        original.updateNode("nome", "Maria");
        original.updateNode("@cod", "20");
        original.updateNode("@rg", "10000");
        original.updateNode("@cpf", null); //Remove o atributo
        original.updateNode("idade", null); //Remove o valor do elemento

        original.updateNode("endereco", "Lago Norte");
        original.updateNode("telefones", "Não faz nada"); //Inocuo
        original.updateNode("telefones/comercial", "7777");
        original.updateNode("telefones/residencial", "6666");

        MElement esperado = MElement.newInstance("teste");
        esperado.addElement("nome", "Maria");
        esperado.addElement("idade");
        esperado.addElement("@cod", 20);
        esperado.addElement("@rg", "10000");
        tels = esperado.addElement("telefones");
        tels.addElement("comercial", "7777");
        tels.addElement("celular", "8888");
        tels.addElement("residencial", "6666");
        esperado.addElement("endereco", "Lago Norte");

        isIgual(esperado, original);
    }

    /**
     * Testa a conversão do toStringExato de caracteres especias.
     */
    @Test
    public void testCaracterEspecial() throws Exception {
        MElement e = MElement.newInstance("teste");
        e.addElement("Ecomercial", "jão & maria");
        e.addElement("MaiorMenor", "a<b>c");
        String original = e.toStringExato(false);
        String esperado = "<teste><Ecomercial>jão &amp; maria</Ecomercial><MaiorMenor>a&lt;b&gt;c</MaiorMenor></teste>";
        if (!original.equals(esperado)) {
            fail("Erro: a conversão de caracteres especiais não funcionou.");
        }
    }

    @Test
    public void testParseCaracterEspecial() throws Exception {
        MElement original = MElement.newInstance("teste");
        original.setAttribute("bomdia", "asdf&asdf><bom");
        original.addElement("Ecomercial", "&jão & maria&");
        original.addElement("MaiorMenor", "<b>");

        MElement parsed = MParser.parse(original.toStringExato());

        isIgual(parsed, original);
    }

}