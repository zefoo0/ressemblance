package org.zbaudoin.ressemblance.ui;

import org.zbaudoin.ressemblance.model.Param;
import org.zbaudoin.ressemblance.model.Comparator;
import org.zbaudoin.ressemblance.service.comparator.FileComparator;
import org.zbaudoin.util.ui.ComponentUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class ParamGUI {

    private Action actDirectory;
    private JTextArea taFile;
    private JFileChooser fcDir;
    private JTable comparatorTable;
    private ComparatorTableModel comparatorTableModel;
    private Runnable onChange;
    private boolean updating;

    JPanel build() {
        JPanel paParam = new JPanel(new BorderLayout());

        initAction();

        taFile = new JTextArea();
        taFile.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                notifyChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                notifyChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                notifyChange();
            }
        });
        paParam.add(buildTextArea("Dossiers racine",
                        taFile,	"Dossiers racine des fichiers à comparer", actDirectory),
                BorderLayout.CENTER);

        comparatorTableModel = new ComparatorTableModel();
        comparatorTable = new JTable(comparatorTableModel);
        comparatorTable.setFillsViewportHeight(true);
        comparatorTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        comparatorTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        comparatorTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        comparatorTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean stopCellEditing() {
                String value = ((JTextField) getComponent()).getText();
                if (!isValidExtensions(value)) {
                    JOptionPane.showMessageDialog(comparatorTable,
                            "Saisissez des extensions séparées par des virgules.",
                            "Extensions invalides", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                return super.stopCellEditing();
            }
        });
        comparatorTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean stopCellEditing() {
                String value = ((JTextField) getComponent()).getText().trim();
                if (!isValidCoefficient(value)) {
                    JOptionPane.showMessageDialog(comparatorTable,
                            "Le coefficient doit être un nombre positif fini.",
                            "Coefficient invalide", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                return super.stopCellEditing();
            }
        });
        JScrollPane comparatorScroll = new JScrollPane(comparatorTable);
        comparatorScroll.setBorder(BorderFactory.createTitledBorder("Comparateurs"));
        comparatorScroll.setPreferredSize(new Dimension(450, 160));
        paParam.add(comparatorScroll, BorderLayout.SOUTH);

        return paParam;
    }

    void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    void refreshUI(Param param) {
        StringBuilder t = new StringBuilder();
        for (File f : param.getFiles()) {
            t.append(f.getAbsolutePath()).append("\n");
        }
        comparatorTableModel.setComparators(param.getComparators());
        updating = true;
        try {
            taFile.setText(t.toString());
        } finally {
            updating = false;
        }
    }

    void refreshParam(Param param) {
        java.util.List<String> filesName = scan(taFile.getText());
        List<File> f = new ArrayList<>();
        for (String fileName : filesName) {
            f.add(new File(fileName));
        }
        param.setFiles(f);
    }

    private void notifyChange() {
        if (!updating && onChange != null) {
            onChange.run();
        }
    }

    private static boolean isValidExtensions(String value) {
        if (value.trim().isEmpty()) {
            return true;
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .noneMatch(String::isEmpty);
    }

    private static boolean isValidCoefficient(String value) {
        try {
            float coefficient = Float.parseFloat(value);
            return Float.isFinite(coefficient) && coefficient >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private class ComparatorTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final String[] columnNames = {"Comparateur", "Extensions", "Coefficient"};
        private List<Comparator> comparators = Collections.emptyList();

        void setComparators(List<Comparator> comparators) {
            this.comparators = comparators == null ? Collections.emptyList() : new ArrayList<>(comparators);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return comparators.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Comparator comparator = comparators.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return comparator.getLibelle();
                case 1:
                    Set<String> extensions = comparator.getExtensions();
                    return extensions == null ? "" : extensions.stream().sorted().collect(Collectors.joining(", "));
                case 2:
                    return comparator.getCoefficient();
                default:
                    throw new IndexOutOfBoundsException("Unknown comparator column: " + columnIndex);
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 2 ? Float.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0 && !(comparators.get(rowIndex) instanceof FileComparator);
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Comparator comparator = comparators.get(rowIndex);
            if (comparator instanceof FileComparator) {
                return;
            }
            if (columnIndex == 1) {
                String extensions = String.valueOf(value).trim();
                Set<String> parsedExtensions = new LinkedHashSet<>();
                if (!extensions.isEmpty()) {
                    Arrays.stream(extensions.split(","))
                            .map(String::trim)
                            .map(extension -> extension.startsWith(".") ? extension.substring(1) : extension)
                            .filter(extension -> !extension.isEmpty())
                            .forEach(parsedExtensions::add);
                }
                comparator.setExtensions(parsedExtensions);
            } else if (columnIndex == 2) {
                float coefficient = Float.parseFloat(String.valueOf(value).trim());
                if (!Float.isFinite(coefficient) || coefficient < 0) {
                    return;
                }
                comparator.setCoefficient(coefficient);
            } else {
                return;
            }
            fireTableCellUpdated(rowIndex, columnIndex);
            if (onChange != null) {
                onChange.run();
            }
        }
    }


    private void initAction() {
        actDirectory  = new AbstractAction("Add Directory") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                File dir = chooseDirectory(taFile);
                if (dir != null) {
                    taFile.setText(taFile.getText() + "\n" + dir.getAbsolutePath());
                }
            }
        };
    }

    private Container buildTextArea(String label, JTextArea ta, String toolTip, Action... actions) {
        JPopupMenu pop = ComponentUtil.addMenuBasicUtilities(ta);
        ComponentUtil.addUndoRedo(ta);
        if (actions.length > 0) {
            pop.add(new JPopupMenu.Separator());
            for (Action action : actions) {
                pop.add(action);
            }
        }

        JPanel pa = new JPanel(new BorderLayout());

        JScrollPane sc = new JScrollPane(ta);

        JLabel lb = new JLabel(label, SwingConstants.CENTER);
        if (toolTip != null) {
            lb.setToolTipText(toolTip);
        }
        pa.add(lb, BorderLayout.NORTH);
        pa.add(sc, BorderLayout.CENTER);

        return pa;
    }

    private File chooseDirectory(Component parent) {
        if (fcDir == null) {
            fcDir = new JFileChooser();
            fcDir.setDialogType(JFileChooser.OPEN_DIALOG);
            fcDir.setDialogTitle("Select root directory");
            fcDir.setMultiSelectionEnabled(false);
            fcDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }

        if (fcDir.showDialog(parent, "Select") != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return fcDir.getSelectedFile();
    }

    private List<String> scan(String txt) {
        String[] lines = txt.split("\\n");
        List<String> res = new ArrayList<>();
        for (String s : lines) {
            String line = s;
            line = line.trim();
            if (!line.isEmpty()) res.add(line);
        }

        return res;
    }

}
