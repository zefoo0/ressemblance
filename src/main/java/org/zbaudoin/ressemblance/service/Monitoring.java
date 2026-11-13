package org.zbaudoin.ressemblance.service;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.zbaudoin.ressemblance.model.*;


public class Monitoring {

	private Timer timer;
	private final Logger logger;
	
	public Monitoring(Logger logger) {
		this.logger = logger;
	}
	
	public Logger getLogger() {
		return logger;
	}	
	
	public void start(MonitoringParam paramMonitoring, Param paramRessemblance) {
		TimerTask task = createTask(paramMonitoring, paramRessemblance);
		if (timer != null) throw new IllegalStateException();
		timer = new Timer();
		timer.schedule(task, 2000, 5 * 60 * 1000);
	}
	
	public void cancel() {
		timer.cancel();
		timer = null;
	}
	
	private TimerTask createTask(final MonitoringParam paramMonitoring, final Param paramRessemblance) {
		return new TimerTask() {
			@Override
			public void run() {
				logger.log(Level.INFO, "Monitoring scan started");
				try {
					processing(paramMonitoring, paramRessemblance);
					logger.log(Level.INFO, "Monitoring scan stop successfully");
				} catch(RuntimeException e) {
					logger.log(Level.SEVERE, "Monitoring scan stop abnormally", e);	
				}
			}
		};
	}
	
	private void processing(MonitoringParam paramMonitoring, Param paramRessemblance) {
		
		File rootDir = new File(paramMonitoring.getDir());
		Param param = paramRessemblance.clone();
		param.getFiles().add(rootDir);
		
		Ressemblance ressemblance = new Ressemblance(param);
		List<ComparisonLine> comparisonLines = ressemblance.run();

		for (ComparisonLine comparisonLine : comparisonLines) {
			if (comparisonLine.getResult() != null && comparisonLine.getResult().getSimilarite() >= paramMonitoring.getOffset()) {
				File file1 = (File) comparisonLine.getFile1();
				File file2 = (File) comparisonLine.getFile2();
				
				if (!file1.equals(file2) 
						&& file1.exists() 
						&& file2.exists()) {
					if (fileIn(file1, rootDir)) {
						delete(file1, file2);
					} else if (fileIn(file2, rootDir)) {
						delete(file2, file1);
					}
				}
			}
		}
	}
	
	private boolean fileIn(File file, File rootDir) {
		File f = file;
		while (!rootDir.equals(f) && f != null) {
			f = f.getParentFile();
		}
		return f != null;
	}
	
	private void delete(File file, File oppositeFile) {
		Date date = new Date();
		File fileTag = new File(file.getParentFile(), "__deleted-file__" + file.getName() + ".txt");
		
		try {
			FileWriter fw = new FileWriter(fileTag);
			fw.write("# Deleted file tag by Ressemblance");
			fw.write("\nOpposite file : " + oppositeFile.getAbsolutePath());
			fw.write("\nDate : " + date);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "File '" + fileTag.getAbsolutePath() + "' no written", e);
		}
		
		try {
			if (file.delete()) {
				logger.log(Level.INFO, "File '" + file.getAbsolutePath() + "' deleted");	
			}
		} catch (SecurityException e1) {
			logger.log(Level.SEVERE, "File '" + file.getAbsolutePath() + "' no deleted", e1);
		}
	}
}
