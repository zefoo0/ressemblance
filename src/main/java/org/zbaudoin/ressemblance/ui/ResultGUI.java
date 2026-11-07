package org.zbaudoin.ressemblance.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import org.zbaudoin.ressemblance.model.*;
import org.zbaudoin.ressemblance.service.Ressemblance.*;


public class ResultGUI {

	private final ParamRessemblance param;
	private final List<ComparisonLine> comparisonLines;
	
	public ResultGUI(final List<ComparisonLine> comparisonLines, ParamRessemblance param) {
		this.param = param;
		this.comparisonLines = comparisonLines;
	}
	
	public void show() {
		JFrame fr = new JFrame("Result");
		fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		TableModel model = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if (columnIndex == 0) {
					return comparisonLines.get(rowIndex).getElement1();
				}
				if (columnIndex == 1) {
					return comparisonLines.get(rowIndex).getElement2();
				}
				return comparisonLines.get(rowIndex).getLevel();
			}
			
			@Override
			public int getRowCount() {
				return comparisonLines.size();
			}
			
			@Override
			public int getColumnCount() {
				return 3;
			}

			@Override
			public String getColumnName(int column) {
				if (column == 0) {
					return "Fichier 1";
				} else if (column == 1) {
					return "Fichier 2";
				} else if (column == 2) {
					return "Score";
				} else {
					throw new IllegalStateException();
				}
			}
		};
				
		final JTable ta = new JTable(model);
		ta.getColumnModel().getColumn(2).setPreferredWidth(50);
		ta.getColumnModel().getColumn(2).setWidth(50);
		ta.getColumnModel().getColumn(2).setMaxWidth(50);
		ta.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					Point p=e.getPoint();
	                int row=ta.rowAtPoint(p);
	                int col = ta.columnAtPoint(p);
	                if (! ta.isRowSelected(row)) ta.changeSelection(row, col, false, false);
				}
			}
		});
		ta.setAutoCreateRowSorter(true);
		
		initPopupMenu(ta);
		
		JScrollPane sc = new JScrollPane(ta);
		fr.getContentPane().add(sc, BorderLayout.CENTER);

		fr.setSize(800, 600);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
	}
	
	private void initPopupMenu(final JTable ta) {
		
		JPopupMenu pop = new JPopupMenu();
		pop.add(new AbstractAction("Run") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Object o = ta.getValueAt(ta.getSelectedRow(), ta.getSelectedColumn());
				if (o instanceof File) {
					try {
						Desktop.getDesktop().open((File)o);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		pop.add(new AbstractAction("Open Directory") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Object o = ta.getValueAt(ta.getSelectedRow(), ta.getSelectedColumn());
				if (o instanceof File) {
					try {
						File f = (File)o;
						URI u = new URI("file://" + f.getParentFile().getAbsolutePath());
						Desktop.getDesktop().browse(u);
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
					}
                }
			}
		});

		pop.add(new AbstractAction("Isolate") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				File fSrc1 = (File)ta.getValueAt(ta.getSelectedRow(), 0);
				File fSrc2 = (File)ta.getValueAt(ta.getSelectedRow(), 1);
				
				String tmp = param.getIsolateDirectory();
				File f = new File(tmp + "/idem");
				f.mkdir();
				if (!f.exists()) {
					tmp = System.getProperty("java.io.tmpdir");
					f = new File(tmp + "/idem");
					f.mkdir();
				}

				File fTar1 = new File(f, "1_" + fSrc1.getName());
				File fTar2 = new File(f, "2_" + fSrc2.getName());
				
				try {
					ProcessBuilder bp = new ProcessBuilder("ln", "-s", fSrc1.getAbsolutePath(), fTar1.getAbsolutePath());
					bp.start();
					
					bp = new ProcessBuilder("ln", "-s", fSrc2.getAbsolutePath(), fTar2.getAbsolutePath());
					bp.start();

				} catch (IOException e2) {
					e2.printStackTrace();
				}

				try {
					URI u = new URI("file://" + f.getAbsolutePath());
					Desktop.getDesktop().browse(u);
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}

                JRadioButton rb1 = new JRadioButton(fTar1.getName());
				rb1.setToolTipText(fSrc1.getAbsolutePath() + "(" + FileSizeFormater.format(fSrc1.length()) + ")");
		        JRadioButton rb2 = new JRadioButton(fTar2.getName());
		        rb2.setToolTipText(fSrc2.getAbsolutePath() + "(" + FileSizeFormater.format(fSrc2.length()) + ")");
		        ButtonGroup group = new ButtonGroup();
		        group.add(rb1);
		        group.add(rb2);
		        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		        radioPanel.add(rb1);
		        radioPanel.add(rb2);
				
				int res = JOptionPane.showConfirmDialog(null, radioPanel, "Delete files ?", JOptionPane.OK_CANCEL_OPTION);
				if (res == JOptionPane.OK_OPTION) {
					if (rb1.isSelected()) {
						fSrc1.delete();
					} else if (rb2.isSelected()) {
						fSrc2.delete();
					}
				}
				
				fTar1.delete();
				fTar2.delete();
				f.delete();
			}
		});
		
		pop.add(new AbstractAction("Delete file 1") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int[] indices = ta.getSelectedRows();
				if (indices.length > 0) {
					File example = (File)ta.getValueAt(indices[0], 0);
					
					int res = JOptionPane.showConfirmDialog(null, 
							"Êtes-vous sur de supprimer " + indices.length + " fichiers ?\n" + example.getAbsolutePath() + " ...", 
							"Delete files A ?", JOptionPane.OK_CANCEL_OPTION);
					if (res == JOptionPane.OK_OPTION) {
						for (int i : indices) {
							File file = (File)ta.getValueAt(i, 0);
							file.delete();
						}
						JOptionPane.showMessageDialog(null, "Opération terminée");
					}
				}
			}
		});

		pop.add(new AbstractAction("Delete file 2") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int[] indices = ta.getSelectedRows();
				if (indices.length > 0) {
					File example = (File)ta.getValueAt(indices[0], 1);
					int res = JOptionPane.showConfirmDialog(null, 
							"Êtes-vous sur de supprimer " + indices.length + " fichiers ?\n" + example.getAbsolutePath() + " ...", 
							"Delete files B ?", JOptionPane.OK_CANCEL_OPTION);
					if (res == JOptionPane.OK_OPTION) {
						for (int i : indices) {
							File file = (File)ta.getValueAt(i, 1);
							file.delete();
						}
						JOptionPane.showMessageDialog(null, "Opération terminée");
					}
				}
			}
		});

		ta.setComponentPopupMenu(pop);
	}
}
