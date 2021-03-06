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

package org.opensingular.form.wicket.mapper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.util.lang.Bytes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.wicket.helpers.AssertionsWComponent;
import org.opensingular.form.wicket.helpers.AssertionsWComponentList;
import org.opensingular.form.wicket.helpers.SingularFormDummyPageTester;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opensingular.form.wicket.AjaxUpdateListenersFactory.SINGULAR_PROCESS_EVENT;

public class DependsOnTest {

    private SingularFormDummyPageTester tester;

    private static STypeString category, element;

    private static STypeString a, b, c;

    static final Map<String, List<String>> OPTIONS = new ImmutableMap.Builder()
            .put("fruits", Lists.newArrayList("avocado", "apple", "pineaple"))
            .put("vegetables", Lists.newArrayList("cucumber", "radish"))
            .put("condiments", Lists.newArrayList("mustard", "rosemary", "coriander"))
            .build();

    private static void buildType(STypeComposite<?> baseCompositeField){
        category = baseCompositeField.addFieldString("category");
        element = baseCompositeField.addFieldString("element");

        category.asAtr().label("category");
        category.selectionOf(OPTIONS.keySet().toArray(new String[] {}));

        element.asAtr()
                .label("Word")
                .dependsOn(category);
        element.selectionOf(String.class)
                .selfIdAndDisplay()
                .simpleProvider(ins -> {
                    String prefix = ins.findNearest(category).get().getValue();
                    return (prefix == null) ? Collections.emptyList() : OPTIONS.getOrDefault(prefix, Lists.newArrayList());
                });

        // Test multiplesDependsOn
        a = baseCompositeField.addFieldString("a");
        b = baseCompositeField.addFieldString("b");
        c = baseCompositeField.addFieldString("c");
        a.asAtr().dependsOn(b);
        a.asAtr().dependsOn(c);
    }

    private AssertionsWComponentList options(){
        return tester.getAssertionsForm().getSubComponents(DropDownChoice.class);
    }


    @Before
    public void setUp(){
        tester = new SingularFormDummyPageTester();
        tester.getDummyPage().setTypeBuilder(DependsOnTest::buildType);
        tester.startDummyPage();
    }

    // DEFAULT
    @Test
    public void rendersBothDropDowns(){
        options().hasSize(2);
    }

    @Test
    public void renderOnlyThePrimaryChoice(){
        DropDownChoice categoryChoices = options().element(0).getTarget(DropDownChoice.class);
        DropDownChoice elementChoices = options().element(1).getTarget(DropDownChoice.class);

        assertThat(categoryChoices.getChoices()).containsOnly(OPTIONS.keySet().toArray());
        assertThat(elementChoices.getChoices()).isEmpty();
    }

    @Test
    public void changingSelectionChangesValue(){
        AssertionsWComponent categoryAssertion = tester.getAssertionsForm().getSubComponentWithType(category);
        categoryAssertion.assertSInstance().getTarget().setValue("fruits");

        tester.executeAjaxEvent(categoryAssertion.getTarget(), SINGULAR_PROCESS_EVENT);

        DropDownChoice elementChoices = options().element(1).getTarget(DropDownChoice.class);

        assertThat(elementChoices.getChoices()).containsOnly(OPTIONS.get("fruits").toArray());
    }

    @Test
    public void testSerialization(){
        JavaSerializer javaSerializer = new JavaSerializer("");

        AssertionsWComponent categoryAssertion = tester.getAssertionsForm().getSubComponentWithType(category);
        categoryAssertion.assertSInstance().getTarget().setValue("fruits");

        tester.executeAjaxEvent(categoryAssertion.getTarget(), SINGULAR_PROCESS_EVENT);

        byte[] serializedPage = javaSerializer.serialize(tester.getDummyPage());
        System.out.println(Bytes.bytes(serializedPage.length));

        for (int i = 0; i < 1000; i++) {

            categoryAssertion.assertSInstance().getTarget().setValue("fruits");

            tester.executeAjaxEvent(categoryAssertion.getTarget(), SINGULAR_PROCESS_EVENT);

            byte[] serializedPage2 = javaSerializer.serialize(tester.getDummyPage());


            Assert.assertEquals(serializedPage.length, serializedPage2.length);
        }

        System.out.println(Bytes.bytes(serializedPage.length));

    }

