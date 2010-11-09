/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import valmo.geco.Geco;
import valmo.geco.control.ArchiveManager;
import valmo.geco.control.GecoControl;
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
	
	private Archive archive;

	public ArchiveViewer(Geco geco) {
		this.geco = geco;
		guiInit();
	}

	private void guiInit() {
		getContentPane().add(initTableScroll(), BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null); // center on screen
		setVisible(true);
	}
	
	private JScrollPane initTableScroll() {
		tableModel = new ArchiveTableModel();
		JTable table = new JTable(tableModel);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setPreferredScrollableViewportSize(new Dimension(800, 600));
		table.setAutoCreateRowSorter(true);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().setSelectionInterval(0, 0);
		return new JScrollPane(table);
	}

	private void refreshTableData() {
		tableModel.setData(archive.runners().toArray(new ArchiveRunner[0]));
	}
	
	public static void main(String[] args) {
		try {
			Messages.put("ui", "valmo.geco.ui.messages"); //$NON-NLS-1$ //$NON-NLS-2$
			ArchiveViewer viewer = new ArchiveViewer(null);
			viewer.archive = new ArchiveManager(new GecoControl()).loadArchiveFrom("./data/licencesFFCO.csv");
			viewer.refreshTableData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
