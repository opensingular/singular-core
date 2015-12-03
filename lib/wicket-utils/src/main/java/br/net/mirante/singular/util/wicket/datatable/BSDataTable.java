package br.net.mirante.singular.util.wicket.datatable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.ComponentTag;

public class BSDataTable<T, S> extends DataTable<T, S> {

    public static final Long DEFAULT_ROWS_PER_PAGE = 10L;

    private boolean stripedRows    = true;
    private boolean hoverRows      = true;
    private boolean borderedTable  = true;
    private boolean advanceTable   = false;
    private boolean condensedTable = false;

    public BSDataTable(String id, List<? extends IColumn<T, S>> columns, ISortableDataProvider<T, S> dataProvider) {
        super(id, ensureSerializable(columns), dataProvider, DEFAULT_ROWS_PER_PAGE);

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
        return new NoRecordsToolbar(this);
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
    }
    
    @Override
    protected void onConfigure() {
        super.onConfigure();
        boolean renderCaption = !getCaption().getRenderBodyOnly();
        getCaption()
            .setOutputMarkupId(renderCaption)
            .setOutputMarkupPlaceholderTag(renderCaption);
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
}
