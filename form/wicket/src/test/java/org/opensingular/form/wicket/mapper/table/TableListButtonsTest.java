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

package org.opensingular.form.wicket.mapper.table;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensingular.form.view.list.SViewListByTable;
import org.opensingular.form.wicket.helpers.SingularFormDummyPageTester;
import org.opensingular.form.wicket.mapper.buttons.InserirButton;
import org.opensingular.form.wicket.mapper.buttons.RemoverButton;
import org.opensingular.form.wicket.mapper.list.ListTestUtil;
import org.opensingular.lib.commons.lambda.ISupplier;
import org.opensingular.lib.wicket.util.ajax.ActionAjaxButton;


public class TableListButtonsTest {

    protected SingularFormDummyPageTester tester;

    @Before
    public void setUp(){
        tester = new SingularFormDummyPageTester();
    }

    @Test
    public void verifyDontHaveActionButton() {

        ISupplier<SViewListByTable> viewListByTable = (ISupplier<SViewListByTable>) () -> new SViewListByTable()
                .disableDelete()
                .disableAdd();

        tester.getDummyPage().setTypeBuilder(m -> ListTestUtil.buildTableForButons(m, viewListByTable));
        tester.startDummyPage();
        tester.getAssertionsForm().findSubComponent(b -> b instanceof InserirButton).isNull();
        Assert.assertTrue(!tester.getAssertionsForm().findSubComponent(b -> b instanceof RemoverButton).getTarget(ActionAjaxButton.class).isVisible());
        AbstractLink linkAddNewElement = ListTestUtil.findAddButton(tester);
        Assert.assertTrue(!linkAddNewElement.isVisible() || !linkAddNewElement.isVisibleInHierarchy());
    }

    @Test
    public void verifyHaveAllActionButtons() {
        //Table List contains 3 buttons : Edit, New, Remove
        ISupplier<SViewListByTable> viewListByTable = (ISupplier<SViewListByTable>) () -> new SViewListByTable()
                .enableInsert();

        tester.getDummyPage().setTypeBuilder(m -> ListTestUtil.buildTableForButons(m, viewListByTable));
        tester.startDummyPage();
        tester.getAssertionsForm().findSubComponent(b -> b instanceof InserirButton).isNotNull();
        tester.getAssertionsForm().findSubComponent(b -> b instanceof RemoverButton).isNotNull();
        AbstractLink linkAddNewElement = ListTestUtil.findAddButton(tester);
        Assert.assertTrue(linkAddNewElement.isVisible() || linkAddNewElement.isVisibleInHierarchy());
    }

}
