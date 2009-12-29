/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.control;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import valmo.geco.core.Announcer;
import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class RegistryStats extends Control
	implements Announcer.RunnerListener, Announcer.StageConfigListener {

	/*
	 * compute stats on registry after initial upload and keep numbers up to date by following
	 * events
	 */
	
	private Map<String, Integer> stageStats;
	
	private Map<String, Integer> courseStats;
	
	/**
	 * @param factory
	 * @param stage
	 */
	public RegistryStats(Factory factory, Stage stage, Announcer announcer) {
		super(factory, stage, announcer);
		announcer.registerRunnerListener(this);
		announcer.registerStageConfigListener(this);
		fullUpdate();
	}
	
	public Collection<String> stageStatsKeys() {
		return stageStats.keySet();
	}

	public Collection<String> courseStatsKeys() {
		Vector<String> v = new Vector<String>();
		for (Course c : registry().getCourses()) {
			v.add(c.getName());
		}
		return v;
	}
	
	public Integer getStageStatsFor(String key) {
		return stageStats.get(key);
	}
	
	public Integer getCourseStatsFor(String key) {
		return courseStats.get(key);
	}
	
	public void fullUpdate() {
		updateStatusStats();
		updateStageStats();
		updateCourseStats();
	}
	
	public void updateStageStats() {
		int total = registry().getRunners().size();
		stageStats.put("total", total);
		stageStats.put("present", total - stageStats.get("DNS"));
		stageStats.put("finished", total - stageStats.get("DNS") - stageStats.get("Unknown"));
		stageStats.put("running", stageStats.get("Unknown"));
	}

	public void updateStatusStats() {
		stageStats = new HashMap<String, Integer>();
		for (Status s : Status.values()) {
			stageStats.put(s.toString(), 0);
		}
		for (RunnerRaceData data : registry().getRunnersData()) {
			inc(data.getResult().getStatus().toString(), stageStats);
		}
	}

	public void updateCourseStats() {
		courseStats = new HashMap<String, Integer>();
		for (Course c : registry().getCourses()) {
			updateStatsForCourse(c);
		}
	}

	private void updateStatsForCourse(Course c) {
		courseStats.put(c.getName(), registry().getRunnersFromCourse(c).size());
		courseStats.put(c.getName() + "running", registry().countRunningRunnersFor(c));
	}

	private void inc(String status, Map<String, Integer> map) {
		map.put(status, map.get(status) + 1);
	}

	private void dec(String status, Map<String, Integer> map) {
		map.put(status, map.get(status) - 1);
	}

	
	@Override
	public void changed(Stage previous, Stage next) {
		super.changed(previous, next);
		fullUpdate();
	}


	/* (non-Javadoc)
	 * @see valmo.geco.ui.Announcer.RunnerListener#runnerCreated(valmo.geco.model.Runner)
	 */
	@Override
	public void runnerCreated(RunnerRaceData data) {
		inc(data.getResult().getStatus().toString(), stageStats);
		updateStageStats();
		String courseName = data.getCourse().getName();
		inc(courseName, courseStats);
		if( data.isRunning() )
			inc(courseName + "running", courseStats);
	}


	/* (non-Javadoc)
	 * @see valmo.geco.ui.Announcer.RunnerListener#runnerDeleted(valmo.geco.model.Runner)
	 */
	@Override
	public void runnerDeleted(RunnerRaceData data) {
		dec(data.getResult().getStatus().toString(), stageStats);
		updateStageStats();
		String courseName = data.getCourse().getName();
		dec(courseName, courseStats);
		if( data.isRunning() )
			dec(courseName + "running", courseStats);
	}


	/* (non-Javadoc)
	 * @see valmo.geco.ui.Announcer.RunnerListener#statusChanged(valmo.geco.model.RunnerRaceData, valmo.geco.model.Status)
	 */
	@Override
	public void statusChanged(RunnerRaceData runnerData, Status oldStatus) {
		Status newStatus = runnerData.getResult().getStatus();
		inc(newStatus.toString(), stageStats);
		dec(oldStatus.toString(), stageStats);
		updateStageStats();
		updateStatsForCourse(runnerData.getCourse());
//		if( newStatus != oldStatus ) {
//			if( isRunning(newStatus) && !isRunning(oldStatus) ) {
//				inc(runnerData.getCourse().getName() + "running", courseStats);
//			}
//			if( !isRunning(newStatus) && isRunning(oldStatus) ) {
//				dec(runnerData.getCourse().getName() + "running", courseStats);
//			}
//		}
	}


	/* (non-Javadoc)
	 * @see valmo.geco.ui.Announcer.RunnerListener#runnerChanged(valmo.geco.model.Runner)
	 */
	@Override
	public void courseChanged(Runner runner, Course oldCourse) {
		inc(runner.getCourse().getName(), courseStats);
		dec(oldCourse.getName(), courseStats);
		updateStatsForCourse(oldCourse);
//		updateStatsForCourse(newCourse); // assume statusChanged is called and will do the update
//		if( isRunning(registry().findRunnerData(runner)) ) {
//			inc(runner.getCourse().getName() + "running", courseStats);
//			dec(oldCourse.getName() + "running", courseStats);			
//		}
	}

	/* (non-Javadoc)
	 * @see valmo.geco.core.Announcer.RunnerListener#cardRead(java.lang.String)
	 */
	@Override
	public void cardRead(String chip) {
		fullUpdate();
	}

	@Override
	public void categoriesChanged() {}

	@Override
	public void clubsChanged() {}

	@Override
	public void coursesChanged() {
		updateCourseStats();
	}

	
}
