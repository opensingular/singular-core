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

package org.opensingular.form.type.core.annotation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SIList;
import org.opensingular.form.SInstance;
import org.opensingular.form.SInstances;
import org.opensingular.form.SingularFormException;
import org.opensingular.form.document.RefSDocumentFactory;
import org.opensingular.form.document.RefType;
import org.opensingular.form.document.SDocument;
import org.opensingular.form.document.SDocumentFactory;
import org.opensingular.form.type.basic.SPackageBasic;
import org.opensingular.form.util.transformer.Value;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Gerencia as anotações associadas a um {@link SDocument}.
 *
 * @author Daniel Bordin on 24/10/2016.
 */
public class DocumentAnnotations {

    private final SDocument document;

    private SIList<SIAnnotation> annotations;

    // Cache para agilizar a localização de anotações
    // Em algumas situações, essa lista pode não conter todas as anotações (no caso de anotações orfãs)
    private ListMultimap<Integer, SIAnnotation> annotationsMap;

    public DocumentAnnotations(SDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    /** Verifica se o documento possui alguma anotação. */
    public boolean hasAnnotations() {
        return annotations != null && !annotations.isEmpty();
    }

    private SIAnnotation newAnnotation() {
        if (annotations == null) {
            annotations = newAnnotationList(document);
            annotationsMap = ArrayListMultimap.create();
        }
        return annotations.addNew();
    }

    final SIAnnotation getAnnotationOrCreate(SInstance instance) {
        return getAnnotationOrCreate(instance, (String) null);
    }

    final <T extends AnnotationClassifier> SIAnnotation getAnnotationOrCreate(SInstance instance,
            T classifier) {
        return getAnnotationOrCreate(instance, classifier.name());
    }

    private SIAnnotation getAnnotationOrCreate(SInstance instance, String classifier) {
        if (classifier == null) {
           return getAnnotationOrCreate(instance, AtrAnnotation.DefaultAnnotationClassifier.DEFAULT_ANNOTATION.name());
        } else {
            SIAnnotation annotation = getAnnotation(instance, classifier);
            if (annotation == null) {
                isValidClassifier(instance, classifier);
                annotation = newAnnotation();
                annotation.setTarget(instance);
                annotation.setClassifier(classifier);
                annotationsMap.put(instance.getId(), annotation);
            }
            return annotation;
        }
    }

    /** Verifice se instancia recebe o classificador de anotação informado. */
    private void isValidClassifier(SInstance instance, @Nonnull String classifier) {
        if (AtrAnnotation.DefaultAnnotationClassifier.DEFAULT_ANNOTATION.name().equals(classifier)) {
            return;
        }
        List<String> classifiers = instance.getAttributeValue(SPackageBasic.ATR_ANNOTATED);
        if (classifiers instanceof ArrayList && !classifiers.contains(classifier)) {
            throw new SingularFormException(String.format(
                    "Classificador de anotação desconhecido para o tipo: %s. Certifique-se que a tipo foi marcado" +
                            " como setAnnotated(%s) ",
                    instance.getType().getName(), classifier), instance);
        }
    }


    private static SIList<SIAnnotation> newAnnotationList(@Nonnull SDocument docRef) {
        return  newAnnotationList(docRef, true);
    }

    /**
     * USO INTERNO: Cria um nova lista de anotações em um novo SDocument, tendo como base as informações de definição
     * do documento informado.
     * Cria uma nova lista de anotações utilizando as configurações de registry do document passado por
     * parâmetro. Essa método tem por objetivo evitar que a nova lista criada fique sem os serviços locais e service
     * registry já configurados na lista original.
     * @param executeInitTypeSetup Se true, dispara as inicializações automáticas implementadas em
     * {@link SInstance#init()}. Usar como false quando a instância está sendo recuperada da persistência. Ver
     * {@link SDocumentFactory#createInstance(RefType, boolean)}.
     */
    public static SIList<SIAnnotation> newAnnotationList(@Nonnull SDocument docRef, boolean executeInitTypeSetup) {
        Optional<RefType> rootRefType = docRef.getRootRefType();
        if (rootRefType.isPresent()) {
            RefType refTypeAnnotation = rootRefType.get().createSubReference(STypeAnnotationList.class);
            SDocumentFactory documentFactory;
            RefSDocumentFactory refSDocumentFactory = docRef.getDocumentFactoryRef();
            if (refSDocumentFactory != null) {
                documentFactory = refSDocumentFactory.get();
            } else {
                documentFactory = SDocumentFactory.empty();
            }
            return (SIList<SIAnnotation>) documentFactory.createInstance(refTypeAnnotation, executeInitTypeSetup);
        }
        return (SIList) docRef.getRoot().getDictionary().newInstance(STypeAnnotationList.class);
    }

    /** Localiza a anotação com o classificado solicitado na instancia informatada. Ou retorna null. */
    private SIAnnotation getAnnotation(SInstance instance, String classifier) {
        if (annotationsMap != null) {
            for (SIAnnotation a : annotationsMap.get(instance.getId())) {
                if (classifier.equals(a.getClassifier())) {
                    return a;
                }
            }
        }
        return null;
    }

    private List<SIAnnotation> getAnnotations(SInstance instance) {
        if (annotationsMap != null && ! annotationsMap.isEmpty()) {
            return annotationsMap.get(instance.getId());
        }
        return Collections.emptyList();
    }

    public SIList<SIAnnotation> getAnnotations() {
        return annotations;
    }

    final List<SIAnnotation> getAnnotationsAsList() {
        return annotations == null ? Collections.emptyList() : annotations.getValues();
    }

    /**
     * Limpa todas a anotações no documento atual
     */
    public void clear() {
        if (annotations != null) {
            annotations.clearInstance();
            annotationsMap.clear();
        }
    }

    /**
     * Loads a collection of getAnnotations onte this instance and its children. The <code>targetId</code> field of the
     * getAnnotation denotes which field that getAnnotation is referring to. Se a intenção é recarregar as anotações é
     * preciso chamar o método {@link #clear()} antes
     *
     * @param annotations to be loaded into the instance.
     */
    public void loadAnnotations(SIList<SIAnnotation> annotations) {
        Map<Integer, SInstance> instancesById = new HashMap<>();
        SInstances.streamDescendants(document.getRoot(), true).forEach(i -> instancesById.put(i.getId(), i));

        for (SIAnnotation annotation : annotations) {
            SIAnnotation newAnnotation = newAnnotation();
            Value.copyValues(annotation, newAnnotation);
            correctReference(newAnnotation, document, instancesById);
        }
    }

    /**
     * Tenta recriar a instancia de acordo com o path original caso a anotação aponte para uma instância que não exista.
     */
    private void correctReference(SIAnnotation newAnnotation, SDocument document,
            Map<Integer, SInstance> instancesById) {
        Integer targetId = newAnnotation.getTargetId();
        if (instancesById.containsKey(targetId)) {
            annotationsMap.put(targetId, newAnnotation);
        } else if (newAnnotation.getTargetPath() != null) {
            try {
                String[] path = StringUtils.split(newAnnotation.getTargetPath(), '/');
                Optional<SInstance> instance = findByXPathAndId(document, instancesById, path, path.length - 1);
                if (instance.isPresent()) {
                    newAnnotation.setTarget(instance.get());
                    annotationsMap.put(instance.get().getId(), newAnnotation);
                }
            } catch (Exception e) {
                throw new SingularFormException("Erro lendo path da anotação: " + newAnnotation.getTargetPath(), e);
            }
        }
    }

    /**
     * Tentar localizar ou recriar a instancia com base no path para o qual a instância apontava. Pode ser que a
     * instância não foi salva pois o conteudo estava vazio, mas a anotação continua sendo valida assim mesmo. Espera
     * trabalhar em cima de um path no formato "order[@id=1]/address[@id=4]/street[@id=5]".
     *
     * @return Optional<SInstance>
     */
    private Optional<SInstance> findByXPathAndId(SDocument document, Map<Integer, SInstance> instancesById, String[] path,
            int index) {
        int pos = path[index].lastIndexOf('=');
        if (pos <= 0) {
            throw new SingularFormException("Trecho path inválido: '" + path[index] + "'");
        }
        Integer id = Integer.valueOf(path[index].substring(pos+1,path[index].length()-1));
        SInstance instance = instancesById.get(id);
        if (instance != null) {
            return Optional.of(instance);
        }
        pos = path[index].indexOf('[');
        if (pos <= 0) {
            throw new SingularFormException("Trecho path inválido: '" + path[index] + "'");
        }
        String name = path[index].substring(0, pos);

        if (index == 0) {
            if (document.getRoot().getName().equals(name)) {
                return Optional.ofNullable(document.getRoot());
            }
        } else {
            Optional<SInstance> parent = findByXPathAndId(document, instancesById, path, index - 1);
            if (parent.isPresent() && parent.get() instanceof SIComposite) {
                return parent.get().getFieldOpt(name);
            }
        }
        return Optional.empty();
    }

    /**
     * Copia para o documento atual todas as anotação do documento fonte que tiverem um caminho corresponde no documento
     * atual.
     */
    public void copyAnnotationsFrom(SDocument source) {
        Objects.requireNonNull(source);
        DocumentAnnotations sourceAnnotations = source.getDocumentAnnotations();
        if (sourceAnnotations.hasAnnotations()) {
            for (SIAnnotation sourceAnnotation : sourceAnnotations.annotations) {
                source.findInstanceById(sourceAnnotation.getTargetId()).ifPresent(si -> {
                    String pathFromRoot = si.getPathFromRoot();
                    //localiza a instancia correspondente no formulario destino
                    SInstance targetInstance = document.getRoot();
                    if (pathFromRoot != null){
                        targetInstance = document.getRoot().getFieldOpt(pathFromRoot).orElse(targetInstance);
                    }
                    //Copiando todos os valores da anotação (inclusive o id na sinstance antiga)
                    SIAnnotation targetAnnotation = getAnnotationOrCreate(targetInstance, sourceAnnotation.getClassifier());
                    Value.copyValues(sourceAnnotation, targetAnnotation);
                    //Corrigindo o ID
                    targetAnnotation.setTarget(targetInstance);
                });
            }
        }
    }


    /**
     * @return A ready to persist object containing all getAnnotations from this instance and its children.
     */
    public SIList<SIAnnotation> persistentAnnotationsClassified(String classifier) {
        return persistentAnnotationsClassified().get(classifier);
    }

    /**
     * @return A ready to persist object containing all getAnnotations from this instance and its children mapped by its
     * classifier
     */
    @SuppressWarnings("unchecked")
    public Map<String, SIList<SIAnnotation>> persistentAnnotationsClassified() {
        Map<String, SIList<SIAnnotation>> classifiedAnnotations = new HashMap<>();
        if (annotations != null) {
            for(SIAnnotation annotation : annotations) {
                SIList<SIAnnotation> list = classifiedAnnotations.get(annotation.getClassifier());
                if (list == null) {
                    list = newAnnotationList(annotations.getDocument());
                    classifiedAnnotations.put(annotation.getClassifier(), list);
                }
                list.addNew(a -> Value.hydrate(a, Value.dehydrate(annotation)));
            }
        }
        return classifiedAnnotations;
    }

    /**
     * @return True if this SIinstance is an annotated type and if the anotation has any value.
     */
    public boolean hasAnnotation(SInstance instance, AnnotationClassifier annotationClassifier) {
        if (annotationsMap != null && ! annotationsMap.isEmpty()) {
            for( SIAnnotation si : annotationsMap.get(instance.getId())) {
                if (annotationClassifier.name().equals(si.getClassifier())
                        && (StringUtils.isNotBlank(si.getText()) || si.getApproved() != null)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retorna true se a instância ou algum de seus filhos tiver alguma anotação preenchida (não em branco).
     */
    public boolean hasAnyAnnotationsOnTree(SInstance instance, AnnotationClassifier annotationClassifier) {
        return SInstances.hasAny(instance, i -> hasAnnotation(i, annotationClassifier));
    }

    /**
     * Returns true if the given instance or any of its children is annotated with an refusal
     * @param instance
     * @return
     */
    public boolean hasAnyRefusalOnTree(SInstance instance, AnnotationClassifier annotationClassifier) {
        return SInstances.hasAny(instance, i -> hasAnnotation(i, annotationClassifier) && BooleanUtils.isFalse(i.asAtrAnnotation().annotation(annotationClassifier).getApproved()));
    }

    /** Retorna true se a instância ou algum de seus filhos tiver uma anotação marcadada como não aprovada. */
    public boolean hasAnyRefusal(SInstance instance, AnnotationClassifier annotationClassifier) {
        return SInstances.hasAny(instance, i -> hasAnnotation(i, annotationClassifier) && BooleanUtils.isFalse(i.asAtrAnnotation().annotation(annotationClassifier).getApproved()));
    }

    /**
     * Retorna true sea instância ou algums de seus filhos é capaz de receber anotação. Ou seja, se foi marcado como
     * anotável.
     */
    public boolean hasAnyAnnotable(SInstance instance, AnnotationClassifier classifier) {
        return SInstances.hasAny(instance, i -> i.asAtrAnnotation().isAnnotated(classifier));
    }
}
