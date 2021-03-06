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

package org.opensingular.form.report;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.filter.FilteringHeaderResponse;
import org.junit.Test;
import org.opensingular.form.PackageBuilder;
import org.opensingular.form.SType;
import org.opensingular.form.wicket.helpers.AssertionsWComponent;
import org.opensingular.form.wicket.helpers.SingularWicketTestCase;
import org.opensingular.lib.commons.table.ColumnType;
import org.opensingular.lib.commons.table.TableTool;
import org.opensingular.lib.commons.views.ViewGenerator;
import org.opensingular.lib.wicket.util.menu.MetronicMenuItem;
import org.opensingular.lib.wicket.util.resource.DefaultIcons;

import java.io.Serializable;
import java.util.Optional;

public class ReportPageTest extends SingularWicketTestCase {

    @Test
    public void testRendering() throws Exception {
        ReportPage reportPage = createPage();
        getTester().startPage(reportPage);
        assertTrue(getTester().getLastRenderedPage().equals(reportPage));

        //getTester().debugComponentTrees();
        //getTester().getAssertionsPage().debugComponentTree();

        AssertionsWComponent menuItem = getTester().getAssertionsForPath("app-body:menu:itens:0:menu-item").isInstanceOf(
                MetronicMenuItem.class);
        menuItem.getSubComponentWithId("title").asLabel().assertValue().isEqualTo("X1");
        //getTester().clickLink(menuItem.getSubComponentWithId("anchor").getTarget());
    }

    private static ReportPage createPage() {
        SingularFormReport report = new SingularFormReport() {


            @Override
            public Optional<SType<?>> getFilterType(PackageBuilder packageBuilder) {
                return Optional.empty();
            }

            @Override
            public String getReportName() {
                return "TestReport";
            }

            @Override
            public ViewGenerator getViewGenerator() {
                TableTool tableTool = new TableTool();
                tableTool.addColumn(ColumnType.STRING, "nome");
                tableTool.createSimpleTablePopulator()
                        .insertLine()
                        .setValue(0, "Danilo");
                return tableTool;
            }

            @Override
            public void loadReportInstance(String XML) {

            }

            @Override
            public String dumpReportInstanceXML() {
                return null;
            }


            @Override
            public void setParam(String key, Serializable val) {

            }

            @Override
            public Serializable getParam(String key) {
                return null;
            }

            @Override
            public Object getFilterValue() {
                return null;
            }

            @Override
            public void setFilterValue(Object value) {

            }
        };
        return new ReportPage(null) {
            @Override
            protected void configureMenu(ReportMenuBuilder menu) {
                menu.addItem(DefaultIcons.PENCIL, "X1", report);
            }

            @Override
            public void renderHead(IHeaderResponse response) {
                new FilteringHeaderResponse(response);
            }
        };
    }
}