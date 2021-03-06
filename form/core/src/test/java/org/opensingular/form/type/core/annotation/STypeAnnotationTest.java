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

package org.opensingular.form.type.core.annotation;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.opensingular.form.PackageBuilder;
import org.opensingular.form.SDictionary;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SIList;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.document.RefType;
import org.opensingular.form.document.SDocumentFactory;
import org.opensingular.form.io.FormSerializationUtil;
import org.opensingular.form.io.FormSerialized;
import org.opensingular.form.type.core.SIString;
import org.opensingular.form.type.core.STypeString;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Properties.extractProperty;

public class STypeAnnotationTest {
    protected static SDictionary dictionary;
    protected        PackageBuilder                        localPackage;
    private          STypeComposite<? extends SIComposite> baseCompositeField, annotated1, annotated2,
            notAnnotated, annotated4;
    private STypeString field11;

    @Before
    public void createDictionary() {
        dictionary = SDictionary.create();

        localPackage = dictionary.createNewPackage("test");
        baseCompositeField = localPackage.createCompositeType("group");
        baseCompositeField.addFieldString("notAnnotated");

        annotated1 = baseCompositeField.addFieldComposite("annotatedGroup1");
        field11 = annotated1.addFieldString("field11");
        annotated1.asAtrAnnotation().setAnnotated();

        annotated2 = baseCompositeField.addFieldComposite("annotatedGroup2");
        annotated2.addFieldString("field121");
        annotated2.addFieldString("field122");
        annotated2.asAtrAnnotation().setAnnotated();

        notAnnotated = baseCompositeField.addFieldComposite("notAnnotatedGroup3");
        notAnnotated.addFieldString("field13");
        annotated4 = notAnnotated.addFieldComposite("annotatedSubGroup4");
        annotated4.addFieldString("field341");
        annotated4.asAtrAnnotation().setAnnotated();

    }

    @Test
    public void aNewInstanceHasNoAnnotations() {
        SIComposite instance = baseCompositeField.newInstance();
        Assertions.assertThat(instance.getDocument().getDocumentAnnotations().getAnnotationsAsList()).isEmpty();
    }

    @Test
    public void returnAllAnnotationsFromInstance() {
        SIComposite instance = baseCompositeField.newInstance();

        asAnnotation(instance, annotated1).annotation().setText("Abacate");
        asAnnotation(instance, annotated2).annotation().setText("Avocado");
        asAnnotation(instance, annotated4).annotation().setText("ukwatapheya");

        List<SIAnnotation> all = instance.getDocument().getDocumentAnnotations().getAnnotationsAsList();
        assertThat(extractProperty("text").from(all)).containsOnly("Abacate","Avocado","ukwatapheya");
    }

    @Test public void returnAllAnnotationsFromInstanceWithoutEmptyOnes(){
        SIComposite instance = baseCompositeField.newInstance();

        asAnnotation(instance, annotated1).annotation().setText("Abacate");
        asAnnotation(instance, annotated2).annotation();
        asAnnotation(instance, annotated4).annotation().setText("ukwatapheya");

        List<SIAnnotation> all = instance.getDocument().getDocumentAnnotations().getAnnotationsAsList();
        assertThat(extractProperty("text").from(all))
                .containsOnly("Abacate",null, "ukwatapheya");
    }

    @Test public void returnAllAnnotationsInAPersitentObject(){
        SIComposite instance = baseCompositeField.newInstance();

        asAnnotation(instance, annotated1).annotation().setText("Abacate");
        asAnnotation(instance, annotated2).annotation().setApproved(true);
        asAnnotation(instance, annotated4).annotation().setText("ukwatapheya");
        SIList persistent = instance.getDocument().getDocumentAnnotations().getAnnotations();
        assertThat(persistent.getType()).isInstanceOf(STypeAnnotationList.class);
        assertThat(persistent.getElementsType()).isInstanceOf(STypeAnnotation.class);
        assertThat(extractProperty("text").from(persistent.getValues()))
                .containsOnly("Abacate", null, "ukwatapheya");
        assertThat(extractProperty("approved").from(persistent.getValues()))
                .containsOnly(null, true);
    }

    @Test public void loadsAnnotationsToAnewObject(){
        RefType ref = RefType.of(() -> baseCompositeField);
        SIComposite instance = (SIComposite) SDocumentFactory.empty().createInstance(ref);

        asAnnotation(instance, annotated1).annotation().setText("Abacate");

        SIList         persistent   = instance.getDocument().getDocumentAnnotations().getAnnotations();
        FormSerialized serialized   = FormSerializationUtil.toSerializedObject(persistent);
        SIList         deserialized = (SIList) FormSerializationUtil.toInstance(serialized);

        SIComposite justCreated = baseCompositeField.newInstance();

        justCreated.getDocument().getDocumentAnnotations().loadAnnotations(deserialized);
        SIList anotherPersistent = justCreated.getDocument().getDocumentAnnotations().getAnnotations();
        assertThat(extractProperty("text").from(anotherPersistent.getValues()))
                .containsOnly("Abacate");
    }


