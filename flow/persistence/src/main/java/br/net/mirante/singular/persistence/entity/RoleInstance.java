package br.net.mirante.singular.persistence.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.net.mirante.singular.flow.core.entity.IEntityRole;
import br.net.mirante.singular.persistence.util.Constants;

/**
 * The persistent class for the TB_INSTANCIA_PAPEL database table.
 */
@Entity
@Table(name = "TB_INSTANCIA_PAPEL", schema = Constants.SCHEMA)
public class RoleInstance extends BaseEntity implements IEntityRole {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CO_INSTANCIA_PAPEL")
    private Integer cod;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_CRIACAO", nullable = false)
    private Date createDate;

    //uni-directional many-to-one association to Actor
    @ManyToOne
    @JoinColumn(name = "CO_ATOR", nullable = false)
    private Actor actor;

    //uni-directional many-to-one association to Actor
    @ManyToOne
    @JoinColumn(name = "CO_ATOR_ALOCADOR")
    private Actor allocatorUser;

    //uni-directional many-to-one association to ProcessInstance
    @ManyToOne
    @JoinColumn(name = "CO_INSTANCIA_PROCESSO", nullable = false)
    private ProcessInstance processInstance;

    //uni-directional many-to-one association to Role
    @ManyToOne
    @JoinColumn(name = "CO_PAPEL", nullable = false)
    private Role role;

    public RoleInstance() {
    }

    @Override
    public Integer getCod() {
        return cod;
    }

    public void setCod(Integer cod) {
        this.cod = cod;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public Actor getAllocatorUser() {
        return allocatorUser;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public void setAllocatorUser(Actor allocatorUser) {
        this.allocatorUser = allocatorUser;
    }

    @Override
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public Actor getUser() {
        return getActor();
    }

    public void setRole(Role role) {
        this.role = role;
    }
}