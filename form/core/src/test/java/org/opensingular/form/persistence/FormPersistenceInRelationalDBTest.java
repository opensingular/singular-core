/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package org.opensingular.form.persistence;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opensingular.form.SDictionary;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInfoPackage;
import org.opensingular.form.SInfoType;
import org.opensingular.form.SPackage;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.TestCaseForm;
import org.opensingular.form.TypeBuilder;
import org.opensingular.form.persistence.FormPersistenceInRelationalDBTest.TestPackage.Form;
import org.opensingular.form.type.core.STypeInteger;
import org.opensingular.form.type.core.STypeString;

/**
 * @author Edmundo Andrade
 */
@RunWith(Parameterized.class)
public class FormPersistenceInRelationalDBTest extends TestCaseForm {
    private RelationalDatabase db;
    private FormRespository<Form, SIComposite> repo;
    private Form form;

    public FormPersistenceInRelationalDBTest(TestFormConfig testFormConfig) {
        super(testFormConfig);
    }

    @Before
    public void setUp() {
        db = mock(RelationalDatabase.class);
        repo = new FormPersistenceInRelationalDB<>(db, null, Form.class);
        SDictionary dictionary = createTestDictionary();
        form = dictionary.getType(Form.class);
    }

    @Test
    public void keyFromObject() {
        FormKey key = repo.keyFromObject("CODE$Integer$1");
        assertEquals("CODE$Integer$1", key.toStringPersistence());
        HashMap<String, Object> internalMap = ((FormKeyRelational) key).getValue();
        assertEquals(1, internalMap.size());
        assertEquals(1, internalMap.get("CODE"));
        assertEquals("CODE$Integer$1", repo.keyFromObject(internalMap).toStringPersistence());
    }

    @Test
    public void countAll() {
        when(db.query("select count(*) from FORM T1", emptyList())).thenReturn(querySingleResult(42L));
        assertEquals(42L, repo.countAll());
    }

    @Test(expected = SingularFormNotFoundException.class)
    public void load() {
        when(db.query(eq(
                "select T1.name, T1.customer, T2.name, T1.CODE from FORM T1 left join CUST T2 on T1.customer = T2.ID where T1.CODE = ?"),
                eq(Arrays.asList(42042)), isNull(), isNull(), any())).thenReturn(Collections.emptyList());
        repo.load(formKey(42042));
    }

    @Test
    public void update() {
        SIComposite previousInstance = form.newInstance();
        previousInstance.setValue("name", "My previous form name");
        previousInstance.setValue("customerKey", 7);
        previousInstance.setValue("customerDisplay", "Edworld");
        FormKey.setOnInstance(previousInstance, formKey(4242));
        SIComposite formInstance = form.newInstance();
        formInstance.setValue("name", "My form name");
        formInstance.setValue("customerKey", 7);
        formInstance.setValue("customerDisplay", "Edworld");
        FormKey.setOnInstance(formInstance, formKey(4242));
        when(db.query(eq(
                "select T1.name, T1.customer, T2.name, T1.CODE from FORM T1 left join CUST T2 on T1.customer = T2.ID where T1.CODE = ?"),
                eq(Arrays.asList(4242)), isNull(), isNull(), any())).thenReturn(Arrays.asList(previousInstance));
        when(db.exec("update FORM T1 set T1.name = ? where T1.CODE = ?", Arrays.asList("My form name", 4242)))
                .thenReturn(1);
        repo.update(formInstance, null);
    }

    @Test
    public void listByCriteriaOrdering() {
        when(db.query(eq(
                "select T1.name, T1.customer, T2.name, T1.CODE from FORM T1 left join CUST T2 on T1.customer = T2.ID where T1.name LIKE ? order by T1.name"),
                eq(Arrays.asList("%Document X%")), isNull(), isNull(), any()))
                        .thenReturn(Arrays.asList(form.newInstance(), form.newInstance()));
        List<SIComposite> list = ((FormPersistenceInRelationalDB<Form, SIComposite>) repo)
                .list(Criteria.isLike(form.name, "%Document X%"), OrderByField.asc(form.name));
        assertEquals(2, list.size());
    }

    @Test
    public void listByExample() {
        SIComposite formExample = form.newInstance();
        formExample.setValue("name", "Requirement 1/2008");
        when(db.query(eq(
                "select T1.name, T1.customer, T2.name, T1.CODE from FORM T1 left join CUST T2 on T1.customer = T2.ID where T1.name = ?"),
                eq(Arrays.asList("Requirement 1/2008")), isNull(), isNull(), any()))
                        .thenReturn(Arrays.asList(form.newInstance()));
        List<SIComposite> list = ((FormPersistenceInRelationalDB<Form, SIComposite>) repo).list(formExample);
        assertEquals(1, list.size());
    }

    private List<Object[]> querySingleResult(Object value) {
        List<Object[]> result = new ArrayList<>();
        result.add(new Object[] { value });
        return result;
    }

    private FormKey formKey(int id) {
        HashMap<String, Object> key = new LinkedHashMap<>();
        key.put("CODE", id);
        return new FormKeyRelational(key);
    }

    @SInfoPackage(name = "testPackage")
    public static final class TestPackage extends SPackage {
        @SInfoType(name = "Customer", spackage = TestPackage.class)
        public static final class Customer extends STypeComposite<SIComposite> {
            public STypeString name;

            @Override
            protected void onLoadType(TypeBuilder tb) {
                asAtr().label("Customer");
                name = addFieldString("name");
                // relational mapping
                asSQL().table("CUST").tablePK("ID");
                name.asSQL().column();
            }
        }

        @SInfoType(name = "Form", spackage = TestPackage.class)
        public static final class Form extends STypeComposite<SIComposite> {
            public STypeString name;
            public STypeInteger customerKey;
            public STypeString customerDisplay;

            @Override
            protected void onLoadType(TypeBuilder tb) {
                asAtr().label("Formulary");
                name = addFieldString("name");
                customerKey = addField("customerKey", STypeInteger.class);
                customerDisplay = addField("customerDisplay", STypeString.class);
                // relational mapping
                asSQL().table("FORM").tablePK("CODE");
                asSQL().addTableFK("customer", Customer.class);
                name.asSQL().column();
                customerKey.asSQL().column("customer");
                customerDisplay.asSQL().foreignColumn("name", "customer", Customer.class);
            }
        }
    }
}