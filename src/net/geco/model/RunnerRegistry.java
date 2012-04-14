/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Simon Denier
 * @since Jun 1, 2011
 *
 */
public class RunnerRegistry {

	private HashMap<Integer, Runner> runnersById;
	
	private HashMap<String, Runner> runnersByEcard;

	private HashMap<Category, List<Runner>> runnersByCategory;

	private HashMap<Course, List<Runner>> runnersByCourse;

	private HashMap<Runner, RunnerRaceData> runnerData;

	private int maxStartId;

	
	public RunnerRegistry() {
		runnersById = new HashMap<Integer, Runner>();
		runnersByEcard = new HashMap<String, Runner>();
		runnersByCategory = new HashMap<Category, List<Runner>>();
		runnersByCourse = new HashMap<Course, List<Runner>>();
		runnerData = new HashMap<Runner, RunnerRaceData>();
		maxStartId = 0;
	}

	public synchronized int maxStartId() {
		return maxStartId;
	}

	private void checkMaxStartId(Runner runner) {
		maxStartId = Math.max(maxStartId, runner.getStartId().intValue());
	}
	
	private void detectMaxStartId() {
		maxStartId = 0;
		for (Runner runner : runnersById.values()) {
			checkMaxStartId(runner);
		}
	}

	public synchronized Set<Integer> getStartIds() {
		return runnersById.keySet();
	}

	public synchronized Set<String> getEcards() {
		return runnersByEcard.keySet();
	}

	public synchronized Collection<Runner> getRunners() {
		return runnersById.values();
	}

	public synchronized Runner findRunnerById(Integer id) {
		return runnersById.get(id);
	}
	
	public synchronized void addRunner(Runner runner) {
		addRunnerWithId(runner);
		putRunnerByEcard(runner);
		putRunnerInCategoryList(runner, runner.getCategory());
		putRunnerInCourseList(runner, runner.getCourse());
	}

	private void putRunnerByEcard(Runner runner) {
		String ecard = runner.getEcard();
		if( ecard!=null && ! ecard.equals("")){ //$NON-NLS-1$
			runnersByEcard.put(ecard, runner);
		}
	}

	private void addRunnerWithId(Runner runner) {
		runnersById.put(runner.getStartId(), runner);
		checkMaxStartId(runner);
	}

	public synchronized void addRunnerWithoutId(Runner runner) {
		runner.setStartId(Integer.valueOf(maxStartId + 1));
		addRunner(runner);
	}

	public synchronized void addRunnerSafely(Runner runner) {
		if( availableStartId(runner.getStartId()) ){
			addRunner(runner);
		} else {
			addRunnerWithoutId(runner);
		}
	}

	public synchronized void removeRunner(Runner runner) {
		Integer startId = runner.getStartId();
		runnersById.remove(startId);
		runnersByEcard.remove(runner.getEcard());
		runnersByCourse.get(runner.getCourse()).remove(runner);
		runnersByCategory.get(runner.getCategory()).remove(runner);
		if( maxStartId==startId.intValue() ){
			detectMaxStartId();
		}
	}

	public synchronized void updateRunnerStartId(Integer oldId, Runner runner) {
		runnersById.remove(oldId);
		addRunnerWithId(runner);
	}

	public synchronized boolean availableStartId(Integer id) {
		return id!=null && ! runnersById.containsKey(id);
	}

	private void putRunnerInCategoryList(Runner runner, Category category) {
		runnersByCategory.get(category).add(runner);
	}

	public synchronized List<Runner> getRunnersFromCategory(Category category) {
		return runnersByCategory.get(category);
	}

	public synchronized void categoryCreated(Category category) {
		runnersByCategory.put(category, new LinkedList<Runner>());
	}

	public synchronized void categoryDeleted(Category category) {
		runnersByCategory.remove(category);
	}

	public synchronized void updateRunnerCategory(Category oldCat, Runner runner) {
		if( !oldCat.equals(runner.getCategory()) ){
			runnersByCategory.get(oldCat).remove(runner);
			putRunnerInCategoryList(runner, runner.getCategory());
		}
	}

	public synchronized Runner findRunnerByEcard(String ecard) {
		return runnersByEcard.get(ecard);
	}

	public synchronized void updateRunnerEcard(String oldEcard, Runner runner) {
		if( oldEcard==null || !oldEcard.equals(runner.getEcard()) ){
			runnersByEcard.remove(oldEcard);
			putRunnerByEcard(runner);
		}
	}

	public synchronized void courseCreated(Course course) {
		runnersByCourse.put(course, new LinkedList<Runner>());
	}

	public synchronized void courseDeleted(Course course) {
		runnersByCourse.remove(course);
	}

	public synchronized List<Runner> getRunnersFromCourse(Course course) {
		return runnersByCourse.get(course);
	}
	
	private void putRunnerInCourseList(Runner runner, Course course){
		runnersByCourse.get(course).add(runner);
	}

	public synchronized void updateRunnerCourse(Course oldCourse, Runner runner) {
		if( !oldCourse.equals(runner.getCourse()) ){
			runnersByCourse.get(oldCourse).remove(runner);
			putRunnerInCourseList(runner, runner.getCourse());
		}
	}

	public synchronized void addRunnerData(RunnerRaceData data) {
		runnerData.put(data.getRunner(), data);
	}

	public synchronized Collection<RunnerRaceData> getRunnersData() {
		return runnerData.values();
	}

	public synchronized RunnerRaceData findRunnerData(Runner runner) {
		return runnerData.get(runner);
	}
	
	public synchronized RunnerRaceData findRunnerData(Integer startId) {
		return runnerData.get(findRunnerById(startId));
	}

	public synchronized RunnerRaceData findRunnerData(String ecard) {
		return runnerData.get(findRunnerByEcard(ecard));
	}

	public synchronized void removeRunnerData(RunnerRaceData data) {
		runnerData.remove(data.getRunner());
	}
	
	public synchronized Map<Course, List<Runner>> getRunnersByCourseFromCategory(Category cat) {
		HashMap<Course, List<Runner>> map = new HashMap<Course, List<Runner>>();
		List<Runner> runners = getRunnersFromCategory(cat);
		if( runners==null ) return map;
		for (Runner runner : runners) {
			if( ! map.containsKey(runner.getCourse()) ) {
				map.put(runner.getCourse(), new LinkedList<Runner>());
			}
			map.get(runner.getCourse()).add(runner);
		}
		return map;
	}

	public synchronized List<RunnerRaceData> getRunnerDataFromCourse(Course course) {
		List<Runner> runners = getRunnersFromCourse(course);
		ArrayList<RunnerRaceData> runnerDataFromCourse = new ArrayList<RunnerRaceData>(runners.size());
		for (Runner runner : runners) {
			runnerDataFromCourse.add(findRunnerData(runner));
		}
		return runnerDataFromCourse;
	}
	
}
