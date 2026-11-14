package org.zbaudoin.filesimilarity.ui;

import org.zbaudoin.filesimilarity.domain.config.ResultTableState;
import org.zbaudoin.filesimilarity.domain.comparison.FileComparison;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.util.List;
import java.util.function.Consumer;

public class ResultsPanel {
    private final ComparisonTableModel model = new ComparisonTableModel();
    private final ResultTableStateSupport tableStateSupport = new ResultTableStateSupport(model);
    private final ResultFileActions fileActions = new ResultFileActions(model);

    public void setOnTableStateChange(Consumer<ResultTableState> onTableStateChange) {
        tableStateSupport.setOnTableStateChange(onTableStateChange);
    }

    public void setTableState(ResultTableState state) {
        tableStateSupport.setTableState(state);
    }

    public void setComparisonLines(List<FileComparison> comparisonLines) {
        model.setComparisonLines(comparisonLines);
    }

    public JComponent build() {
        JTable table = new JTable(model);
        tableStateSupport.install(table);
        fileActions.install(table);
        return new JScrollPane(table);
    }
}
