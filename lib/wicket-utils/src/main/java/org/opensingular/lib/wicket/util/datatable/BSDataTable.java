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

package org.opensingular.lib.wicket.util.datatable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.opensingular.lib.commons.lambda.IConsumer;
import org.opensingular.lib.wicket.util.util.Shortcuts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BSDataTable<T, S> extends DataTable<T, S> {
    public static final Long DEFAULT_ROWS_PER_PAGE = 10L;

    private boolean            stripedRows          = true;
    private boolean            hoverRows            = true;
    private boolean            borderedTable        = true;
    private boolean            advanceTable         = false;
    private boolean            condensedTable       = false;
    private boolean            showNoRecordsToolbar = true;
    private IConsumer<Item<T>> onNewRowItem         = IConsumer.noop();
    private IModel<String>     noRecordsMessage;

    public BSDataTable(String id
            , List<? extends IColumn<T, S>> columns
            , ISortableDataProvider<T, S> dataProvider) {
        this(id, ensureSerializable(columns), dataProvider, new ResourceModel("datatable.no-records-found"));
    }

    public BSDataTable(String id
            , List<? extends IColumn<T, S>> columns
            , ISortableDataProvider<T, S> dataProvider
            , IModel<String> noRecordsMessage) {
        super(id, ensureSerializable(columns), dataProvider, DEFAULT_ROWS_PER_PAGE);
        this.noRecordsMessage = noRecordsMessage;

        setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);

        final AbstractToolbar headersToolbar = newHeadersToolbar(dataProvider);
        if (headersToolbar != null) {
            addTopToolbar(headersToolbar);
        }

        final AbstractToolbar noRecordsToolbar = newNoRecordsToolbar();
        if (noRecordsToolbar != null) {
            addBottomToolbar(noRecordsToolbar);
        }

        final AbstractToolbar paginationToolbar = newPaginationToolbar();
        if (paginationToolbar != null) {
            addBottomToolbar(paginationToolbar);
        }
    }

    private static <T extends Serializable> List<T> ensureSerializable(List<T> list) {
        return (list instanceof Serializable) ? list : new ArrayList<>(list);
    }

    protected AbstractToolbar newHeadersToolbar(ISortableDataProvider<T, S> dataProvider) {
        return new BSHeadersToolbar<>(this, dataProvider);
    }

    protected AbstractToolbar newNoRecordsToolbar() {
        if (showNoRecordsToolbar) {
            return new NoRecordsToolbar(this, noRecordsMessage);
        } else {
            return null;
        }
    }

    protected AbstractToolbar newPaginationToolbar() {
        return new BSPaginationToolbar(this);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        StringBuilder sbClass = new StringBuilder(StringUtils.defaultString(tag.getAttribute("class")));
        sbClass.append(" table");
        if (isStripedRows())
            sbClass.append(" table-striped");
        if (isHoverRows())
            sbClass.append(" table-hover");
        if (isAdvanceTable())
            sbClass.append(" table-advance");
        else
            sbClass.append(" dataTable");
        if (isBorderedTable())
            sbClass.append(" table-bordered");
        if (isCondensedTable())
            sbClass.append(" table-condensed");
        tag.put("class", sbClass.toString().trim());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        getCaption().setOutputMarkupId(false).setOutputMarkupPlaceholderTag(false);
        getCaption().add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);
                boolean renderCaption = !component.getRenderBodyOnly();
                getCaption()
                        .setOutputMarkupId(renderCaption)
                        .setOutputMarkupPlaceholderTag(renderCaption);
            }
        });
    }

    @Override
    protected Item<T> newRowItem(String id, int index, IModel<T> model) {
        Item<T> rowItem = super.newRowItem(id, index, model);
        onNewRowItem.accept(rowItem);
        return rowItem;
    }

    public BSDataTable<T, S> setOnNewRowItem(IConsumer<Item<T>> onNewRowItem) {
        this.onNewRowItem = IConsumer.noopIfNull(onNewRowItem);
        return this;
    }

    public boolean isStripedRows() {
        return stripedRows;
    }

    public BSDataTable<T, S> setStripedRows(boolean stripedRows) {
        this.stripedRows = stripedRows;
        return this;
    }

    public boolean isHoverRows() {
        return hoverRows;
    }

    public BSDataTable<T, S> setHoverRows(boolean hoverRows) {
        this.hoverRows = hoverRows;
        return this;
    }

    public boolean isAdvanceTable() {
        return advanceTable;
    }

    public BSDataTable<T, S> setAdvanceTable(boolean advanceTable) {
        this.advanceTable = advanceTable;
        return this;
    }

    public boolean isBorderedTable() {
        return borderedTable;
    }

    public BSDataTable<T, S> setBorderedTable(boolean borderedTable) {
        this.borderedTable = borderedTable;
        return this;
    }

    public boolean isCondensedTable() {
        return condensedTable;
    }

    public BSDataTable<T, S> setCondensedTable(boolean condensedTable) {
        this.condensedTable = condensedTable;
        return this;
    }

    public BSDataTable<T, S> setRowsPerPage(Long items) {
        super.setItemsPerPage(ObjectUtils.defaultIfNull(items, DEFAULT_ROWS_PER_PAGE));
        return this;
    }

    public long getRowsPerPage() {
        return getItemsPerPage();
    }

    public boolean isShowNoRecordsToolbar() {
        return showNoRecordsToolbar;
    }

    public BSDataTable<T, S> setShowNoRecordsToolbar(boolean showNoRecordsToolbar) {
        this.showNoRecordsToolbar = showNoRecordsToolbar;
        return this;
    }
}
