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

package org.opensingular.form.wicket.util;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.opensingular.form.SInstance;
import org.opensingular.form.wicket.model.ISInstanceAwareModel;

import java.util.List;


public class SInstanceBoundedComponentUpdateVisitor implements IVisitor<Component, Void> {

    private final AjaxRequestTarget ajaxRequestTarget;
    private final List<SInstance>   instances;

    public SInstanceBoundedComponentUpdateVisitor(AjaxRequestTarget ajaxRequestTarget, List<SInstance> instances) {
        this.ajaxRequestTarget = ajaxRequestTarget;
        this.instances = instances;
    }

    @Override
    public void component(Component component, IVisit<Void> v) {
        IModel<?> model = component.getDefaultModel();
        if (model instanceof ISInstanceAwareModel) {
            SInstance compSInstance = ((ISInstanceAwareModel) model).getSInstance();
            if (compSInstance != null && instances.stream().anyMatch(compSInstance::isSameOrDescendantOf)) {
                WicketFormProcessing.refreshComponentOrCellContainer(ajaxRequestTarget, component);
                /*Como o valor pode ter sido alterado no updatelistener, é necessario chamar modelChanged*/
                if (component instanceof FormComponent) {
                    component.modelChanged();
                }
            }
        }
    }

}