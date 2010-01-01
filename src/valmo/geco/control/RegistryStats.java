/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.control;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public static enum StatItem {
		OK, MP, DNS, DNF, DSQ, Unknown, Registered, Present, Finished
		// short list: reg, pres, unknown, ok, mp
	}
	
	private Map<String, Map<String, Integer>> stats;
	private int totalOk;
	private int totalMp;
	private int totalDns;
	private int totalDnf;
	private int totalDsq;
	private int totalUnknown;
	
	public RegistryStats(Factory factory, Stage stage, Announcer announcer) {
		super(factory, stage, announcer);
		announcer.registerRunnerListener(this);
		announcer.registerStageConfigListener(this);
		fullUpdate();
	}
	
	public StatItem[] statuses() {
		return StatItem.values();
//		return stats.entrySet().iterator().next().getValue().keySet();
	}
	
	public Collection<String> entries() {
		return stats.keySet();
	}

	public Map<String, Integer> getCourseStatsFor(String coursename) {
		return stats.get(coursename);
	}

	public Map<String, Integer> getTotalCourse() {
		return stats.get("Total");
	}
	
	public Integer getCourseStatsFor(String course, String status) {
		return getCourseStatsFor(course).get(status);
	}
	
	public void fullUpdate() {
		initStatMaps();
		for (Course course : registry().getCourses()) {
			stats.put(course.getName(), new HashMap<String, Integer>());
			computeCourseStats(course);
		}
		computeTotalStats();
	}
	
	private void initStatMaps() {
		stats = new HashMap<String, Map<String,Integer>>();
		stats.put("Total", new HashMap<String, Integer>());
		totalOk = 0;
		totalMp = 0;
		totalDns = 0;
		totalDnf = 0;
		totalDsq = 0;
		totalUnknown = 0;
	}
	
	private void computeCourseStats(Course course) {
		int courseOk = 0, courseMp = 0, courseDns = 0, courseDnf = 0, courseDsq = 0, courseUnknown = 0;
		List<Runner> courseData = registry().getRunnersFromCourse(course);
		int total = courseData.size();
		for (Runner runner : courseData) {
			switch (registry().findRunnerData(runner).getResult().getStatus()) {
			case OK: courseOk++; totalOk++; break;
			case MP: courseMp++; totalMp++; break;
			case DNS: courseDns++; totalDns++; break;
			case DNF: courseDnf++; totalDnf++; break;
			case DSQ: courseDsq++; totalDsq++; break;
			case Unknown: courseUnknown++; totalUnknown++; break;
			}
		}
		Map<String, Integer> courseStats = stats.get(course.getName());
		storeStats(courseOk, courseMp, courseDns, courseDnf, courseDsq, courseUnknown, total, courseStats);
	}

	private void computeTotalStats() {
		int total = registry().getRunners().size();
		storeStats(totalOk, totalMp, totalDns, totalDnf, totalDsq, totalUnknown, total, getTotalCourse());
	}
	
	private void storeStats(int ok, int mp, int dns, int dnf, int dsq, int unknown, int total,
			Map<String, Integer> courseStats) {
		courseStats.put(StatItem.OK.toString(), ok);
		courseStats.put(StatItem.MP.toString(), mp);
		courseStats.put(StatItem.DNS.toString(), dns);
		courseStats.put(StatItem.DNF.toString(), dnf);
		courseStats.put(StatItem.DSQ.toString(), dsq);
		courseStats.put(StatItem.Unknown.toString(), unknown);
		courseStats.put(StatItem.Registered.toString(), total);
		courseStats.put(StatItem.Present.toString(), (total - dns));
		courseStats.put(StatItem.Finished.toString(), (total - dns - unknown));
	}

	
	private void updateCourseStats(Map<String, Integer> courseStats, int total) {
		int dns = courseStats.get(StatItem.DNS.toString());
		int unknown = courseStats.get(StatItem.Unknown.toString());
		courseStats.put(StatItem.Registered.toString(), total);
		courseStats.put(StatItem.Present.toString(), (total - dns));
		courseStats.put(StatItem.Finished.toString(), (total - dns - unknown));
	}

	private int inc(String status, Map<String, Integer> map) {
		int value = map.get(status) + 1;
		map.put(status, value);
		return value;
	}

	private int dec(String status, Map<String, Integer> map) {
		int value = map.get(status) - 1;
		map.put(status, value);
		return value;
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
		String status = data.getResult().getStatus().toString();
		
		Map<String, Integer> courseStats = getCourseStatsFor(data.getCourse().getName());
		inc(status, courseStats);
		int courseTotal = inc(StatItem.Registered.toString(), courseStats);
		updateCourseStats(courseStats, courseTotal);
		
		inc(status, getTotalCourse());
		int total = inc(StatItem.Registered.toString(), getTotalCourse());
		updateCourseStats(getTotalCourse(), total);
	}


	/* (non-Javadoc)
	 * @see valmo.geco.ui.Announcer.RunnerListener#runnerDeleted(valmo.geco.model.Runner)
	 */
	@Override
	public void runnerDeleted(RunnerRaceData data) {
		String status = data.getResult().getStatus().toString();
		
		Map<String, Integer> courseStats = getCourseStatsFor(data.getCourse().getName());
		dec(status, courseStats);
		int courseTotal = dec(StatItem.Registered.toString(), courseStats);
		updateCourseStats(courseStats, courseTotal);
		
		dec(status, getTotalCourse());
		int total = dec(StatItem.Registered.toString(), getTotalCourse());
		updateCourseStats(getTotalCourse(), total);
	}


	/* (non-Javadoc)
	 * @see valmo.geco.ui.Announcer.RunnerListener#statusChanged(valmo.geco.model.RunnerRaceData, valmo.geco.model.Status)
	 */
	@Override
	public void statusChanged(RunnerRaceData data, Status oldStatus) {
		String status = data.getResult().getStatus().toString();

		Map<String, Integer> courseStats = getCourseStatsFor(data.getCourse().getName());
		inc(status, courseStats);
		dec(oldStatus.toString(), courseStats);
		int courseTotal = getCourseStatsFor(data.getCourse().getName(), StatItem.Registered.toString());
		updateCourseStats(courseStats, courseTotal);
		
		dec(status, getTotalCourse());
		int total = dec(StatItem.Registered.toString(), getTotalCourse());
		updateCourseStats(getTotalCourse(), total);
	}

	@Override
	public void courseChanged(Runner runner, Course oldCourse) {
		computeCourseStats(oldCourse);
		computeCourseStats(runner.getCourse());
	}

	@Override
	public void cardRead(String chip) {}

	@Override
	public void categoriesChanged() {}

	@Override
	public void clubsChanged() {}

	@Override
	public void coursesChanged() {
		fullUpdate();
	}

	@Override
	public void runnersChanged() {
		fullUpdate();		
	}

	
}
