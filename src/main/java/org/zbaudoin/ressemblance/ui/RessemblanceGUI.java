package org.zbaudoin.ressemblance.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.zbaudoin.ressemblance.dao.ParamDAO;
import org.zbaudoin.ressemblance.dao.ParamDAO.RessourceParamException;
import org.zbaudoin.ressemblance.model.ParamMonitoring;
import org.zbaudoin.ressemblance.model.ParamRessemblance;
import org.zbaudoin.ressemblance.model.Seem;
import org.zbaudoin.ressemblance.service.Monitoring;
import org.zbaudoin.ressemblance.service.Ressemblance;
import org.zbaudoin.util.ui.ComponentUtil;
import org.zbaudoin.util.ui.WindowUtil;


class RessemblanceGUI {

	private JFrame fr;
	private JFileChooser fcDir;
	private JFileChooser fcFile;
	
	private Action actNew;
	private Action actOpen;
	private Action actSave;
	private Action actSaveAs;
	private Action actExit;
	private Action actRun;
	private Action actExplor;
	private Action actMonitorStart;
	private Action actMonitorStop;
	
	private JTextArea taSep;
	private JTextArea taExc;
	private JTextArea taFile;

	private File currectFile;

	private ParamRessemblance param;
	
	private final Monitoring monitoring;
	
	private MonitoringConsoleGUI monitoringConsole;
	
	private final Preferences preferences;
	
	
	RessemblanceGUI() {
		
		Logger log = Logger.getLogger(Monitoring.class.getName());
		
		param = new ParamRessemblance();
		monitoring = new Monitoring(log);

		initAction();
		buildUI();
		refreshUI();
		
		preferences = Preferences.userNodeForPackage(this.getClass());
	}
	
