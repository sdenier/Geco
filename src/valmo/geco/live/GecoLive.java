/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
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
import valmo.geco.model.Course;
import valmo.geco.model.Registry;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.impl.POFactory;
import valmo.geco.model.xml.CourseSaxImporter;
import valmo.geco.ui.GecoLauncher;
import valmo.geco.ui.SwingUtils;

/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class GecoLive {

//	150dpi
	private static final int yTrans = -25;
	private static final int xTrans = -18;
	private static final float factor = 150 / 25.4f; // dpi / mm/inch 
	private static final float xFactor = factor;
	private static final float yFactor = factor;

//	300 dpi
//	private static final int yTrans = -25;
//	private static final int xTrans = -18;
//	private static final float xFactor = 11.8f;
//	private static final float yFactor = xFactor;

	private GecoControl gecoControl;
//	private ResultBuilder resultBuilder;
	private Map<String, ControlCircle> controls;
	private Map<String, Punch> courses;

	private JTable table;
	private GecoMapComponent map;
	private RunnerResultPanel runnerP;

	
	public static void main(String[] args) {
		GecoLive gecoLive = new GecoLive();
		gecoLive.importCourseData();
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

//		stageControl = new StageControl(gecoControl);
//		runnerControl = new RunnerControl(gecoControl);
//		resultBuilder = new ResultBuilder(gecoControl);
//		stats = new RegistryStats(gecoControl);
	}
	private String launcher() throws Exception {
		return new GecoLauncher(System.getProperty("user.dir")).open(null);
	}

	private void importCourseData() {
		try {
			CourseSaxImporter importer = new CourseSaxImporter(new POFactory());
			importer.importFromXml("hellemmes.xml");
			controls = createMapControlsFrom(importer.controls());
			courses = createCoursesFrom(importer.courses());
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		splitPane.add(initMapPanel());
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

	private JPanel initMapPanel() {
		JPanel mapPanel = new JPanel(new BorderLayout());
		runnerP = new RunnerResultPanel();
		mapPanel.add(SwingUtils.embed(runnerP), BorderLayout.WEST);

		map = new GecoMapComponent();
//		map.loadMapImage("hellemmes_all150.jpg");
		map.loadMapImage("hellemmes.jpg");
		mapPanel.add(new JScrollPane(map), BorderLayout.CENTER);
		return mapPanel;
	}
	
	private Component initRunnersTable() {
		ExtendedRunnersTableModel tableModel = new ExtendedRunnersTableModel();
		table = new JTable(tableModel);
//		table.setPreferredScrollableViewportSize(new Dimension(800, 300));
		tableModel.initCellRenderers(table);
		tableModel.initTableColumnSize(table);
		enableRowSorting(tableModel);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().setSelectionInterval(0, 0);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
		row.add(new JScrollPane(table));
		row.add(Box.createHorizontalStrut(100));
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.add("Runners", new JScrollPane(table));
		tabs.add("Config", new JPanel());
		return tabs;
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
		table.setRowSorter(sorter);
	}

	
	private void updateRunnerMap() {
		RunnerRaceData runnerData = registry().findRunnerData(selectedChip());
		if( runnerData!=null ) {
			runnerP.updateRunnerData(runnerData);
			displayTraceFor( runnerData );
		}
	}

	private String selectedChip() {
		String chip = "";
		int selectedRow = table.getSelectedRow();
		if( selectedRow!=-1 && table.getRowCount() > 0) {
			// we have to test the number of displayed rows too.
			// If user inputs a filter which matches nothins,
			// there is no row to show but table still points to the 0-index.
			chip = (String) table.getValueAt(selectedRow, 1);
		}
		return chip;
	}
	
	private void displayTraceFor(RunnerRaceData runnerData) {
		Punch course = courses.get(runnerData.getCourse().getName());
		if( runnerData.hasTrace() ) {
			map.showTrace(createPunchTraceFor(course, runnerData.getResult().formatTrace().split(",") ));
		} else {
			resetControls();
			map.showTrace(course);
		}
	}
	

	public static Map<String, ControlCircle> createMapControlsFrom(Map<String, Float[]> controls) {
		Point mapOrigin = new Point();
		Map<String, ControlCircle> mCtr = new HashMap<String, ControlCircle>();
		for (String controlId : controls.keySet()) {
			Float[] origin = controls.get(controlId);
			 Point position = createPointFrom(origin);
			if( controlId.equals("Map") ) {
				mapOrigin = position;
			} else {
				mCtr.put(controlId, new ControlCircle(controlId, position));
			}
		}
		for (ControlCircle mapControl : mCtr.values()) {
			mapControl.getPosition().translate( xTrans - mapOrigin.x, yTrans - mapOrigin.y);
		}
		return mCtr;
	}

	public Map<String, Punch> createCoursesFrom(Vector<Course> courses) {
		HashMap<String,Punch> startPunches = new HashMap<String, Punch>();
		for (Course course : courses) {
			Punch previousPunch = createPunch("S1");
			startPunches.put(course.getName(), previousPunch);
			int i = 1;
			for (int code : course.getCodes()) {
				Punch punch = createPunch(Integer.toString(code), i);
				if( previousPunch!=null ) {
					previousPunch.setNextPunch(punch);
				}
				previousPunch = punch;
				i++;
			}
			previousPunch.setNextPunch(createPunch("F1"));
		}
		return startPunches;
	}

	private Punch createPunch(String code) {
		return new Punch(controls.get(code));
	}

	private Punch createPunch(String code, int order) {
		return new Punch(controls.get(code), order);
	}

	private static Point createPointFrom(Float[] origin) {
		// TODO: check, is is circle center or bounding box origin?
		float x = origin[0].floatValue() * xFactor;
		float y = origin[1].floatValue() * -1 * yFactor;
		return new Point((int) x, (int) y);
	}
	
	private Punch createPunchTrace(Map<String, ControlCircle> controls) {
		Punch start = createPunch("31");
		Punch punch2 = createPunch("32");
		Punch punch3 = createPunch("33");
		Punch punch4 = createPunch("41");
		Punch punch5 = createPunch("36");
		start.setNextPunch(punch2);
		punch2.setNextPunch(punch3);
		punch2.nextPunchMissed();
		punch2.setNextPunch(punch4);
		punch3.setNextPunch(punch5);
		punch4.setNextPunch(punch5);
		start.beOk();
		punch2.beOk();
		punch4.beAdded();
		punch5.beOk();
		return start;
	}

	/**
	 * @param punch
	 * @return
	 */
	private Punch createPunchTraceA(Punch punch) {
		String[] traceString = new String[] {
				"31", "+40", "32", "-33+41", "-34", "35", "36", "+38", "37", "38", "39", "-31"
		};
		return createPunchTraceFor(punch, traceString);
	}

	private Punch createPunchTraceFor(Punch punch, String[] traceString) {
		resetControls();
		Punch startPunch = punch.clone(); // start control
		Punch previousPunch = startPunch;
		for (String code : traceString) {
			if( code.startsWith("-") ) {
				Punch nextPunch = previousPunch.getNextPunch().getNextPunch();
				if( !previousPunch.isAdded() ) {
					previousPunch.nextPunchMissed();
				} else {
					previousPunch.getNextPunch().beMissed();
				}
				previousPunch.setNextPunch(nextPunch);
				if( code.contains("+") ) {
					Punch addedPunch = createPunch(code.substring(code.indexOf("+") + 1));
					addedPunch.beAdded();
					addedPunch.setNextPunch(previousPunch.getNextPunch());
					previousPunch.setNextPunch(addedPunch);
					previousPunch = previousPunch.getNextPunch();
				}
			} else {
				if( code.startsWith("+") ) {
					Punch addedPunch = createPunch(code.substring(1));
					addedPunch.beAdded();
					addedPunch.setNextPunch(previousPunch.getNextPunch());
					previousPunch.setNextPunch(addedPunch);
				} else {
					previousPunch.getNextPunch().beOk();
				}
				previousPunch = previousPunch.getNextPunch();
			}
		}
		return startPunch;
	}

	/**
	 * 
	 */
	private void resetControls() {
		for (ControlCircle control : controls.values()) {
			control.resetStatus();
		}
	}


}
