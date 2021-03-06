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

package org.opensingular.form.wicket.mapper.masterdetail;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.junit.Test;
import org.opensingular.form.STypeList;
import org.opensingular.form.type.core.STypeMonetary;
import org.opensingular.form.view.list.SViewListByMasterDetail;
import org.opensingular.form.wicket.helpers.SingularFormDummyPageTester;

import java.math.BigDecimal;

public class MasterDetailAndUpdateListenerTest {

    @Test
    public void test() {

        SingularFormDummyPageTester ctx = new SingularFormDummyPageTester();
        ctx.getDummyPage().setTypeBuilder(root ->  {
            STypeList values = root.addFieldListOfComposite("valores", "valor");
            values.withView(SViewListByMasterDetail::new);
            STypeMonetary total = (STypeMonetary) root.addField("total", STypeMonetary.class);
            total.asAtr()
                    .dependsOn(values)
                    .updateListener(i -> {
                        i.findNearest(total).ifPresent(ix -> ix.setValue("10"));
                    });
        });

        ctx.getDummyPage().setAsEditView();
        ctx.startDummyPage();

        AjaxLink addButton = ctx.getAssertionsForm().getSubComponentWithId("addButton").getTarget(AjaxLink.class);

        ctx.executeAjaxEvent(addButton, "click");

        MasterDetailModal modal = ctx.getAssertionsForm().getSubComponents(MasterDetailModal.class).element(0).getTarget(MasterDetailModal.class);

        ctx.newFormTester().submit(modal.addButton);
        ctx.getAssertionsForm().getSubComponentWithTypeNameSimple("total").assertSInstance().isValueEquals(new BigDecimal("10"));

    }

}