/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import valmo.geco.model.Course;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.impl.POFactory;
import valmo.geco.model.xml.CourseSaxImporter;
import valmo.geco.ui.StartStopButton;
import valmo.geco.ui.SwingUtils;

/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class LiveComponent {

	public JFrame jFrame;
	public LiveMapComponent map;
	public RunnerResultPanel runnerP;

	private LiveMapControl liveControl;

	private Map<String, Float[]> controlPos;
	private Collection<Course> courses;
	private JFormattedTextField portF;
	private StartStopButton listenB;


	public static void main(String[] args) {
		LiveComponent gecoLive = new LiveComponent().initWindow();
//		gecoLive.loadMapImage("hellemmes.jpg");
//		gecoLive.importCourseData("hellemmes.xml");
//		float factor = GecoLiveConfig.dpi2dpmmFactor(150);
//		gecoLive.createCourses(factor, factor, -18, -25);
		gecoLive.openWindow();
	}

	public LiveComponent() {
		controlPos = Collections.emptyMap();
		courses = Collections.emptyList();
		liveControl = new LiveMapControl();
	}

	public LiveComponent initWindow() {
		jFrame = new JFrame();
		initGui(jFrame.getContentPane());
		jFrame.pack();
		return this;
	}
	public void openWindow() {
		jFrame.setVisible(true);
	}
	public void closeWindow() {
		jFrame.setVisible(false);
	}
	public boolean isShowing() {
		return jFrame.isShowing();
	}

	public Container initGui(Container mainContainer) {
		mainContainer.setLayout(new BorderLayout());
		mainContainer.add(initControlPanel(), BorderLayout.WEST);
		map = new LiveMapComponent();
		mainContainer.add(new JScrollPane(map), BorderLayout.CENTER);
		return mainContainer;
	}
	private Container initControlPanel() {
		JTabbedPane controlPanel = new JTabbedPane();
		runnerP = new RunnerResultPanel();
		controlPanel.add("Config", SwingUtils.embed(new LiveConfigPanel(jFrame, this)));
		controlPanel.add("Live", initLivePanel());
		return controlPanel;
	}
	
	private Component initLivePanel() {
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
					// TODO gecoControl parameter
					server = new LiveServerMulti(null, Integer.parseInt(portF.getText())).accept();
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

		JPanel networkConfigP = new JPanel(new GridLayout(0, 3));
		networkConfigP.setBorder(BorderFactory.createTitledBorder("Start Live Server"));
		networkConfigP.add(SwingUtils.embed(listenB));
		networkConfigP.add(new JLabel("Port:", SwingConstants.RIGHT));
		networkConfigP.add(SwingUtils.embed(portF));

		JPanel livePanel = new JPanel(new BorderLayout());
		livePanel.add(SwingUtils.embed(runnerP), BorderLayout.NORTH);
		livePanel.add(networkConfigP, BorderLayout.SOUTH);
		return livePanel;
	}

	public void loadMapImage(String mapfile) {
		map.loadMapImage(mapfile);
	}

	public void importCourseData(String filename) {
		try {
			controlPos.clear(); 
			courses.clear();
			CourseSaxImporter importer = new CourseSaxImporter(new POFactory()); // TODO: service
			importer.importFromXml(filename);
			controlPos = importer.controls();
			courses = importer.courses();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createCourses(float xFactor, float yFactor, int dx, int dy) {
		liveControl.setXFactor(xFactor);
		liveControl.setYFactor(yFactor);
		liveControl.createControlsFrom(controlPos, dx, dy);
		liveControl.createCoursesFrom(courses);
	}
	
	public Vector<String> coursenames() {
		Vector<String> coursenames = new Vector<String>();
		for (Course course : courses) {
			coursenames.add(course.getName());
		}
		return coursenames;
	}

	public void displayMap() {
		map.showControls(null);
	}
	
	public void displayAllControls() {
		liveControl.resetControls();
		map.showControls(liveControl.allControls());
	}
	
	public void displayCourse(String coursename) {
		LivePunch course = liveControl.startPunchForCourse(coursename);
		if( course!=null ) {
			liveControl.resetControls();
			map.showTrace(course);
		}
	}

	public void displayRunnerMap(RunnerRaceData runnerData) {
		if( runnerData!=null ) {
			runnerP.updateRunnerData(runnerData);
			displayTraceFor( runnerData );
		}
	}

	private void displayTraceFor(RunnerRaceData runnerData) {
		LivePunch course = liveControl.startPunchForCourse(runnerData.getCourse().getName());
		if( course!=null ) {
			liveControl.resetControls();
			if( runnerData.hasTrace() ) {
				map.showTrace( liveControl.createPunchTraceFor(course, runnerData.getResult().formatTrace().split(",")) );
			} else {
				map.showTrace(course);
			}
		}
	}
	
}
