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

package org.opensingular.form;

import org.opensingular.form.type.core.SIBoolean;
import org.opensingular.form.type.core.STypeBoolean;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Métodos utilitários para manipulação de SInstance.
 *
 * @author Daniel C. Bordin
 */
public abstract class SInstances {

    private SInstances() {}

    /**
     * Faz um pecorrimento em profundidade de parent e seus filhos.
     */
    public static <I extends SInstance, R> Optional<R> visit(SInstance instance, IVisitor<I, R> visitor) {
        return visit(instance, IVisitFilter.visitAll(), visitor);
    }

    /**
     * Faz um pecorrimento em profundidade dos filhos de parent.
     */
    public static <R> Optional<R> visitChildren(SInstance parent, IVisitor<SInstance, R> visitor) {
        return visitChildren(parent, IVisitFilter.visitAll(), visitor);
    }

    /**
     * Faz um pecorrimento em profundidade da instância e seus filhos, em ordem pós-fixada (primeiro filhos, depois pais).
     */
    public static <R> Optional<R> visitPostOrder(SInstance parent, IVisitor<SInstance, R> visitor) {
        return visitPostOrder(parent, IVisitFilter.visitAll(), visitor);
    }

    /**
     * Faz um pecorrimento em profundidade de parent e seus filhos.
     */
    @SuppressWarnings("unchecked")
    public static <I extends SInstance, R> Optional<R> visit(SInstance rootInstance, IVisitFilter filter, IVisitor<I, R> visitor) {
        final Visit<R> visit = new Visit<>(null);
        if (filter.visitObject(rootInstance)) {
            visitor.onInstance((I) rootInstance, visit);
            if (visit.dontGoDeeper || visit.stopped)
                return Optional.ofNullable(visit.result);

        }
        internalVisitChildren(rootInstance, visitor, filter, visit);
        return Optional.ofNullable(visit.result);
    }

    /**
     * Faz um pecorrimento em profundidade dos filhos de parent.
     */
    public static <R> Optional<R> visitChildren(SInstance rootInstance, IVisitFilter filter, IVisitor<SInstance, R> visitor) {
        Visit<R> visit = new Visit<>(null);
        internalVisitChildren(rootInstance, visitor, filter, visit);
        return Optional.ofNullable(visit.result);
    }

    /**
     * Faz um pecorrimento em profundidade da instância e seus filhos, em ordem pós-fixada (primeiro filhos, depois pais).
     */
    public static <R> Optional<R> visitPostOrder(SInstance rootInstance, IVisitFilter filter, IVisitor<SInstance, R> visitor) {
        Visit<R> visit = new Visit<>(null);
        internalVisitPostOrder(rootInstance, visitor, filter, visit);
        return Optional.ofNullable(visit.result);
    }

    /**
     * Implements the prefixed traversal logic.
     */
    @SuppressWarnings("unchecked")
    private static <I extends SInstance, R> void internalVisitChildren(SInstance rootInstance, IVisitor<I, R> visitor, IVisitFilter filter, Visit<R> visit) {
        if (!(rootInstance instanceof ICompositeInstance)) {
            return;
        }
        for (SInstance object : ((ICompositeInstance) rootInstance).getAllChildren()) {
            if (filter.visitObject(object)) {
                I child = (I) object;
                final Visit<R> childVisit = new Visit<>(visit.getPartial());
                visitor.onInstance(child, childVisit);
                visit.setPartial(childVisit.getPartial());

                if (childVisit.stopped) {
                    visit.stop(childVisit.result);
                    return;
                } else if (childVisit.dontGoDeeper) {
                    continue;
                }
            }

            if (!visit.dontGoDeeper && (object instanceof ICompositeInstance) && filter.visitChildren(object)) {
                internalVisitChildren(object, visitor, filter, visit);
                if (visit.stopped) {
                    return;
                }
            }
        }
    }

