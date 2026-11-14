package org.zbaudoin.ressemblance.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResultTableState {
	public static final String FILE1 = "file1";
	public static final String FILE2 = "file2";
	public static final String SCORE = "score";
	public static final String NAME1 = "name1";
	public static final String NAME2 = "name2";
	public static final String PARENT1 = "parent1";
	public static final String PARENT2 = "parent2";
	public static final String SIZE1 = "size1";
	public static final String SIZE2 = "size2";
	public static final String RESULT_COMPARATOR = "resultComparator";
	public static final String ERROR_COMPARATORS = "errorComparators";

	private static final List<String> DEFAULT_COLUMN_ORDER = Arrays.asList(
			FILE1, PARENT1, NAME1, SIZE1, FILE2, NAME2, PARENT2,  SIZE2,
			SCORE, RESULT_COMPARATOR, ERROR_COMPARATORS);

	private List<String> columnOrder = new ArrayList<>(DEFAULT_COLUMN_ORDER);
	private List<String> visibleColumns = new ArrayList<>(DEFAULT_COLUMN_ORDER);
	private Map<String, Integer> columnWidths = defaultColumnWidths();
	private String sortColumn;
	private boolean sortAscending = true;

	private static Map<String, Integer> defaultColumnWidths() {
		Map<String, Integer> widths = new LinkedHashMap<>();
		widths.put(FILE1, 200);
		widths.put(FILE2, 200);
		widths.put(SCORE, 50);
		widths.put(NAME1, 140);
		widths.put(NAME2, 140);
		widths.put(PARENT1, 140);
		widths.put(PARENT2, 140);
		widths.put(SIZE1, 90);
		widths.put(SIZE2, 90);
		widths.put(RESULT_COMPARATOR, 160);
		widths.put(ERROR_COMPARATORS, 220);
		return widths;
	}

	public List<String> getColumnOrder() {
		return columnOrder;
	}

	public void setColumnOrder(List<String> columnOrder) {
		this.columnOrder = columnOrder;
	}

	public List<String> getVisibleColumns() {
		return visibleColumns;
	}

	public void setVisibleColumns(List<String> visibleColumns) {
		this.visibleColumns = visibleColumns;
	}

	public Map<String, Integer> getColumnWidths() {
		return columnWidths;
	}

	public void setColumnWidths(Map<String, Integer> columnWidths) {
		this.columnWidths = columnWidths;
	}

	public String getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}

	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}

	public ResultTableState copy() {
		ResultTableState copy = new ResultTableState();
		copy.setColumnOrder(columnOrder == null ? null : new ArrayList<>(columnOrder));
		copy.setVisibleColumns(visibleColumns == null ? null : new ArrayList<>(visibleColumns));
		copy.setColumnWidths(columnWidths == null ? null : new LinkedHashMap<>(columnWidths));
		copy.setSortColumn(sortColumn);
		copy.setSortAscending(sortAscending);
		return copy;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof ResultTableState)) return false;
		ResultTableState that = (ResultTableState) other;
		return sortAscending == that.sortAscending
				&& Objects.equals(columnOrder, that.columnOrder)
				&& Objects.equals(visibleColumns, that.visibleColumns)
				&& Objects.equals(columnWidths, that.columnWidths)
				&& Objects.equals(sortColumn, that.sortColumn);
	}

	@Override
	public int hashCode() {
		return Objects.hash(columnOrder, visibleColumns, columnWidths, sortColumn, sortAscending);
	}
}
