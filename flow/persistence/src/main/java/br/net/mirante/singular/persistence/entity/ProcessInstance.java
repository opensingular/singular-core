package br.net.mirante.singular.persistence.entity;

import br.net.mirante.singular.flow.core.MUser;
import br.net.mirante.singular.flow.core.entity.IEntityProcessInstance;
import br.net.mirante.singular.flow.core.entity.IEntityRole;
import br.net.mirante.singular.persistence.entity.util.ActorWrapper;
import br.net.mirante.singular.persistence.util.Constants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the TB_INSTANCIA_PROCESSO database table.
 * 
 */
@Entity
@Table(name="TB_INSTANCIA_PROCESSO", schema = Constants.SCHEMA)
public class ProcessInstance implements IEntityProcessInstance {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="CO_INSTANCIA_PROCESSO")
	private Long cod;

	@Column(name="DS_INSTANCIA_PROCESSO")
	private String description;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DT_FIM")
	private Date endDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DT_INICIO")
	private Date beginDate;

	//uni-directional many-to-one association to Actor
	@ManyToOne
	@JoinColumn(name="CO_ATOR_CRIADOR")
	private Actor userCreator;

	//bi-directional many-to-one association to TaskInstance
	@ManyToOne
	@JoinColumn(name="CO_INSTANCIA_TAREFA_PAI")
	private TaskInstance parentTask;

	// TODO remover este campo, era apenas provisorio no modelo
	@ManyToOne
	@JoinColumn(name="CO_TAREFA_ATUAL")
	@Deprecated
	private TaskDefinition currentTaskDefinition;

	//uni-directional many-to-one association to Process
	@ManyToOne
	@JoinColumn(name="CO_PROCESSO")
	private Process process;

	//bi-directional many-to-one association to TaskInstance
	@OneToMany(mappedBy="processInstance")
	private List<TaskInstance> tasks;

	//bi-directional many-to-one association to Variable
	@OneToMany(mappedBy="processInstance")
	private List<Variable> variables;

	//bi-directional many-to-one association to ExecutionVariable
	@OneToMany(mappedBy="processInstance")
	private List<ExecutionVariable> historicalVariables;

	@OneToMany(mappedBy="processInstance")
	private List<RoleInstance> roles;


	@Override
	public Long getCod() {
		return cod;
	}

	public void setCod(Long cod) {
		this.cod = cod;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	@Override
	public MUser getUserCreator() {
		return ActorWrapper.wrap(userCreator);
	}

	public void setUserCreator(Actor userCreator) {
		this.userCreator = userCreator;
	}

	@Override
	public TaskInstance getParentTask() {
		return parentTask;
	}

	public void setParentTask(TaskInstance parentTask) {
		this.parentTask = parentTask;
	}

	public TaskDefinition getCurrentTaskDefinition() {
		return currentTaskDefinition;
	}

	public void setCurrentTaskDefinition(TaskDefinition currentTaskDefinition) {
		this.currentTaskDefinition = currentTaskDefinition;
	}

	@Override
	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	@Override
	public List<TaskInstance> getTasks() {
		return tasks;
	}

	public void setTasks(List<TaskInstance> tasks) {
		this.tasks = tasks;
	}

	@Override
	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

	@Override
	public List<ExecutionVariable> getHistoricalVariables() {
		return historicalVariables;
	}

	@Override
	public List<RoleInstance> getRoles() {
		return roles;
	}

	public void setHistoricalVariables(List<ExecutionVariable> historicalVariables) {
		this.historicalVariables = historicalVariables;
	}

    public void setRoles(List<RoleInstance> roles) {
        this.roles = roles;
    }

	public void addTask(TaskInstance taskInstance) {
		if (getTasks() == null) {
			setTasks(new ArrayList<>());
		}

		getTasks().add(taskInstance);
	}

	public void addRole(RoleInstance roleInstance) {
		if (getRoles() == null) {
			setRoles(new ArrayList<>());
		}

		getRoles().add(roleInstance);
	}
}