    @Test public void youCanRePersistTheAnnotationAsMuchAsYouWant(){
        RefType ref = RefType.of(() -> baseCompositeField);
        SIComposite instance = (SIComposite) SDocumentFactory.empty().createInstance(ref);

        asAnnotation(instance, annotated1).annotation().setText("Abacate");

        SIList persistent = instance.getDocument().getDocumentAnnotations().getAnnotations();
        FormSerialized serialized = FormSerializationUtil.toSerializedObject(persistent);
        SIList deserialized = (SIList) FormSerializationUtil.toInstance(serialized);
        instance.getDocument().getDocumentAnnotations().clear();
        instance.getDocument().getDocumentAnnotations().loadAnnotations(deserialized);
        SIList anotherPersistent = instance.getDocument().getDocumentAnnotations().getAnnotations();
        assertThat(extractProperty("text").from(anotherPersistent.getValues())).containsOnly("Abacate");
    }

    @Test public void youCanEreaseAnnotationsFromASingleInstance(){
        SIComposite instance = baseCompositeField.newInstance();

        asAnnotation(instance, annotated1).annotation().setText("Abacate");

        asAnnotation(instance, annotated1).annotation().clear();

        assertThat(asAnnotation(instance, annotated1).annotation()).isNotNull();
        assertThat(asAnnotation(instance, annotated1).annotation().getText()).isNullOrEmpty();
        assertThat(asAnnotation(instance, annotated1).annotation().getApproved()).isNull();
    }

    @Test public void youCanEreaseAnnotationsFromTheMixin(){
        SIComposite instance = baseCompositeField.newInstance();

        asAnnotation(instance, annotated1).annotation().setText("Abacate");

        instance.getDocument().getDocumentAnnotations().clear();

        List<SIAnnotation> all = instance.getDocument().getDocumentAnnotations().getAnnotationsAsList();
        assertThat(asAnnotation(instance, annotated1).annotation().getText()).isNullOrEmpty();
        assertThat(asAnnotation(instance, annotated1).annotation().getApproved()).isNull();
    }

    @Test public void aNewInstanceHasNoAnnotationsOrChilds(){
        SIComposite instance = baseCompositeField.newInstance();
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyAnnotationOnTree()).isFalse();
    }

    @Test public void itsOwnAnnotationCountsAsChild(){
        SIComposite instance = baseCompositeField.newInstance();
        instance.asAtrAnnotation().annotation().setText("anything");
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyAnnotationOnTree()).isTrue();
    }

    @Test public void returnsIfAnyOfItsChildHasAnnotations(){
        SIComposite instance = baseCompositeField.newInstance();
        asAnnotation(instance, annotated1).annotation().setText("anything");
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyAnnotationOnTree()).isTrue();
    }

    @Test public void anEmptyInstanceHasNoRefusals(){
        SIComposite instance = baseCompositeField.newInstance();
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyRefusal()).isFalse();
    }

    @Test public void anApprovedInstanceIsnNotRefused(){
        SIComposite instance = baseCompositeField.newInstance();
        instance.asAtrAnnotation().annotation().setApproved(true);
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyRefusal()).isFalse();
    }

    @Test public void anInstanceWithARejectedFieldOnSubTree(){
        SIComposite instance = baseCompositeField.newInstance();
        SIString field11Instance = instance.getDescendant(field11);

        field11Instance.asAtrAnnotation().annotation().setApproved(false);
        Assertions.assertThat(field11Instance.asAtrAnnotation().hasAnyRefusalOnTree()).isTrue();
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyRefusalOnTree()).isTrue();
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyRefusal()).isTrue();

        field11Instance.asAtrAnnotation().annotation().setApproved(true);
        Assertions.assertThat(field11Instance.asAtrAnnotation().hasAnyRefusalOnTree()).isFalse();
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyRefusal()).isFalse();
    }


    @Test public void anRejectedInstanceIsnRefused(){
        SIComposite instance = baseCompositeField.newInstance();
        instance.asAtrAnnotation().annotation().setApproved(false);
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyRefusal()).isTrue();
    }

    @Test public void returnsIfAnyOfItsChildIsRefused(){
        SIComposite instance = baseCompositeField.newInstance();
        asAnnotation(instance, annotated1).annotation().setApproved(false);
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyRefusal()).isTrue();
    }

    @Test public void returnsIfHasAnAnnotatedChild(){
        SIComposite instance = baseCompositeField.newInstance();
        Assertions.assertThat(instance.asAtrAnnotation().hasAnyAnnotable()).isTrue();
    }

    private AtrAnnotation asAnnotation(SIComposite instance, STypeComposite<? extends SIComposite> field) {
        return instance.getDescendant(field).asAtrAnnotation();
    }
}
