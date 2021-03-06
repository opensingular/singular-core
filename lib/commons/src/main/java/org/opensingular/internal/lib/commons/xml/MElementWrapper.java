/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.opensingular.internal.lib.commons.xml;

import org.opensingular.lib.commons.base.SingularException;
import org.opensingular.lib.commons.internal.function.SupplierUtil;
import org.opensingular.lib.commons.lambda.ISupplier;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author Daniel C. Bordin
 */
public class MElementWrapper extends MElement implements EWrapper {

    /**
     * Caracter separador de nomes de Elementos (Xpath).
     */
    static final char ELEMENT_PATH_SEPARATOR = '/';

    /**
     * Elemento que contem realmente os dados.
     */
    private final ISupplier<Element> original;

    /**
     * Constroi um MElement para ler e alterar o Element informado.
     *
     * @param original -
     */
    MElementWrapper(@Nonnull Element original) {
        Objects.requireNonNull(original);
        if (original instanceof MElementWrapper) {
            this.original = ((MElementWrapper) original).original;
        } else {
            this.original = SupplierUtil.serializable(original);
        }
    }

    /**
     * Constroi um MElement raiz com o nome informado.
     */
    MElementWrapper(@Nonnull String rootName) {
        original = SupplierUtil.serializable(XmlUtil.newRootElement(rootName));
    }

    /**
     * Constroi um MElement raiz com o nome informado.
     *
     * @param namespaceURI  Tipicamente o name space possui o formato de uma URL
     *                      (não é obrigatório) no formato, por exemplo,
     * @param qualifiedName o nome do elemento que será criado. Pode conter
     *                      prefixo (ex.: "fin:ContaPagamento").
     */
    MElementWrapper(String namespaceURI, String qualifiedName) {
        original = SupplierUtil.serializable(newRootElement(namespaceURI, qualifiedName));
    }

    /**
     * Obtem o element original.
     */
    @Override
    public final Element getOriginal() {
        return original.get();
    }

    /**
     * Cria um elemento XML em um novo documento para um determinado namespace.
     *
     * @param namespaceURI  Tipicamente o name space possui o formato de uma URL
     *                      (não é obrigatório) no formato, por exemplo,
     * @param qualifiedName o nome do elemento que será criado. Pode conter
     *                      prefixo (ex.: "fin:ContaPagamento").
     * @return o elemento que foi criado
     */
    static Element newRootElement(String namespaceURI, String qualifiedName) {

        Document d          = XmlUtil.newDocument();
        Element  newElement = d.createElementNS(namespaceURI, qualifiedName);

        //Verifica se precisa colocar um atributo por conta do Namespace
        if ((qualifiedName != null) && (qualifiedName.length() != 0)) {
            int posPrefixo = qualifiedName.indexOf(':');
            if ((posPrefixo == -1)) {
                newElement.setAttribute("xmlns", namespaceURI);
            } else {
                String prefixo = qualifiedName.substring(0, posPrefixo);
                newElement.setAttribute("xmlns:" + prefixo, namespaceURI);
            }
            //novo.setAttribute("xmlns:" + nome.getPrefix(), namespaceURI);
        }
        d.appendChild(newElement);
        return newElement;
    }

    /**
     * Metodo utilizado para colocar apenas o conteúdo de um elemento dentro de
     * outro elemento. Os elementos contidos no elemento <code>no</code> serão
     * colocados ao final da lista de elementos elemento <code>pai</code>
     * (append).
     *
     * @param parent elemento que o conteúdo do outro elemento
     * @param node   elemento cujo conteúdo será colocado dentro do elemento pai
     */
    static void copyElement(Element parent, Element node) {
        if ((parent == null) || (node == null)) {
            throw new IllegalArgumentException("Null não permitido");
        }

        Document doc = parent.getOwnerDocument();

        Node current = node.getFirstChild();
        while (current != null) {
            switch (current.getNodeType()) {
                case (TEXT_NODE):
                    parent.appendChild(doc.createTextNode(current.getNodeValue()));
                    break;
                case (ELEMENT_NODE):
                    Element newElement = newElement(doc, (Element) current);
                    parent.appendChild(newElement);
                    copyElement(newElement, (Element) current);
                    break;
                default:
                    throw new SingularException("O no do tipo "
                            + current.getNodeType()
                            + " não é suportado");
            }
            current = current.getNextSibling();
        }
    }

