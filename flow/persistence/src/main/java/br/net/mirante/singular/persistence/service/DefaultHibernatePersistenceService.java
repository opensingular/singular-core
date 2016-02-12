package br.net.mirante.singular.persistence.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import br.net.mirante.singular.commons.base.SingularProperties;
import br.net.mirante.singular.flow.core.MUser;
import br.net.mirante.singular.flow.core.entity.IEntityExecutionVariable;
import br.net.mirante.singular.flow.core.entity.IEntityTaskHistoricType;
import br.net.mirante.singular.flow.core.entity.IEntityTaskInstanceHistory;
import br.net.mirante.singular.flow.core.entity.IEntityVariableInstance;
import br.net.mirante.singular.flow.core.entity.IEntityVariableType;
import br.net.mirante.singular.persistence.entity.Actor;
import br.net.mirante.singular.persistence.entity.CategoryEntity;
import br.net.mirante.singular.persistence.entity.ExecutionVariableEntity;
import br.net.mirante.singular.persistence.entity.ProcessDefinitionEntity;
import br.net.mirante.singular.persistence.entity.ProcessInstanceEntity;
import br.net.mirante.singular.persistence.entity.ProcessVersionEntity;
import br.net.mirante.singular.persistence.entity.RoleDefinitionEntity;
import br.net.mirante.singular.persistence.entity.RoleInstanceEntity;
import br.net.mirante.singular.persistence.entity.TaskDefinitionEntity;
import br.net.mirante.singular.persistence.entity.TaskHistoricTypeEntity;
import br.net.mirante.singular.persistence.entity.TaskInstanceEntity;
import br.net.mirante.singular.persistence.entity.TaskInstanceHistoryEntity;
import br.net.mirante.singular.persistence.entity.TaskVersionEntity;
import br.net.mirante.singular.persistence.entity.VariableInstanceEntity;
import br.net.mirante.singular.persistence.entity.VariableTypeInstance;
import br.net.mirante.singular.persistence.entity.util.SessionLocator;
import br.net.mirante.singular.persistence.util.Constants;
import org.hibernate.LockMode;
import org.hibernate.criterion.Restrictions;

import javax.xml.transform.Result;