    // WITH SELECTED VALUES
    @Test
    public void preloadSelectedValuesWithSelectedValues(){
        AssertionsWComponent categoryAssertion = tester.getAssertionsForm().getSubComponentWithType(category);

        categoryAssertion.assertSInstance().getTarget().setValue("vegetables");
        tester.getAssertionsForm().getSubComponentWithType(element).assertSInstance().getTarget().setValue("radish");

        DropDownChoice categoryChoices = options().element(0).getTarget(DropDownChoice.class);
        DropDownChoice elementChoices = options().element(1).getTarget(DropDownChoice.class);

        tester.newFormTester().setValue(categoryChoices, "vegetables");

        assertThat(categoryChoices.getChoices()).containsOnly(OPTIONS.keySet().toArray());
        assertThat(categoryChoices.getValue()).isEqualTo("vegetables");

        assertThat(elementChoices.getChoices()).containsOnly(OPTIONS.get("vegetables").toArray());
        assertThat(elementChoices.getValue()).isEqualTo("radish");
    }

    // WITH UNEXISTANT SELECTED VALUES
    @Test
    public void addPreloadedOptionsToListIfNotPresentWithUnexistantSelectedValues(){
        tester.getAssertionsForm().getSubComponentWithType(category).assertSInstance().getTarget().setValue("special");
        tester.getAssertionsForm().getSubComponentWithType(element).assertSInstance().getTarget().setValue("gluten");

        DropDownChoice elementChoices = options().element(1).getTarget(DropDownChoice.class);

        tester.getAssertionsForm().getSubComponentWithType(category).assertSInstance().isValueEquals("special");
        tester.getAssertionsForm().getSubComponentWithType(element).assertSInstance().isValueEquals("gluten");

        assertThat(elementChoices.getChoices()).containsOnly("gluten");
    }


    // WITH UNEXISTANT DEPENDEND SELECTED VALUES
    @Test
    public void addPreloadedOptionsToListIfNotPresentWithUnexistantDependendSelectedValues(){
        tester.getAssertionsForm().getSubComponentWithType(category).assertSInstance().getTarget().setValue("vegetables");
        tester.getAssertionsForm().getSubComponentWithType(element).assertSInstance().getTarget().setValue("gluten");

        DropDownChoice categoryChoices = options().element(0).getTarget(DropDownChoice.class);
        DropDownChoice elementChoices = options().element(1).getTarget(DropDownChoice.class);

        tester.getAssertionsForm().getSubComponentWithType(category).assertSInstance().isValueEquals("vegetables");
        tester.getAssertionsForm().getSubComponentWithType(element).assertSInstance().isValueEquals("gluten");

        assertThat(categoryChoices.getChoices()).contains("vegetables");
        assertThat(elementChoices.getChoices()).contains("gluten").containsAll(OPTIONS.get("vegetables"));
    }

    @Test
    public void whenChangingValueRemovesDanglingOptionsWithUnexistantDependendSelectedValues() {
        tester.getAssertionsForm().getSubComponentWithType(category).assertSInstance().getTarget().setValue("condiments");

        tester.executeAjaxEvent(tester.getAssertionsForm()
                .getSubComponentWithType(category).getTarget(), SINGULAR_PROCESS_EVENT);

        DropDownChoice elementChoices = options().element(1).getTarget(DropDownChoice.class);

        assertThat(elementChoices.getChoices()).containsOnly(OPTIONS.get("condiments").toArray());
    }

    // MULTIPLOS DEPENDS ON
    @Test
    public void multiplesDependsOn() {
        assertThat(b.isDependentType(a)).isTrue();
        assertThat(c.isDependentType(a)).isTrue();
        assertThat(a.isDependentType(b)).isFalse();
        assertThat(a.isDependentType(c)).isFalse();
        assertThat(b.isDependentType(c)).isFalse();
        assertThat(c.isDependentType(b)).isFalse();
    }
}
