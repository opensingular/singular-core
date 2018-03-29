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

package org.opensingular.form.wicket.mapper.tree;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.opensingular.form.provider.Config;
import org.opensingular.form.wicket.WicketBuildContext;
import org.opensingular.form.wicket.mapper.search.SearchModalBodyPanel;
import org.opensingular.lib.commons.lambda.IConsumer;

@SuppressWarnings("unchecked")
public class SearchModalBodyTreePanel extends SearchModalBodyPanel {

    public SearchModalBodyTreePanel(String id, WicketBuildContext ctx, IConsumer<AjaxRequestTarget> selectCallback) {
        super(id, ctx, selectCallback);
    }

    @Override
    public WebMarkupContainer buildResultTable(Config config) {
        return new WebMarkupContainer(RESULT_TABLE_ID);
    }
}