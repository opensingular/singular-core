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

package org.opensingular.form.io;

import org.apache.commons.lang3.StringUtils;
import org.opensingular.form.ICompositeInstance;
import org.opensingular.form.ICompositeType;
import org.opensingular.form.InternalAccess;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SIList;
import org.opensingular.form.SISimple;
import org.opensingular.form.SInstance;
import org.opensingular.form.SInstances;
import org.opensingular.form.SType;
import org.opensingular.form.STypeSimple;
import org.opensingular.form.SingularFormException;
import org.opensingular.form.document.RefType;
import org.opensingular.form.document.SDocumentFactory;
import org.opensingular.form.type.basic.AtrXML;
import org.opensingular.internal.lib.commons.xml.MDocument;
import org.opensingular.internal.lib.commons.xml.MElement;
import org.opensingular.internal.lib.commons.xml.MParser;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Métodos utilitários para converter instancias e anotaçãoes para e de XML.
 *
 * @author Daniel C. Bordin
 */
public final class SFormXMLUtil {

    private static final String ID_ATTRIBUTE      = "id";
    private static final String LAST_ID_ATTRIBUTE = "lastId";

    private SFormXMLUtil() {
    }

    /**
     * Cria uma instância não passível de serialização para do tipo com o
     * conteúdo persistido no XML informado.
     */
    @Nonnull
    public static <T extends SInstance> T fromXML(@Nonnull SType<T> type, @Nullable String xmlString) {
        return fromXML(type.newInstance(), parseXml(xmlString));
    }

    /**
     * Cria uma instância não passível de serialização para do tipo com o
     * conteúdo persistido no XML informado.
     */
    @Nonnull
    public static <T extends SInstance> T fromXML(@Nonnull SType<T> type, @Nullable MElement xml) {
        return fromXML(type.newInstance(), xml);
    }

    /** Creates a {@link SInstance} that can be serialized  with informed type and document factory. */
    @Nonnull
    public static <T extends SInstance> T fromXML(@Nonnull RefType refType, @Nullable String xmlString,
                                                  @Nonnull SDocumentFactory documentFactory) {
        return fromXML(refType, parseXml(xmlString), documentFactory);
    }

    /** Creates a {@link SInstance} that can be serialized  with informed type and document factory. */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends SInstance> T fromXML(@Nonnull RefType refType, @Nullable MElement xml,
                                                  @Nonnull SDocumentFactory documentFactory) {
        SInstance instance = documentFactory.createInstance(refType, false);
        return (T) fromXML(instance, xml);
    }

    public static <T extends SInstance> void fromXML(@Nonnull T instance, @Nullable String xml) {
        fromXML(instance, parseXml(xml));
    }

    @Nonnull
    public static <T extends SInstance> T fromXML(@Nonnull T newInstance, @Nullable MElement xml) {
        return fromXMLInterno(newInstance, xml, true);
    }

    /**
     * Preenche a instância criada com o xml fornecido.
     * @param restoreMode if false new ids will be generated for the entire form, used to read an instance from an
     *                    raw xml not managed by Singular Form
     */
    @Nonnull
    private static <T extends SInstance> T fromXMLInterno(@Nonnull T newInstance, @Nullable MElement xml, boolean restoreMode) {
        Integer lastId = 0;
        if (xml != null) {
            lastId = xml.getInteger("@" + LAST_ID_ATTRIBUTE);
        }

        // Colocar em modo de não geraçao de IDs
        if (restoreMode) {
            newInstance.getDocument().initRestoreMode();
            if (newInstance.getDocument().getRoot() == newInstance) {
                newInstance.getDocument().setLastId(0);
            }
            removeIDs(newInstance);
        }
        Integer idMax = fromXMLIntermediary(newInstance, xml);
        lastId = max(lastId, idMax);

        if (lastId != null) {
            newInstance.getDocument().setLastId(lastId);
        }
        if (restoreMode) {
            newInstance.getDocument().finishRestoreMode();
        }
        verifyIds(newInstance.getRoot(), new HashSet<>());
        return newInstance;
    }

    private static void removeIDs(@Nonnull SInstance instance) {
        instance.setId(null);
        instance.forEachChild(SFormXMLUtil::removeIDs);
    }

