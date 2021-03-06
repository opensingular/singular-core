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

package org.opensingular.form.wicket.mapper.decorator;

import com.google.common.collect.Lists;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SIList;
import org.opensingular.form.SInstance;
import org.opensingular.form.decorator.action.ActionClassifier;
import org.opensingular.form.decorator.action.ISInstanceActionCapable;
import org.opensingular.form.decorator.action.ISInstanceActionsProvider;
import org.opensingular.form.decorator.action.SInstanceAction;
import org.opensingular.lib.commons.lambda.IPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;


/**
 * Classe utilitária para simplificar o gerenciamento de instâncias de ISInstanceActionsProvider.
 */
public class SInstanceActionsProviders implements Serializable {

    private List<Entry>             entries;
    private ISInstanceActionCapable owner;

    /**
     * Construtor.
     * @param owner instância de ISInstanceActionCapable a ser passado aos providers
     */
    public SInstanceActionsProviders(ISInstanceActionCapable owner) {
        this.owner = owner;
    }
    
    /**
     * Adiciona um provider.
     */
    public void addSInstanceActionsProvider(int sortPosition, ISInstanceActionsProvider provider) {
        if (this.entries == null)
            this.entries = new ArrayList<>();
        this.entries.add(new Entry(sortPosition, provider));
        this.entries.sort(comparingInt(it -> it.position));
    }

    /**
     * Retorna uma lista ordenada de ações correspondentes à instância do model
     */
    public List<SInstanceAction> actionList(final IModel<? extends SInstance> model, ActionClassifier actionClassifier) {
        return actionStream(model, actionClassifier)
            .collect(toList());
    }

    /**
     * Retorna uma lista filtrada e ordenada de ações correspondentes à instância do model
     */
    public List<SInstanceAction> actionList(final IModel<? extends SInstance> model, IPredicate<SInstanceAction> filter, ActionClassifier actionClassifier) {
        return actionStream(model, actionClassifier)
            .filter(filter)
            .collect(toList());
    }

    private Stream<SInstanceAction> actionStream(final IModel<? extends SInstance> model, ActionClassifier actionClassifier) {
        if (this.entries == null)
            return Stream.empty();

        return this.entries.stream()
            .map(it -> it.provider)
            .flatMap(it -> Lists.newArrayList(it.getActions(owner, model.getObject(), actionClassifier)).stream());
    }
    
    /**
     * Retorna uma lista filtrada e ordenada de ações correspondentes à instância do model
     */
    public List<SInstanceAction> listFieldActionList(final IModel<? extends SIList<?>> model, IPredicate<SInstanceAction> filter, String field, ActionClassifier actionClassifier) {
        return listFieldActionStream(model, field, actionClassifier)
            .filter(filter)
            .collect(toList());
    }
    
    private Stream<SInstanceAction> listFieldActionStream(final IModel<? extends SIList<?>> model, String field, ActionClassifier actionClassifier) {
        if (this.entries == null)
            return Stream.empty();
        
        return this.entries.stream()
            .map(it -> it.provider)
            .flatMap(it -> Lists.newArrayList(it.getListFieldActions(owner, model.getObject(), field, actionClassifier)).stream());
    }

    private static final class Entry implements Serializable {
        public final int                       position;
        public final ISInstanceActionsProvider provider;
        public Entry(int position, ISInstanceActionsProvider provider) {
            this.position = position;
            this.provider = provider;
        }
    }
}