    /**
     * Cria um novo element com os mesmos atributos e namespace do elemento
     * fornecido.
     *
     * @param owner    Document a ser utilizado na criação do Element
     * @param original Elemento a sercopiado
     * @return Sempre diferente de null
     */
    private static Element newElement(Document owner, Element original) {
        Element newElement = owner.createElementNS(original.getNamespaceURI(), original.getTagName());

        if (original.hasAttributes()) {
            NamedNodeMap domAttributes  = original.getAttributes();
            int          noOfAttributes = domAttributes.getLength();
            Attr         attr;
            for (int i = 0; i < noOfAttributes; i++) {
                attr = (Attr) domAttributes.item(i);
                if (attr.getNamespaceURI() == null) {
                    newElement.setAttribute(attr.getNodeName(), attr.getNodeValue());
                } else {
                    newElement.setAttributeNS(attr.getNamespaceURI(), attr.getNodeName(), attr
                            .getNodeValue());
                }
            }
        }

        return newElement;
    }

    /**
     * Metodo utilizado para colocar um elemento (e seu conteúdo) dentro de
     * outro elemento, podendo ser usado um outro nome ao invés do nome do
     * elemento sendo copiado. O elemento é copiado ao final da lista de
     * elementos (append) do elemento de destino.
     *
     * @param parent  elemento que receberá um novo elemento com o conteúdo do
     *                elemento <code>no</code>
     * @param no      elemento cujo conteúdo será colocado dentro de um elemento de
     *                nome <code>novoNome</code>, que será colocado dentro do
     *                elemento <code>pai</code>
     * @param newName nome do elemento que receberá o conteúdo do elemento
     *                <code>no</code> e que será colocado dentro de <code>pai</code>;
     *                se for <code>null</code>, é usado o nome do elemento
     *                <code>no</code>
     * @return O novo no criado no novo pai
     */
    static Element copyElement(Element parent, Element no, String newName) {
        if ((parent == null) || (no == null)) {
            throw new IllegalArgumentException("Null não permitido");
        }
        Document doc = parent.getOwnerDocument();

        Element newElement;
        if (newName == null) {
            newElement = newElement(doc, no);
        } else {
            newElement = doc.createElement(newName);
        }
        parent.appendChild(newElement);
        copyElement(newElement, no);

        return newElement;
    }

    /**
     * Adiciona um elemento binario no formato BASE64 dentro do elemento pai. O
     * formato BASE64 é definido pelo RFC1521 do RFC1521. Ele transforma um
     * binário em uma string, um codificação de 6 bits. Deste modo, um array
     * binário ocupa 33% mais espaço no formato BASE, contudo passa a ser uma
     * string simples. É necessário levar em consideração questões de gasto de
     * memória e de custo de conversão de binário para string e string para
     * binário ao se decidir pelo uso deste formato.
     *
     * @param value o array binário do elemento adicionado (a ser convertido p/
     *              BASE64)
     * @return o elemento que foi adicionado
     */
    static String toBASE64(byte[] value) {
        if (value == null) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(value);
    }

    /**
     * Converte um binario para String no formato BASE64 até esgotar a
     * InputStream. O formato BASE64 é definido pelo RFC1521 do RFC1521. Ele
     * transforma um binário em uma string, um codificação de 6 bits. Deste
     * modo, um array binário ocupa 33% mais espaço no formato BASE, contudo
     * passa a ser uma string simples. É necessário levar em consideração
     * questões de gasto de memória e de custo de conversão de binário para
     * string e string para binário ao se decidir pelo uso deste formato.
     *
     * @param in Stream com os dados a serem convertidos p/ BASE64.
     * @return -
     */
    static String toBASE64(@Nonnull InputStream in, @Nonnull Charset charset) {
        return encodeFromInputStream(in, charset);
    }

