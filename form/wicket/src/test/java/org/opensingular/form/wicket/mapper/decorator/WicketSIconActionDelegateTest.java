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

package org.opensingular.form.wicket.mapper.decorator;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.junit.Test;
import org.opensingular.form.decorator.action.SInstanceAction.FormDelegate;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.wicket.helpers.SingularFormDummyPageTester;
import org.opensingular.lib.commons.ref.Out;

import java.util.Arrays;

public class WicketSIconActionDelegateTest {

    @Test
    @SuppressWarnings("unchecked")
    public void test() {
        SingularFormDummyPageTester tester = new SingularFormDummyPageTester();
        tester.getDummyPage().setTypeBuilder(c -> c.addField("bla", STypeString.class).asAtr().help("HELP!!!"));
        tester.startDummyPage();

        Out<FormDelegate> fd = new Out<>();

        WicketSIconActionDelegate delegate = new WicketSIconActionDelegate(Model.of(), Arrays.asList(tester.getLastRenderedPage()));
        delegate.showMessage("Text", "text");
        delegate.showMessage("HTML", "<b>html</b>");
        delegate.showMessage("Markdown", "**markdown***");
        delegate.openForm(fd, "form", null, () -> tester.getDummyPage().getInstance(), it -> Arrays.asList());
        fd.get().close();
        delegate.getInstanceRef();
        delegate.getInternalContext(Component.class);
    }

}
