/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.form.wicket.model;


import net.vidageek.mirror.dsl.Mirror;
import org.apache.wicket.model.IModel;
import org.junit.Assert;
import org.junit.Test;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;
import org.opensingular.form.SInstanceViewState;
import org.opensingular.form.curriculo.SPackageCurriculo;
import org.opensingular.form.document.RefType;
import org.opensingular.form.document.SDocumentFactory;
import org.opensingular.form.event.ISInstanceListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ModelSerializationTest {


    private static SIComposite newInstance() {
        RefType ref = RefType.of(dic -> {
            dic.get().loadPackage(SPackageCurriculo.class);
            return dic.get().getType(SPackageCurriculo.TIPO_CURRICULO);
        });
        return (SIComposite) SDocumentFactory.empty().createInstance(ref);
    }

    private void populateInstance(SIComposite instance) {
        instance.setValue("informacoesPessoais.nome", "João");
        instance.setValue("informacoesPessoais.dataNascimento", new Date());
    }


    /**
     * Verifica se as referencias apontadas pelos models se mantém as mesmas
     * após a serialização e deserialização.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testModelSerialization() throws Exception {
        SIComposite curriculo = newInstance();
        populateInstance(curriculo);

        IModel<SInstance> model1 = new SInstanceRootModel<>(curriculo);
        IModel<SInstance> model2 = new SInstanceRootModel<>(curriculo);

        List<IModel<SInstance>> modelsList = new ArrayList<>(2);
        modelsList.add(model1);
        modelsList.add(model2);


        Assert.assertSame(modelsList.get(0).getObject(), modelsList.get(1).getObject());

        ByteArrayOutputStream baos               = new ByteArrayOutputStream();
        ObjectOutputStream    objectOutputStream = new ObjectOutputStream(baos);
        objectOutputStream.writeObject(modelsList);
        objectOutputStream.close();

        byte[] serializedObject = baos.toByteArray();


        ObjectInputStream       objectInputStream      = new ObjectInputStream(new ByteArrayInputStream(serializedObject));
        List<IModel<SInstance>> modelsListDeserialized = (List<IModel<SInstance>>) objectInputStream.readObject();

        Assert.assertSame(modelsListDeserialized.get(0).getObject(), modelsListDeserialized.get(1).getObject());
    }


    /**
     * Verifica se o estado de exibição da view de lista (atributo exists) se mantém calculado após serialização e deserialização.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSInstanceVisibilityAttributesRecalculation() throws Exception {
        SIComposite curriculo = newInstance();
        populateInstance(curriculo);
        curriculo.attachEventCollector();

        curriculo.getField("primeiroEmprego").setValue(true);
        curriculo.getDocument().updateAttributes((ISInstanceListener.EventCollector) new Mirror().on(curriculo).get().field("eventCollector"));

        IModel<SInstance> model1 = new SInstanceRootModel<>(curriculo);

        Assert.assertEquals(SInstanceViewState.HIDDEN, SInstanceViewState.get(((SIComposite) model1.getObject()).getField("experienciasProfissionais")));

        ByteArrayOutputStream baos               = new ByteArrayOutputStream();
        ObjectOutputStream    objectOutputStream = new ObjectOutputStream(baos);
        objectOutputStream.writeObject(model1);
        objectOutputStream.close();

        byte[] serializedObject = baos.toByteArray();

        ObjectInputStream       objectInputStream      = new ObjectInputStream(new ByteArrayInputStream(serializedObject));
        IModel<SInstance>  model1Deserialized = (IModel<SInstance>) objectInputStream.readObject();

        Assert.assertEquals(SInstanceViewState.HIDDEN, SInstanceViewState.get(((SIComposite) model1Deserialized.getObject()).getField("experienciasProfissionais")));
    }

}
