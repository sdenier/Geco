/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import net.geco.basics.Announcer;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;


/**
 * Compute statistics on registry after initial upload and keep numbers up to date by following events.
 * 
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class RegistryStats extends Control
	implements Announcer.StageListener, Announcer.RunnerListener, Announcer.StageConfigListener {

	public static enum StatItem {
		Registered {
			public String toString() {
				return Messages.getString("RegistryStats.RegisteredLabel"); //$NON-NLS-1$
			}},
		Present {
			public String toString() {
				return Messages.getString("RegistryStats.PresentLabel"); //$NON-NLS-1$
			}},
		Unresolved {
			public String toString() {
				return Messages.getString("RegistryStats.UnresolvedLabel"); //$NON-NLS-1$
			}},
		NOS {
			public String toString() {
				return Messages.getString("Status.NotStartedLabel"); //$NON-NLS-1$
			}},
		RUN {
			public String toString() {
				return Messages.getString("Status.RunningLabel"); //$NON-NLS-1$
			}},
		Finished {
			public String toString() {
				return Messages.getString("RegistryStats.FinishedLabel"); //$NON-NLS-1$
			}},
		OK {
			public String toString() {
				return Messages.getString("Status.OKLabel"); //$NON-NLS-1$
			}},
		MP {
			public String toString() {
				return Messages.getString("Status.MPLabel"); //$NON-NLS-1$
			}},
		DNS {
			public String toString() {
				return Messages.getString("Status.DNSLabel"); //$NON-NLS-1$
			}},
		DNF {
			public String toString() {
				return Messages.getString("Status.DNFLabel"); //$NON-NLS-1$
			}},
		DSQ {
			public String toString() {
				return Messages.getString("Status.DSQLabel"); //$NON-NLS-1$
			}},
		UNK {
			public String toString() {
				return Messages.getString("Status.UnknownLabel"); //$NON-NLS-1$
			}},
		DUP {
			public String toString() {
				return Messages.getString("Status.DuplicateLabel"); //$NON-NLS-1$
			}};
	}

	public static final StatItem[] shortStatusList = new StatItem[] {
		StatItem.Present, StatItem.Unresolved, StatItem.NOS, StatItem.Finished, StatItem.OK, StatItem.MP,
	};
	
	// Courses stats
	private Map<String, Map<StatItem, Integer>> stats;

	// Memo fields for computing total stats
	private int totalOk;
	private int totalMp;
	private int totalDns;
	private int totalDnf;
	private int totalDsq;
	private int totalNos;
	private int totalRun;
	private int totalUnk;
	private int totalDup;
	
	public RegistryStats(GecoControl gecoControl) {
		super(RegistryStats.class, gecoControl);
		Announcer announcer = gecoControl.announcer();
		announcer.registerStageListener(this);
		announcer.registerRunnerListener(this);
		announcer.registerStageConfigListener(this);
		fullUpdate();
	}
	
	public StatItem[] longStatuses() {
		return StatItem.values();
	}
	
	public StatItem[] shortStatuses() {
		return shortStatusList;
	}
	
	public String[] sortedEntries() {
		Vector<String> entries = new Vector<String>(registry().getSortedCourseNames());
		entries.add(totalName());
		return entries.toArray(new String[0]);
	}
	
	public static String totalName() {
		return Messages.getString("RegistryStats.TotalLabel"); //$NON-NLS-1$
	}

	public Map<StatItem, Integer> getCourseStatsFor(String coursename) {
		return stats.get(coursename);
	}

	public Map<StatItem, Integer> getTotalCourse() {
		return stats.get(totalName());
	}
	
	public Integer getCourseStatsFor(String course, StatItem item) {
		return getCourseStatsFor(course).get(item);
	}
	
	public void fullUpdate() {
		initStatMaps();
		for (Course course : registry().getCourses()) {
			stats.put(course.getName(), new HashMap<StatItem, Integer>());
			computeCourseStats(course);
		}
		computeTotalStats();
	}
	
	public StatItem convertStatus(Status status) {
		return StatItem.valueOf(status.name());
	}
	
	
	private void initStatMaps() {
		stats = new HashMap<String, Map<StatItem,Integer>>();
		stats.put(totalName(), new HashMap<StatItem, Integer>());
		totalOk = 0;
		totalMp = 0;
		totalDns = 0;
		totalDnf = 0;
		totalDsq = 0;
		totalNos = 0;
		totalRun = 0;
		totalUnk = 0;
		totalDup = 0;
	}
	
	private void computeCourseStats(Course course) {
		int courseOk = 0, courseMp = 0, courseDns = 0, courseDnf = 0, courseDsq = 0;
		int courseNos = 0, courseRun = 0, courseUnk = 0, courseDup = 0;
		List<Runner> courseData = registry().getRunnersFromCourse(course);
		int total = courseData.size();
		for (Runner runner : courseData) {
			switch (registry().findRunnerData(runner).getResult().getStatus()) {
			case OK: courseOk++; totalOk++; break;
			case MP: courseMp++; totalMp++; break;
			case DNS: courseDns++; totalDns++; break;
			case DNF: courseDnf++; totalDnf++; break;
			case DSQ: courseDsq++; totalDsq++; break;
			case NOS: courseNos++; totalNos++; break;
			case RUN: courseRun++; totalRun++; break;
			case UNK: courseUnk++; totalUnk++; break;
			case DUP: courseDup++; totalDup++; break;
			}
		}
		Map<StatItem, Integer> courseStats = stats.get(course.getName());
		storeStats(courseOk, courseMp, courseDns, courseDnf, courseDsq, courseNos, courseRun, courseUnk, courseDup,
					total, courseStats);
	}

	private void computeTotalStats() {
		int total = registry().getRunners().size();
		storeStats(totalOk, totalMp, totalDns, totalDnf, totalDsq, totalNos, totalRun, totalUnk, totalDup,
					total, getTotalCourse());
	}
	
	private void storeStats(int ok, int mp, int dns, int dnf, int dsq, int nos, int run, int unk, int dup, 
			int total, Map<StatItem, Integer> courseStats) {
		courseStats.put(StatItem.OK, ok);
		courseStats.put(StatItem.MP, mp);
		courseStats.put(StatItem.DNS, dns);
		courseStats.put(StatItem.DNF, dnf);
		courseStats.put(StatItem.DSQ, dsq);
		courseStats.put(StatItem.NOS, nos);
		courseStats.put(StatItem.RUN, run);
		courseStats.put(StatItem.UNK, unk);
		courseStats.put(StatItem.DUP, dup);
		int unresolved = nos + run + unk + dup;
		courseStats.put(StatItem.Registered, total);
		courseStats.put(StatItem.Present, (total - dns));
		courseStats.put(StatItem.Unresolved, unresolved);
		courseStats.put(StatItem.Finished, (total - dns - unresolved));
	}

	private void updateCourseStats(Map<StatItem, Integer> courseStats, int total) {
		int dns = courseStats.get(StatItem.DNS);
		int nos = courseStats.get(StatItem.NOS);
		int run = courseStats.get(StatItem.RUN);
		int unk = courseStats.get(StatItem.UNK);
		int dup = courseStats.get(StatItem.DUP);
		int unresolved = nos + run + unk + dup;
		courseStats.put(StatItem.Registered, total);
		courseStats.put(StatItem.Present, (total - dns));
		courseStats.put(StatItem.Unresolved, unresolved);
		courseStats.put(StatItem.Finished, (total - dns - unresolved));
	}

	private int inc(StatItem item, Map<StatItem, Integer> map) {
		int value = map.get(item) + 1;
		map.put(item, value);
		return value;
	}

	private int dec(StatItem item, Map<StatItem, Integer> map) {
		int value = map.get(item) - 1;
		map.put(item, value);
		return value;
	}


	@Override
	public void runnerCreated(RunnerRaceData data) {
		StatItem item = convertStatus(data.getStatus());
		
		Map<StatItem, Integer> courseStats = getCourseStatsFor(data.getCourse().getName());
		inc(item, courseStats);
		int courseTotal = inc(StatItem.Registered, courseStats);
		updateCourseStats(courseStats, courseTotal);
		
		inc(item, getTotalCourse());
		int total = inc(StatItem.Registered, getTotalCourse());
		updateCourseStats(getTotalCourse(), total);
	}

	@Override
	public void runnerDeleted(RunnerRaceData data) {
		StatItem item = convertStatus(data.getStatus());
		
		Map<StatItem, Integer> courseStats = getCourseStatsFor(data.getCourse().getName());
		dec(item, courseStats);
		int courseTotal = dec(StatItem.Registered, courseStats);
		updateCourseStats(courseStats, courseTotal);
		
		dec(item, getTotalCourse());
		int total = dec(StatItem.Registered, getTotalCourse());
		updateCourseStats(getTotalCourse(), total);
	}

	@Override
	public void statusChanged(RunnerRaceData data, Status oldStatus) {
		StatItem item = convertStatus(data.getStatus());
		StatItem oldItem = convertStatus(oldStatus);
		
		Map<StatItem, Integer> courseStats = getCourseStatsFor(data.getCourse().getName());
		inc(item, courseStats);
		dec(oldItem, courseStats);
		int courseTotal = getCourseStatsFor(data.getCourse().getName(), StatItem.Registered);
		updateCourseStats(courseStats, courseTotal);
		
		inc(item, getTotalCourse());
		dec(oldItem, getTotalCourse());
		int total = getCourseStatsFor(totalName(), StatItem.Registered);
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