    /**
     * Implements the postfixed traversal logic.
     */
    @SuppressWarnings("unchecked")
    private static <I extends SInstance, R> void internalVisitPostOrder(SInstance rootInstance, IVisitor<I, R> visitor, IVisitFilter filter, Visit<R> visit) {
        boolean dontGoAbove = false;
        if ((rootInstance instanceof ICompositeInstance) && filter.visitChildren(rootInstance)) {
            final ICompositeInstance parent = (ICompositeInstance) rootInstance;
            final Visit<R> childVisit = new Visit<>(visit.getPartial());
            for (SInstance child : parent.getAllChildren()) {
                if (!filter.visitObject(child)) {
                    continue;
                }
                internalVisitPostOrder(child, visitor, filter, childVisit);
                visit.setPartial(childVisit.getPartial());
                if (childVisit.dontGoDeeper)
                    dontGoAbove = true;
                if (childVisit.stopped) {
                    visit.stop(childVisit.result);
                    return;
                }
            }
        }

        if (!dontGoAbove && filter.visitObject(rootInstance))
            visitor.onInstance((I) rootInstance, visit);
    }

    /**
     * Busca por um ancestral de <code>node</code> do tipo especificado.
     *
     * @param node         instância inicial da busca
     * @param ancestorType tipo do ancestral
     * @return instância do ancestral do tipo especificado
     * @throws SingularFormException se não encontrar nenhum ancestral deste tipo
     */
    @Nonnull
    public static <P extends SInstance & ICompositeInstance> P getAncestor(SInstance node, SType<P> ancestorType) {
        return findAncestor(node, ancestorType).orElseThrow(
            () -> new SingularFormException("Não foi encontrado " + ancestorType + " em " + node, node));
    }

    @Nonnull
    public static <A extends SType<?>> Optional<SInstance> findAncestor(SInstance node, Class<A> ancestorType) {
        for (SInstance parent = node.getParent(); parent != null; parent = parent.getParent()) {
            if (parent.getType().getClass().equals(ancestorType)) {
                return Optional.of(parent);
            }
        }
        return Optional.empty();
    }

    /**
     * Busca por um ancestral de <code>node</code> do tipo especificado.
     *
     * @param node         instância inicial da busca
     * @param ancestorType tipo do ancestral
     * @return Optional da instância do ancestral do tipo especificado
     */
    @SuppressWarnings("unchecked")
    public static <A extends SInstance & ICompositeInstance> Optional<A> findAncestor(SInstance node, SType<A> ancestorType) {
        for (SInstance parent = node.getParent(); parent != null; parent = parent.getParent()) {
            if (parent.isTypeOf(ancestorType)) {
                return Optional.of((A) parent);
            }
        }
        return Optional.empty();
    }

