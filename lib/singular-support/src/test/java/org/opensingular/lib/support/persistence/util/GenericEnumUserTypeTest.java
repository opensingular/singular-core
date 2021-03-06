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

package org.opensingular.lib.support.persistence.util;


import org.hibernate.HibernateException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensingular.lib.support.persistence.entityanddao.DatabaseConfigurationMock;
import org.opensingular.lib.support.persistence.entityanddao.TestDAO;
import org.opensingular.lib.support.persistence.entityanddao.TestEntity;
import org.opensingular.lib.support.persistence.enums.SimNao;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DatabaseConfigurationMock.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class GenericEnumUserTypeTest {

    @Inject
    private TestDAO dao;

    /**
     * Verifica se a referencia estática ao nome da classe está de acordo com o pacote
     * onde a classe de fato está.
     */
    @Test
    public void testStaticClassReference(){
        Assert.assertEquals(GenericEnumUserType.CLASS_NAME, GenericEnumUserType.class.getName());
    }

    @Test
    public void sqlTypesTest(){
        GenericEnumUserType type = new GenericEnumUserType();
        Assert.assertNull(type.sqlTypes());

        Properties properties = new Properties();
        properties.setProperty("enumClass", SimNao.class.getName());
        type.setParameterValues(properties);

        Assert.assertEquals(SimNao.class, type.returnedClass());

        Assert.assertEquals(1, type.sqlTypes().length);
    }

    @Test(expected = HibernateException.class)
    public void setParametersValueExceptionTest(){
        GenericEnumUserType type = new GenericEnumUserType();

        Properties properties = new Properties();
        properties.setProperty("enumClass", "empty");
        type.setParameterValues(properties);
    }

    @Test
    public void testMethodsWithReturnIgualParameter(){
        GenericEnumUserType type = new GenericEnumUserType();

        Assert.assertFalse(type.equals("x","y"));
        Assert.assertTrue(type.equals("x","x"));

        Assert.assertFalse(type.isMutable());

        Assert.assertEquals(120, type.hashCode("x"));

        Assert.assertEquals("some string", type.disassemble(new String("some string")));

        Double toCopy = new Double(123456.00);
        Assert.assertEquals(toCopy, type.deepCopy(toCopy));

        Integer assemble = new Integer(987);
        Assert.assertEquals(assemble, type.assemble(assemble, null));

        Integer replaced = 123456;
        Assert.assertEquals(123456, type.replace(replaced, 123, null));
    }

    @Test
    @Transactional
    public void testNullSafeGetAndSet() throws SQLException {
        TestEntity newObj = new TestEntity(20, "name", "field");
        dao.save(newObj);
        TestEntity testEntities = dao.listAll().get(0);

        dao.saveOrUpdate(newObj);

        testEntities.setSimNaoEnum(SimNao.SIM);
        dao.saveOrUpdate(newObj);

        SimNao simNaoEnum = dao.find(20).get().getSimNaoEnum();

        Assert.assertEquals(SimNao.SIM, simNaoEnum);
    }


    public enum TestEnum{
        VAL_1, VAL_2;
    }
}
