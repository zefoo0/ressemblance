package org.zbaudoin.filesimilarity.ui;

import org.zbaudoin.filesimilarity.domain.config.ResultTableState;


import org.junit.Test;
import org.zbaudoin.filesimilarity.domain.config.ResultTableState;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SortOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ResultTableStateSupportTest {

    @Test
    public void restoresVisibleColumnOrderWidthsAndSortAndPersistsUserChanges() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ComparisonTableModel model = new ComparisonTableModel();
            ResultTableStateSupport stateSupport = new ResultTableStateSupport(model);
            List<ResultTableState> changes = new ArrayList<>();
            stateSupport.setOnTableStateChange(changes::add);

            ResultTableState state = new ResultTableState();
            state.setColumnOrder(Arrays.asList(
                    ResultTableState.NAME1, ResultTableState.FILE2, ResultTableState.FILE1));
            state.setVisibleColumns(Arrays.asList(ResultTableState.FILE2, ResultTableState.NAME1));
            Map<String, Integer> widths = new LinkedHashMap<>();
            widths.put(ResultTableState.NAME1, 175);
            widths.put(ResultTableState.FILE2, 350);
            state.setColumnWidths(widths);
            state.setSortColumn(ResultTableState.FILE2);
            state.setSortAscending(false);
            stateSupport.setTableState(state);

            JTable table = new JTable(model);
            stateSupport.install(table);

            assertEquals(2, table.getColumnModel().getColumnCount());
            assertEquals(ResultTableState.NAME1,
                    table.getColumnModel().getColumn(0).getIdentifier());
            assertEquals(ResultTableState.FILE2,
                    table.getColumnModel().getColumn(1).getIdentifier());
            assertEquals(175, table.getColumnModel().getColumn(0).getWidth());
            assertEquals(350, table.getColumnModel().getColumn(1).getWidth());
            assertEquals(ComparisonTableModel.columnIndex(ResultTableState.FILE2),
                    table.getRowSorter().getSortKeys().get(0).getColumn());
            assertEquals(SortOrder.DESCENDING, table.getRowSorter().getSortKeys().get(0).getSortOrder());

            table.moveColumn(0, 1);

            assertEquals(1, changes.size());
            assertEquals(Arrays.asList(
                    ResultTableState.FILE2, ResultTableState.NAME1, ResultTableState.FILE1),
                    changes.get(0).getColumnOrder().subList(0, 3));
        });
    }

    @Test
    public void alwaysLeavesAtLeastOneColumnVisible() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ResultTableState state = new ResultTableState();
            state.setVisibleColumns(Collections.emptyList());

            JTable table = new JTable(new ComparisonTableModel());
            ResultTableStateSupport stateSupport = new ResultTableStateSupport((ComparisonTableModel) table.getModel());
            stateSupport.setTableState(state);
            stateSupport.install(table);

            assertEquals(1, table.getColumnModel().getColumnCount());
            assertEquals(ResultTableState.FILE1, table.getColumnModel().getColumn(0).getIdentifier());
        });
    }
}
