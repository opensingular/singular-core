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

package org.opensingular.form.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class FormAttachmentEntityId implements Serializable {

    @Column(name = "CO_VERSAO_FORMULARIO", nullable = false)
    private Long formVersionCod;

    @Column(name = "CO_ARQUIVO", nullable = false)
    private Long attachmentCod;

    public FormAttachmentEntityId() {
    }

    public FormAttachmentEntityId(Long formVersionCod, Long attachmentCod) {
        this.formVersionCod = formVersionCod;
        this.attachmentCod = attachmentCod;
    }

    public Long getFormVersionCod() {
        return formVersionCod;
    }

    public Long getAttachmentCod() {
        return attachmentCod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FormAttachmentEntityId that = (FormAttachmentEntityId) o;
        return Objects.equals(formVersionCod, that.formVersionCod) && Objects.equals(attachmentCod, that.attachmentCod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formVersionCod, attachmentCod);
    }
}
