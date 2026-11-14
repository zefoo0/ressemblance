package org.zbaudoin.ressemblance.service;

import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.*;

import org.zbaudoin.ressemblance.model.*;


public class Monitoring {

	private Timer timer;
	private AtomicBoolean cancellationRequested;
	private final Logger logger;
	
	public Monitoring(Logger logger) {
		this.logger = logger;
	}
	
	public Logger getLogger() {
		return logger;
	}	
	
	public synchronized void start(MonitoringParam paramMonitoring, Param paramRessemblance) {
		if (timer != null) throw new IllegalStateException();
		cancellationRequested = new AtomicBoolean();
		TimerTask task = createTask(paramMonitoring, paramRessemblance, cancellationRequested);
		timer = new Timer();
		timer.schedule(task, 2000, 5 * 60 * 1000);
	}
	
	public synchronized void cancel() {
		if (cancellationRequested != null) {
			cancellationRequested.set(true);
			cancellationRequested = null;
		}
		if (timer != null) {
			timer.cancel();
		}
		timer = null;
	}
	
	private TimerTask createTask(final MonitoringParam paramMonitoring, final Param paramRessemblance,
			final AtomicBoolean cancellationRequested) {
		return new TimerTask() {
			@Override
			public void run() {
				logger.log(Level.INFO, "Monitoring scan started");
				try {
					processing(paramMonitoring, paramRessemblance, cancellationRequested);
					logger.log(Level.INFO, "Monitoring scan stop successfully");
				} catch (CancellationException e) {
					logger.log(Level.INFO, "Monitoring scan cancelled");
				} catch(RuntimeException e) {
					logger.log(Level.SEVERE, "Monitoring scan stop abnormally", e);	
				}
			}
		};
	}
	
	private void processing(MonitoringParam paramMonitoring, Param paramRessemblance,
			AtomicBoolean cancellationRequested) {
		
		File rootDir = new File(paramMonitoring.getDir());
		Param param = paramRessemblance.clone();
		param.setFiles(new ArrayList<>(paramRessemblance.getFiles()));
		param.getFiles().add(rootDir);
		
		Ressemblance ressemblance = new Ressemblance(param);
		List<ComparisonLine> comparisonLines = ressemblance.run((operation, completed, total) -> {},
				cancellationRequested::get);

		for (ComparisonLine comparisonLine : comparisonLines) {
			if (cancellationRequested.get()) {
				throw new CancellationException("Monitoring scan cancelled");
			}
			if (comparisonLine.getResult() != null && comparisonLine.getResult().getSimilarite() >= paramMonitoring.getOffset()) {
				File file1 = comparisonLine.getFile1();
				File file2 = comparisonLine.getFile2();
				
				if (!file1.equals(file2) 
						&& file1.exists() 
						&& file2.exists()) {
					if (fileIn(file1, rootDir)) {
							deleteIfNotCancelled(file1, file2, cancellationRequested);
					} else if (fileIn(file2, rootDir)) {
							deleteIfNotCancelled(file2, file1, cancellationRequested);
					}
				}
			}
		}
	}

	private synchronized void deleteIfNotCancelled(File file, File oppositeFile,
			AtomicBoolean cancellationRequested) {
		if (cancellationRequested.get()) {
			throw new CancellationException("Monitoring scan cancelled");
		}
		delete(file, oppositeFile);
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
