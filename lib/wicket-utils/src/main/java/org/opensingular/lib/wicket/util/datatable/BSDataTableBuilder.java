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

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.ISortableTreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.opensingular.lib.commons.lambda.IConsumer;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.wicket.util.datatable.column.BSActionColumn;
import org.opensingular.lib.wicket.util.datatable.column.BSPropertyActionColumn;
import org.opensingular.lib.wicket.util.datatable.column.BSPropertyColumn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @param <T>       Tipo de objeto que sera renderizado pelas celulas da coluna
 * @param <S>       Propriedade de Ordenacao
 * @param <PREVCOL> Coluna
 */
public class BSDataTableBuilder<T, S, PREVCOL extends IColumn<T, S>> implements Serializable {

    public interface BSActionColumnCallback<T, S> extends IConsumer<BSActionColumn<T, S>> {
    }

    private final List<? extends IColumn<T, S>> columns     = new ArrayList<>();
    private       ISortableDataProvider<T, S>   dataProvider;
    private       ISortableTreeProvider<T, S>   treeProvider;
    private       Long                          rowsPerPage = null;

    private boolean        stripedRows          = true;
    private boolean        hoverRows            = true;
    private boolean        borderedTable        = true;
    private boolean        advanceTable         = false;
    private boolean        condensedTable       = false;
    private boolean        showNoRecordsToolbar = true;
    private IModel<String> noRecordsMessage     = new ResourceModel("datatable.no-records-found");


    public BSDataTableBuilder() {
    }

    public BSDataTableBuilder(ISortableDataProvider<T, S> dataProvider) {
        setDataProvider(dataProvider);
    }

    public BSDataTableBuilder(ISortableTreeProvider<T, S> treeProvider) {
        setTreeProvider(treeProvider);
    }

    public static <T> BSDataTableBuilder<T, ?, ?> create() {
        return new BSDataTableBuilder<>();
    }

