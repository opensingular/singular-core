package br.net.mirante.singular.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

import br.net.mirante.singular.flow.core.entity.IEntityProcessGroup;

/**
 * The base persistent class for the TB_GRUPO_PROCESSO database table.
 * <p>
 *
 * @param <PROCESS_DEF>
 */
@MappedSuperclass
@Table(name = "TB_GRUPO_PROCESSO")
public abstract class AbstractProcessGroupEntity extends BaseEntity<String> implements IEntityProcessGroup {

    @Id
    @Column(name = "CO_GRUPO_PROCESSO")
    private String cod;

    @Column(name = "NO_GRUPO", length = 100, nullable = false)
    private String name;

    @Column(name = "URL_CONEXAO", length = 300, nullable = false)
    private String connectionURL;

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

}