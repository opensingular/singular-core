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

package org.opensingular.form.view.list;

import org.opensingular.form.SInstance;

public class ButtonsConfig {

    private ButtonAction editButton = new ButtonAction(null, "Editar", null);
    private ButtonAction deleteButton = new ButtonAction(null, "Remover", null);

    public ButtonAction getEditButton() {
        return editButton;
    }

    protected void setEditButton(ButtonAction editButton) {
        this.editButton = editButton;
    }

    public ButtonAction getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(ButtonAction deleteButton) {
        this.deleteButton = deleteButton;
    }

    public boolean isDeleteEnabled(SInstance instance) {
        return deleteButton.getVisibleFor() == null || deleteButton.getVisibleFor().test(instance);
    }

    public boolean isEditEnabled(SInstance instance) {
        return editButton.getVisibleFor() == null || editButton.getVisibleFor().test(instance);
    }
}