    @SuppressWarnings("unchecked")
    public static <T, S> BSDataTableBuilder<T, S, IColumn<T, S>> create(Class<T> beanType, Class<S> sortType) {
        return (BSDataTableBuilder<T, S, IColumn<T, S>>) create();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <C extends IColumn<T, S>> BSDataTableBuilder<T, S, C> appendColumn(C column) {
        ((List) columns).add(column);
        return (BSDataTableBuilder<T, S, C>) this;
    }

    @SuppressWarnings("unchecked")
    public BSDataTableBuilder<T, S, PREVCOL> configurePreviousColumn(IConsumer<PREVCOL> columnConfig) {
        columnConfig.accept((PREVCOL) columns.get(columns.size() - 1));
        return this;
    }

    public BSDataTableBuilder<T, S, BSPropertyColumn<T, S>> appendPropertyColumn(IModel<String> displayModel, IFunction<T, Object> propertyFunction) {
        return appendColumn(new BSPropertyColumn<>(displayModel, propertyFunction));
    }

    public BSDataTableBuilder<T, S, BSPropertyColumn<T, S>> appendPropertyColumn(String headerTitle, IFunction<T, Object> propertyFunction) {
        return appendPropertyColumn(Model.of(headerTitle), propertyFunction);
    }

    public BSDataTableBuilder<T, S, BSPropertyColumn<T, S>> appendPropertyColumn(IModel<String> displayModel, String propertyExpression) {
        return appendColumn(new BSPropertyColumn<>(displayModel, propertyExpression));
    }

    public BSDataTableBuilder<T, S, BSPropertyColumn<T, S>> appendPropertyColumn(String headerTitle, String propertyExpression) {
        return appendPropertyColumn(Model.of(headerTitle), propertyExpression);
    }

    public BSDataTableBuilder<T, S, BSPropertyColumn<T, S>> appendPropertyColumn(IModel<String> displayModel, S sortProperty, IFunction<T, ?> propertyFunction) {
        return appendColumn(new BSPropertyColumn<>(displayModel, sortProperty, propertyFunction));
    }

    public BSDataTableBuilder<T, S, BSPropertyColumn<T, S>> appendPropertyColumn(String headerTitle, S sortProperty, IFunction<T, ?> propertyFunction) {
        return appendPropertyColumn(Model.of(headerTitle), sortProperty, propertyFunction);
    }

    public BSDataTableBuilder<T, S, BSPropertyColumn<T, S>> appendPropertyColumn(IModel<String> displayModel, S sortProperty, String propertyExpression) {
        return appendColumn(new BSPropertyColumn<>(displayModel, sortProperty, propertyExpression));
    }

    public BSDataTableBuilder<T, S, BSPropertyColumn<T, S>> appendPropertyColumn(IModel<String> displayModel, S sortProperty, String propertyExpression, String cssClass) {
        return appendColumn(new BSPropertyColumn<T, S>(displayModel, sortProperty, propertyExpression) {
            @Override
            public String getCssClass() {
                return cssClass;
            }
        });
    }

    public BSDataTableBuilder<T, S, BSPropertyColumn<T, S>> appendPropertyColumn(String headerTitle, S sortProperty, String propertyExpression) {
        return appendPropertyColumn(Model.of(headerTitle), sortProperty, propertyExpression);
    }

    public BSDataTableBuilder<T, S, BSPropertyActionColumn<T, S>> appendPropertyActionColumn(IModel<String> displayModel,
                                                                                             IFunction<T, Object> propertyFunction) {
        return appendColumn(new BSPropertyActionColumn<>(displayModel, propertyFunction));
    }

    public BSDataTableBuilder<T, S, BSActionColumn<T, S>> appendActionColumn(IModel<String> displayModel, BSActionColumnCallback<T, S> callback) {
        BSActionColumn<T, S> column = new BSActionColumn<>(displayModel);
        callback.accept(column);
        return appendColumn(column);
    }

    public BSDataTableBuilder<T, S, BSActionColumn<T, S>> appendStaticActionColumn(BSActionColumn column) {
        return appendColumn(column);
    }

    public BSDataTableBuilder<T, S, BSActionColumn<T, S>> appendActionColumn(String headerTitle, BSActionColumnCallback<T, S> callback) {
        return appendActionColumn(Model.of(headerTitle), callback);
    }

    public BSDataTableBuilder<T, S, ?> setDataProvider(ISortableDataProvider<T, S> dataProvider) {
        this.dataProvider = dataProvider;
        return this;
    }

    public BSDataTableBuilder<T, S, ?> setTreeProvider(ISortableTreeProvider<T, S> treeProvider) {
        this.treeProvider = treeProvider;
        return this;
    }

    public BSDataTableBuilder<T, S, ?> setStripedRows(boolean stripedRows) {
        this.stripedRows = stripedRows;
        return this;
    }

    public BSDataTableBuilder<T, S, ?> setHoverRows(boolean hoverRows) {
        this.hoverRows = hoverRows;
        return this;
    }

    public BSDataTableBuilder<T, S, ?> setAdvancedTable(boolean advanceTable) {
        this.advanceTable = advanceTable;
        return this;
    }

    public BSDataTableBuilder<T, S, ?> setBorderedTable(boolean borderedTable) {
        this.borderedTable = borderedTable;
        return this;
    }

    public BSDataTableBuilder<T, S, ?> setCondensedTable(boolean condensedTable) {
        this.condensedTable = condensedTable;
        return this;
    }

    public BSDataTableBuilder<T, S, ?> setRowsPerPage(long rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
        return this;
    }

    public BSDataTableBuilder<T, S, ?> withNoRecordsToolbar() {
        showNoRecordsToolbar = false;
        return this;
    }

    public BSDataTableBuilder<T, S, PREVCOL> disablePagination() {
        setRowsPerPage(Long.MAX_VALUE);
        return this;
    }

    public IModel<String> getNoRecordsMessage() {
        return noRecordsMessage;
    }

    public BSDataTableBuilder<T, S, PREVCOL> setNoRecordsMessage(IModel<String> noRecordsMessage) {
        this.noRecordsMessage = noRecordsMessage;
        return this;
    }

    public BSDataTable<T, S> build(String id) {
        return newDatatable(id, new ArrayList<>(columns), dataProvider)
                .setRowsPerPage(rowsPerPage)
                .setStripedRows(stripedRows)
                .setHoverRows(hoverRows)
                .setAdvanceTable(advanceTable)
                .setBorderedTable(borderedTable)
                .setCondensedTable(condensedTable)
                .setShowNoRecordsToolbar(showNoRecordsToolbar);
    }

    protected BSDataTable<T, S> newDatatable(String id, List<? extends IColumn<T, S>> columns, ISortableDataProvider<T, S> dataProvider) {
        return new BSDataTable<>(id, new ArrayList<>(columns), dataProvider, noRecordsMessage);
    }

    public BSFlexDataTable<T, S> buildFlex(String id) {
        BSFlexDataTable<T, S> table = new BSFlexDataTable<>(id, new ArrayList<>(columns), dataProvider);
        table
                .setRowsPerPage(rowsPerPage)
                .setStripedRows(stripedRows)
                .setHoverRows(hoverRows)
                .setBorderedTable(borderedTable)
                .setCondensedTable(condensedTable);
        return table;
    }

    public BSTableTree<T, S> buildTree(String id) {
        return new BSTableTree<>(id, new ArrayList<>(columns), treeProvider)
                .setRowsPerPage(rowsPerPage)
                .setStripedRows(stripedRows)
                .setHoverRows(hoverRows)
                .setBorderedTable(borderedTable)
                .setCondensedTable(condensedTable);
    }
}