    @SuppressWarnings("squid:S2095")
    private static String encodeFromInputStream(@Nonnull InputStream in, @Nonnull Charset charset) {
        try (BufferedReader buff = new BufferedReader(new InputStreamReader(Objects.requireNonNull(in), charset))) {

            StringBuilder builder = new StringBuilder();
            String        line;

            while ((line = buff.readLine()) != null) {
                builder.append(line);
                if (buff.ready()) {
                    builder.append("\r\n");
                }
            }
            return java.util.Base64.getEncoder().encodeToString(builder.toString().getBytes(charset));
        } catch (IOException e) {
            throw SingularException.rethrow("Error encoding from the input stream", e);
        }
    }

    /**
     * Converte a String com codificação BASE64 de volta para um array de bytes.
     *
     * @param stringValue String a ser convertida
     * @return null se a string for null
     */
    static byte[] fromBASE64(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        return java.util.Base64.getDecoder().decode(stringValue);
    }

    /**
     * Converte a String com codificação BASE64 escrevendo o resultado para a
     * stream informada.
     *
     * @param stringValue String a ser convertida.
     * @param out         Destino do bytes decodificados.
     */
    static void fromBASE64(String stringValue, OutputStream out) {
        if (stringValue == null || out == null) {
            throw new IllegalArgumentException("parametro null");
        }

        try {
            out.write(java.util.Base64.getDecoder().decode(stringValue));
        } catch (IOException e) {
            throw SingularException.rethrow("Error decoding from the output stream", e);
        }
    }

    /**
     * Adiciona um elemento a um elemento pai. <br>
     * O elemento é adicionado sem valor.
     *
     * @param parent        o elemento dentro do qual um elemento será inserido
     * @param namespaceURI  poder ser null
     * @param qualifiedName o nome do elemento que será inserido
     * @return o elemento que foi adicionado
     */
    static Element addElementNS(Node parent, String namespaceURI, String qualifiedName) {
        Node     resolvedParent        = parent;
        Document d                     = resolvedParent.getOwnerDocument();
        String   resolvedNamespaceURI  = namespaceURI;
        String   resolvedQualifiedName = qualifiedName;
        int      pos                   = resolvedQualifiedName.lastIndexOf(ELEMENT_PATH_SEPARATOR);
        if (pos != -1) {
            if (pos == 0) {
                resolvedParent = XmlUtil.getRootParent(resolvedParent);
            } else {
                resolvedParent = getElementCriando(d, resolvedParent, resolvedNamespaceURI, resolvedQualifiedName.substring(0, pos));
            }
            resolvedQualifiedName = resolvedQualifiedName.substring(pos + 1);
            resolvedNamespaceURI = null;
        }
        Element newElement;
        if (isVazio(resolvedNamespaceURI)) {
            newElement = addElementNSVazio(resolvedParent, d, resolvedQualifiedName);
        } else {
            newElement = addElementNSNaoVazio(resolvedParent, d, resolvedNamespaceURI, resolvedQualifiedName);
        }
        resolvedParent.appendChild(newElement);
        return newElement;
    }

    private static Element addElementNSNaoVazio(Node resolvedParent, Document d, String resolvedNamespaceURI, String resolvedQualifiedName) {
        Element newElement;
        newElement = d.createElementNS(resolvedNamespaceURI, resolvedQualifiedName);

        if (!Objects.equals(resolvedNamespaceURI, resolvedParent.getNamespaceURI())) {
            int posPrefixo = resolvedQualifiedName.indexOf(':');
            if ((posPrefixo == -1)) {
                newElement.setAttribute("xmlns", resolvedNamespaceURI);
            } else {
                String prefixo = resolvedQualifiedName.substring(0, posPrefixo);
                newElement.setAttribute("xmlns:" + prefixo, resolvedNamespaceURI);
            }
            //novo.setAttribute("xmlns:" + nome.getPrefix(), namespaceURI);
        }
        return newElement;
    }

