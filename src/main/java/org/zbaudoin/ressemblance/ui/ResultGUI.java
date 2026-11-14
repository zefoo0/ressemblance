package org.zbaudoin.ressemblance.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.zbaudoin.ressemblance.model.*;

public class ResultGUI {
	private static final String[] COLUMN_IDS = {
			ResultTableState.FILE1, ResultTableState.FILE2, ResultTableState.SCORE,
			ResultTableState.NAME1, ResultTableState.NAME2, ResultTableState.PARENT1,
			ResultTableState.PARENT2, ResultTableState.SIZE1, ResultTableState.SIZE2,
			ResultTableState.RESULT_COMPARATOR, ResultTableState.ERROR_COMPARATORS
	};
	private static final String[] COLUMN_NAMES = {
			"Fichier 1", "Fichier 2", "Score", "Nom 1", "Nom 2", "Parent 1",
			"Parent 2", "Taille 1", "Taille 2", "Comparaison retenue", "Comparaisons en erreur"
	};

	private final AbstractTableModel model;
	private List<ComparisonLine> comparisonLines;
	private JTable table;
	private TableRowSorter<AbstractTableModel> sorter;
	private Map<String, TableColumn> columns;
	private ResultTableState tableState;
	private Consumer<ResultTableState> onTableStateChange;
	private boolean restoringState;

	public ResultGUI() {
		comparisonLines = new ArrayList<>();
		tableState = new ResultTableState();
		model = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				ComparisonLine line = comparisonLines.get(rowIndex);
				File file1 = line.getFile1();
				File file2 = line.getFile2();
				switch (COLUMN_IDS[columnIndex]) {
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
						throw new IllegalStateException("Unknown result column: " + COLUMN_IDS[columnIndex]);
				}
			}

			@Override
			public int getRowCount() {
				return comparisonLines.size();
			}

			@Override
			public int getColumnCount() {
				return COLUMN_IDS.length;
			}

			@Override
			public String getColumnName(int column) {
				return COLUMN_NAMES[column];
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (COLUMN_IDS[columnIndex]) {
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
		};
	}

	public void setOnTableStateChange(Consumer<ResultTableState> onTableStateChange) {
		this.onTableStateChange = onTableStateChange;
	}

