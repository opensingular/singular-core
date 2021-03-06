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

package org.opensingular.flow.test.definicao;

import org.opensingular.flow.core.DefinitionInfo;
import org.opensingular.flow.core.ExecutionContext;
import org.opensingular.flow.core.FlowDefinition;
import org.opensingular.flow.core.FlowInstance;
import org.opensingular.flow.core.FlowMap;
import org.opensingular.flow.core.ITaskDefinition;
import org.opensingular.flow.core.ITaskPredicate;
import org.opensingular.flow.core.TaskPredicates;
import org.opensingular.flow.core.builder.BuilderBusinessRole;
import org.opensingular.flow.core.builder.FlowBuilderImpl;
import org.opensingular.flow.core.defaults.PermissiveTaskAccessStrategy;

import java.util.Calendar;

import static org.opensingular.flow.test.definicao.SampleRequirement.SampleTask.AGUARDANDO_ANALISE;
import static org.opensingular.flow.test.definicao.SampleRequirement.SampleTask.AGUARDANDO_GERENTE;
import static org.opensingular.flow.test.definicao.SampleRequirement.SampleTask.AGUARDANDO_PUBLICACAO;
import static org.opensingular.flow.test.definicao.SampleRequirement.SampleTask.DEFERIDO;
import static org.opensingular.flow.test.definicao.SampleRequirement.SampleTask.EM_EXIGENCIA;
import static org.opensingular.flow.test.definicao.SampleRequirement.SampleTask.INDEFERIDO;
import static org.opensingular.flow.test.definicao.SampleRequirement.SampleTask.NOTIFICAR_NOVA_INSTANCIA;
import static org.opensingular.flow.test.definicao.SampleRequirement.SampleTask.PUBLICADO;

@DefinitionInfo("Peticoes")
public class SampleRequirement extends FlowDefinition<FlowInstance> {

    public enum SampleTask implements ITaskDefinition {
        NOTIFICAR_NOVA_INSTANCIA("Notificar nova instância"),
        AGUARDANDO_ANALISE("Aguardando análise"),
        EM_EXIGENCIA("Em exigência"),
        AGUARDANDO_GERENTE("Aguardando gerente"),
        AGUARDANDO_PUBLICACAO("Aguardando publicação"),
        INDEFERIDO("Indeferido"),
        DEFERIDO("Deferido"),
        PUBLICADO("Publicado");

        private final String name;

        SampleTask(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static final String ENVIAR_PARA_ANALISE = "Enviar para análise";
    public static final String COLOCAR_EM_EXIGENCIA = "Colocar em exigência";
    public static final String INDEFERIR = "Indeferir";
    public static final String APROVAR_TECNICO = "Aprovar técnico";
    public static final String APROVAR_GERENTE = "Aprovar gerente";
    public static final String PUBLICAR = "Publicar";
    public static final String DEFERIR = "Deferir";
    public static final String CUMPRIR_EXIGENCIA = "Cumprir exigência";
    public static final String SOLICITAR_AJUSTE_ANALISE = "Solicitar ajuste análise técnica";

    // Papeis
    public static final String PAPEL_ANALISTA = "analista";
    public static final String PAPEL_GERENTE = "GERENTE";

    public SampleRequirement() {
        super(FlowInstance.class);
    }

    @Override
    protected FlowMap createFlowMap() {
        setName("Teste", "Petição");

        FlowBuilderImpl flow = new FlowBuilderImpl(this);

        BuilderBusinessRole<?> papelAnalista = flow.addBusinessRole("ANALISTA", PAPEL_ANALISTA, false);
        BuilderBusinessRole<?> papelGerente = flow.addBusinessRole("GERENTE", PAPEL_GERENTE, false);

        flow.addJavaTask(NOTIFICAR_NOVA_INSTANCIA).call(this::notificar);
        flow.addHumanTask(AGUARDANDO_ANALISE, papelAnalista);
        flow.addHumanTask(EM_EXIGENCIA, new PermissiveTaskAccessStrategy());
        flow.addHumanTask(AGUARDANDO_GERENTE, papelGerente)
                .withTargetDate((flowInstance, taskInstance) -> addDias(flowInstance, 1).getTime());
        flow.addHumanTask(AGUARDANDO_PUBLICACAO, new PermissiveTaskAccessStrategy());
        flow.addEndTask(INDEFERIDO);
        flow.addEndTask(DEFERIDO);
        flow.addEndTask(PUBLICADO).addStartedTaskListener((taskInstance, executionContext) -> System.out.println(taskInstance.getName() + " Iniciado"));
        flow.setStartTask(NOTIFICAR_NOVA_INSTANCIA);

        flow.from(NOTIFICAR_NOVA_INSTANCIA).go(ENVIAR_PARA_ANALISE, AGUARDANDO_ANALISE);
        flow.from(AGUARDANDO_ANALISE).go(COLOCAR_EM_EXIGENCIA, EM_EXIGENCIA);
        flow.from(EM_EXIGENCIA).go(CUMPRIR_EXIGENCIA, AGUARDANDO_ANALISE);
        flow.from(AGUARDANDO_ANALISE).go(INDEFERIR, INDEFERIDO);
        flow.from(AGUARDANDO_ANALISE).go(APROVAR_TECNICO, AGUARDANDO_GERENTE);
        flow.from(AGUARDANDO_GERENTE).go(APROVAR_GERENTE, AGUARDANDO_PUBLICACAO);
        flow.from(AGUARDANDO_GERENTE).go(SOLICITAR_AJUSTE_ANALISE, AGUARDANDO_ANALISE);
        flow.from(AGUARDANDO_PUBLICACAO).go(PUBLICAR, PUBLICADO);
        flow.from(AGUARDANDO_GERENTE).go(DEFERIR, DEFERIDO);

        // Tarefa aguardando analise a mais de um dia é indeferida automaticamente
        ITaskPredicate oneDayTimeLimit = TaskPredicates.timeLimitInDays(1);
        flow.from(AGUARDANDO_ANALISE).go(INDEFERIDO, oneDayTimeLimit);
        flow.from(AGUARDANDO_GERENTE).go(AGUARDANDO_PUBLICACAO, oneDayTimeLimit);

        return flow.build();
    }

    private Calendar addDias(Object taskInstance, int dias) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(((FlowInstance) taskInstance).getBeginDate());
        calendar.add(Calendar.DATE, dias);
        return calendar;
    }

    public Object notificar(ExecutionContext ctxExecucao) {
        System.out.println("Notificado");
        return null;
    }

}
