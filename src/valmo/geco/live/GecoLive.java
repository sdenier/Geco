/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.Component;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
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

/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class GecoLive {

	private GecoControl gecoControl;
//	private ResultBuilder resultBuilder;

	private JTable runnersTable;
	private GecoLiveComponent liveComponent;
	
	
	public static void main(String[] args) {
		GecoLive gecoLive = new GecoLive();
//		gecoLive.importCourseData();
		gecoLive.guiLaunch();
		
	}

	public GecoLive() {
		String startDir = null;
		try {
//			startDir = launcher();
			startDir = "demo/hellemmes";
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(0);
		}

		gecoControl = new GecoControl(startDir);
		liveComponent = new GecoLiveComponent();

//		stageControl = new StageControl(gecoControl);
//		runnerControl = new RunnerControl(gecoControl);
//		resultBuilder = new ResultBuilder(gecoControl);
//		stats = new RegistryStats(gecoControl);
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
		splitPane.add(liveComponent.initGui(new JPanel()));
		splitPane.add(initRunnersTable());

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Map", splitPane);
		tabbedPane.add("Results", new JPanel());
		tabbedPane.add("Stats", null);

		jFrame.add(tabbedPane);
		jFrame.pack();
		jFrame.setVisible(true);
	}
	
//	public void exit() {
//		System.exit(0);
//	}

	private Component initRunnersTable() {
		ExtendedRunnersTableModel tableModel = new ExtendedRunnersTableModel();
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
		tableModel.setData(new Vector<RunnerRaceData>(registry().getRunnersData()));
		
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));
		row.add(Box.createHorizontalStrut(100));
		row.add(new JScrollPane(runnersTable));
		row.add(Box.createHorizontalStrut(100));
		
		return new JScrollPane(runnersTable);
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
	
	
//	private Punch createPunchTrace(Map<String, ControlCircle> controls) {
//		Punch start = createPunch("31");
//		Punch punch2 = createPunch("32");
//		Punch punch3 = createPunch("33");
//		Punch punch4 = createPunch("41");
//		Punch punch5 = createPunch("36");
//		start.setNextPunch(punch2);
//		punch2.setNextPunch(punch3);
//		punch2.nextPunchMissed();
//		punch2.setNextPunch(punch4);
//		punch3.setNextPunch(punch5);
//		punch4.setNextPunch(punch5);
//		start.beOk();
//		punch2.beOk();
//		punch4.beAdded();
//		punch5.beOk();
//		return start;
//	}

	/**
	 * @param punch
	 * @return
	 */
//	private Punch createPunchTraceA(Punch punch) {
//		String[] traceString = new String[] {
//				"31", "+40", "32", "-33+41", "-34", "35", "36", "+38", "37", "38", "39", "-31"
//		};
//		return createPunchTraceFor(punch, traceString);
//	}




}