    private static void verifyIds(@Nonnull SInstance instance, @Nonnull Set<Integer> ids) {
        Integer id = instance.getId();
        if (!ids.add(id)) {
            throw new SingularFormException("A instance has a duplicated ID (equals to other instance) id=" + id, instance);
        }
        instance.forEachChild(child -> verifyIds(child, ids));
    }

    @Nullable
    private static Integer fromXMLIntermediary(@Nonnull SInstance instance, @Nullable MElement xml) {
        if (xml == null) {
            return null; // Não precisa fazer nada
        }
        instance.clearInstance();
        Integer idMax = readAttributes(instance, xml);
        if (instance instanceof SISimple) {
            fromXMLSISImple((SISimple<?>) instance, xml);
        } else if (instance instanceof SIComposite) {
            idMax = max(idMax, fromXMLSIComposite((SIComposite) instance, xml));
        } else if (instance instanceof SIList) {
            idMax = max(idMax, fromXMLSIList((SIList<?>) instance, xml));
        } else {
            throw new SingularFormException(
                    "Conversão não implementando para a classe " + instance.getClass().getName(), instance);
        }
        return idMax;
    }

    @Nullable
    private static Integer max(@Nullable Integer v1, @Nullable Integer v2) {
        if (v2 != null && (v1 == null || v2 > v1)) {
            return v2;
        }
        return v1;
    }

    private static void fromXMLSISImple(@Nonnull SISimple<?> instance, @Nullable MElement xml) {
        if (xml != null) {
            STypeSimple<?, ?> type = instance.getType();
            instance.setValue(type.fromStringPersistence(xml.getTextContent()));
        }
    }

    @Nullable
    private static Integer fromXMLSIList(@Nonnull SIList<?> list, @Nullable MElement xml) {
        Integer idMax = null;
        if (xml != null) {
            String childrenName = list.getType().getElementsType().getNameSimple();
            for (MElement xmlChild = xml.getPrimeiroFilho(); xmlChild != null; xmlChild = xmlChild.getProximoIrmao()) {
                if (childrenName.equals(xmlChild.getTagName())) {
                    idMax = max(idMax, fromXMLIntermediary(list.addNew(), xmlChild));
                } else {
                    InternalAccess.INTERNAL.addUnreadInfo(list, xmlChild);
                }
            }
        }
        return idMax;
    }

    @Nullable
    private static Integer fromXMLSIComposite(@Nonnull SIComposite instc, @Nullable MElement xml) {
        if (xml == null) {
            return null;
        }
        Integer idMax = null;
        for (MElement xmlChild = xml.getPrimeiroFilho(); xmlChild != null; xmlChild = xmlChild.getProximoIrmao()) {
            Optional<SInstance> instcField = instc.getFieldOpt(xmlChild.getTagName());
            if (instcField.isPresent()) {
                idMax = max(idMax, fromXMLIntermediary(instcField.get(), xmlChild));
            } else {
                InternalAccess.INTERNAL.addUnreadInfo(instc, xmlChild);
            }
        }
        return idMax;
    }