    private static Element addElementNSVazio(Node resolvedParent, Document d, String resolvedQualifiedName) {
        String  resolvedNamespaceURI;
        Element newElement;
        if ((resolvedParent.getNamespaceURI() != null) && isVazio(resolvedParent.getPrefix())) {
            resolvedNamespaceURI = resolvedParent.getNamespaceURI();
        } else {
            resolvedNamespaceURI = null; //Podia ser String vazia
        }
        newElement = d.createElementNS(resolvedNamespaceURI, resolvedQualifiedName);
        return newElement;
    }

    /**
     * Adiciona um elemento com valor a um elemento pai. <br>
     *
     * @param parent o elemento dentro do qual um elemento será inserido
     * @param name   o nome do elemento que será inserido
     * @param value  o valor <code>String</code> do elemento adicionado
     * @return o elemento que foi adicionado
     */
    static Element addElement(Element parent, final String name, String value) {
        if (value == null) {
            throw new IllegalArgumentException("O set do valor de "
                    + XPathToolkit.getFullPath(parent)
                    + "/"
                    + name
                    + ": não é permitido valor null. Se for necessário um "
                    + "element empty, utilize addElement sem parâmetro valor");
        }

        Element newElement;
        String  elementName = name;
        int     pos         = elementName.lastIndexOf('@');
        if (pos != -1) {
            String attributeName = elementName.substring(pos + 1);
            if ((pos > 1) && (elementName.charAt(pos - 1) == '/')) {
                pos--; //Conse a barra antes da arroba (ex: cd/@cod)
            }
            if (pos > 0) {
                elementName = elementName.substring(0, pos);
                newElement = getElementCriando(parent.getOwnerDocument(), parent, null, elementName);
            } else {
                newElement = parent;
            }

            if (value.length() == 0) {
                newElement.removeAttribute(attributeName);
            } else {
                newElement.setAttribute(attributeName, value);
            }
        } else {
            newElement = addElementNS(parent, null, elementName);
            if (value.length() != 0) {
                Document d   = parent.getOwnerDocument();
                Text     txt = d.createTextNode(value);
                newElement.appendChild(txt);
            }
        }

        return newElement;
    }

    private static Element getElementCriando(Document d, Node parent, final String namespaceURI,
                                             final String qualifiedName) {

        String subTrecho             = null;
        String resolvedQualifiedName = qualifiedName;
        int    pos                   = resolvedQualifiedName.indexOf(ELEMENT_PATH_SEPARATOR);
        Node   resolvedParent        = parent;
        if (pos != -1) {
            if (pos == 0) {
                resolvedParent = XmlUtil.getRootParent(resolvedParent);
                resolvedQualifiedName = resolvedQualifiedName.substring(1);
                pos = resolvedQualifiedName.indexOf(ELEMENT_PATH_SEPARATOR);
            }
            if (pos != -1) {
                subTrecho = resolvedQualifiedName.substring(pos + 1);
                resolvedQualifiedName = resolvedQualifiedName.substring(0, pos);
            }
        }

        Element e = XmlUtil.nextSiblingOfTypeElement(resolvedParent.getFirstChild(), resolvedQualifiedName);

        if (e == null) {
            String resolvedNamespaceURI = namespaceURI;
            if (isVazio(resolvedNamespaceURI)) {
                if (!isVazio(resolvedParent.getNamespaceURI()) && isVazio(resolvedParent.getPrefix())) {
                    resolvedNamespaceURI = resolvedParent.getNamespaceURI();
                }
            }
            e = d.createElementNS(resolvedNamespaceURI, resolvedQualifiedName);
            resolvedParent.appendChild(e);
        }

        // verifica se precisa procurar um sub-item
        if (subTrecho != null) {
            return getElementCriando(d, e, null, subTrecho);
        }

        return e;
    }

