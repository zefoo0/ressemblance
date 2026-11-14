package org.zbaudoin.ressemblance.ui;

import org.zbaudoin.ressemblance.model.ResultTableState;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

final class ResultTableStateSupport {
    private final ComparisonTableModel model;
    private JTable table;
    private TableRowSorter<ComparisonTableModel> sorter;
    private Map<String, TableColumn> columns;
    private ResultTableState tableState = new ResultTableState();
    private Consumer<ResultTableState> onTableStateChange;
    private boolean restoringState;

    ResultTableStateSupport(ComparisonTableModel model) {
        this.model = model;
    }

    void setOnTableStateChange(Consumer<ResultTableState> onTableStateChange) {
        this.onTableStateChange = onTableStateChange;
    }

    void setTableState(ResultTableState state) {
        tableState = normalizeState(state);
        if (table == null) {
            return;
        }

        restoringState = true;
        try {
            sorter.setSortKeys(Collections.emptyList());
            while (table.getColumnModel().getColumnCount() > 0) {
                table.removeColumn(table.getColumnModel().getColumn(0));
            }
            for (Map.Entry<String, Integer> entry : tableState.getColumnWidths().entrySet()) {
                TableColumn column = columns.get(entry.getKey());
                if (column != null && entry.getValue() != null && entry.getValue() > 0) {
                    column.setPreferredWidth(entry.getValue());
                    column.setWidth(entry.getValue());
                }
            }
            Set<String> visible = new HashSet<>(tableState.getVisibleColumns());
            for (String id : tableState.getColumnOrder()) {
                if (visible.contains(id)) {
                    TableColumn column = columns.get(id);
                    if (column != null) {
                        table.addColumn(column);
                    }
                }
            }
            if (tableState.getSortColumn() != null) {
                int sortIndex = ComparisonTableModel.columnIndex(tableState.getSortColumn());
                if (sortIndex >= 0) {
                    SortOrder order = tableState.isSortAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING;
                    sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortIndex, order)));
                }
            }
        } finally {
            restoringState = false;
        }
    }

    void install(JTable table) {
        this.table = table;
        table.setAutoCreateColumnsFromModel(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        columns = new LinkedHashMap<>();
        for (int i = 0; i < ComparisonTableModel.resultColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setIdentifier(ComparisonTableModel.getColumnId(i));
            columns.put(ComparisonTableModel.getColumnId(i), column);
        }

        sorter = new TableRowSorter<>(model);
        sorter.setMaxSortKeys(1);
        sorter.setComparator(ComparisonTableModel.columnIndex(ResultTableState.FILE1),
                java.util.Comparator.comparing(file -> ((File) file).getAbsolutePath()));
        sorter.setComparator(ComparisonTableModel.columnIndex(ResultTableState.FILE2),
                java.util.Comparator.comparing(file -> ((File) file).getAbsolutePath()));
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(ComparisonTableModel.columnIndex(ResultTableState.SCORE))
                .setPreferredWidth(50);
        table.getColumnModel().getColumn(ComparisonTableModel.columnIndex(ResultTableState.SCORE)).setMaxWidth(50);
        DefaultTableCellRenderer sizeRenderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setText(value == null ? "" : FileSizeFormater.format((Long) value));
            }
        };
        sizeRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (String id : Arrays.asList(ResultTableState.SIZE1, ResultTableState.SIZE2)) {
            table.getColumnModel().getColumn(ComparisonTableModel.columnIndex(id)).setCellRenderer(sizeRenderer);
        }

        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) { }

            @Override
            public void columnRemoved(TableColumnModelEvent e) { }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                if (!restoringState && e.getFromIndex() != e.getToIndex()) {
                    persistCurrentState();
                }
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                if (!restoringState) {
                    persistCurrentState();
                }
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) { }
        });
        sorter.addRowSorterListener(e -> {
            if (!restoringState) {
                persistCurrentState();
            }
        });
        table.getTableHeader().setComponentPopupMenu(createColumnsPopup());
        setTableState(tableState);
    }

    private JPopupMenu createColumnsPopup() {
        JPopupMenu popup = new JPopupMenu();
        for (int i = 0; i < ComparisonTableModel.resultColumnCount(); i++) {
            String id = ComparisonTableModel.getColumnId(i);
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(ComparisonTableModel.resultColumnName(i));
            item.addActionListener(e -> setColumnVisible(id, item.isSelected()));
            popup.add(item);
        }
        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                Set<String> visible = new HashSet<>(getVisibleColumnIds());
                for (int i = 0; i < popup.getComponentCount(); i++) {
                    JCheckBoxMenuItem item = (JCheckBoxMenuItem) popup.getComponent(i);
                    item.setSelected(visible.contains(ComparisonTableModel.getColumnId(i)));
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) { }
        });
        return popup;
    }

    private void setColumnVisible(String id, boolean visible) {
        boolean currentlyVisible = getVisibleColumnIds().contains(id);
        if (visible == currentlyVisible || (!visible && table.getColumnModel().getColumnCount() <= 1)) {
            return;
        }
        if (visible) {
            table.addColumn(columns.get(id));
            int targetIndex = 0;
            for (String orderedId : tableState.getColumnOrder()) {
                if (orderedId.equals(id)) {
                    break;
                }
                if (currentlyVisible(orderedId)) {
                    targetIndex++;
                }
            }
            int lastIndex = table.getColumnModel().getColumnCount() - 1;
            if (targetIndex < lastIndex) {
                table.moveColumn(lastIndex, targetIndex);
            }
        } else {
            table.removeColumn(columns.get(id));
        }
        persistCurrentState();
    }

    private boolean currentlyVisible(String id) {
        return getVisibleColumnIds().contains(id);
    }

    private List<String> getVisibleColumnIds() {
        List<String> visible = new ArrayList<>();
        if (table != null) {
            for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
                visible.add(String.valueOf(table.getColumnModel().getColumn(i).getIdentifier()));
            }
        }
        return visible;
    }

    private void persistCurrentState() {
        if (restoringState) {
            return;
        }
        ResultTableState updated = captureState();
        if (!updated.equals(tableState)) {
            tableState = updated;
            if (onTableStateChange != null) {
                onTableStateChange.accept(updated.copy());
            }
        }
    }

    private ResultTableState captureState() {
        ResultTableState result = new ResultTableState();
        List<String> order = new ArrayList<>(tableState.getColumnOrder());
        List<String> visible = getVisibleColumnIds();
        Set<String> visibleSet = new HashSet<>(visible);
        int visibleIndex = 0;
        for (int i = 0; i < order.size(); i++) {
            if (visibleSet.contains(order.get(i))) {
                order.set(i, visible.get(visibleIndex++));
            }
        }
        result.setColumnOrder(order);
        List<String> visibleInOrder = new ArrayList<>();
        for (String id : order) {
            if (visibleSet.contains(id)) {
                visibleInOrder.add(id);
            }
        }
        result.setVisibleColumns(visibleInOrder);
        Map<String, Integer> widths = new LinkedHashMap<>();
        for (Map.Entry<String, TableColumn> entry : columns.entrySet()) {
            widths.put(entry.getKey(), entry.getValue().getWidth());
        }
        result.setColumnWidths(widths);
        List<? extends RowSorter.SortKey> sortKeys = sorter.getSortKeys();
        if (!sortKeys.isEmpty()) {
            RowSorter.SortKey key = sortKeys.get(0);
            result.setSortColumn(ComparisonTableModel.getColumnId(key.getColumn()));
            result.setSortAscending(key.getSortOrder() != SortOrder.DESCENDING);
        }
        return result;
    }

    private ResultTableState normalizeState(ResultTableState state) {
        ResultTableState normalized = new ResultTableState();
        List<String> order = new ArrayList<>();
        if (state != null && state.getColumnOrder() != null) {
            for (String id : state.getColumnOrder()) {
                if (ComparisonTableModel.columnIndex(id) >= 0 && !order.contains(id)) {
                    order.add(id);
                }
            }
        }
        for (int i = 0; i < ComparisonTableModel.resultColumnCount(); i++) {
            String id = ComparisonTableModel.getColumnId(i);
            if (!order.contains(id)) {
                order.add(id);
            }
        }
        Set<String> requestedVisible = new HashSet<>();
        if (state != null && state.getVisibleColumns() != null) {
            for (String id : state.getVisibleColumns()) {
                if (ComparisonTableModel.columnIndex(id) >= 0) {
                    requestedVisible.add(id);
                }
            }
        } else {
            for (int i = 0; i < ComparisonTableModel.resultColumnCount(); i++) {
                requestedVisible.add(ComparisonTableModel.getColumnId(i));
            }
        }
        List<String> visible = new ArrayList<>();
        for (String id : order) {
            if (requestedVisible.contains(id)) {
                visible.add(id);
            }
        }
        if (visible.isEmpty()) {
            visible.add(ResultTableState.FILE1);
        }
        normalized.setColumnOrder(order);
        normalized.setVisibleColumns(visible);
        Map<String, Integer> widths = new LinkedHashMap<>(new ResultTableState().getColumnWidths());
        if (state != null && state.getColumnWidths() != null) {
            for (Map.Entry<String, Integer> entry : state.getColumnWidths().entrySet()) {
                if (ComparisonTableModel.columnIndex(entry.getKey()) >= 0
                        && entry.getValue() != null && entry.getValue() > 0) {
                    widths.put(entry.getKey(), entry.getValue());
                }
            }
        }
        normalized.setColumnWidths(widths);
        if (state != null && ComparisonTableModel.columnIndex(state.getSortColumn()) >= 0) {
            normalized.setSortColumn(state.getSortColumn());
            normalized.setSortAscending(state.isSortAscending());
        }
        return normalized;
    }
}
