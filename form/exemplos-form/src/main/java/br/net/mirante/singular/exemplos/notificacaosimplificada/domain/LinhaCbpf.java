/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package br.net.mirante.singular.exemplos.notificacaosimplificada.domain;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.enums.SimNao;
import br.net.mirante.singular.exemplos.notificacaosimplificada.domain.generic.VocabularioControlado;
import br.net.mirante.singular.support.persistence.util.GenericEnumUserType;

/**
 * LinhaCbpf generated by Vinicius Uriel
 */

@Entity
@Table(name="TB_LINHA_CBPF", schema="DBMEDICAMENTO")
@PrimaryKeyJoinColumn(name="CO_LINHA_CBPF", referencedColumnName="CO_SEQ_VOCABULARIO_CONTROLADO")
public class LinhaCbpf extends VocabularioControlado {
	
	private static final long serialVersionUID = -3105005456489332341L;

	@Column(name = "ST_LINHA_RESTRITIVA", nullable = false, length = 1)
	@Type(type = GenericEnumUserType.CLASS_NAME, parameters = {
			@Parameter(name = "enumClass", value = SimNao.ENUM_CLASS_NAME),
			@Parameter(name = "identifierMethod", value = "getCodigo"),
			@Parameter(name = "valueOfMethod", value = "valueOfEnum")})
	private SimNao situacaoLinhaRestritiva;

	@ManyToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinTable(schema="DBMEDICAMENTO", name="RL_LINHACBPF_FORMAFARMACEUTICA",
				joinColumns = { @JoinColumn(name="CO_LINHA_CBPF", updatable = false, nullable = false) },
				inverseJoinColumns = { @JoinColumn(name="CO_FORMA_FARM_ESPEC", updatable = false, nullable = false)})
	private Set<FormaFarmaceuticaEspecifica> listaFormaFarmaceuticaEspecifica = new LinkedHashSet<FormaFarmaceuticaEspecifica>(0);

	public LinhaCbpf() {
    }

    public LinhaCbpf(Long id, String descricao, SimNao ativa) {
        this.id = id;
        this.descricao = descricao;
        this.ativa = ativa;
    }

	public SimNao getSituacaoLinhaRestritiva() {
		return situacaoLinhaRestritiva;
	}

	public void setSituacaoLinhaRestritiva(SimNao situacaoLinhaRestritiva) {
		this.situacaoLinhaRestritiva = situacaoLinhaRestritiva;
	}

	public Set<FormaFarmaceuticaEspecifica> getListaFormaFarmaceuticaEspecifica() {
		return listaFormaFarmaceuticaEspecifica;
	}

	public void setListaFormaFarmaceuticaEspecifica(
			Set<FormaFarmaceuticaEspecifica> listaFormaFarmaceuticaEspecifica) {
		this.listaFormaFarmaceuticaEspecifica = listaFormaFarmaceuticaEspecifica;
	}
}
