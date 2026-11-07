package org.zbaudoin.ressemblance.ui;

import java.awt.*;
import java.util.logging.*;

import javax.swing.*;

import org.zbaudoin.ressemblance.ui.GUIHelper.*;


public class MonitoringConsoleGUI extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private final Handler handler;
	
	public MonitoringConsoleGUI(Frame owner) {
		super(owner, false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		ScrollTextAreaOutputStream ta = GUIHelper.createScrollTextAreaOutputStream(400, 300);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(ta.getScrollPane(), BorderLayout.CENTER);
		
		handler = new StreamHandler(ta.getOutputStream(), new SimpleFormatter()) {
			@Override
			public synchronized void publish(LogRecord record) {
				super.publish(record);
				super.flush();
			}
		};
		
		pack();
		setLocationRelativeTo(owner);
	}
	
	public Handler getHandler() {
		return handler;
	}
}