public class DefaultHibernatePersistenceService extends
        AbstractHibernatePersistenceService<CategoryEntity, ProcessDefinitionEntity,
                ProcessVersionEntity,
                ProcessInstanceEntity,
                TaskInstanceEntity,
                TaskDefinitionEntity,
                TaskVersionEntity,
                VariableInstanceEntity,
                RoleDefinitionEntity,
                RoleInstanceEntity> {

    public DefaultHibernatePersistenceService(SessionLocator sessionLocator) {
        super(sessionLocator);
    }

    // -------------------------------------------------------
    // ProcessIntance
    // -------------------------------------------------------

    @Override
    protected ProcessInstanceEntity newProcessInstance(ProcessVersionEntity processVersion) {
        ProcessInstanceEntity processInstance = new ProcessInstanceEntity();
        processInstance.setProcessVersion(processVersion);
        processInstance.setRoles(new ArrayList<>());
        return processInstance;
    }

    @Override
    protected RoleInstanceEntity newEntityRole(ProcessInstanceEntity instance, RoleDefinitionEntity role, MUser user, MUser allocator) {
        user = saveUserIfNeeded(user);
        final RoleInstanceEntity entityRole = new RoleInstanceEntity();
        entityRole.setProcessInstance(instance);
        entityRole.setUser((Actor) user);
        entityRole.setRole(role);
        entityRole.setAllocatorUser((Actor) allocator);
        entityRole.setCreateDate(new Date());
        return entityRole;
    }

    @Override
    protected MUser saveUserIfNeeded(MUser mUser) {
        MUser result = null;

        if (result == null &&mUser != null && mUser.getCod() != null) {
            result =  (MUser) getSession().createCriteria(Actor.class).add(Restrictions.eq("cod", mUser.getCod())).uniqueResult();
        }

        if (result == null &&mUser != null && ((Actor) mUser).getCodUsuario() != null ){
            result =  (MUser) getSession().createCriteria(Actor.class).add(Restrictions.eq("codUsuario", ((Actor) mUser).getCodUsuario())).uniqueResult();
        }

        if (result == null && mUser != null && mUser.getCod() == null){
            if ("sequence".equals(SingularProperties.INSTANCE.getProperty(SingularProperties.HIBERNATE_GENERATOR))){
                getSession().getSession().doWork(connection -> {
                    PreparedStatement ps = connection.prepareStatement("insert into " + Constants.SCHEMA + ".TB_ATOR (CO_ATOR, CO_USUARIO) VALUES (" + Constants.SCHEMA + ".SQ_CO_ATOR.NEXTVAL, ? )");
                    ps.setString(1, ((Actor)mUser).getCodUsuario());
                    ps.execute();
                });
            } else {
                getSession().getSession().doWork(connection -> {
                    PreparedStatement ps = connection.prepareStatement("insert into " + Constants.SCHEMA + ".TB_ATOR (CO_USUARIO) VALUES (?)");
                    ps.setString(1, ((Actor)mUser).getCodUsuario());
                    ps.execute();
                });
            }
            getSession().flush();
            result =  (MUser) getSession().createCriteria(Actor.class).add(Restrictions.eq("codUsuario", ((Actor) mUser).getCodUsuario())).uniqueResult();
        }
        return result;
    }

    // -------------------------------------------------------
    // Task
    // -------------------------------------------------------

    @Override
    protected Class<TaskInstanceEntity> getClassTaskInstance() {
        return TaskInstanceEntity.class;
    }

    @Override
    protected TaskInstanceEntity newTaskInstance(ProcessInstanceEntity processInstance, TaskVersionEntity taskVersion) {
        TaskInstanceEntity taskInstance = new TaskInstanceEntity();
        taskInstance.setProcessInstance(processInstance);
        taskInstance.setTask(taskVersion);
        return taskInstance;
    }

    @Override
    protected IEntityTaskInstanceHistory newTaskInstanceHistory(TaskInstanceEntity task, IEntityTaskHistoricType taskHistoryType,
            MUser allocatedUser, MUser responsibleUser) {

        TaskInstanceHistoryEntity history = new TaskInstanceHistoryEntity();
        history.setTaskInstance(task);
        history.setType((TaskHistoricTypeEntity) taskHistoryType);
        history.setAllocatedUser((Actor) allocatedUser);
        history.setAllocatorUser((Actor) responsibleUser);
        return history;
    }

    @Override
    protected Class<? extends TaskHistoricTypeEntity> getClassEntityTaskHistoricType() {
        return TaskHistoricTypeEntity.class;
    }

    // -------------------------------------------------------
    // Process Definition e Version
    // -------------------------------------------------------

    @Override
    public ProcessVersionEntity retrieveProcessVersionByCod(Integer cod) {
        return getSession().refreshByPk(ProcessVersionEntity.class, cod);
    }

    // -------------------------------------------------------
    // Variable
    // -------------------------------------------------------

    @Override
    protected VariableInstanceEntity retrieveVariableInstanceByCod(Integer cod) {
        return getSession().retrieve(VariableInstanceEntity.class, cod);
    }

    @Override
    protected VariableInstanceEntity newVariableInstance(ProcessInstanceEntity processInstance, String name) {
        VariableInstanceEntity variable = new VariableInstanceEntity();
        variable.setProcessInstance(processInstance);
        variable.setName(name);
        return variable;
    }


    @Override
    protected IEntityExecutionVariable newExecutionVariable(ProcessInstanceEntity instance, IEntityVariableInstance processInstanceVar,
            TaskInstanceEntity originTask, TaskInstanceEntity destinationTask, IEntityVariableType type) {
        ExecutionVariableEntity novo = new ExecutionVariableEntity();
        novo.setVariable((VariableInstanceEntity) processInstanceVar);
        novo.setProcessInstance(instance);
        novo.setOriginTask(originTask);
        novo.setDestinationTask(destinationTask);
        novo.setType((VariableTypeInstance) type);
        return novo;
    }

    @Override
    protected Class<? extends IEntityVariableType> getClassEntityVariableType() {
        return VariableTypeInstance.class;
    }

    @Override
    protected Class<ProcessInstanceEntity> getClassProcessInstance() {
        return ProcessInstanceEntity.class;
    }
    
    // -------------------------------------------------------
    // Listagens
    // -------------------------------------------------------

    @Override
    public List<? extends MUser> retrieveUsersByCod(Collection<Integer> cods) {
        throw new UnsupportedOperationException("Método não implementado");
    }

}