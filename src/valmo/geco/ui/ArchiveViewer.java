/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import valmo.geco.Geco;
import valmo.geco.control.ArchiveManager;
import valmo.geco.core.Messages;
import valmo.geco.model.Archive;
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

	
	public ArchiveViewer(Geco geco) {
		this.geco = geco;
		guiInit();
	}

	private ArchiveManager archiveManager() {
		return geco.archiveManager();
	}
	
	public void open() {
		refresh();
		setVisible(true);		
	}
	
	public void close() {
		setVisible(false);
	}

	private ArchiveRunner getSelectedRunner() {
		return tableModel.getRunner( table.convertRowIndexToModel(table.getSelectedRow()) );
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

	private void refreshTableData() {
		Archive archive = null;
		try {
			archive = archiveManager().archive();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
					this,
					e.getLocalizedMessage(),
					"Error loading archive",
					JOptionPane.ERROR_MESSAGE);
			try {
				archive = archiveManager().archive();
			} catch (IOException e1) {
				geco.debug(e1.getLocalizedMessage());
			}
		}
		tableModel.setData(archive.runners().toArray(new ArchiveRunner[0]));
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
		JButton loadFileB = new JButton(Messages.uiGet("ArchiveViewer.LoadArchiveLabel")); //$NON-NLS-1$
		loadFileB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Messages.uiGet("ArchiveViewer.SelectArchiveLabel")); //$NON-NLS-1$
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
		
		final JButton insertB = new JButton(Messages.uiGet("ArchiveViewer.InsertLabel")); //$NON-NLS-1$
		insertB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				archiveManager().insertRunner(getSelectedRunner());
			}
		});
		panel.add(insertB);
		
		((JComponent) getContentPane()).getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"insertRunner"); //$NON-NLS-1$
		((JComponent) getContentPane()).getActionMap().put("insertRunner", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				insertB.doClick();
			}
		});

		
		panel.add(new JLabel(Messages.uiGet("ArchiveViewer.FindLabel"))); //$NON-NLS-1$
		filterField = new JTextField(20);
		filterField.setToolTipText(Messages.uiGet("RunnersPanel.FindTooltip")); //$NON-NLS-1$
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
		
		((JComponent) getContentPane()).getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"focusOnFilter"); //$NON-NLS-1$
		((JComponent) getContentPane()).getActionMap().put("focusOnFilter", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				filterField.selectAll();
				filterField.requestFocusInWindow();
			}
		});
		((JComponent) getContentPane()).getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"cancelFilter"); //$NON-NLS-1$
		((JComponent) getContentPane()).getActionMap().put("cancelFilter", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				filterField.setText(""); //$NON-NLS-1$
				sorter.setRowFilter(null);
				showRowCount();
			}
		});

		return new JScrollPane(table);
	}
	
	private Component initStatusbar() {
		Box panel = Box.createHorizontalBox();
		panel.add(new JLabel(Messages.uiGet("ArchiveViewer.ArchiveDateLabel"))); //$NON-NLS-1$
		archiveDateL = new JLabel(""); //$NON-NLS-1$
		panel.add(archiveDateL);
		panel.add(new JLabel(" - ")); //$NON-NLS-1$
		nbEntriesL = new JLabel("0"); //$NON-NLS-1$
		panel.add(nbEntriesL);
		panel.add(new JLabel(Messages.uiGet("ArchiveViewer.NbEntriesLabel"))); //$NON-NLS-1$
		return panel;
	}
	
}