    /**
     * Verifica se a string é null ou tamanho 0.
     *
     * @param s String a ser verificada
     * @return se null ou tamanho igual a 0
     */
    private static boolean isVazio(String s) {
        return (s == null) || (s.length() == 0);
    }

    //---------------------------------------------------------
    // Método abstratos da interface MElement
    //---------------------------------------------------------

    /**
     * @see org.w3c.dom.Element#getTagName()
     */
    @Override
    public String getTagName() {
        return original.get().getTagName();
    }

    /**
     * @see org.w3c.dom.Element#getAttribute(String)
     */
    @Override
    public String getAttribute(String arg0) {
        return original.get().getAttribute(arg0);
    }

    /**
     * @see org.w3c.dom.Element#setAttribute(String, String)
     */
    @Override
    public void setAttribute(String arg0, String arg1) {
        original.get().setAttribute(arg0, arg1);
    }

    /**
     * @see org.w3c.dom.Element#removeAttribute(String)
     */
    @Override
    public void removeAttribute(String arg0) {
        original.get().removeAttribute(arg0);
    }

    /**
     * @see org.w3c.dom.Element#getAttributeNode(String)
     */
    @Override
    public Attr getAttributeNode(String arg0) {
        return original.get().getAttributeNode(arg0);
    }

    /**
     * @see org.w3c.dom.Element#setAttributeNode(Attr)
     */
    @Override
    public Attr setAttributeNode(Attr arg0) {
        return original.get().setAttributeNode(arg0);
    }

    /**
     * @see org.w3c.dom.Element#removeAttributeNode(Attr)
     */
    @Override
    public Attr removeAttributeNode(Attr arg0) {
        return original.get().removeAttributeNode(arg0);
    }

    /**
     * @see org.w3c.dom.Element#getElementsByTagName(String)
     */
    @Override
    public NodeList getElementsByTagName(String arg0) {
        return original.get().getElementsByTagName(arg0);
    }

    /**
     * @see org.w3c.dom.Element#getAttributeNS(String, String)
     */
    @Override
    public String getAttributeNS(String arg0, String arg1) {
        return original.get().getAttributeNS(arg0, arg1);
    }

    /**
     * @see org.w3c.dom.Element#setAttributeNS(String, String, String)
     */
    @Override
    public void setAttributeNS(String arg0, String arg1, String arg2) {
        original.get().setAttributeNS(arg0, arg1, arg2);
    }

    /**
     * @see org.w3c.dom.Element#removeAttributeNS(String, String)
     */
    @Override
    public void removeAttributeNS(String arg0, String arg1) {
        original.get().removeAttributeNS(arg0, arg1);
    }

    /**
     * @see org.w3c.dom.Element#getAttributeNodeNS(String, String)
     */
    @Override
    public Attr getAttributeNodeNS(String arg0, String arg1) {
        return original.get().getAttributeNodeNS(arg0, arg1);
    }

    /**
     * @see org.w3c.dom.Element#setAttributeNodeNS(Attr)
     */
    @Override
    public Attr setAttributeNodeNS(Attr arg0) {
        return original.get().setAttributeNodeNS(arg0);
    }

    /**
     * @see org.w3c.dom.Element#getElementsByTagNameNS(String, String)
     */
    @Override
    public NodeList getElementsByTagNameNS(String arg0, String arg1) {
        return original.get().getElementsByTagNameNS(arg0, arg1);
    }

    /**
     * @see org.w3c.dom.Element#hasAttribute(String)
     */
    @Override
    public boolean hasAttribute(String arg0) {
        return original.get().hasAttribute(arg0);
    }

    /**
     * @see org.w3c.dom.Element#hasAttributeNS(String, String)
     */
    @Override
    public boolean hasAttributeNS(String arg0, String arg1) {
        return original.get().hasAttributeNS(arg0, arg1);
    }

