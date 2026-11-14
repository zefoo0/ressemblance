package org.zbaudoin.ressemblance.ui;

import org.zbaudoin.ressemblance.model.ComparisonLine;
import org.zbaudoin.ressemblance.model.ComparisonLineError;
import org.zbaudoin.ressemblance.model.ResultTableState;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

final class ResultFileActions {
    private final ComparisonTableModel model;

    ResultFileActions(ComparisonTableModel model) {
        this.model = model;
    }

    void install(JTable table) {
        initPopupMenu(table);
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
                    showComparisonErrors(table, row);
                }
            }
        });
    }

    private void initPopupMenu(final JTable table) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new AbstractAction("Isolate") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    isolate(getFileAtViewRow(table, row, ResultTableState.FILE1),
                            getFileAtViewRow(table, row, ResultTableState.FILE2));
                }
            }
        });
        popup.add(createAction("Open file 1",
                () -> openFile(getFileAtSelectedRow(table, ResultTableState.FILE1))));
        popup.add(createAction("Open file 2",
                () -> openFile(getFileAtSelectedRow(table, ResultTableState.FILE2))));
        popup.add(createAction("Delete file 1",
                () -> deleteSelectedFiles(table, ResultTableState.FILE1, "Delete files A ?")));
        popup.add(createAction("Delete file 2",
                () -> deleteSelectedFiles(table, ResultTableState.FILE2, "Delete files B ?")));

        JMenuItem openDirectory1 = new JMenuItem("Open directory");
        openDirectory1.addActionListener(e ->
                openDirectory(getFileAtSelectedRow(table, ResultTableState.FILE1)));
        popup.add(openDirectory1);

        JMenuItem openDirectory2 = new JMenuItem("Open directory 2");
        openDirectory2.addActionListener(e ->
                openDirectory(getFileAtSelectedRow(table, ResultTableState.FILE2)));
        popup.add(openDirectory2);
        popup.addSeparator();
        popup.add(createAction("Copy cell", () -> copySelectedCell(table)));

        JMenuItem showErrors = new JMenuItem("Afficher les erreurs");
        showErrors.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                showComparisonErrors(table, row);
            }
        });
        popup.add(showErrors);
        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                int row = table.getSelectedRow();
                ComparisonLine line = row < 0 ? null
                        : model.getComparisonLine(table.convertRowIndexToModel(row));
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
        table.setComponentPopupMenu(popup);
    }

    private AbstractAction createAction(String name, Runnable operation) {
        return new AbstractAction(name) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                operation.run();
            }
        };
    }

    private void copySelectedCell(JTable table) {
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        if (row >= 0 && column >= 0) {
            Object value = table.getValueAt(row, column);
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component rendered = renderer.getTableCellRendererComponent(
                    table, value, false, false, row, column);
            String text = rendered instanceof JLabel
                    ? ((JLabel) rendered).getText()
                    : String.valueOf(value);
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(text == null ? "" : text), null);
        }
    }

    private File getFileAtSelectedRow(JTable table, String id) {
        int row = table.getSelectedRow();
        return row < 0 ? null : getFileAtViewRow(table, row, id);
    }

    private File getFileAtViewRow(JTable table, int viewRow, String id) {
        return model.getFileAt(table.convertRowIndexToModel(viewRow), id);
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

    private void deleteSelectedFiles(JTable table, String fileColumn, String title) {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        List<File> files = new ArrayList<>();
        for (int row : rows) {
            File file = getFileAtViewRow(table, row, fileColumn);
            if (file != null && !files.contains(file)) {
                files.add(file);
            }
        }
        if (files.isEmpty()) {
            return;
        }
        File example = files.get(0);
        int result = JOptionPane.showConfirmDialog(null,
                "Êtes-vous sur de supprimer " + files.size() + " fichiers ?\n"
                        + example.getAbsolutePath() + " ...",
                title, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            List<File> deletedFiles = new ArrayList<>();
            List<File> failedFiles = new ArrayList<>();
            for (File file : files) {
                if (file.delete()) {
                    deletedFiles.add(file);
                } else {
                    failedFiles.add(file);
                }
            }
            model.removeComparisonsContaining(deletedFiles);
            if (failedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Opération terminée");
            } else {
                StringJoiner failures = new StringJoiner("\n",
                        "Impossible de supprimer les fichiers suivants :\n", "");
                for (File file : failedFiles) {
                    failures.add(file.getAbsolutePath());
                }
                JOptionPane.showMessageDialog(null, failures.toString(),
                        "Suppression impossible", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void showComparisonErrors(JTable table, int viewRow) {
        ComparisonLine line = model.getComparisonLine(table.convertRowIndexToModel(viewRow));
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
            File deletedFile = null;
            if (rb1.isSelected()) {
                deletedFile = file1;
            } else if (rb2.isSelected()) {
                deletedFile = file2;
            }
            if (deletedFile != null) {
                if (deletedFile.delete()) {
                    model.removeComparisonsContaining(Collections.singleton(deletedFile));
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Impossible de supprimer le fichier :\n" + deletedFile.getAbsolutePath(),
                            "Suppression impossible", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        link1.delete();
        link2.delete();
        directory.delete();
    }
}
