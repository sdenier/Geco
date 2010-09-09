/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import valmo.geco.core.Announcer;
import valmo.geco.model.Course;
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
	implements Announcer.StageListener, Announcer.RunnerListener, Announcer.StageConfigListener {

	/*
	 * compute stats on registry after initial upload and keep numbers up to date by following
	 * events
	 */
	
	public static enum StatItem {
		Registered, Present, Unknown, Finished, OK, MP, DNS, DNF, DSQ
	}

	public static final String[] shortStatusList = new String[] {
		"Present", "Unknown", "Finished", "OK", "MP"
	};
	
	private Map<String, Map<String, Integer>> stats;
	private int totalOk;
	private int totalMp;
	private int totalDns;
	private int totalDnf;
	private int totalDsq;
	private int totalUnknown;
	
	public RegistryStats(GecoControl gecoControl) {
		super(gecoControl);
		Announcer announcer = gecoControl.announcer();
		announcer.registerStageListener(this);
		announcer.registerRunnerListener(this);
		announcer.registerStageConfigListener(this);
		fullUpdate();
	}
	
	public String[] longStatuses() {
		String[] statusKeys = new String[StatItem.values().length];
		for (int i = 0; i < StatItem.values().length; i++) {
			statusKeys[i] = StatItem.values()[i].toString();			
		}
		return statusKeys;
	}
	
	public String[] shortStatuses() {
		return shortStatusList;
	}
	
	public Collection<String> entries() {
		return stats.keySet();
	}
	
	public String[] sortedEntries() {
		Vector<String> entries = new Vector<String>(registry().getSortedCoursenames());
		entries.add(totalName());
		return entries.toArray(new String[0]);
	}
	
	private String totalName() {
		return "Total";
	}

	public Map<String, Integer> getCourseStatsFor(String coursename) {
		return stats.get(coursename);
	}

	public Map<String, Integer> getTotalCourse() {
		return stats.get(totalName());
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
		stats.put(totalName(), new HashMap<String, Integer>());
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
		
		inc(status, getTotalCourse());
		dec(oldStatus.toString(), getTotalCourse());
		int total = getCourseStatsFor(totalName(), StatItem.Registered.toString());
//		int total = dec(StatItem.Registered.toString(), getTotalCourse());
		updateCourseStats(getTotalCourse(), total);
	}

	@Override
	public void courseChanged(Runner runner, Course oldCourse) {
		computeCourseStats(oldCourse);
		computeCourseStats(runner.getCourse());
	}

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
	
	@Override
	public void changed(Stage previous, Stage next) {
		fullUpdate();
	}

	@Override
	public void saving(Stage stage, Properties properties) {	}

	@Override
	public void closing(Stage stage) {	}

	
}
