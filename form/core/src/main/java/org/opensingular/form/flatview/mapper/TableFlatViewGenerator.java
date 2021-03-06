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

package org.opensingular.form.flatview.mapper;

import org.apache.commons.lang3.StringUtils;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SIList;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.STypeComposite;
import org.opensingular.form.flatview.AbstractFlatViewGenerator;
import org.opensingular.form.flatview.FlatViewContext;
import org.opensingular.form.flatview.FlatViewGenerator;
import org.opensingular.form.view.ViewResolver;
import org.opensingular.form.view.list.SViewListByTable;
import org.opensingular.lib.commons.canvas.DocumentCanvas;
import org.opensingular.lib.commons.canvas.EmptyDocumentCanvas;
import org.opensingular.lib.commons.canvas.FormItem;
import org.opensingular.lib.commons.canvas.table.TableBodyCanvas;
import org.opensingular.lib.commons.canvas.table.TableCanvas;
import org.opensingular.lib.commons.canvas.table.TableRowCanvas;

import java.util.ArrayList;
import java.util.List;

/**
 * Show list of composites in a table
 */
public class TableFlatViewGenerator extends AbstractFlatViewGenerator {
    @Override
    protected void doWriteOnCanvas(DocumentCanvas canvas, FlatViewContext context) {
        canvas.addSubtitle(context.getLabel());

        SIList<?> siList = getElementList(context);
        SType<?> elementsType = siList.getElementsType();
        boolean renderCompositeFieldsAsColumns = elementsType.isComposite() && isRenderCompositeFieldAsColumns(siList);

        List<String> headerColumns = new ArrayList<>();
        if (renderCompositeFieldsAsColumns) {
            doRenderCompositeFieldAsColumns((STypeComposite<?>) elementsType, headerColumns);
        } else {
            String label = elementsType.asAtr().getLabel();
            if (label != null) {
                headerColumns.add(label);
            }
        }

        TableCanvas tableCanvas = canvas.addTable();

        if (!headerColumns.isEmpty()) {
            writeHeaders(headerColumns, tableCanvas);
        }

        TableBodyCanvas tableBody = tableCanvas.getTableBody();
        for (SInstance child : siList) {
            writeChild(renderCompositeFieldsAsColumns, tableBody, child);
        }
    }

    protected SIList<?> getElementList(FlatViewContext context) {
        return context.getInstanceAs(SIList.class);
    }

    private void writeHeaders(List<String> headerColumns, TableCanvas tableCanvas) {
        TableRowCanvas tableHeaderRow = tableCanvas.getTableHeader().addRow();
        for (String column : headerColumns) {
            tableHeaderRow.addColumn(column);
        }
    }

    private void writeChild(boolean renderCompositeFieldsAsColumns, TableBodyCanvas tableBody, SInstance child) {
        TableRowDocumentCanvasAdapter row = new TableRowDocumentCanvasAdapter(tableBody.addRow());
        if (renderCompositeFieldsAsColumns) {
            for (SInstance compositeField : ((SIComposite) child).getAllFields()) {
                callListItemDoWrite(row, compositeField);
            }
        } else {
            callListItemDoWrite(row, child);
        }
    }

    private void doRenderCompositeFieldAsColumns(STypeComposite<?> elementsType, List<String> headerColumns) {
        for (SType<?> e : elementsType.getFields()) {
            if (e.asAtr().isVisible()) {
                headerColumns.add(StringUtils.trimToEmpty(e.asAtr().getLabel()));
            }
        }
    }

    private boolean isRenderCompositeFieldAsColumns(SIList<?> siList) {
        SViewListByTable view                           = (SViewListByTable) ViewResolver.resolveView(siList.getType());
        boolean          renderCompositeFieldsAsColumns = false;
        if (view != null) {
            renderCompositeFieldsAsColumns = view.isRenderCompositeFieldsAsColumns();
        }
        return renderCompositeFieldsAsColumns;
    }

    private void callListItemDoWrite(TableRowDocumentCanvasAdapter row, SInstance field) {
        field.getAspect(FlatViewGenerator.ASPECT_FLAT_VIEW_GENERATOR)
                .ifPresent(viewGenerator -> viewGenerator
                        .writeOnCanvas(row, new FlatViewContext(field, true, true)));
    }

    public static class TableRowDocumentCanvasAdapter extends EmptyDocumentCanvas {
        private final TableRowCanvas tableRow;

        TableRowDocumentCanvasAdapter(TableRowCanvas tableRow) {
            this.tableRow = tableRow;
        }

        @Override
        public void addFormItem(FormItem formItem) {
            String value;
            if (StringUtils.isBlank(formItem.getValue())) {
                value = "-";
            } else {
                value = formItem.getValue();
            }
            tableRow.addColumn(value);
        }
    }

}
