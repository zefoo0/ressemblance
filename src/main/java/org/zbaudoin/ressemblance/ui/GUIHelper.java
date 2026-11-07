package org.zbaudoin.ressemblance.ui;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import org.zbaudoin.util.ui.OutputStreamTextArea;

class GUIHelper {

	static ScrollTextAreaOutputStream createScrollTextAreaOutputStream(int width, int height) {
		
		JTextArea ta = new JTextArea();
		ta.setEditable(false);

		JScrollPane sc = new JScrollPane(ta);
		sc.setPreferredSize(new Dimension(width, height));
		
		return new ScrollTextAreaOutputStream(new OutputStreamTextArea(ta), sc);
	}
	
	static class ScrollTextAreaOutputStream {
		
		private final OutputStream outputStream;
		private final JScrollPane scrollPane;
 
		private ScrollTextAreaOutputStream(OutputStream outputStream, JScrollPane scrollPane) {
			this.outputStream = outputStream;
			this.scrollPane = scrollPane;
		}
 
		public JScrollPane getScrollPane() {
			return scrollPane;
		}
		
		public OutputStream getOutputStream() {
			return outputStream;
		}
	}
}
