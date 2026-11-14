package org.zbaudoin.ressemblance.ui;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import org.zbaudoin.ressemblance.model.*;


class MonitoringGUI {

	private JDialog dialog;
	private JFileChooser fcDir;
	
	private JTextField tfDir;
	private JTextField tfOffset;
	
	private MonitoringParam response;
	
	MonitoringGUI(JFrame fr, MonitoringParam param) {
		build(fr, param);
	}

	MonitoringParam show() {
		dialog.setVisible(true);
		return response;
	}
	
	private void build(JFrame fr, MonitoringParam param) {
		dialog = new JDialog(fr, "Monitoring", true);
		
		tfDir = new JTextField(50);
		tfDir.setText(param.getDir());
		tfOffset = new JTextField(10);
		tfOffset.setText(String.valueOf(param.getOffset()));
		
		JButton btDir = new JButton("...");
		btDir.addActionListener(e -> {
            File f = chooseDirectory(dialog);
            if (f != null) tfDir.setText(f.getAbsolutePath());
        });

		JPanel paCenter = new JPanel(new FlowLayout());
		paCenter.add(new JLabel("Root directory to monitoring"));
		paCenter.add(tfDir);
		paCenter.add(btDir);
		paCenter.add(new JLabel("Offset"));
		paCenter.add(tfOffset);

		JButton btCancel = new JButton("Cancel");
		btCancel.addActionListener(e -> {
			response = null;
			dialog.dispose();
		});
		JButton btOk = new JButton("Ok");
		btOk.addActionListener(e -> {
			File f = new File(tfDir.getText());
			if (!f.exists()) {
				JOptionPane.showMessageDialog(dialog, "File '" + f.getAbsolutePath() + "' not exists !");
				return;
			}

			try {
				response = new MonitoringParam(tfDir.getText(), Integer.parseInt(tfOffset.getText()));
				dialog.dispose();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(dialog, "Offset no numeric !");
			}
		});
		JPanel paSouth = new JPanel(new FlowLayout());
		paSouth.add(btCancel);
		paSouth.add(btOk);

		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(paCenter, BorderLayout.CENTER);
		dialog.getContentPane().add(paSouth, BorderLayout.SOUTH);

		dialog.pack();
		dialog.setLocationRelativeTo(dialog.getOwner());
	}

	private File chooseDirectory(Component parent) {
		if (fcDir == null) {
			fcDir = new JFileChooser();
			fcDir.setDialogType(JFileChooser.OPEN_DIALOG);
			fcDir.setDialogTitle("Select root directory");
			fcDir.setMultiSelectionEnabled(false);
			fcDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			try {
				File dir = new File(tfDir.getText());
				if (dir.exists()) fcDir.setCurrentDirectory(dir);
			} catch(RuntimeException e) { /**/ }
		}
		
		if (fcDir.showDialog(parent, "Select") != JFileChooser.APPROVE_OPTION) {
			return null;
		} 
		
		return fcDir.getSelectedFile();
	}
	
}
