package org.opensingular.singular.exemplos.notificacaosimplificada.domain.enums;

import org.opensingular.singular.support.persistence.util.EnumId;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * @author allysson.cavalcante
 */
@XmlEnum
public enum ObjetivoPosologia implements EnumId<ObjetivoPosologia, Integer> {

    /**
     * Tratamento.
     */
    @XmlEnumValue("1")
    TRATAMENTO(1, "Tratamento"),

    /**
     * Prevencao
     */
    @XmlEnumValue("2")
    PREVENCAO(2, "Prevenção"),

    /**
     * Auxiliar diagnostico
     */
    @XmlEnumValue("3")
    AUXILIAR_DIAGNOSTICO(3, "Auxiliar diagnóstico"),

    /**
     * Diagnostico
     */
    @XmlEnumValue("4")
    DIAGNOSTICO(4, "Diagnóstico");

    public static final String ENUM_CLASS_NAME = "org.opensingular.singular.exemplos.notificacaosimplificada.domain.enums.ObjetivoPosologia";

    /**
     * Identificador do tipo de unidade de medida.
     */
    private final Integer codigo;

    /**
     * Descrição do tipo de unidade de medida.
     */
    private final String descricao;

    private ObjetivoPosologia(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    @Override
    public Integer getCodigo() {
        return this.codigo;
    }

    @Override
    public String getDescricao() {
        return this.descricao;
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ObjetivoPosologia valueOfEnum(Integer id) {
        for (ObjetivoPosologia tipo : values()) {
            if (tipo.getCodigo().equals(id)) {
                return tipo;
            }
        }
        return null;
    }
}