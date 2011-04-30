/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import net.geco.basics.GecoResources;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.RunnerRaceData;
import net.geco.model.impl.POFactory;
import net.geco.model.xml.CourseSaxImporter;
import net.geco.ui.basics.SwingUtils;


/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class LiveComponent {

	static {
		Messages.put("live", "valmo.geco.live.messages"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public JFrame jFrame;
	public LiveMapComponent map;
	public ResultPanel runnerP;

	private LiveMapControl mapControl;
	private Map<String, Float[]> controlPos;
	private Collection<Course> courses;
	private LiveConfigPanel configP;


	public static void main(String[] args) {
		LiveComponent gecoLive = new LiveComponent().initWindow(false);
		gecoLive.setStartDir("demo/hellemmes_live"); //$NON-NLS-1$
		gecoLive.openWindow();
	}

	public LiveComponent() {
		controlPos = Collections.emptyMap();
		courses = Collections.emptyList();
		mapControl = new LiveMapControl();
	}
	
	public void setStartDir(String dir) {
		try {
			Properties liveProp = new Properties();
			liveProp.load(GecoResources.getReaderFor(dir + GecoResources.sep + "live.prop")); //$NON-NLS-1$
			loadMapImage(dir + GecoResources.sep + liveProp.getProperty("MapFile")); //$NON-NLS-1$
			importCourseData(dir + GecoResources.sep + liveProp.getProperty("CourseFile")); //$NON-NLS-1$
			configP.setProperties(liveProp);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public LiveComponent initWindow(boolean leisureMode) {
		jFrame = new JFrame();
		initGui(jFrame.getContentPane(), leisureMode);
		jFrame.pack();
		jFrame.setLocationRelativeTo(null);
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

	public Container initGui(Container mainContainer, boolean leisureMode) {
		return initGui(mainContainer, leisureMode, true);
	}
	public Container initGui(Container mainContainer, boolean leisureMode, boolean showConfigPanel) {
		mainContainer.setLayout(new BorderLayout());
		mainContainer.add(initControlPanel(leisureMode, showConfigPanel), BorderLayout.WEST);
		map = new LiveMapComponent();
		mainContainer.add(new JScrollPane(map), BorderLayout.CENTER);
		return mainContainer;
	}
	private Container initControlPanel(boolean leisureMode, boolean showConfigPanel) {
		JTabbedPane controlPanel = new JTabbedPane();
		configP = new LiveConfigPanel(jFrame, this);
		if( showConfigPanel ) {
			controlPanel.add(Messages.liveGet("LiveComponent.ConfigTitle"), SwingUtils.embed(configP)); //$NON-NLS-1$
		}
		if( leisureMode ) {
			runnerP = new LeisureResultPanel();
		} else {
			runnerP = new RunnerResultPanel();
		}
		controlPanel.add(Messages.liveGet("LiveComponent.RunnerTitle"), runnerP); //$NON-NLS-1$
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
		mapControl.setXFactor(xFactor);
		mapControl.setYFactor(yFactor);
		mapControl.createControlsFrom(controlPos, dx, dy);
		mapControl.createCoursesFrom(courses);
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
		mapControl.resetControls();
		map.showControls(mapControl.allControls());
	}
	
	public void displayCourse(String coursename) {
		LivePunch course = mapControl.startPunchForCourse(coursename);
		if( course!=null ) {
			mapControl.resetControls();
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
		LivePunch course = mapControl.startPunchForCourse(runnerData.getCourse().getName());
		if( course!=null ) {
			mapControl.resetControls();
			if( runnerData.hasTrace() ) {
				map.showTrace( mapControl.createPunchTraceFor(course, runnerData.getResult().formatTrace().split(",")) ); //$NON-NLS-1$
			} else {
				map.showTrace(course);
			}
		}
	}
	
}
