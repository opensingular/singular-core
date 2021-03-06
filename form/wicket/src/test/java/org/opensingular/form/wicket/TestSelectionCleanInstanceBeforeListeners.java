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

package org.opensingular.form.wicket;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.visit.IVisit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensingular.form.SType;
import org.opensingular.form.type.core.SIString;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.type.country.brazil.STypeUF;
import org.opensingular.form.wicket.helpers.SingularFormDummyPageTester;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.opensingular.form.wicket.AjaxUpdateListenersFactory.*;

public class TestSelectionCleanInstanceBeforeListeners implements Serializable {


    private transient SingularFormDummyPageTester tester;
    private transient STypeUF                     uf;
    private transient STypeString                 pais;
    private transient STypeString                 ecapital;
    private transient String                     PAIS_SIMPLE_PROVIDER           = "PAIS_SIMPLE_PROVIDER";
    private transient String                     ECAPITAL_UPDATE_LISTENER       = "ECAPITAL_UPDATE_LISTENER";
    /**
     * Records the execution order of update listeners and simple providers in the stype below
     */
    private transient List<Pair<String, String>> EXECUTION_ORDER_AND_PAIS_VALUE = new ArrayList<>();

    @Before
    public void testClear() throws Exception {
        EXECUTION_ORDER_AND_PAIS_VALUE.clear();
        tester = new SingularFormDummyPageTester();
        tester.getDummyPage().setTypeBuilder(root -> {
            uf = (STypeUF) root.addField("uf", STypeUF.class);
            uf.asAtr().label("UF");

            pais = root.addFieldString("pais");
            pais.asAtr().dependsOn(uf).label("Capital do país:");
            pais.selection().selfIdAndDisplay().simpleProvider(siString -> {
                EXECUTION_ORDER_AND_PAIS_VALUE.add(Pair.of(PAIS_SIMPLE_PROVIDER, siString.findNearest(pais).map(SIString::getValue).orElse(null)));
                if (siString.findNearest(uf.sigla).map(SIString::getValue).map("DF"::equalsIgnoreCase).orElse(false)) {
                    return Lists.newArrayList("Brasil");
                }
                return Collections.emptyList();
            });

            ecapital = root.addFieldString("ecapital");
            ecapital
                    .withUpdateListener(si -> {
                        EXECUTION_ORDER_AND_PAIS_VALUE.add(Pair.of(ECAPITAL_UPDATE_LISTENER, si.findNearest(pais).map(SIString::getValue).orElse(null)));
                        if (si.findNearest(pais).map(SIString::getValue).map("Brasil"::equals).orElse(false)) {
                            si.setValue("Brasil está selecionado!");
                        } else {
                            si.clearInstance();
                        }
                    })
                    .asAtr()
                    .label("É capital?")
                    .dependsOn(pais, uf);
        });
        tester.startDummyPage();
    }

    /**
     * In this scenario the we select "DF" in the uf drop down and then select "Brasil" in pais drop down.
     * After this selection we change the first drop down to "AC", this change impacts the simple provider from pais and "Brasil" is no longer a valid value.
     * When the the ecapital update listener runs it should find no value in pais field since its old value is not valid the selection anymore.
     * @throws Exception
     */
    @Test
    public void testChangeUfShouldClearPaisBeforeEcapitalUpdateListener() throws Exception {
        EXECUTION_ORDER_AND_PAIS_VALUE.clear();
        setValueOnFieldOneAndCallAjaxValidate("DF", uf);
        setValueOnFieldOneAndCallAjaxValidate("Brasil", pais);
        setValueOnFieldOneAndCallAjaxValidate("AC", uf);
        printExecutionOrder();
        String paisValue = tester.getAssertionsInstance().getTarget().getField(pais).getValue();
        Assert.assertEquals(null, paisValue);
        Assert.assertEquals(3, EXECUTION_ORDER_AND_PAIS_VALUE.stream().filter(p -> ECAPITAL_UPDATE_LISTENER.equals(p.getKey())).count());
        Assert.assertEquals(Lists.newArrayList(null, "Brasil", null), EXECUTION_ORDER_AND_PAIS_VALUE.stream().filter(p -> ECAPITAL_UPDATE_LISTENER.equals(p.getKey())).map(Pair::getValue).collect(Collectors.toList()));
    }


    @Test
    public void testChangeUfShouldClearEcapitalField() throws Exception {
        EXECUTION_ORDER_AND_PAIS_VALUE.clear();
        setValueOnFieldOneAndCallAjaxValidate("DF", uf);
        setValueOnFieldOneAndCallAjaxValidate("Brasil", pais);
        setValueOnFieldOneAndCallAjaxValidate("AC", uf);
        printExecutionOrder();
        String ecapitalValue = tester.getAssertionsInstance().getTarget().getField(ecapital).getValue();
        Assert.assertEquals(null, ecapitalValue);
        Assert.assertEquals(3, EXECUTION_ORDER_AND_PAIS_VALUE.stream().filter(p -> ECAPITAL_UPDATE_LISTENER.equals(p.getKey())).count());
    }

    private void setValueOnFieldOneAndCallAjaxValidate(String value, SType<?> field) {
        FormTester       formTester = tester.newFormTester();
        FormComponent<?> component  = findFormComponentForType(field);
        formTester.setValue(component, value);
        callAjaxProcessEvent(component);
    }

    private void printExecutionOrder() {
        System.out.println("Execution Order and Pais Value Pairs: ");
        System.out.println(EXECUTION_ORDER_AND_PAIS_VALUE.toString().replaceAll("\\),", "),\n"));
    }

    private FormComponent<?> findFormComponentForType(SType<?> field) {
        FormComponent formComponent = null;
        Component     component     = tester.getAssertionsForm().getSubComponentWithType(field).getTarget();
        if (component instanceof FormComponent<?>) {
            formComponent = (FormComponent<?>) component;
        } else if (component instanceof MarkupContainer) {
            MarkupContainer cotainer = (MarkupContainer) component;
            formComponent = cotainer.visitChildren(FormComponent.class, this::visit);
        }
        return formComponent;
    }

    private <S extends Component, R> void visit(S s, IVisit<R> riVisit) {
        if (s instanceof FormComponent) {
            riVisit.stop((R) s);
        }
    }


    private void callAjaxProcessEvent(Component fieldOne) {
        tester.executeAjaxEvent(fieldOne, SINGULAR_PROCESS_EVENT);
    }

}
