package org.opensingular.form.exemplos.notificacaosimplificada.domain;
// Generated 16/03/2010 08:00:26

import org.opensingular.lib.support.persistence.enums.SimNao;
import org.opensingular.form.exemplos.notificacaosimplificada.domain.generic.VocabularioControlado;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * EtapaFabricacao generated by Vinicius Uriel
 */

@XmlRootElement(name = "etapa-fabricacao", namespace = "http://www.anvisa.gov.br/reg-med/schema/domains")
@XmlType(name = "etapa-fabricacao", namespace = "http://www.anvisa.gov.br/reg-med/schema/domains")
@Entity
@Table(name = "TB_ETAPA_FABRICACAO", schema = "DBMEDICAMENTO")
@PrimaryKeyJoinColumn(name = "CO_ETAPA_FABRICACAO", referencedColumnName = "CO_SEQ_VOCABULARIO_CONTROLADO")
@NamedQueries({
        @NamedQuery(name = "EtapaFabricacao.findAll", query = "Select etapaFabricacao From EtapaFabricacao as etapaFabricacao where etapaFabricacao.ativa = 'S'  Order by etapaFabricacao.descricao  ")})
public class EtapaFabricacao extends VocabularioControlado {

    private static final long serialVersionUID = -589184368317463592L;

    @Column(name = "TP_ETAPA_FABRICACAO", nullable = false, length = 1)
    private Character tipoFabricacao;

    public EtapaFabricacao() {
    }

    public EtapaFabricacao(Long id, String descricao, Character tipoFabricacao, SimNao ativa) {
        this.id = id;
        this.descricao = descricao;
        this.tipoFabricacao = tipoFabricacao;
        this.ativa = ativa;
    }

    public Character getTipoFabricacao() {
        return tipoFabricacao;
    }

    public void setTipoFabricacao(Character tipoFabricacao) {
        this.tipoFabricacao = tipoFabricacao;
    }

}