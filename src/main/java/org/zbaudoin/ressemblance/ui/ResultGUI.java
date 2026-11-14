package org.zbaudoin.ressemblance.ui;

import org.zbaudoin.ressemblance.model.ComparisonLine;
import org.zbaudoin.ressemblance.model.ResultTableState;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.util.List;
import java.util.function.Consumer;

public class ResultGUI {
    private final ComparisonTableModel model = new ComparisonTableModel();
    private final ResultTableStateSupport tableStateSupport = new ResultTableStateSupport(model);
    private final ResultFileActions fileActions = new ResultFileActions(model);

    public void setOnTableStateChange(Consumer<ResultTableState> onTableStateChange) {
        tableStateSupport.setOnTableStateChange(onTableStateChange);
    }

    public void setTableState(ResultTableState state) {
        tableStateSupport.setTableState(state);
    }

    public void setComparisonLines(List<ComparisonLine> comparisonLines) {
        model.setComparisonLines(comparisonLines);
    }

    public JComponent build() {
        JTable table = new JTable(model);
        tableStateSupport.install(table);
        fileActions.install(table);
        return new JScrollPane(table);
    }
}