	private void initAction() {
		actNew = new AbstractAction("New") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				param = new ParamRessemblance();
				refreshUI();
				setCurrentFile(null);
			}
		};
		actOpen = new AbstractAction("Open") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				File file = chooseFileToOpen(fr);
				if (file != null) {
					try {
						fileToParam(file);
					} catch (RessourceParamException e1) {
						showException(e1);
					}
				}
			}
		};
		actSave = new AbstractAction("Save") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshParam();
				
				File file = currectFile;
				if (file == null) {
					file = chooseFileToSave(fr);
					if (file == null) return;
				}
				try {
					paramToFile(file);
				} catch (RessourceParamException e1) {
					showException(e1);
				}
			}
		};
		actSave.setEnabled(false);
		
		actSaveAs = new AbstractAction("Save as") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshParam();
				File file = chooseFileToSave(fr);
				if (file != null) {
					try {
						paramToFile(file);
					} catch (RessourceParamException e1) {
						showException(e1);
					}
				}
			}
		};
		actExit = new AbstractAction("Quit") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		
		actExplor = new AbstractAction("Add Directory") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				File dir = chooseDirectory(fr);
				if (dir != null) {
					taFile.setText(taFile.getText() + "\n" + dir.getAbsolutePath());
				}
			}
		};
		
		actRun = new AbstractAction("Run") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshParam();
				try {
					Ressemblance r = new Ressemblance(param);
					List<Seem> seems = r.run();
					if (!seems.isEmpty()) {
						ResultGUI seemsUI = new ResultGUI(seems, param);
						seemsUI.show();
					}
				} catch (Exception e1) {
					showException(e1);
				}
			}
		};		
		actMonitorStart = new AbstractAction("Monitoring start") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshParam();
				MonitoringGUI mg = new MonitoringGUI(fr, new ParamMonitoring(preferences.get("monitorig.directory.path", null), 9999));
				ParamMonitoring pMonitor = mg.show();
				if (pMonitor != null) {
					preferences.put("monitorig.directory.path", pMonitor.getDir());
					
					monitoringConsole = new MonitoringConsoleGUI(fr);
					monitoring.getLogger().addHandler(monitoringConsole.getHandler());
					
					monitoring.start(pMonitor, param);

					actMonitorStart.setEnabled(false);
					actMonitorStop.setEnabled(true);
					
					SwingUtilities.invokeLater(() -> monitoringConsole.setVisible(true));
				}
			}
		};
		actMonitorStart.setEnabled(true);

		actMonitorStop = new AbstractAction("Monitoring stop") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				monitoring.cancel();
				
				monitoringConsole.setVisible(false);
				monitoring.getLogger().removeHandler(monitoringConsole.getHandler());
				
				actMonitorStart.setEnabled(true);
				actMonitorStop.setEnabled(false);
			}
		};
		actMonitorStop.setEnabled(false);
	}
	
	private void setCurrentFile(File file) {
		currectFile = file;
		actSave.setEnabled(currectFile != null);
		if (currectFile == null) {
			fr.setTitle("Ressenblance");
		} else {
			fr.setTitle("Ressenblance : " + currectFile.getAbsolutePath());	
		}
		
	}
	
	private void buildUI() {
		fr = new JFrame("Ressenblance");
		fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JMenuBar bar = new JMenuBar();
		JMenu menuFile = new JMenu("Configuration");
		JMenu menuRun = new JMenu("Execution");
		bar.add(menuFile);
		bar.add(menuRun);
		menuFile.add(actNew);
		menuFile.add(actOpen);
		menuFile.add(actSave);
		menuFile.add(actSaveAs);
		menuFile.add(new Separator());
		menuFile.add(actExit);
		menuRun.add(actRun);
		menuRun.add(new Separator());
		menuRun.add(actMonitorStart);
		menuRun.add(actMonitorStop);
		
		fr.setJMenuBar(bar);

		taSep = new JTextArea();
		taExc = new JTextArea();
		taExc.setPreferredSize(new Dimension(150, 100));
		taFile = new JTextArea();
		
		fr.getContentPane().add(buildTextArea("Séparateurs", taSep, "Séparateurs utilisés pour " +
				"découper les noms de fichiers en mots"), BorderLayout.WEST);
		fr.getContentPane().add(buildTextArea("Dossiers racine",
						taFile,	"Dossiers racine des fichiers à comparer", actExplor),
				BorderLayout.CENTER);
		fr.getContentPane().add(buildTextArea("Termes à exclure", taExc, "Termes à exclure de la comparaison"), BorderLayout.EAST);
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
	
	private void refreshUI() {
		StringBuilder sep = new StringBuilder();
        for (String s : param.getSeparators()) {
            sep.append(s).append("\n");
        }
		taSep.setText(sep.toString());

		StringBuilder exc = new StringBuilder();
        for (String s : param.getExcludes()) {
            exc.append(s).append("\n");
        }
		taExc.setText(exc.toString());

		StringBuilder t = new StringBuilder();
        for (File f : param.getFiles()) {
            t.append(f.getAbsolutePath()).append("\n");
        }
		taFile.setText(t.toString());
	}

	private void refreshParam() {
		
		param.setSeparators(scan(taSep.getText()));

		List<String> ss = scan(taExc.getText().toUpperCase());
		ss.sort((o1, o2) -> o2.length() - o1.length());
		param.setExcludes(ss);

		List<String> filesName = scan(taFile.getText());
		List<File> f = new ArrayList<>();
        for (String fileName : filesName) {
            f.add(new File(fileName));

        }
		param.setFiles(f);
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
	
	void display() {
		fr.setSize(600, 500);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
	}
	
	private File chooseDirectory(Component parent) {
		if (fcDir == null) {
			fcDir = new JFileChooser();
			fcDir.setDialogType(JFileChooser.OPEN_DIALOG);
			fcDir.setDialogTitle("Select root directory");
			fcDir.setMultiSelectionEnabled(false);
			fcDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			try {
				File dirPref = new File(preferences.get("directory.path", System.getProperty("user.home")));
				fcDir.setCurrentDirectory(dirPref);
			} catch(RuntimeException e) {
				showException(e);
			}
		}
		
		if (fcDir.showDialog(parent, "Select") != JFileChooser.APPROVE_OPTION) {
			return null;
		} 
		
		preferences.put("directory.path", fcDir.getSelectedFile().getAbsolutePath());
		
		return fcDir.getSelectedFile();
	}
	
	private File chooseFileToOpen(Component parent) {
		initFileChooserFile();
		
		fcFile.setDialogTitle("Select file");
		return showAndValidFileChooserFile(parent);
	}

	private File chooseFileToSave(Component parent) {
		initFileChooserFile();
		fcFile.setDialogTitle("Save as");
		return showAndValidFileChooserFile(parent);
	}
	
	private void initFileChooserFile() {
		if (fcFile == null) {
			fcFile = new JFileChooser();
			fcFile.setDialogType(JFileChooser.OPEN_DIALOG);
			fcFile.setMultiSelectionEnabled(false);
			fcFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			try {
				File filePref = new File(preferences.get("file.path", System.getProperty("user.home")));
				fcFile.setCurrentDirectory(filePref);
			} catch(RuntimeException e) {
				showException(e);
			}
		}
	}
	
	private File showAndValidFileChooserFile(Component parent) {
		if (fcFile.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
			return null;
		} 
		
		File dir = fcFile.getSelectedFile().getParentFile();
		if (dir != null) preferences.put("file.path", dir.getAbsolutePath());
		
		return fcFile.getSelectedFile();
	}
	
	private void fileToParam(File file) throws RessourceParamException {
		ParamDAO dao = new ParamDAO(file);
		param = dao.load();
		refreshUI();
		setCurrentFile(file);
	}
	
	private void paramToFile(File file) throws RessourceParamException {
		ParamDAO dao = new ParamDAO(file);
		dao.save(param);
		setCurrentFile(file);
	}
	
	private void showException(Exception e) {
		WindowUtil.showExceptionDialog(fr, "Erreur !!!", e);
	}
		
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
            RessemblanceGUI r = new RessemblanceGUI();
            r.display();
        });
	}
}
