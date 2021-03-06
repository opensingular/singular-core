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

package org.opensingular.form.wicket.mapper.selection;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Before;
import org.junit.Test;
import org.opensingular.form.SInstance;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.provider.Config;
import org.opensingular.form.provider.FilteredProvider;
import org.opensingular.form.provider.ProviderContext;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.view.SViewSearchModal;
import org.opensingular.form.wicket.helpers.AssertionsWComponentList;
import org.opensingular.form.wicket.helpers.SingularFormDummyPageTester;
import org.opensingular.form.wicket.mapper.search.SearchModalPanel;
import org.opensingular.lib.wicket.util.ajax.ActionAjaxLink;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class STypeStringModalSearchTest {

    private static STypeString selectType;
    private SingularFormDummyPageTester tester;

    private static void buildBaseType(STypeComposite<?> baseType) {
        selectType = baseType.addFieldString("favoriteFruit");
        selectType.withView(SViewSearchModal::new);
        selectType.asAtrProvider().filteredProvider(new FilteredProvider<String>() {
            @Override
            public void configureProvider(Config cfg) {
                cfg.getFilter().addFieldString("string");
                cfg.result().addColumn("Fruta");
                cfg.setCache(true);
            }

            @Override
            public List<String> load(ProviderContext<SInstance> context) {
                return Arrays.asList("strawberry", "apple", "orange", "banana");
            }
        });
    }

    void assertHasATable() {
        String responseTxt = tester.getLastResponse().getDocument();
        TagTester table = TagTester.createTagByAttribute(responseTxt, "table");
        assertThat(table).isNotNull();
    }

    void clickOpenLink() {
        AjaxLink ajaxLink = tester.getAssertionsForm()
                .getSubComponentWithId(SearchModalPanel.MODAL_TRIGGER_ID).getTarget(AjaxLink.class);
        tester.executeAjaxEvent(ajaxLink, "click");
    }

    @Before
    public void setUp() {
        tester = new SingularFormDummyPageTester();
        tester.getDummyPage().setTypeBuilder(STypeStringModalSearchTest::buildBaseType);
    }

    @Test
    public void showModalWhenClicked() {
        tester.startDummyPage();
        tester.assertContainsNot("Filtrar");

        clickOpenLink();

        tester.assertContains("Filtrar");

        assertHasATable();

        tester.assertContains("strawberry");
        tester.assertContains("apple");
        tester.assertContains("orange");
        tester.assertContains("banana");
    }

    @Test
    public void showPreviousValueWhenRendering() {
        tester.getDummyPage()
                .addInstancePopulator(instance -> instance.setValue(selectType.getNameSimple(), "apple"));
        tester.startDummyPage();

        tester.assertContains("apple");
        tester.assertContainsNot("strawberry");
    }

    @Test
    public void changeValueWhenSelected() {
        tester.getDummyPage()
                .addInstancePopulator(instance -> instance.setValue(selectType.getNameSimple(), "apple"));
        tester.startDummyPage();

        clickOpenLink();

        AssertionsWComponentList subComponents = tester.getAssertionsForm().getSubComponents(ActionAjaxLink.class);
        subComponents.hasSize(4);

        tester.executeAjaxEvent(subComponents.element(3).getTarget(), "click");

        tester.getAssertionsForm().getSubComponentWithType(selectType).assertSInstance().isValueEquals("banana");
    }

    @Test
    public void showDanglingValueOnOptions() {
        tester.getDummyPage()
                .addInstancePopulator(instance -> instance.setValue(selectType.getNameSimple(), "avocado"));
        tester.startDummyPage();

        tester.assertContains("avocado");
    }
}
