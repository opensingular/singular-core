/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.form.exemplos.notificacaosimplificada.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import org.opensingular.form.exemplos.notificacaosimplificada.domain.generic.VocabularioControlado;
import org.opensingular.lib.support.persistence.enums.SimNao;
import org.opensingular.lib.support.persistence.util.GenericEnumUserType;

/**
 * LinhaCbpf generated by Vinicius Uriel
 */

@Entity
@Table(name = "TB_LINHA_CBPF", schema = "DBMEDICAMENTO")
@PrimaryKeyJoinColumn(name = "CO_LINHA_CBPF", referencedColumnName = "CO_SEQ_VOCABULARIO_CONTROLADO")
public class LinhaCbpf extends VocabularioControlado {

    private static final long serialVersionUID = -3105005456489332341L;

    @Column(name = "ST_LINHA_RESTRITIVA", nullable = false, length = 1)
    @Type(type = GenericEnumUserType.CLASS_NAME, parameters = {
            @Parameter(name = "enumClass", value = SimNao.ENUM_CLASS_NAME),
            @Parameter(name = "identifierMethod", value = "getCodigo"),
            @Parameter(name = "valueOfMethod", value = "valueOfEnum")})
    private SimNao situacaoLinhaRestritiva;

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

}
