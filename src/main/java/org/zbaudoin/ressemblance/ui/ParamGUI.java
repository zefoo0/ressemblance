package org.zbaudoin.ressemblance.ui;

import org.zbaudoin.ressemblance.model.Param;
import org.zbaudoin.util.ui.ComponentUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ParamGUI {

    private Action actDirectory;
    private JTextArea taFile;
    private JFileChooser fcDir;
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

//            try {
//                File dirPref = new File(preferences.get("directory.path", System.getProperty("user.home")));
//                fcDir.setCurrentDirectory(dirPref);
//            } catch(RuntimeException e) {
//                showException(e);
//            }
        }

        if (fcDir.showDialog(parent, "Select") != JFileChooser.APPROVE_OPTION) {
            return null;
        }

//        preferences.put("directory.path", fcDir.getSelectedFile().getAbsolutePath());

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
