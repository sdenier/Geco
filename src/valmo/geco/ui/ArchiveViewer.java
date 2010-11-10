/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import valmo.geco.Geco;
import valmo.geco.control.ArchiveManager;
import valmo.geco.control.GecoControl;
import valmo.geco.core.Messages;
import valmo.geco.model.ArchiveRunner;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class ArchiveViewer extends JFrame {
	
	private Geco geco;
	
	private ArchiveTableModel tableModel;
	
	private JTable table;
	
	private JTextField filterField;

	private JLabel archiveFileL;

	private JLabel archiveDateL;

	private JLabel nbEntriesL;

	
	
	private ArchiveManager archiveManager;

	

	public ArchiveViewer(Geco geco) {
		this.geco = geco;
		this.archiveManager = new ArchiveManager(new GecoControl()); // TODO remove
		guiInit();
	}

	private ArchiveManager archiveManager() {
		return this.archiveManager;
	}
	
	public void open() {
		refresh();
		setVisible(true);		
	}
	
	public void close() {
		setVisible(false);
	}

	private void refreshTableData() {
		tableModel.setData(archiveManager().archive().runners().toArray(new ArchiveRunner[0]));
	}
	
	public void loadArchive(File archiveFile) {
		try {
			archiveManager().loadArchiveFrom(archiveFile);
			refresh();
		} catch (IOException e1) {
			geco.debug(e1.getLocalizedMessage());
		}
	}
	
	public void refresh() {
		refreshTableData();
		archiveFileL.setText(archiveManager().getArchiveName());
		archiveDateL.setText(archiveManager().archiveLastModified());
		showRowCount();
	}

	private void showRowCount() {
		nbEntriesL.setText(Integer.toString(table.getRowCount()));
	}


	private void guiInit() {
		getContentPane().add(initToolbar(), BorderLayout.NORTH);
		getContentPane().add(initTableScroll(), BorderLayout.CENTER);
		getContentPane().add(initStatusbar(), BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(null); // center on screen
	}

	private Component initToolbar() {
		Box panel = Box.createHorizontalBox();
		JButton loadFileB = new JButton("Load Archive");
		loadFileB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
				fileChooser.setDialogTitle("Select archive file");
				int answer = fileChooser.showOpenDialog(ArchiveViewer.this);
				if( answer==JFileChooser.APPROVE_OPTION ) {
					loadArchive(fileChooser.getSelectedFile());
				}
			}
		});
		panel.add(loadFileB);
		archiveFileL = new JLabel();
		archiveFileL.setBorder(BorderFactory.createEtchedBorder());
		panel.add(archiveFileL);
		
		panel.add(Box.createHorizontalGlue());
		
		JButton insertB = new JButton("Insert");
		panel.add(insertB);
		panel.add(new JLabel("Find:"));
		filterField = new JTextField(20);
//		filterField.setToolTipText(Messages.uiGet("RunnersPanel.FindTooltip")); //$NON-NLS-1$
		filterField.setMaximumSize(new Dimension(50, SwingUtils.SPINNERHEIGHT));
		filterField.requestFocusInWindow();
		panel.add(filterField);
		
		return panel;
	}
	
	private Component initTableScroll() {
		tableModel = new ArchiveTableModel();
		table = new JTable(tableModel);
//		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setPreferredScrollableViewportSize(new Dimension(700, 500));
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().setSelectionInterval(0, 0);
		final TableRowSorter<ArchiveTableModel> sorter = new TableRowSorter<ArchiveTableModel>(tableModel);
		table.setRowSorter(sorter);
		filterField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					RowFilter<Object,Object> filter = RowFilter.regexFilter("(?i)" + filterField.getText()); //$NON-NLS-1$
					sorter.setRowFilter(filter);
					showRowCount();
//					table.getSelectionModel().setSelectionInterval(0, 0);
				} catch (java.util.regex.PatternSyntaxException e1) {
					return;
				}				
			}
		});
		return new JScrollPane(table);
	}
	
	private Component initStatusbar() {
		Box panel = Box.createHorizontalBox();
		panel.add(new JLabel("Archive from "));
		archiveDateL = new JLabel("");
		panel.add(archiveDateL);
		panel.add(new JLabel(" - "));
		nbEntriesL = new JLabel("0");
		panel.add(nbEntriesL);
		panel.add(new JLabel(" entries"));
		return panel;
	}
	
	public static void main(String[] args) {
		Messages.put("ui", "valmo.geco.ui.messages"); //$NON-NLS-1$ //$NON-NLS-2$
		ArchiveViewer viewer = new ArchiveViewer(null);
		viewer.open();
	}

}
