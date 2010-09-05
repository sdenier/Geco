/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import valmo.geco.model.Course;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.impl.POFactory;
import valmo.geco.model.xml.CourseSaxImporter;
import valmo.geco.ui.SwingUtils;

/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class GecoLiveComponent {

	public JFrame jFrame;
	public GecoMapComponent map;
	public RunnerResultPanel runnerP;

	private GecoLiveControl liveControl;

	private Map<String, Float[]> controlPos;
	private Collection<Course> courses;


	public static void main(String[] args) {
		GecoLiveComponent gecoLive = new GecoLiveComponent().initWindow();
//		gecoLive.loadMapImage("hellemmes.jpg");
//		gecoLive.importCourseData("hellemmes.xml");
//		float factor = GecoLiveConfig.dpi2dpmmFactor(150);
//		gecoLive.createCourses(factor, factor, -18, -25);
		gecoLive.openWindow();
	}

	public GecoLiveComponent() {
		controlPos = Collections.emptyMap();
		courses = Collections.emptyList();
		liveControl = new GecoLiveControl();
	}

	public GecoLiveComponent initWindow() {
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
		map = new GecoMapComponent();
		mainContainer.add(new JScrollPane(map), BorderLayout.CENTER);
		return mainContainer;
	}
	private Container initControlPanel() {
		JTabbedPane controlPanel = new JTabbedPane();
		runnerP = new RunnerResultPanel();
		controlPanel.add("Config", SwingUtils.embed(new GecoLiveConfig(jFrame, this)));
		controlPanel.add("Runner", SwingUtils.embed(runnerP));
		return controlPanel;
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
		liveControl.createMapControlsFrom(controlPos, dx, dy);
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
		Punch course = liveControl.startPunchForCourse(coursename);
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
		Punch course = liveControl.startPunchForCourse(runnerData.getCourse().getName());
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
