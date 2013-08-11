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
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.RunnerRaceData;
import net.geco.model.impl.POFactory;
import net.geco.model.xml.CourseSaxImporter;
import net.geco.ui.basics.SwingUtils;
import net.geco.ui.framework.RunnersTableAnnouncer;
import net.geco.ui.framework.RunnersTableAnnouncer.RunnersTableListener;
import net.geco.ui.framework.UIAnnouncers;


/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class LiveComponent implements RunnersTableListener {

	static {
		Messages.put("live", "net.geco.live.messages"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private JFrame jFrame;
	private LiveMapComponent map;
	private ResultPanel runnerP;

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
		configP.loadFromProperties(dir);
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
	
	public LiveMapComponent mapComponent() {
		return map;
	}
	
	public void loadMapImage(String mapfile) throws FileNotFoundException, IOException {
		map.loadMapImage(mapfile);
	}

	public void importCourseData(String filename) {
		try {
			controlPos.clear(); 
			courses.clear();
			CourseSaxImporter importer = new CourseSaxImporter(new POFactory());
			importer.importFromXml(filename);
			controlPos = importer.controls();
			courses = importer.courses();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createControls(float dpmmFactor) {
		mapControl.createControls(controlPos, dpmmFactor);
	}
	
	public void createCourses() {
		mapControl.createCoursesFrom(courses);
	}

	public void translateControls(int dx, int dy) {
		mapControl.translateControls(dx, dy);
	}

	public void adjustControls(int ox, int oy, float xFactor, float yFactor) {
		mapControl.adjustControls(ox, oy, xFactor, yFactor);
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
				map.showTrace( mapControl.createPunchTraceFor(course,
										runnerData.getTraceData().formatTrace().split(",")) ); //$NON-NLS-1$
			} else {
				map.showTrace(course);
			}
		} else {
			displayMap();
		}
	}

	@Override
	public void selectedRunnerChanged(RunnerRaceData raceData) {
		if( raceData!=null && isShowing() ) {
			displayRunnerMap(raceData);
		}		
	}

	public void registerWith(UIAnnouncers announcers) {
		announcers.getAnnouncer(RunnersTableAnnouncer.class).registerRunnersTableListener(this);
	}
	
}
