package org.zbaudoin.ressemblance.ui;

import org.zbaudoin.ressemblance.dao.ParamDAO;
import org.zbaudoin.ressemblance.dao.ParamDAO.RessourceParamException;
import org.zbaudoin.ressemblance.dao.SessionParamStore;
import org.zbaudoin.ressemblance.model.ComparisonLine;
import org.zbaudoin.ressemblance.model.MonitoringParam;
import org.zbaudoin.ressemblance.model.Param;
import org.zbaudoin.ressemblance.service.Monitoring;
import org.zbaudoin.ressemblance.service.Ressemblance;
import org.zbaudoin.util.ui.WindowUtil;

import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.prefs.Preferences;


class RessemblanceGUI {

	private JFrame fr;
	private JFileChooser fcFile;
	
	private Action actNew;
	private Action actOpen;
	private Action actSave;
	private Action actSaveAs;
	private Action actExit;
	private Action actRun;
	private Action actMonitorStart;
	private Action actMonitorStop;
	private Action actRunStop;
	
	private File currectFile;

	private Param param;
	private final Preferences preferences;
	private final SessionParamStore sessionParamStore;
	private Timer sessionSaveTimer;
	private boolean hasUnsavedSessionChanges;
	
	private final Monitoring monitoring;
	private MonitoringConsoleGUI monitoringConsole;

	private final JTabbedPane tbPane;
	private final ResultGUI resultGUI;
	private final ParamGUI paramGUI;
	private JProgressBar runProgress;
	private AtomicBoolean runCancellationRequested;
	
	RessemblanceGUI() {
		preferences = Preferences.userNodeForPackage(this.getClass());
		sessionParamStore = new SessionParamStore(preferences);
		
		Logger log = Logger.getLogger(Monitoring.class.getName());
		
		param = new Param();
		monitoring = new Monitoring(log);
		tbPane = new JTabbedPane();
		resultGUI = new ResultGUI();
		paramGUI =  new ParamGUI();

		paramGUI.setOnChange(this::scheduleSessionSave);
		resultGUI.setOnTableStateChange(state -> {
			param.setResultTableState(state);
			scheduleSessionSave();
		});
		initAction();
		buildUI();
		try {
			Param savedSessionParam = sessionParamStore.load();
			if (savedSessionParam != null) {
				param = savedSessionParam;
				hasUnsavedSessionChanges = true;
			}
		} catch (RessourceParamException e) {
			showException(e);
		}
		refreshUI();
	}
	
