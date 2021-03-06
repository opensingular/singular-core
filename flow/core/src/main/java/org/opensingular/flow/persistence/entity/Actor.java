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

package org.opensingular.flow.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Subselect;
import org.opensingular.flow.core.SUser;
import org.opensingular.lib.support.persistence.entity.BaseEntity;
import org.opensingular.lib.support.persistence.util.Constants;

/**
 * The persistent class for the TB_ATOR database table.
 */
@Entity
@Subselect("SELECT A.CO_ATOR, A.CO_USUARIO, A.NO_ATOR, A.DS_EMAIL FROM " + Constants.SCHEMA + ".VW_ATOR A")
public class Actor extends BaseEntity<Integer> implements SUser {

    /* nao deve ter generator, deve ser uma view*/
    @Id
    @Column(name = "CO_ATOR")
    private Integer cod;

    @Column(name = "CO_USUARIO", nullable = false)
    private String codUsuario;

    @Column(name = "NO_ATOR", nullable = false)
    private String nome;

    @Column(name = "DS_EMAIL", nullable = false)
    private String email;

    public Actor() {
    }

    public Actor(Integer cod, String codUsuario, String nome, String email) {
        this.cod = cod;
        this.codUsuario = codUsuario;
        this.nome = nome;
        this.email = email;
    }

    @Override
    public String getSimpleName() {
        return getNome();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getCodUsuario() {
        return codUsuario;
    }

    public void setCodUsuario(String codUsuario) {
        this.codUsuario = codUsuario;
    }

    @Override
    public Integer getCod() {
        return cod;
    }

    public void setCod(Integer cod) {
        this.cod = cod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Actor actor = (Actor) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(codUsuario, actor.codUsuario)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(codUsuario)
                .toHashCode();
    }
}