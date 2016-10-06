package org.opensingular.form.exemplos.notificacaosimplificada.domain.enums;

import org.opensingular.lib.support.persistence.util.EnumId;

public enum TipoEnsaio implements EnumId<TipoEnsaio, Character> {
    /**
     * Ensaio de embalagem
     */
    EMBALAGEM('1', "Controle de Qualidade de Embalagem"),

    /**
     * Ensaio de produto
     */
    PRODUTO('2', "Controle de Qualidade do Produto");

    public static final String ENUM_CLASS_NAME = "org.opensingular.singular.exemplos.notificacaosimplificada.domain.enums.TipoEnsaio";

    private TipoEnsaio(Character codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    private Character codigo;

    private String descricao;

    @Override
    public Character getCodigo() {
        return codigo;
    }

    @Override
    public String getDescricao() {
        return descricao;
    }

    @Override
    public TipoEnsaio valueOfEnum(Character id) {
        for (TipoEnsaio tipo : values()) {
            if (tipo.getCodigo().equals(id)) {
                return tipo;
            }
        }
        return null;
    }
}