	private void initAction() {
		actNew = new AbstractAction("New") {
			@Override
			public void actionPerformed(ActionEvent e) {
				param = new Param();
				refreshUI();
				setCurrentFile(null);
				hasUnsavedSessionChanges = true;
				scheduleSessionSave();
			}
		};
		actOpen = new AbstractAction("Open") {
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
			@Override
			public void actionPerformed(ActionEvent e) {
				persistPendingSession();
				System.exit(0);
			}
		};
		
		actRun = new AbstractAction("Run") {

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshParam();
				startRun();
			}
		};		
		actRunStop = new AbstractAction("Arrêter") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (runCancellationRequested != null) {
					runCancellationRequested.set(true);
					actRunStop.setEnabled(false);
					runProgress.setString("Arrêt en cours...");
				}
			}
		};
		actRunStop.setEnabled(false);
		actMonitorStart = new AbstractAction("Monitoring start") {

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshParam();
				MonitoringGUI mg = new MonitoringGUI(fr, new MonitoringParam(preferences.get("monitorig.directory.path", null), 9999));
				MonitoringParam pMonitor = mg.show();
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
			fr.setTitle("Ressemblance");
		} else {
			fr.setTitle("Ressemblance : " + currectFile.getAbsolutePath());
		}
	}
	
	private void buildUI() {
		fr = new JFrame("Ressemblance");
		fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		fr.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				persistPendingSession();
			}
		});
		
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
		fr.getContentPane().add(tbPane, BorderLayout.CENTER);
		tbPane.add("Paramètres", paramGUI.build());
		tbPane.add("Résultat", resultGUI.build());

		JPanel paProgress = new JPanel(new BorderLayout(8, 0));
		runProgress = new JProgressBar(0, 100);
		runProgress.setStringPainted(true);
		runProgress.setString("En attente");
		paProgress.add(runProgress, BorderLayout.CENTER);
		paProgress.add(new JButton(actRunStop), BorderLayout.EAST);
		fr.getContentPane().add(paProgress, BorderLayout.SOUTH);
	}

	private void startRun() {
		Ressemblance ressemblance = new Ressemblance(param);
		AtomicBoolean cancellationRequested = new AtomicBoolean();
		runCancellationRequested = cancellationRequested;
		actRun.setEnabled(false);
		actRunStop.setEnabled(true);
		runProgress.setValue(0);
		runProgress.setIndeterminate(true);
		runProgress.setString("Préparation...");

		SwingWorker<List<ComparisonLine>, ProgressUpdate> worker = new SwingWorker<>() {
			@Override
			protected List<ComparisonLine> doInBackground() {
				return ressemblance.run((operation, completed, total) ->
								publish(new ProgressUpdate(operation, completed, total)),
						cancellationRequested::get);
			}

			@Override
			protected void process(List<ProgressUpdate> updates) {
				ProgressUpdate update = updates.get(updates.size() - 1);
				runProgress.setIndeterminate(false);
				if (update.total == 0) {
					runProgress.setValue(100);
					runProgress.setString("Aucune comparaison");
				} else {
					int percent = (int) Math.min(100, 100L * update.completed / update.total);
					runProgress.setValue(percent);
					String progressText = update.completed + " / " + update.total;
					runProgress.setString(update.operation.isEmpty()
							? progressText
							: update.operation + " : " + progressText);
				}
			}

			@Override
			protected void done() {
				actRun.setEnabled(true);
				actRunStop.setEnabled(false);
				runCancellationRequested = null;
				try {
					List<ComparisonLine> comparisonLines = get();
					if (!comparisonLines.isEmpty()) {
						resultGUI.setComparisonLines(comparisonLines);
						tbPane.setSelectedIndex(1);
					}
					runProgress.setIndeterminate(false);
					runProgress.setValue(100);
					runProgress.setString("Terminé");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					showException(e);
				} catch (CancellationException e) {
					runProgress.setIndeterminate(false);
					runProgress.setValue(0);
					runProgress.setString("Arrêté");
				} catch (ExecutionException e) {
					showException(e);
					runProgress.setIndeterminate(false);
					runProgress.setString("Erreur");
				}
			}
		};
		worker.execute();
	}

	private static class ProgressUpdate {
		private final String operation;
		private final long completed;
		private final long total;

		private ProgressUpdate(String operation, long completed, long total) {
			this.operation = operation;
			this.completed = completed;
			this.total = total;
		}
	}

	private void refreshUI() {
		paramGUI.refreshUI(param);
		resultGUI.setTableState(param.getResultTableState());
	}

	private void refreshParam() {
		paramGUI.refreshParam(param);
	}

	private void scheduleSessionSave() {
		hasUnsavedSessionChanges = true;
		if (sessionSaveTimer == null) {
			sessionSaveTimer = new Timer(500, e -> saveSessionParam());
			sessionSaveTimer.setRepeats(false);
		}
		sessionSaveTimer.restart();
	}

	private void persistPendingSession() {
		if (sessionSaveTimer != null) {
			sessionSaveTimer.stop();
		}
		if (hasUnsavedSessionChanges) {
			saveSessionParam();
		}
	}

	private void saveSessionParam() {
		refreshParam();
		try {
			sessionParamStore.save(param);
		} catch (RessourceParamException e) {
			showException(e);
		}
	}

	private void clearSessionParam() throws RessourceParamException {
		if (sessionSaveTimer != null) {
			sessionSaveTimer.stop();
		}
		sessionParamStore.clear();
		hasUnsavedSessionChanges = false;
	}

	void display() {
		fr.setSize(600, 500);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
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
		Param loadedParam = dao.load();
		if (sessionSaveTimer != null) {
			sessionSaveTimer.stop();
		}
		sessionParamStore.clear();
		param = loadedParam;
		hasUnsavedSessionChanges = false;
		refreshUI();
		setCurrentFile(file);
	}
	
	private void paramToFile(File file) throws RessourceParamException {
		ParamDAO dao = new ParamDAO(file);
		dao.save(param);
		setCurrentFile(file);
		clearSessionParam();
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
