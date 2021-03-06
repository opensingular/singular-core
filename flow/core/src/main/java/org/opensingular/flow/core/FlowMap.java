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

package org.opensingular.flow.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import org.opensingular.flow.core.property.MetaDataKey;
import org.opensingular.flow.core.variable.VarService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Esta classe representa o mapa de atividades e transições de uma dada definição de fluxo.
 *
 * @author Daniel Bordin
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class FlowMap {

    private final FlowDefinition<?> flowDefinition;

    private final Map<String, STask<?>> tasksByName = new HashMap<>();

    private final Map<String, STask<?>> tasksByAbbreviation = new HashMap<>();

    private final Map<String, STaskEnd> endTasks = new HashMap<>();

    private final Map<String, SBusinessRole> rolesByAbbreviation = new HashMap<>();

    private final List<SStart> starts = new ArrayList<>();

    private IRoleChangeListener roleChangeListener;

    private final Map<String, DashboardView> dashboardViews = new LinkedHashMap<>();

    private DisplayInfoFlow displayInfo;

    /**
     * Instancia um novo mapa para a definição de fluxo especificado.
     */
    public FlowMap(@Nonnull FlowDefinition<?> flowDefinition) {
        this.flowDefinition = Objects.requireNonNull(flowDefinition);
    }

    /**
     * Ponto de extensão para customizações. Cria uma nova transição com as características
     * informadas.
     *
     * @param name o nome da transição.
     */
    @Nonnull
    protected STransition newTransition(@Nonnull STask<?> origin, @Nonnull String name, @Nonnull STask<?> destination) {
        return new STransition(origin, name, destination);
    }

    /**
     * Retorna as tarefas definidas neste mapa. Apenas tarefas que não são do tipo fim
     * ({@link TaskType#END}) são retornadas.
     */
    @Nonnull
    public Collection<STask<?>> getTasks() {
        return tasksByName.values();
    }

    /**
     * Returns all tasks defined, including the end nodes of the flow.
     */
    @Nonnull
    public Collection<STask<?>> getAllTasks() {
        Collection<STask<?>> tasks = getTasks();
        Collection<STaskEnd> ends = getEndTasks();
        ArrayList<STask<?>> all = new ArrayList<>(tasks.size() + ends.size());
        all.addAll(tasks);
        all.addAll(ends);
        return all;
    }

    /**
     * Retorna as tarefas definidas neste mapa do tipo {@link TaskType#HUMAN} ou uma lista vazia.
     */
    @Nonnull
    public Collection<STaskHuman> getHumanTasks() {
        return (Collection<STaskHuman>) getTasks(TaskType.HUMAN);
    }

    /**
     * Retorna as tarefas definidas neste mapa do tipo {@link TaskType#JAVA} ou uma lista vazia.
     */
    @Nonnull
    public Collection<STaskJava> getJavaTasks() {
        return (Collection<STaskJava>) getTasks(TaskType.JAVA);
    }

    /**
     * Retorna as tarefas definidas neste mapa do tipo {@link TaskType#WAIT} ou uma lista vazia.
     */
    @Nonnull
    public Collection<STaskWait> getWaitTasks() {
        return (Collection<STaskWait>) getTasks(TaskType.WAIT);
    }

    /**
     * Retorna as tarefas definidas neste mapa do tipo especificado ou lista vazia.
     */
    @Nonnull
    public Collection<? extends STask<?>> getTasks(IEntityTaskType IEntityTaskType) {
        final Builder<STask<?>> builder = ImmutableList.builder();
        for (final STask sTask : getTasks()) {
            if (sTask.getTaskType() == IEntityTaskType) {
                builder.add(sTask);
            }
        }
        return builder.build();
    }

    /**
     * Retorna as tarefas definidas neste mapa do tipo fim ({@link TaskType#END}) ou uma lista vazia.
     */
    @Nonnull
    public Collection<STaskEnd> getEndTasks() {
        return endTasks.values();
    }

    /**
     * Verifica se há um papel definido com a sigla especificada.
     */
    public boolean hasRoleWithAbbreviation(String abbreviation) {
        return rolesByAbbreviation.containsKey(abbreviation.toLowerCase());
    }

    /**
     * Retorna o papel definido com a sigla especificada.
     */
    @Nullable
    public SBusinessRole getRoleWithAbbreviation(String abbreviation) {
        return rolesByAbbreviation.get(abbreviation.toLowerCase());
    }

    /**
     * Retorna os papeis definidos. A coleção retornada é do tipo {@link ImmutableSet}.
     */
    @Nonnull
    public Collection<SBusinessRole> getRoles() {
        return ImmutableSet.copyOf(rolesByAbbreviation.values());
    }

    /**
     * Adiciona um novo papel a este mapa.
     *
     * @param automaticUserAllocation indicador de alocação automática.
     */
    public SBusinessRole addRoleDefinition(String name, String abbreviation,
            BusinessRoleStrategy<? extends FlowInstance> businessRoleStrategy,
            boolean automaticUserAllocation) {
        final SBusinessRole businessRole = new SBusinessRole(name, abbreviation, businessRoleStrategy, automaticUserAllocation);
        if (hasRoleWithAbbreviation(businessRole.getAbbreviation())) {
            throw new SingularFlowException("Role with abbreviation '" + businessRole.getAbbreviation() + "' already defined", this);
        }
        rolesByAbbreviation.put(businessRole.getAbbreviation().toLowerCase(), businessRole);
        return businessRole;
    }

    /**
     * Registra um <i>listener</i> para mudaças de papel.
     *
     * @param <T> o tipo deste mapa de fluxo.
     * @param roleChangeListener o <i>listener</i> do tipo {@link IRoleChangeListener}.
     * @return este mapa com o <i>listener</i> registrado.
     */
    public <T extends FlowInstance> FlowMap setRoleChangeListener(IRoleChangeListener<T> roleChangeListener) {
        this.roleChangeListener = roleChangeListener;
        return this;
    }

    /**
     * Notifica mudança de papel. Internamente notifica o <i>listener</i> registrado, caso exista.
     *
     * @param instance a instância de fluxo.
     * @param role o papel.
     * @param previousUser o usuário anteriormente atribuído ao papel.
     * @param newUser o novo usuário atribuído ao papel.
     */
    public void notifyRoleChange(final FlowInstance instance, final SBusinessRole role, SUser previousUser, SUser newUser) {
        if (roleChangeListener != null) {
            roleChangeListener.execute(instance, role, previousUser, newUser);
        }
    }

    /**
     * Adiciona uma nova tarefa.
     */
    protected <T extends STask> T addTask(T task) {

        String name = task.getName();
        String abbreviation = task.getAbbreviation();

        if (tasksByAbbreviation.containsKey(abbreviation)) {
            throw new SingularFlowException("Task with abbreviation '" + abbreviation + "' already defined", this);
        }
        if (tasksByName.containsKey(name)) {
            throw new SingularFlowException("Task with name '" + name + "' already defined", this);
        }

        tasksByName.put(name, task);
        tasksByAbbreviation.put(abbreviation, task);

        return task;
    }

    /**
     * <p>Cria e adiciona uma nova tarefa do tipo {@link TaskType#HUMAN}.</p>
     *
     * @param definition a definição da tarefa.
     * @return a nova tarefa criada e adicionada.
     */
    public STaskHuman addHumanTask(ITaskDefinition definition) {
        return addTask(new STaskHuman(this, definition.getName(), definition.getKey()));
    }

    /**
     * <p>Cria e adiciona uma nova tarefa do tipo {@link TaskType#JAVA}.</p>
     *
     * @param definition a definição da tarefa.
     * @return a nova tarefa criada e adicionada.
     */
    public STaskJava addJavaTask(ITaskDefinition definition) {
        return addTask(new STaskJava(this, definition.getName(), definition.getKey()));
    }

    /**
     * <p>Cria e adiciona uma nova tarefa do tipo {@link TaskType#WAIT}.</p>
     *
     * @param definition a definição da tarefa.
     * @return a nova tarefa criada e adicionada.
     */
    public STaskWait addWaitTask(ITaskDefinition definition) {
        return addWaitTask(definition, null);
    }

    /**
     * <p>Cria e adiciona uma nova tarefa do tipo {@link TaskType#WAIT}.</p>
     *
     * <p>Configura a estratégia de execução conforme a especificada ({@link IExecutionDateStrategy}).
     * Isso define a data alvo de uma instância desta tarefa.</p>
     *
     * @param <T> o tipo da instância de fluxo.
     * @param definition a definição da tarefa.
     * @param dateExecutionStrategy a estratégia de execução.
     * @return a nova tarefa criada e adicionada.
     */
    public <T extends FlowInstance> STaskWait addWaitTask(ITaskDefinition definition,
            IExecutionDateStrategy<T> dateExecutionStrategy) {
        return addTask(new STaskWait(this, definition.getName(), definition.getKey(), dateExecutionStrategy));
    }

    /**
     * Define a tarefa inicial do fluxo.
     * @deprecated addStart() should be used instead
     */
    @Deprecated
    @Nonnull
    public SStart setStart(@Nonnull ITaskDefinition initialTask) {
        if (!starts.isEmpty()) {
            throw new SingularFlowException("The start point is already set", this);
        }
        return addStart(getTask(initialTask));
    }

    /** Sets the tasks as one possible start point of the flow. */
    @Nonnull
    public SStart addStart(@Nonnull ITaskDefinition initialTask) {
        return addStart(getTask(initialTask));
    }

    /** Sets the tasks as one possible start point of the flow. */
    @Nonnull
    public SStart addStart(@Nonnull STask<?> task) {
        Objects.requireNonNull(task);
        if (task.getFlowMap() != this) {
            throw new SingularFlowException("The task does not belong to this flow", this).add(task);
        } else if (task.isEnd()) {
            throw new SingularFlowException("Task of type end, can't be a start point", this).add(task);
        } else if (task.getStartPointInfo().isPresent()) {
            throw new SingularFlowException("This task is already defined as a start point", this).add(task);
        }
        SStart s = new SStart(task);
        task.setStartPoint(s);
        starts.add(s);
        return s;
    }

    /** Retorna a tarefa inicial deste mapa.
     * @deprecated {@link #getStarts()} should be used.
     */
    @Nonnull
    @Deprecated
    public SStart getStart() {
        if (starts.isEmpty()) {
            throw new SingularFlowException("Task inicial não definida no fluxo", this);
        } else if (starts.size() > 1) {
            throw new SingularFlowException("There is more than on start point. getStarts() must be used.", this);
        }
        return starts.get(0);
    }

    /** List all starts points of the flow. */
    @Nonnull
    public List<SStart> getStarts() {
        return starts.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(starts);
    }

    /**
     * <p>Retorna a definição de fluxo deste mapa.</p>
     *
     * @return a definição de fluxo.
     */
    public FlowDefinition<?> getFlowDefinition() {
        return flowDefinition;
    }

    /**
     * <p>Cria e adiciona uma nova tarefa do tipo fim ({@link TaskType#END}).</p>
     *
     * @param definition a definição da tarefa.
     * @return a nova tarefa criada e adicionada.
     */
    public STaskEnd addEnd(ITaskDefinition definition) {
        Objects.requireNonNull(definition.getKey());
        Objects.requireNonNull(definition.getName());
        if (endTasks.containsKey(definition.getName())) {
            throw new SingularFlowException("End task '" + definition.getName() + "' already defined", this);
        }
        final STaskEnd fim = new STaskEnd(this, definition.getName(), definition.getKey());
        endTasks.put(definition.getName(), fim);
        tasksByAbbreviation.put(fim.getAbbreviation(), fim);
        return fim;
    }

    /**
     * <p>Retorna a tarefa deste mapa com a sigla especificada.</p>
     *
     * @param abbreviation a sigla especificada.
     * @return a tarefa deste mapa com a sigla especificada; ou {@code null} caso não a encontre.
     */
    public Optional<STask<?>> getTaskByAbbreviation(String abbreviation) {
        return Optional.ofNullable(tasksByAbbreviation.get(abbreviation));
    }

    /**
     * <p>Retorna a tarefa deste mapa com a sigla especificada.</p>
     *
     * @param abbreviation a sigla especificada.
     * @return a tarefa deste mapa com a sigla especificada.
     * @throws SingularFlowException caso não encontre tarefa com a sigla especificada.
     */
    public STask<?> getTaskByAbbreviationOrException(String abbreviation) {
        return getTaskByAbbreviation(abbreviation).orElseThrow(
                () -> new SingularFlowException("Task with abbreviation '" + abbreviation + "' not found", this));
    }

    /**
     * <p>Retorna a tarefa do tipo {@link TaskType#HUMAN} deste mapa com a sigla especificada.</p>
     *
     * @param abbreviation a sigla especificada.
     * @return a tarefa deste mapa com a sigla especificada; ou {@code null} caso não a encontre.
     */
    public Optional<STaskHuman> getHumanTaskByAbbreviation(String abbreviation) {
        return getTaskByAbbreviation(abbreviation).map(task -> castCheck(task, STaskHuman.class, abbreviation));
    }

    /**
     * <p>Retorna a tarefa do tipo {@link TaskType#HUMAN} deste mapa com a sigla especificada.</p>
     *
     * @param abbreviation a sigla especificada.
     * @return a tarefa deste mapa com a sigla especificada.
     * @throws SingularFlowException caso não encontre tarefa com a sigla especificada.
     */
    public STaskHuman getHumanTaskByAbbreviationOrException(String abbreviation) {
        return castCheck(getTaskByAbbreviationOrException(abbreviation), STaskHuman.class, abbreviation);
    }

    private <T extends STask> T castCheck(STask<?> target, Class<T> expectedClass, String abbreviation) {
        if (target == null) {
            return null;
        } else if (expectedClass.isInstance(target)) {
            return expectedClass.cast(target);
        }
        throw new SingularFlowException("Task with abbreviation '" + abbreviation + "' found, but it is of type "
                + target.getClass().getName() + " and was expected to be " + expectedClass.getName(), this);
    }

    /**
     * <p>Encontra a definição da tarefa informada ou dispara uma exceção caso não a encontre.</p>
     *
     * @param taskDefinition a definição informada.
     * @return a definição da tarefa informada.
     * @throws SingularFlowException caso não encontre a tarefa.
     */
    @Nonnull
    public STask<?> getTask(@Nonnull ITaskDefinition taskDefinition) {
        STask<?> task = getTaskWithName(taskDefinition.getName());
        if (task == null) {
            throw new SingularFlowException(
                    "Task " + taskDefinition.getKey() + " não encontrada em " + getFlowDefinition().getKey(), this);
        }
        return task;
    }

    /**
     * <p>Retorna a tarefa deste mapa com o none especificado.</p>
     *
     * @param name o nome especificado.
     * @return a tarefa deste mapa com o nome especificado; ou {@code null} caso não a encontre.
     */
    public STask<?> getTaskWithName(String name) {
        if (tasksByName.containsKey(name)) {
            return tasksByName.get(name);
        }
        return endTasks.get(name);
    }

    /**
     * <p>Verifica a consistência deste mapa.</p>
     *
     * <p>Um mapa é considerado consistente caso passe nos seguintes testes:</p>
     * <ul>
     *     <li>Cada tarefa definida neste mapa é consistente</li>
     *     <li>A tarefa inicial foi selecionada</li>
     *     <li>Todas as transições levam a uma tarefa válida</li>
     * </ul>
     */
    public void verifyConsistency() {
        verifyTasksConsistency();
        if (starts.isEmpty()) {
            throw new SingularFlowException("There is no initial task set", this);
        }
        checkRouteToTheEnd();
    }

    private void verifyTasksConsistency() {
        tasksByAbbreviation.values().forEach(STask::verifyConsistency);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void checkRouteToTheEnd() {
        Set<STask<?>> tasks = new HashSet<>(tasksByName.values());
        Set<STask<?>> taskWithoutOut = tasks.stream().filter(t -> t.getTransitions().isEmpty() ||
                t.getTransitions().stream().allMatch(trans -> trans.getDestination().equals(t))).collect(
                Collectors.toSet());
        if (!taskWithoutOut.isEmpty()) {
            throw new SingularFlowException(
                    "The following tasks have no way to reach the end (without out transition): \n" +
                            joinTaskNames(tasks), this);
        }
        while (removeIfReachesTheEnd(tasks)) {
            /* CORPO VAZIO */
        }
        if (!tasks.isEmpty()) {
            throw new SingularFlowException("The following tasks have no way to reach the end (circular reference): \n"
                    + joinTaskNames(tasks), this);
        }
    }

    private static boolean removeIfReachesTheEnd(Set<STask<?>> tasks) {
        return tasks.removeIf((task) -> task.getTransitions().stream()
                .anyMatch((transition) -> transition.getDestination().isEnd()
                        || !tasks.contains(transition.getDestination())));
    }

    private static String joinTaskNames(Set<STask<?>> tasks) {
        return tasks.stream().map(STask::toString).collect(Collectors.joining(";\n     ", "     ", ";"));
    }

    /**
     * <p>Retorna o serviço de consulta das definições de variáveis.</p>
     */
    protected VarService getVarService() {
        return flowDefinition.getVarService();
    }

    @Override
    public String toString() {
        return "FlowMap [flowDefinition=" + flowDefinition.getName() + "]";
    }

    public void addDashboardView(DashboardView dashboardView) {
        dashboardViews.put(dashboardView.getName(), dashboardView);
    }

    public List<DashboardView> getDashboardViews() {
        return new ArrayList<>(dashboardViews.values());
    }

    public DashboardView getDashboardViewWithName(String name) {
        return dashboardViews.get(name);
    }

    @Nonnull
    public <T extends Serializable> FlowMap setMetaDataValue(@Nonnull MetaDataKey<T> key, T value) {
        getFlowDefinition().setMetaDataValue(key, value);
        return this;
    }

    /**
     * Returns display information of the flow object that may be used to help (guide) the diagram generation of the
     * flow.
     * <p>It doesn't affect the runtime of the flow.</p>
     */
    @Nonnull
    public DisplayInfoFlow getDisplayInfo() {
        if (displayInfo == null) {
            displayInfo = new DisplayInfoFlow(this);
        }
        return displayInfo;
    }
}
