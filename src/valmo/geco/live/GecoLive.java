/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import valmo.geco.control.GecoControl;
import valmo.geco.core.TimeManager;
import valmo.geco.model.Registry;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.ui.GecoLauncher;
import valmo.geco.ui.StartStopButton;

/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class GecoLive implements LiveListener {

	private GecoControl gecoControl;
	private ExtendedRunnersTableModel tableModel;
	private JTable runnersTable;

	private LiveComponent liveComponent;
	private JFormattedTextField portF;
	private StartStopButton listenB;

	
	public static void main(String[] args) {
		GecoLive gecoLive = new GecoLive();
		gecoLive.guiLaunch();
	}

	public GecoLive() {
		String startDir = null;
		try {
			startDir = launcher();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(0);
		}

		gecoControl = new GecoControl(startDir);
		liveComponent = new LiveComponent();
	}
	private String launcher() throws Exception {
		return new GecoLauncher(System.getProperty("user.dir")).open(null);
	}

	private Registry registry() {
		return gecoControl.registry();
	}
	
	private void guiLaunch() {
		JFrame jFrame = new JFrame();
//		if( platformIsMacOs() ) {
//		GecoMacos.setupQuitAction(geco);
//	}

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setOneTouchExpandable(true);
		splitPane.add(liveComponent.initGui(new JPanel(), false));
		splitPane.add(initRunnersTable());

		liveComponent.setStartDir(gecoControl.stage().getBaseDir());
		
		jFrame.add(splitPane);
		jFrame.pack();
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);
	}
	
//	public void exit() {
//		System.exit(0);
//	}

	private Component initRunnersTable() {
		tableModel = new ExtendedRunnersTableModel();
		runnersTable = new JTable(tableModel);
//		table.setPreferredScrollableViewportSize(new Dimension(800, 300));
		tableModel.initCellRenderers(runnersTable);
		tableModel.initTableColumnSize(runnersTable);
		enableRowSorting(tableModel);
		runnersTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		runnersTable.getSelectionModel().setSelectionInterval(0, 0);
		runnersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() ) {
					updateRunnerMap();
				}
			}
		});
		refreshTable();
		
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));
		row.add(Box.createHorizontalStrut(100));
		row.add(new JScrollPane(runnersTable));
		row.add(Box.createHorizontalStrut(100));
		
		JScrollPane scrollPane = new JScrollPane(runnersTable);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(initNetworkPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private void refreshTable() {
		tableModel.setData(new Vector<RunnerRaceData>(registry().getRunnersData()));
	}
	
	private Component initNetworkPanel() {
		DecimalFormat format = new DecimalFormat();
		format.setGroupingUsed(false);
		portF = new JFormattedTextField(format);
		portF.setText("4444");
		portF.setColumns(5);
		listenB = new StartStopButton() {
			private Color defaultColor;
			LiveServerMulti server;
			@Override
			public void actionOn() {
				try {
					server = new LiveServerMulti(gecoControl, Integer.parseInt(portF.getText())).accept();
					server.registerListener(GecoLive.this);
					defaultColor = listenB.getBackground();
					listenB.setBackground(Color.GREEN);
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}				
			}
			@Override
			public void actionOff() {
				server.stop();
				listenB.setBackground(defaultColor);
			}
		};
		listenB.setText("Listen");

		JPanel networkConfigP = new JPanel(new FlowLayout(FlowLayout.LEFT));
		networkConfigP.add(listenB);
		networkConfigP.add(Box.createHorizontalStrut(20));
		networkConfigP.add(new JLabel("Port:"));
		networkConfigP.add(portF);
		return networkConfigP;
	}

	private void enableRowSorting(ExtendedRunnersTableModel tableModel) {
		TableRowSorter<ExtendedRunnersTableModel> sorter = new TableRowSorter<ExtendedRunnersTableModel>(tableModel);
		sorter.setComparator(1, new Comparator<String>() { // Chip column
			@Override
			public int compare(String o1, String o2) {
				try {
					Integer n1 = new Integer(o1);
					Integer n2 = new Integer(o2);
					return n1.compareTo(n2);
				} catch (NumberFormatException e) {
					// TODO: hackish dealing with xxxxaa chip numbers
					return 0;
				}
			}
		});
		sorter.setComparator(7, new Comparator<String>() { // Date column
			@Override
			public int compare(String o1, String o2) {
				try {
					return TimeManager.userParse(o1).compareTo(TimeManager.userParse(o2));
				} catch (ParseException e) {
					return 0;
				}
			}
		});
		sorter.setComparator(9, new Comparator<String>() { // Date column
			@Override
			public int compare(String o1, String o2) {
				try {
					return TimeManager.userParse(o1).compareTo(TimeManager.userParse(o2));
				} catch (ParseException e) {
					return 0;
				}
			}
		});
		sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey[] {
					new RowSorter.SortKey(0, SortOrder.ASCENDING) }));
		runnersTable.setRowSorter(sorter);
	}

	
	private void updateRunnerMap() {
		RunnerRaceData runnerData = registry().findRunnerData(selectedChip());
		if( runnerData!=null ) {
			liveComponent.displayRunnerMap(runnerData);
		}
	}

	private String selectedChip() {
		String chip = "";
		int selectedRow = runnersTable.getSelectedRow();
		if( selectedRow!=-1 && runnersTable.getRowCount() > 0) {
			// we have to test the number of displayed rows too.
			// If user inputs a filter which matches nothins,
			// there is no row to show but table still points to the 0-index.
			chip = (String) runnersTable.getValueAt(selectedRow, 1);
		}
		return chip;
	}

	@Override
	public void dataReceived(RunnerRaceData data) {
		int index = tableModel.getData().indexOf(data);
		if( index!=-1 ) {
			int newIndex = runnersTable.convertRowIndexToView(index);
			runnersTable.getSelectionModel().setSelectionInterval(newIndex, newIndex);
			runnersTable.scrollRectToVisible(runnersTable.getCellRect(newIndex, 0, true));
		} else {
			// TODO: temp check
			System.err.println("Unregistered data? " + data.infoString());
		}
	}

	@Override
	public void newDataIncoming() {
		refreshTable();
	}

}
