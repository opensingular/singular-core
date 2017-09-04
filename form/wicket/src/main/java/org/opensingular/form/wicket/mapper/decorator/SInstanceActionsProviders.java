package org.opensingular.form.wicket.mapper.decorator;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.wicket.model.IModel;
import org.opensingular.form.SInstance;
import org.opensingular.form.decorator.action.ISInstanceActionCapable;
import org.opensingular.form.decorator.action.ISInstanceActionsProvider;
import org.opensingular.form.decorator.action.SInstanceAction;
import org.opensingular.lib.commons.lambda.IPredicate;

import com.google.common.collect.Lists;


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
    public List<SInstanceAction> actionList(final IModel<? extends SInstance> model) {
        return actionStream(model)
            .collect(toList());
    }

    /**
     * Retorna uma lista filtrada e ordenada de ações correspondentes à instância do model
     */
    public List<SInstanceAction> actionList(final IModel<? extends SInstance> model, IPredicate<SInstanceAction> filter) {
        return actionStream(model)
            .filter(filter)
            .collect(toList());
    }

    private Stream<SInstanceAction> actionStream(final IModel<? extends SInstance> model) {
        if (this.entries == null)
            return Stream.empty();

        return this.entries.stream()
            .map(it -> it.provider)
            .flatMap(it -> Lists.newArrayList(it.getActions(owner, model.getObject())).stream());
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
