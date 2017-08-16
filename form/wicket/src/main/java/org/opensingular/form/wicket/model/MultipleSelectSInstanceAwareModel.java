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

package org.opensingular.form.wicket.model;

import org.apache.wicket.model.IModel;
import org.opensingular.form.SIList;
import org.opensingular.form.SInstance;
import org.opensingular.form.SingularFormException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class MultipleSelectSInstanceAwareModel extends AbstractSInstanceAwareModel<List<Serializable>> {

    private static final long serialVersionUID = -4455601838581324870L;

    private final IModel<? extends SIList<?>>     model;
    private final List<SelectSInstanceAwareModel> selects;

    public MultipleSelectSInstanceAwareModel(IModel<? extends SInstance> model) {
        if (! (model.getObject() instanceof SIList)) {
            throw new SingularFormException("Este model somente deve ser utilizado para tipo lista", model.getObject());
        }
        this.model = (IModel<? extends SIList<?>>) model;
        this.selects = new ArrayList<>();
        final SIList<?> list = this.model.getObject();
        for (int i = 0; i < list.size(); i += 1) {
            selects.add(new SelectSInstanceAwareModel(new SInstanceListItemModel<>(model, i), getCustomSelectConverterResolver()));
        }
    }

    @Override
    public SInstance getSInstance() {
        return model.getObject();
    }

    @Override
    public List<Serializable> getObject() {
        return selects.stream().map(IModel::getObject).collect(Collectors.toList());
    }

    @Override
    public void setObject(List<Serializable> objects) {
        SIList<?> list = model.getObject();
        //check if the selection actually has changed beacause in this case wicket do not do that.
        if (checkIfChanged(objects, list)) {
            list.clearInstance();
            selects.clear();
            for (int i = 0; i <= objects.size(); i += 1) {
                final Serializable o = objects.get(i);
                final SInstance newElement = list.addNew();
                model.getObject().asAtrProvider().getConverter().fillInstance(newElement, o);
                selects.add(new SelectSInstanceAwareModel(new SInstanceListItemModel<>(model, i), getCustomSelectConverterResolver()));
            }
        }
    }

    /**
     * compares the objects list
     * here we consider value and order to decide if the value list has changed.
     * @param objects
     * @param list
     * @return
     */
    private boolean checkIfChanged(List<Serializable> objects, SIList<?> list){
        if (objects.size() == list.size()){
            for (int i = 0; i < objects.size(); i++) {
                Object currentValue = model.getObject().asAtrProvider().getConverter().toObject(list.get(i));
                if (!currentValue.equals(objects.get(i))){
                    break;
                }
            }
            return false;
        }
        return true;
    }

    public SelectSInstanceAwareModel.SelectConverterResolver getCustomSelectConverterResolver(){
        return si -> Optional.ofNullable(si.getParent().asAtrProvider().getConverter());
    }

}