	public void setTableState(ResultTableState state) {
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
					table.addColumn(columns.get(id));
				}
			}
			if (tableState.getSortColumn() != null) {
				int sortIndex = columnIndex(tableState.getSortColumn());
				if (sortIndex >= 0) {
					SortOrder order = tableState.isSortAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING;
					sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortIndex, order)));
				}
			}
		} finally {
			restoringState = false;
		}
	}

	public void setComparisonLines(List<ComparisonLine> comparisonLines) {
		this.comparisonLines = comparisonLines;
		model.fireTableDataChanged();
	}

	public JComponent build() {
		table = new JTable(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		columns = new LinkedHashMap<>();
		for (int i = 0; i < COLUMN_IDS.length; i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setIdentifier(COLUMN_IDS[i]);
			columns.put(COLUMN_IDS[i], column);
		}

		sorter = new TableRowSorter<>(model);
		sorter.setMaxSortKeys(1);
		sorter.setComparator(columnIndex(ResultTableState.FILE1),
				java.util.Comparator.comparing(file -> ((File) file).getAbsolutePath()));
		sorter.setComparator(columnIndex(ResultTableState.FILE2),
				java.util.Comparator.comparing(file -> ((File) file).getAbsolutePath()));
		table.setRowSorter(sorter);
		table.getColumnModel().getColumn(columnIndex(ResultTableState.SCORE))
				.setPreferredWidth(50);
		table.getColumnModel().getColumn(columnIndex(ResultTableState.SCORE)).setMaxWidth(50);
		DefaultTableCellRenderer sizeRenderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void setValue(Object value) {
				setText(value == null ? "" : FileSizeFormater.format((Long) value));
			}
		};
		sizeRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		for (String id : Arrays.asList(ResultTableState.SIZE1, ResultTableState.SIZE2)) {
			table.getColumnModel().getColumn(columnIndex(id)).setCellRenderer(sizeRenderer);
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
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					Point point = e.getPoint();
					int row = table.rowAtPoint(point);
					int column = table.columnAtPoint(point);
					if (row >= 0 && column >= 0 && !table.isRowSelected(row)) {
						table.changeSelection(row, column, false, false);
					}
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int column = table.columnAtPoint(e.getPoint());
				if (e.getClickCount() == 2 && row >= 0 && column >= 0
						&& ResultTableState.ERROR_COMPARATORS.equals(
								table.getColumnModel().getColumn(column).getIdentifier())) {
					showComparisonErrors(row);
				}
			}
		});
		initPopupMenu(table);
		setTableState(tableState);
		return new JScrollPane(table);
	}

	private JPopupMenu createColumnsPopup() {
		JPopupMenu popup = new JPopupMenu();
		for (int i = 0; i < COLUMN_IDS.length; i++) {
			String id = COLUMN_IDS[i];
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(COLUMN_NAMES[i]);
			item.addActionListener(e -> setColumnVisible(id, item.isSelected()));
			popup.add(item);
		}
		popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
				Set<String> visible = new HashSet<>(getVisibleColumnIds());
				for (int i = 0; i < popup.getComponentCount(); i++) {
					JCheckBoxMenuItem item = (JCheckBoxMenuItem) popup.getComponent(i);
					item.setSelected(visible.contains(COLUMN_IDS[i]));
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }

			@Override
			public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
		});
		return popup;
	}

	private void setColumnVisible(String id, boolean visible) {
		boolean currentlyVisible = getVisibleColumnIds().contains(id);
		if (visible == currentlyVisible) {
			return;
		}
		if (!visible && table.getColumnModel().getColumnCount() <= 1) {
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
			result.setSortColumn(COLUMN_IDS[key.getColumn()]);
			result.setSortAscending(key.getSortOrder() != SortOrder.DESCENDING);
		}
		return result;
	}

	private ResultTableState normalizeState(ResultTableState state) {
		ResultTableState normalized = new ResultTableState();
		List<String> order = new ArrayList<>();
		if (state != null && state.getColumnOrder() != null) {
			for (String id : state.getColumnOrder()) {
				if (columnIndex(id) >= 0 && !order.contains(id)) {
					order.add(id);
				}
			}
		}
		for (String id : COLUMN_IDS) {
			if (!order.contains(id)) {
				order.add(id);
			}
		}
		Set<String> requestedVisible = new HashSet<>();
		if (state != null && state.getVisibleColumns() != null) {
			for (String id : state.getVisibleColumns()) {
				if (columnIndex(id) >= 0) {
					requestedVisible.add(id);
				}
			}
		} else {
			requestedVisible.addAll(Arrays.asList(COLUMN_IDS));
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
				if (columnIndex(entry.getKey()) >= 0 && entry.getValue() != null && entry.getValue() > 0) {
					widths.put(entry.getKey(), entry.getValue());
				}
			}
		}
		normalized.setColumnWidths(widths);
		if (state != null && columnIndex(state.getSortColumn()) >= 0) {
			normalized.setSortColumn(state.getSortColumn());
			normalized.setSortAscending(state.isSortAscending());
		}
		return normalized;
	}

	private static int columnIndex(String id) {
		for (int i = 0; i < COLUMN_IDS.length; i++) {
			if (COLUMN_IDS[i].equals(id)) {
				return i;
			}
		}
		return -1;
	}

	private static String parentName(File file) {
		File parent = file.getParentFile();
		return parent == null ? "" : parent.getName();
	}

	private void initPopupMenu(final JTable ta) {
		JPopupMenu popup = new JPopupMenu();
		popup.add(new AbstractAction("Isolate") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = ta.getSelectedRow();
				if (row >= 0) {
					isolate(getFileAtViewRow(ta, row, ResultTableState.FILE1),
							getFileAtViewRow(ta, row, ResultTableState.FILE2));
				}
			}
		});

		popup.add(new AbstractAction("Open file 1") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				openFile(getFileAtSelectedRow(ta, ResultTableState.FILE1));
			}
		});

		popup.add(new AbstractAction("Open file 2") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				openFile(getFileAtSelectedRow(ta, ResultTableState.FILE2));
			}
		});

		popup.add(new AbstractAction("Delete file 1") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedFiles(ta, ResultTableState.FILE1, "Delete files A ?");
			}
		});

		popup.add(new AbstractAction("Delete file 2") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedFiles(ta, ResultTableState.FILE2, "Delete files B ?");
			}
		});

		JMenuItem openDirectory1 = new JMenuItem("Open directory");
		openDirectory1.addActionListener(e ->
				openDirectory(getFileAtSelectedRow(ta, ResultTableState.FILE1)));
		popup.add(openDirectory1);

		JMenuItem openDirectory2 = new JMenuItem("Open directory 2");
		openDirectory2.addActionListener(e ->
				openDirectory(getFileAtSelectedRow(ta, ResultTableState.FILE2)));
		popup.add(openDirectory2);

		popup.addSeparator();

		popup.add(new AbstractAction("Copy cell") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = ta.getSelectedRow();
				int column = ta.getSelectedColumn();
				if (row >= 0 && column >= 0) {
					Object value = ta.getValueAt(row, column);
					TableCellRenderer renderer = ta.getCellRenderer(row, column);
					Component rendered = renderer.getTableCellRendererComponent(
							ta, value, false, false, row, column);
					String text = rendered instanceof JLabel
							? ((JLabel) rendered).getText()
							: String.valueOf(value);
					Toolkit.getDefaultToolkit().getSystemClipboard()
							.setContents(new StringSelection(text == null ? "" : text), null);
				}
			}
		});

		JMenuItem showErrors = new JMenuItem("Afficher les erreurs");
		showErrors.addActionListener(e -> {
			int row = ta.getSelectedRow();
			if (row >= 0) {
				showComparisonErrors(row);
			}
		});
		popup.add(showErrors);
		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				int row = ta.getSelectedRow();
				ComparisonLine line = row < 0 ? null
						: comparisonLines.get(ta.convertRowIndexToModel(row));
				showErrors.setEnabled(line != null && !line.getErreurs().isEmpty());

				File file1 = line == null ? null : line.getFile1();
				File file2 = line == null ? null : line.getFile2();
				File directory1 = file1 == null ? null : file1.getParentFile();
				File directory2 = file2 == null ? null : file2.getParentFile();
				boolean differentDirectories = directory1 != null && directory2 != null
						&& !directory1.getAbsoluteFile().toPath().normalize()
								.equals(directory2.getAbsoluteFile().toPath().normalize());
				openDirectory1.setText(differentDirectories ? "Open directory 1" : "Open directory");
				openDirectory1.setEnabled(directory1 != null);
				openDirectory2.setVisible(differentDirectories);
				openDirectory2.setEnabled(differentDirectories);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) { }
		});
		ta.setComponentPopupMenu(popup);
	}

	private File getFileAtSelectedRow(JTable ta, String id) {
		int row = ta.getSelectedRow();
		return row < 0 ? null : getFileAtViewRow(ta, row, id);
	}

	private void openFile(File file) {
		if (file != null) {
			try {
				Desktop.getDesktop().open(file);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void openDirectory(File file) {
		if (file != null && file.getParentFile() != null) {
			try {
				Desktop.getDesktop().browse(file.getParentFile().toURI());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private File getFileAtViewRow(JTable ta, int viewRow, String id) {
		Object value = model.getValueAt(ta.convertRowIndexToModel(viewRow), columnIndex(id));
		return value instanceof File ? (File) value : null;
	}

	private void deleteSelectedFiles(JTable ta, String fileColumn, String title) {
		int[] rows = ta.getSelectedRows();
		if (rows.length == 0) {
			return;
		}
		File example = getFileAtViewRow(ta, rows[0], fileColumn);
		int result = JOptionPane.showConfirmDialog(null,
				"Êtes-vous sur de supprimer " + rows.length + " fichiers ?\n"
						+ example.getAbsolutePath() + " ...",
				title, JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			for (int row : rows) {
				getFileAtViewRow(ta, row, fileColumn).delete();
			}
			JOptionPane.showMessageDialog(null, "Opération terminée");
		}
	}

	private void showComparisonErrors(int viewRow) {
		ComparisonLine line = comparisonLines.get(table.convertRowIndexToModel(viewRow));
		if (line.getErreurs().isEmpty()) {
			return;
		}
		StringWriter text = new StringWriter();
		PrintWriter writer = new PrintWriter(text);
		for (ComparisonLineError error : line.getErreurs()) {
			writer.println("Comparaison : " + error.getComparator().getLibelle());
			error.getException().printStackTrace(writer);
			writer.println();
		}
		writer.flush();

		JTextArea details = new JTextArea(text.toString(), 20, 80);
		details.setEditable(false);
		details.setCaretPosition(0);
		JOptionPane.showMessageDialog(table, new JScrollPane(details),
				"Erreurs de comparaison", JOptionPane.ERROR_MESSAGE);
	}

	private void isolate(File file1, File file2) {
		if (file1 == null || file2 == null) {
			return;
		}
		String tmp = System.getProperty("user.home");
		File directory = new File(tmp + "/.ressemblance");
		directory.mkdir();
		if (!directory.exists()) {
			tmp = System.getProperty("java.io.tmpdir");
			directory = new File(tmp + "/.ressemblance");
			directory.mkdir();
		}

		File link1 = new File(directory, "1_" + file1.getName());
		File link2 = new File(directory, "2_" + file2.getName());
		try {
			new ProcessBuilder("ln", "-s", file1.getAbsolutePath(), link1.getAbsolutePath()).start();
			new ProcessBuilder("ln", "-s", file2.getAbsolutePath(), link2.getAbsolutePath()).start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			Desktop.getDesktop().browse(directory.toURI());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		JRadioButton rb1 = new JRadioButton(link1.getName());
		rb1.setToolTipText(file1.getAbsolutePath() + "(" + FileSizeFormater.format(file1.length()) + ")");
		JRadioButton rb2 = new JRadioButton(link2.getName());
		rb2.setToolTipText(file2.getAbsolutePath() + "(" + FileSizeFormater.format(file2.length()) + ")");
		ButtonGroup group = new ButtonGroup();
		group.add(rb1);
		group.add(rb2);
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		radioPanel.add(rb1);
		radioPanel.add(rb2);

		int result = JOptionPane.showConfirmDialog(null, radioPanel, "Delete files ?", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			if (rb1.isSelected()) {
				file1.delete();
			} else if (rb2.isSelected()) {
				file2.delete();
			}
		}
		link1.delete();
		link2.delete();
		directory.delete();
	}
}