    /**
     * @see org.w3c.dom.Node#getNodeName()
     */
    @Override
    public String getNodeName() {
        return original.get().getNodeName();
    }

    /**
     * @see org.w3c.dom.Node#getNodeValue()
     */
    @Override
    public String getNodeValue() {
        return original.get().getNodeValue();
    }

    /**
     * @see org.w3c.dom.Node#setNodeValue(String)
     */
    @Override
    public void setNodeValue(String arg0) {
        original.get().setNodeValue(arg0);
    }

    /**
     * @see org.w3c.dom.Node#getNodeType()
     */
    @Override
    public short getNodeType() {
        return original.get().getNodeType();
    }

    /**
     * @see org.w3c.dom.Node#getParentNode()
     */
    @Override
    public Node getParentNode() {
        return original.get().getParentNode();
    }

    /**
     * @see org.w3c.dom.Node#getChildNodes()
     */
    @Override
    public NodeList getChildNodes() {
        return original.get().getChildNodes();
    }

    /**
     * @see org.w3c.dom.Node#getFirstChild()
     */
    @Override
    public Node getFirstChild() {
        return original.get().getFirstChild();
    }

    /**
     * @see org.w3c.dom.Node#getLastChild()
     */
    @Override
    public Node getLastChild() {
        return original.get().getLastChild();
    }

    /**
     * @see org.w3c.dom.Node#getPreviousSibling()
     */
    @Override
    public Node getPreviousSibling() {
        return original.get().getPreviousSibling();
    }

    /**
     * @see org.w3c.dom.Node#getNextSibling()
     */
    @Override
    public Node getNextSibling() {
        return original.get().getNextSibling();
    }

    /**
     * @see org.w3c.dom.Node#getAttributes()
     */
    @Override
    public NamedNodeMap getAttributes() {
        return original.get().getAttributes();
    }

    /**
     * @see org.w3c.dom.Node#getOwnerDocument()
     */
    @Override
    public Document getOwnerDocument() {
        return original.get().getOwnerDocument();
    }

    /**
     * @see org.w3c.dom.Node#insertBefore(Node, Node)
     */
    @Override
    public Node insertBefore(Node arg0, Node arg1) {
        return original.get().insertBefore(EWrapper.getOriginal(arg0), EWrapper.getOriginal(arg1));
    }

    /**
     * @see org.w3c.dom.Node#replaceChild(Node, Node)
     */
    @Override
    public Node replaceChild(Node arg0, Node arg1) {
        return original.get().replaceChild(EWrapper.getOriginal(arg0), arg1);
    }

    /**
     * @see org.w3c.dom.Node#removeChild(Node)
     */
    @Override
    public Node removeChild(Node arg0) {
        return original.get().removeChild(EWrapper.getOriginal(arg0));
    }

    /**
     * @see org.w3c.dom.Node#appendChild(Node)
     */
    @Override
    public Node appendChild(Node arg0) {
        return original.get().appendChild(EWrapper.getOriginal(arg0));
    }

    /**
     * @see org.w3c.dom.Node#hasChildNodes()
     */
    @Override
    public boolean hasChildNodes() {
        return original.get().hasChildNodes();
    }

    /**
     * @see org.w3c.dom.Node#cloneNode(boolean)
     */
    @Override
    public Node cloneNode(boolean arg0) {
        return original.get().cloneNode(arg0);
    }

    /**
     * @see org.w3c.dom.Node#normalize()
     */
    @Override
    public void normalize() {
        original.get().normalize();
    }

    /**
     * @see org.w3c.dom.Node#isSupported(String, String)
     */
    @Override
    public boolean isSupported(String arg0, String arg1) {
        return original.get().isSupported(arg0, arg1);
    }

    /**
     * @see org.w3c.dom.Node#getNamespaceURI()
     */
    @Override
    public String getNamespaceURI() {
        return original.get().getNamespaceURI();
    }

