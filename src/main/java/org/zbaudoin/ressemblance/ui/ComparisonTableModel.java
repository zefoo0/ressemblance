package org.zbaudoin.ressemblance.ui;

import org.zbaudoin.ressemblance.model.ComparisonLine;
import org.zbaudoin.ressemblance.model.ComparisonLineError;
import org.zbaudoin.ressemblance.model.ComparisonLines;
import org.zbaudoin.ressemblance.model.ResultTableState;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

final class ComparisonTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private static final List<String> COLUMN_IDS = Collections.unmodifiableList(Arrays.asList(
            ResultTableState.FILE1, ResultTableState.FILE2, ResultTableState.SCORE,
            ResultTableState.NAME1, ResultTableState.NAME2, ResultTableState.PARENT1,
            ResultTableState.PARENT2, ResultTableState.SIZE1, ResultTableState.SIZE2,
            ResultTableState.RESULT_COMPARATOR, ResultTableState.ERROR_COMPARATORS));
    private static final List<String> COLUMN_NAMES = Collections.unmodifiableList(Arrays.asList(
            "Fichier 1", "Fichier 2", "Score", "Nom 1", "Nom 2", "Parent 1",
            "Parent 2", "Taille 1", "Taille 2", "Comparaison retenue", "Comparaisons en erreur"));

    private ComparisonLines comparisonLines = new ComparisonLines(Collections.emptyList());

    static int resultColumnCount() {
        return COLUMN_IDS.size();
    }

    static String getColumnId(int index) {
        return COLUMN_IDS.get(index);
    }

    static String resultColumnName(int index) {
        return COLUMN_NAMES.get(index);
    }

    static int columnIndex(String id) {
        return COLUMN_IDS.indexOf(id);
    }

    void setComparisonLines(List<ComparisonLine> lines) {
        comparisonLines = new ComparisonLines(lines);
        fireTableDataChanged();
    }

    ComparisonLine getComparisonLine(int modelRow) {
        return comparisonLines.get(modelRow);
    }

    File getFileAt(int modelRow, String columnId) {
        ComparisonLine line = getComparisonLine(modelRow);
        if (ResultTableState.FILE1.equals(columnId)) {
            return line.getFile1();
        }
        if (ResultTableState.FILE2.equals(columnId)) {
            return line.getFile2();
        }
        throw new IllegalArgumentException("Not a file column: " + columnId);
    }

    void removeComparisonsContaining(Collection<File> files) {
        boolean changed = false;
        for (File file : files) {
            changed |= comparisonLines.removeComparisonsContaining(file);
        }
        if (changed) {
            fireTableDataChanged();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ComparisonLine line = comparisonLines.get(rowIndex);
        File file1 = line.getFile1();
        File file2 = line.getFile2();
        switch (COLUMN_IDS.get(columnIndex)) {
            case ResultTableState.FILE1:
                return file1;
            case ResultTableState.FILE2:
                return file2;
            case ResultTableState.SCORE:
                return line.getResult() == null ? -1 : line.getResult().getSimilarite();
            case ResultTableState.NAME1:
                return file1.getName();
            case ResultTableState.NAME2:
                return file2.getName();
            case ResultTableState.PARENT1:
                return parentName(file1);
            case ResultTableState.PARENT2:
                return parentName(file2);
            case ResultTableState.SIZE1:
                return file1.length();
            case ResultTableState.SIZE2:
                return file2.length();
            case ResultTableState.RESULT_COMPARATOR:
                return line.getResult() == null ? "" : line.getResult().getComparator().getLibelle();
            case ResultTableState.ERROR_COMPARATORS:
                StringJoiner labels = new StringJoiner(", ");
                for (ComparisonLineError error : line.getErreurs()) {
                    labels.add(error.getComparator().getLibelle());
                }
                return labels.toString();
            default:
                throw new IllegalStateException("Unknown result column: " + COLUMN_IDS.get(columnIndex));
        }
    }

    @Override
    public int getRowCount() {
        return comparisonLines.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_IDS.size();
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES.get(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (COLUMN_IDS.get(columnIndex)) {
            case ResultTableState.FILE1:
            case ResultTableState.FILE2:
                return File.class;
            case ResultTableState.SCORE:
                return Integer.class;
            case ResultTableState.SIZE1:
            case ResultTableState.SIZE2:
                return Long.class;
            default:
                return String.class;
        }
    }

    private static String parentName(File file) {
        File parent = file.getParentFile();
        return parent == null ? "" : parent.getName();
    }
}