    /**
     * Busca por um ancestral de <code>node</code> cujo tipo é um ancestral comum do tipo de <code>node</code>
     * e <code>targetType</code>.
     *
     * @param node       instância inicial da busca
     * @param targetType tipo de outro campo
     * @return Optional da instância do ancestral comum
     */
    @SuppressWarnings("unchecked")
    public static <CA extends SInstance & ICompositeInstance> Optional<CA> findCommonAncestor(SInstance node, SType<?> targetType) {
        for (SScope type = targetType; type != null; type = type.getParentScope()) {
            for (SInstance ancestor = node; ancestor != null; ancestor = ancestor.getParent()) {
                if (SType.class.isAssignableFrom(type.getClass()) && ancestor.isTypeOf((SType<?>) type) && ancestor instanceof ICompositeInstance) {
                    return Optional.of((CA) ancestor);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Busca por um ancestral de <code>node</code> cujo tipo é um ancestral comum do tipo de <code>node</code>
     * e <code>targetTypeClass</code>.
     *
     * @param node            instância inicial da busca
     * @param targetTypeClass Classe que define um  tipo de outro campo
     * @return Optional da instância do ancestral comum
     */
    @SuppressWarnings("unchecked")
    public static <CA extends SInstance & ICompositeInstance> Optional<CA> findCommonAncestorByStypeClass(SInstance node, Class<? extends SType<?>> targetTypeClass) {
        for (SInstance ancestor = node; ancestor != null; ancestor = ancestor.getParent()) {
            if (targetTypeClass.isAssignableFrom(ancestor.getType().getClass()) && ancestor instanceof ICompositeInstance) {
                return Optional.of((CA) ancestor);
            }
        }
        return Optional.empty();
    }

    /**
     * Busca por o no mais próximo de <code>node</code> na hierarquia de instâncias, cujo tipo é igual a <code>targetType</code>.
     *
     * @param node       instância inicial da busca
     * @param targetType tipo do campo a ser procurado
     * @return Optional da instância do targetType encontrado
     */
    public static <A extends SInstance> Optional<A> findNearest(SInstance node, SType<A> targetType) {
        Optional<A> desc = SInstances.findDescendant(node, targetType);
        if (desc.isPresent())
            return desc;
        else
            return SInstances.findCommonAncestor(node, targetType)
                .flatMap(ancestor -> ancestor.findDescendant(targetType))
                .map(targetNode -> targetNode);
    }

    /**
     * Busca por o no mais próximo de <code>node</code> na hierarquia de instâncias, cujo tipo é definido pela classe  <code>targetTypeClass</code>.
     *
     * @param node            instância inicial da busca
     * @param targetTypeClass Classe que define o  tipo do campo a ser procurado
     * @return Optional da instância do targetType encontrado
     */
    public static <A extends SInstance> Optional<A> findNearest(SInstance node, Class<? extends SType<A>> targetTypeClass) {
        return findNearest(null, node, targetTypeClass);
    }

    @SuppressWarnings("unchecked")
    public static <A extends SInstance> Optional<A> findNearest(SInstance children, SInstance node, Class<? extends SType<A>> targetTypeClass) {
        Optional<A> desc = (Optional<A>) SInstances.streamDescendants(node, true)
            .filter(sInstance -> sInstance != children)
            .filter(sInstance -> targetTypeClass.isAssignableFrom(sInstance.getType().getClass()))
            .findFirst();
        if (desc.isPresent()) {
            return desc;
        } else if (node.getParent() != null) {
            return findNearest(node, node.getParent(), targetTypeClass);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Lista os ancestrais de <code>node</code>.
     *
     * @param instance instância inicial da busca
     * @return Lista das instâncias de ancestrais do tipo especificado
     */
    public static List<SInstance> listAscendants(SInstance instance) {
        return listAscendants(instance, null);
    }

    public static List<SInstance> listAscendants(SInstance instance, boolean selfIncluded) {
        return listAscendants(instance, null, selfIncluded);
    }

    /**
     * Lista os ancestrais de <code>node</code>.
     *
     * @param instance instância inicial da busca
     * @return Lista das instâncias de ancestrais do tipo especificado
     */
    public static List<SInstance> listAscendants(SInstance instance, SType<?> limitInclusive) {
        return listAscendants(instance, limitInclusive, false);
    }

    /**
     * Lista os ancestrais de <code>node</code>.
     *
     * @param instance instância inicial da busca
     * @return Lista das instâncias de ancestrais do tipo especificado
     */
    public static List<SInstance> listAscendants(SInstance instance, SType<?> limitInclusive, boolean selfIncluded) {
        List<SInstance> list = new ArrayList<>();
        if (selfIncluded) {
            list.add(instance);
        }
        SInstance node = instance.getParent();
        while (node != null && (limitInclusive == null || !node.isTypeOf(limitInclusive))) {
            list.add(node);
            node = node.getParent();
        }
        return list;
    }

    /**
     * Busca pelo primeiro descendente de <code>node</code> do tipo especificado.
     *
     * @param node           instância inicial da busca
     * @param descendantType tipo do descendente
     * @return instância do primeiro descendente do tipo especificado
     * @throws NoSuchElementException se não encontrar nenhum descendente deste tipo
     */
    public static <D extends SInstance> D getDescendant(SInstance node, SType<D> descendantType) {
        return findDescendant(node, descendantType).orElseThrow(
            () -> new SingularFormException("Não foi encontrado " + descendantType + " em " + node, node));
    }

    /**
     * Busca pelo primeiro descendente de <code>node</code> do tipo especificado.
     *
     * @param instance       instância inicial da busca
     * @param descendantType tipo do descendente
     * @return Optional da instância do primeiro descendente do tipo especificado
     */
    @SuppressWarnings("unchecked")
    public static <D extends SInstance> Optional<D> findDescendant(SInstance instance, SType<D> descendantType) {
        final Deque<SInstance> deque = new ArrayDeque<>();
        deque.add(instance);
        while (!deque.isEmpty()) {
            final SInstance node = deque.removeFirst();
            if (node.isTypeOf(descendantType)) {
                return Optional.of((D) node);
            } else {
                addAllChildren(deque, node);
            }
        }
        return Optional.empty();
    }

    /**
     * Lista os descendentes de <code>node</code> do tipo especificado.
     *
     * @param instance       instância inicial da busca
     * @param descendantType tipo do descendente
     * @return Lista das instâncias de descendentes do tipo especificado
     */
    public static <D extends SInstance> List<D> listDescendants(SInstance instance, SType<D> descendantType) {
        return listDescendants(instance, descendantType, Function.identity());
    }

    /**
     * Lista os descendentes de <code>node</code> do tipo especificado.
     *
     * @param instance       instância inicial da busca
     * @param descendantType tipo do descendente
     * @return Lista das instâncias de descendentes do tipo especificado
     */
    @SuppressWarnings("unchecked")
    public static <D extends SInstance, V> List<V> listDescendants(SInstance instance, SType<?> descendantType, Function<D, V> function) {
        List<V> result = new ArrayList<>();
        final Deque<SInstance> deque = new ArrayDeque<>();
        deque.add(instance);
        while (!deque.isEmpty()) {
            final SInstance node = deque.removeFirst();
            if (node.isTypeOf(descendantType)) {
                result.add(function.apply((D) node));
            } else {
                addAllChildren(deque, node);
            }
        }
        return result;
    }

    /**
     * Lista os descendentes de <code>node</code> do tipo especificado.
     *
     * @param instance       instância inicial da busca
     * @param descendantClass classe do descendente
     * @return Lista das instâncias de descendentes do tipo especificado
     */
    public static <D extends SInstance> List<D> listDescendants(SInstance instance, Class<? extends SType<D>> descendantClass) {
        return listDescendants(instance, descendantClass, Function.identity());
    }

    /**
     * Lista os descendentes de <code>node</code> do tipo especificado.
     *
     * @param instance       instância inicial da busca
     * @param descendantClass classe do descendente
     * @return Lista das instâncias de descendentes do tipo especificado
     */
    @SuppressWarnings("unchecked")
    public static <D extends SInstance, V> List<V> listDescendants(SInstance instance, Class<? extends SType<D>> descendantClass, Function<D, V> function) {
        List<V> result = new ArrayList<>();
        final Deque<SInstance> deque = new ArrayDeque<>();
        deque.add(instance);
        while (!deque.isEmpty()) {
            final SInstance node = deque.removeFirst();
            if (descendantClass.isAssignableFrom(node.getType().getClass())) {
                result.add(function.apply((D) node));
            } else {
                addAllChildren(deque, node);
            }
        }
        return result;
    }

    /**
     * Retorna uma Stream que percorre os descendentes de <code>node</code> do tipo especificado.
     *
     * @param root           instância inicial da busca
     * @param descendantType tipo do descendente
     * @return Stream das instâncias de descendentes do tipo especificado
     */
    @SuppressWarnings("unchecked")
    public static <D extends SInstance> Stream<D> streamDescendants(SInstance root, boolean includeRoot, SType<D> descendantType) {
        return streamDescendants(root, includeRoot)
            .filter(it -> it.isTypeOf(descendantType))
            .map(it -> (D) it);
    }

    /**
     * Retorna uma Stream que percorre os descendentes de <code>node</code> do tipo especificado.
     *
     * @param root instância inicial da busca
     * @return Stream das instâncias de descendentes
     */
    public static Stream<SInstance> streamDescendants(SInstance root, boolean includeRoot) {
        return StreamSupport.stream(new SInstanceRecursiveSpliterator(root, includeRoot), false);
    }

    /**
     * Verifica se a instância ou algum filho atende a condição informada ou não.
     */
    public static boolean hasAny(SInstance instance, Predicate<SInstance> predicate) {
        return hasAny(instance, true, predicate);
    }

    /**
     * Verifica se a instância ou algum filho atende a condição informada ou não.
     *
     * @param checkRoot Indica se a condição deve ser verificada ao nó raiz informado ou somente a partir dos filhos.
     */
    private static boolean hasAny(SInstance instance, boolean checkRoot, Predicate<SInstance> predicate) {
        if (checkRoot && predicate.test(instance)) {
            return true;
        } else if (instance instanceof ICompositeInstance) {
            for (SInstance si : ((ICompositeInstance) instance).getAllChildren()) {
                if (hasAny(si, true, predicate)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * Lista os filhos diretos da instância <code>node</code>, criando-os se necessário.
     */
    static void addAllChildren(Deque<SInstance> deque, SInstance node) {
        if (node instanceof ICompositeInstance) {
            deque.addAll(((ICompositeInstance) node).getAllChildren());
        }
    }

    public static void updateBooleanAttribute(
        SInstance instance,
        AtrRef<STypeBoolean, SIBoolean, Boolean> valueAttribute,
        AtrRef<STypePredicate, SIPredicate, Predicate<SInstance>> predicateAttribute) {

        Predicate<SInstance> pred = instance.getAttributeValue(predicateAttribute);
        if (pred != null)
            instance.setAttributeValue(valueAttribute, pred.test(instance));
    }

    public static <V> V attributeValue(SInstance instance, AtrRef<?, ?, V> attribute, V defaultValue) {
        V value = instance.getAttributeValue(attribute);
        return (value != null) ? value : defaultValue;
    }

    public static <V> boolean hasAttributeValue(SInstance instance, AtrRef<?, ?, V> attribute) {
        V value = instance.getAttributeValue(attribute);
        return (value != null);
    }

    public static interface IVisit<R> {
        void stop();

        void stop(R result);

        void dontGoDeeper();

        R getPartial();

        void setPartial(R result);
    }

    @FunctionalInterface
    public interface IVisitor<I extends SInstance, R> {
        public void onInstance(I object, IVisit<R> visit);
    }

    public interface IVisitFilter extends Serializable {
        public static IVisitFilter visitAll() {
            return o -> true;
        }

        boolean visitObject(Object object);

        default boolean visitChildren(Object object) {
            return true;
        }
    }

    public static class TypeVisitFilter implements SInstances.IVisitFilter {

        private Class<? extends SType> type;

        public TypeVisitFilter(Class<? extends SType> type) {
            this.type = type;
        }

        @Override
        public boolean visitObject(Object object) {
            if (object instanceof SInstance) {
                SInstance ins = (SInstance) object;
                if (type.isAssignableFrom(ins.getType().getClass())) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean visitChildren(Object object) {
            if (object instanceof SInstance) {
                SInstance ins = (SInstance) object;
                if (type.isAssignableFrom(ins.getType().getClass())) {
                    return false;
                }
            }

            return true;
        }
    }

    private static class Visit<R> implements IVisit<R> {
        boolean dontGoDeeper;
        boolean stopped;
        R       result;
        R       partial;

        public Visit(R partial) {
            this.partial = partial;
        }

        @Override
        public void dontGoDeeper() {
            this.dontGoDeeper = true;
        }

        @Override
        public void stop() {
            this.stopped = true;
        }

        @Override
        public void stop(R result) {
            this.result = result;
            stop();
        }

        @Override
        public R getPartial() {
            return partial;
        }

        @Override
        public void setPartial(R result) {
            partial = result;
        }
    }
}