    /**
     * @see org.w3c.dom.Node#getPrefix()
     */
    @Override
    public String getPrefix() {
        return original.get().getPrefix();
    }

    /**
     * @see org.w3c.dom.Node#setPrefix(String)
     */
    @Override
    public void setPrefix(String arg0) {
        original.get().setPrefix(arg0);
    }

    /**
     * @see org.w3c.dom.Node#getLocalName()
     */
    @Override
    public String getLocalName() {
        return original.get().getLocalName();
    }

    /**
     * @see org.w3c.dom.Node#hasAttributes()
     */
    @Override
    public boolean hasAttributes() {
        return original.get().hasAttributes();
    }

    //-------------------------------------------
    // Métodos para o Jdk 1.5
    //-------------------------------------------

    /**
     * @see org.w3c.dom.Element#getSchemaTypeInfo()
     */
    @Override
    public TypeInfo getSchemaTypeInfo() {
        return original.get().getSchemaTypeInfo();
    }

    /**
     * @see org.w3c.dom.Element#setIdAttribute(java.lang.String, boolean)
     */
    @Override
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        original.get().setIdAttribute(name, isId);
    }

    /**
     * @see org.w3c.dom.Element#setIdAttributeNS(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        original.get().setIdAttributeNS(namespaceURI, localName, isId);
    }

    /**
     * @see org.w3c.dom.Element#setIdAttributeNode(org.w3c.dom.Attr, boolean)
     */
    @Override
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        original.get().setIdAttributeNode(idAttr, isId);
    }

    /**
     * @see org.w3c.dom.Node#getBaseURI()
     */
    @Override
    public String getBaseURI() {
        return original.get().getBaseURI();
    }

    /**
     * @see org.w3c.dom.Node#compareDocumentPosition(org.w3c.dom.Node)
     */
    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        return original.get().compareDocumentPosition(other);
    }

    /**
     * @see org.w3c.dom.Node#getTextContent()
     */
    @Override
    public String getTextContent() throws DOMException {
        return original.get().getTextContent();
    }

    /**
     * @see org.w3c.dom.Node#setTextContent(java.lang.String)
     */
    @Override
    public void setTextContent(String textContent) throws DOMException {
        original.get().setTextContent(textContent);

    }

    /**
     * @see org.w3c.dom.Node#isSameNode(org.w3c.dom.Node)
     */
    @Override
    public boolean isSameNode(Node other) {
        return original.get().isSameNode(other);
    }

    /**
     * @see org.w3c.dom.Node#lookupPrefix(java.lang.String)
     */
    @Override
    public String lookupPrefix(String namespaceURI) {
        return original.get().lookupPrefix(namespaceURI);
    }

    /**
     * @see org.w3c.dom.Node#isDefaultNamespace(java.lang.String)
     */
    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        return original.get().isDefaultNamespace(namespaceURI);
    }

    /**
     * @see org.w3c.dom.Node#lookupNamespaceURI(java.lang.String)
     */
    @Override
    public String lookupNamespaceURI(String prefix) {
        return original.get().lookupNamespaceURI(prefix);
    }

    /**
     * @see org.w3c.dom.Node#isEqualNode(org.w3c.dom.Node)
     */
    @Override
    public boolean isEqualNode(Node arg) {
        return original.get().isEqualNode(arg);
    }

    /**
     * @see org.w3c.dom.Node#getFeature(java.lang.String, java.lang.String)
     */
    @Override
    public Object getFeature(String feature, String version) {
        return original.get().getFeature(feature, version);
    }

    /**
     * @see org.w3c.dom.Node#setUserData(java.lang.String, java.lang.Object, org.w3c.dom.UserDataHandler)
     */
    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return original.get().setUserData(key, data, handler);
    }

    /**
     * @see org.w3c.dom.Node#getUserData(java.lang.String)
     */
    @Override
    public Object getUserData(String key) {
        return original.get().getUserData(key);
    }
    //-------------------------------------------
    // Fim Métodos para o Jdk 1.5
    //-------------------------------------------

}