    @Nullable
    private static Integer readAttributes(SInstance instance, MElement xml) {
        Integer id = null;
        NamedNodeMap attributes = xml.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr at = (Attr) attributes.item(i);
                if (at.getName().equals(ID_ATTRIBUTE)) {
                    id = Integer.valueOf(at.getValue());
                    instance.setId(id);
                } else if (!at.getName().equals(LAST_ID_ATTRIBUTE)) {
                    InternalAccess.INTERNAL.setAttributeValueSavingForLatter(instance, at.getName(), at.getValue());
                }
            }
        }
        return id;
    }

    /**
     * Gera uma string XML representando a instância de forma apropriada para persitência permanente (ex: para
     * armazenamento em banco de dados). Já trata escapes de caracteres especiais dentro dos valores.
     *
     * @return Se a instância não conter nenhum valor, então retorna um resultado null no Optional
     */
    @Nonnull
    public static Optional<String> toStringXML(@Nonnull SInstance instance) {
        return toXML(instance).map(MElement::toStringExato);
    }

    /**
     * Gera uma string XML representando a instância de forma apropriada para persitência permanente (ex: para
     * armazenamento em banco de dados). Já trata escapes de caracteres especiais dentro dos valores.
     *
     * @return Se a instância não conter nenhum valor, então retorna um XML com apenas o nome do tipo da instância.
     */
    @Nonnull
    public static String toStringXMLOrEmptyXML(@Nonnull SInstance instance) {
        return toXMLOrEmptyXML(instance).toStringExato();
    }

    /**
     * Gera um XML representando a instância de forma apropriada para persitência permanente (ex: para armazenamento em
     * banco de dados).
     *
     * @return Se a instância não conter nenhum valor, então retorna um resultado null no Optional
     */
    @Nonnull
    public static Optional<MElement> toXML(@Nonnull SInstance instance) {
        return Optional.ofNullable(createDefaultBuilder().toXML(instance));
    }

    /**
     * Gera uma string XML representando a instância de forma apropriada para persitência permanente (ex: para
     * armazenamento em banco de dados).
     *
     * @return Se a instância não conter nenhum valor, então retorna um XML com apenas o nome do tipo da instância.
     */
    @Nonnull
    public static MElement toXMLOrEmptyXML(@Nonnull SInstance instance) {
        return createDefaultBuilder().withReturnNullXML(false).toXML(instance);
    }

    /**
     * Cria uma configuração default para a geração de XML.
     */
    private static PersistenceBuilderXML createDefaultBuilder() {
        return new PersistenceBuilderXML().withPersistNull(false);
    }

    /**
     * Gera uma string XML representando os dados da instância e o atributos de runtime para persistência temporária
     * (provavelemnte temporariamente durante a tela de edição).
     */
    @Nonnull
    public static MElement toXMLPreservingRuntimeEdition(@Nonnull SInstance instance) {
        return new PersistenceBuilderXML().withPersistNull(true).withPersistAttributes(true).withReturnNullXML(false)
                .toXML(instance);
    }

    private static boolean hasKeepNodePredicatedInAnyChildren(@Nonnull SType<?> type) {
        if (type.as(AtrXML::new).isKeepNodePredicateConfigured()) {
            return true;
        }
        if (type instanceof ICompositeType) {
            for (SType<?> field : ((ICompositeType) type).getContainedTypes()) {
                if (!field.isRecursiveReference() && hasKeepNodePredicatedInAnyChildren(field)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void initSubFieldsIfNeeded(@Nonnull SInstance instance) {
        if (hasKeepNodePredicatedInAnyChildren(instance.getType())) {
            //Forces all sub fields in all sub composites to be created just by walking through then
            SInstances.streamDescendants(instance, true).forEach(SInstance::getId);
        }
    }

    @Nullable
    static MElement toXML(@Nonnull SInstance instance, @Nonnull PersistenceBuilderXML builder) {
        Objects.requireNonNull(instance);
        MDocument xmlDocument;
        if (builder.getParent() == null) {
            xmlDocument = MDocument.newInstance();
        } else {
            xmlDocument = builder.getParent().getMDocument();
        }
        ConfXMLGeneration conf = new ConfXMLGeneration(builder, xmlDocument);

        initSubFieldsIfNeeded(instance);

        MElement xmlResult = toXML(conf, instance);
        if (xmlResult == null) {
            if (builder.isReturnNullXML()) {
                return builder.getParent();
            }
            xmlResult = conf.createMElement(instance);
        }
        if (builder.getParentName() != null) {
            MElement newElement = xmlDocument.createMElement(builder.getParentName());
            newElement.addElement(xmlResult);
            xmlResult = newElement;
        }
        if (builder.getParent() != null) {
            builder.getParent().addElement(xmlResult);
            return builder.getParent();
        }
        xmlDocument.setRoot(xmlResult);
        if (builder.isPersistId()) {
            xmlResult.setAttribute(LAST_ID_ATTRIBUTE, Integer.toString(instance.getDocument().getLastId()));
        }

        return xmlResult;
    }

    @Nullable
    static MElement parseXml(@Nullable String xmlString) {
        try {
            if (xmlString == null || StringUtils.isBlank(xmlString)) {
                return null;
            }
            return MParser.parse(xmlString);
        } catch (Exception e) {
            throw new SingularFormException("Erro lendo xml (parse)", e);
        }
    }

    /**
     * Gera o xml para instance e para seus dados interno.
     */
    @Nullable
    private static MElement toXML(@Nonnull ConfXMLGeneration conf, @Nonnull SInstance instance) {
        MElement newElement = null;
        if (instance instanceof SISimple<?>) {
            SISimple<?> iSimples     = (SISimple<?>) instance;
            String      sPersistence = iSimples.toStringPersistence();
            if (sPersistence != null) {
                newElement = conf.createMElementWithValue(instance, sPersistence);
            } else if (conf.isPersistNull() || instance.as(AtrXML::new).getKeepNodePredicate().test(instance)) {
                newElement = conf.createMElement(instance);
            }
        } else if (instance instanceof ICompositeInstance) {
            if (instance.as(AtrXML::new).getKeepNodePredicate().test(instance)) {
                newElement = conf.createMElement(instance);
            }
            newElement = toXMLChildren(conf, instance, newElement, ((ICompositeInstance) instance).getChildren());
        } else {
            throw new SingularFormException("Instancia da classe " + instance.getClass().getName() + " não suportada",
                    instance);
        }
        //Verifica se há alguma informação lida anteriormente que deva ser grava novamente
        newElement = toXMLOldElementWithoutType(conf, instance, newElement);

        return newElement;
    }


    /**
     * Gera no XML a os elemento filhos (senão existirem).
     */
    @Nullable
    private static MElement toXMLChildren(@Nonnull ConfXMLGeneration conf, @Nonnull SInstance instance,
            @Nullable MElement newElement, @Nonnull List<? extends SInstance> children) {
        MElement result = newElement;
        for (SInstance child : children) {
            MElement xmlChild = toXML(conf, child);
            if (xmlChild != null) {
                if (result == null) {
                    result = conf.createMElement(instance);
                }
                result.appendChild(xmlChild);
            }
        }
        return result;
    }

    /**
     * Escreve para o XML os elemento que foram lidos do XML anterior e foram preservados apesar de não terem um type
     * correspondente. Ou seja, mantêm campo "fantasmas" entre leituras e gravações.
     */
    private static MElement toXMLOldElementWithoutType(ConfXMLGeneration conf, SInstance instance,
                                                       MElement newElement) {
        List<MElement> unreadInfo = InternalAccess.INTERNAL.getUnreadInfo(instance);
        MElement       result     = newElement;
        if (!unreadInfo.isEmpty()) {
            if (result == null) {
                result = conf.createMElement(instance);
            }
            for (MElement extra : unreadInfo) {
                result.copy(extra, null);
            }
        }
        return result;
    }

    private static final class ConfXMLGeneration {

        private final MDocument             xmlDocument;
        private final PersistenceBuilderXML builder;

        ConfXMLGeneration(@Nonnull PersistenceBuilderXML builder, @Nonnull MDocument xmlDocument) {
            this.builder = builder;
            this.xmlDocument = xmlDocument;
        }

        boolean isPersistNull() {
            return builder.isPersistNull();
        }

        @Nonnull
        MElement createMElement(@Nonnull SInstance instance) {
            return complement(instance, xmlDocument.createMElement(instance.getType().getNameSimple()));
        }

        @Nonnull
        MElement createMElementWithValue(@Nonnull SInstance instance, String persistenceValue) {
            return complement(instance, xmlDocument.createMElementWithValue(instance.getType().getNameSimple(), persistenceValue));
        }

        @Nonnull
        private MElement complement(@Nonnull SInstance instance, @Nonnull MElement element) {
            Integer id = instance.getId();
            if (builder.isPersistId()) {
                element.setAttribute(ID_ATTRIBUTE, id.toString());
            }
            if (builder.isPersistAttributes()) {
                for (SInstance atr : instance.getAttributes()) {
                    String name = atr.getAttributeInstanceInfo().getName();
                    if (atr instanceof SISimple) {
                        String sPersistence = ((SISimple<?>) atr).toStringPersistence();
                        element.setAttribute(name, sPersistence);
                    } else {
                        throw new SingularFormException("Não implementada a persitência de atributos compostos: " + name,
                                instance);
                    }
                }
            }
            return element;
        }
    }
}
