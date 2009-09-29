/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import valmo.geco.control.PenaltyChecker;


/**
 * Registry holds all data related to the stage itself and provides a facade to access them. It does not
 * hold properties related to the application (controls, widgets). It should evolve towards an interface
 * masking the persistence framework (database).
 * 
 * Currently, Registry is also able to perform some batch operations on itself.
 * 
 * @author Simon Denier
 * @since Jan 4, 2009
 *
 */
public class Registry {
	
	private Map<String, Course> courses;
	
	private Map<String, Club> clubs;
	
	private Map<String, Category> categories;
	
	private Map<String, Runner> runnersByChip;
	
	private Map<Course, List<Runner>> runnersByCourse;
	
	private Map<Category, List<Runner>> runnersByCategory;
	
	private Map<Runner, RunnerRaceData> runnerData;
	
	private Map<String, HeatSet> heatsets;
	
	/**
	 * 
	 */
	public Registry() {
		courses = new HashMap<String, Course>();
		clubs = new HashMap<String, Club>();
		categories = new HashMap<String, Category>();
		runnersByChip = new HashMap<String, Runner>();
		runnersByCategory = new HashMap<Category, List<Runner>>();
		runnersByCourse = new HashMap<Course, List<Runner>>();
		runnerData = new HashMap<Runner, RunnerRaceData>();
		heatsets = new HashMap<String, HeatSet>();
	}
	
	
	public Collection<Course> getCourses() {
		return courses.values();
	}

	public Vector<String> getCoursenames() {
		return new Vector<String>(courses.keySet());
	}
	
	public Course findCourse(String name) {
		return courses.get(name);
	}
	
	public void addCourse(Course course) {
		courses.put(course.getName(), course);
	}


	public Collection<Club> getClubs() {
		return clubs.values();
	}
	
	public Vector<String> getClubnames() {
		return new Vector<String>(clubs.keySet());
	}
	
	public Club findClub(String name) {
		return clubs.get(name);
	}
	
	public void addClub(Club club) {
		clubs.put(club.getName(), club);
	}

	
	public Collection<Category> getCategories() {
		return categories.values();
	}

	public Vector<String> getCategorynames() {
		return new Vector<String>(categories.keySet());
	}

	public Category findCategory(String name) {
		return categories.get(name);
	}
	
	public void addCategory(Category cat) {
		categories.put(cat.getShortname(), cat);
	}


	public Collection<Runner> getRunners() {
		return runnersByChip.values();
	}
	
	public Runner findRunnerByChip(String chip) {
		return runnersByChip.get(chip);
	}
	
	public void addRunner(Runner runner) {
		runnersByChip.put(runner.getChipnumber(), runner);
		addRunnerinCategoryList(runner, runner.getCategory());
		addRunnerinCourseList(runner, runner.getCourse());
	}
	
	private void addRunnerinCourseList(Runner runner, Course course) {
		if( !runnersByCourse.containsKey(course) ) {
			runnersByCourse.put(course, new Vector<Runner>());
		}
		runnersByCourse.get(course).add(runner);
	}

	private void addRunnerinCategoryList(Runner runner, Category cat) {
		if( !runnersByCategory.containsKey(cat) ) {
			runnersByCategory.put(cat, new Vector<Runner>());
		}
		runnersByCategory.get(cat).add(runner);
	}

	public void updateRunnerChip(String oldChip, Runner runner) {
		runnersByChip.remove(oldChip);
		runnersByChip.put(runner.getChipnumber(), runner);
	}

	public void updateRunnerCourse(Course oldCourse, Runner runner) {
		if( !oldCourse.equals(runner.getCourse() )) {
			runnersByCourse.get(oldCourse).remove(runner);
			addRunnerinCourseList(runner, runner.getCourse());
		}
	}

	public void updateRunnerCategory(Category oldCat, Runner runner) {
		if( !oldCat.equals(runner.getCategory() )) {
			runnersByCategory.get(oldCat).remove(runner);
			addRunnerinCategoryList(runner, runner.getCategory());
		}
	}
	
	public void removeRunner(Runner runner) {
		runnersByChip.remove(runner.getChipnumber());
		runnersByCategory.get(runner.getCategory()).remove(runner);
		runnersByCourse.get(runner.getCourse()).remove(runner);
	}

	public Collection<RunnerRaceData> getRunnersData() {
		return runnerData.values();
	}
	
	public RunnerRaceData findRunnerData(Runner runner) {
		return runnerData.get(runner);
	}

	public RunnerRaceData findRunnerData(String chip) {
		return runnerData.get(findRunnerByChip(chip));
	}
	
	public void addRunnerData(RunnerRaceData data) {
		runnerData.put(data.getRunner(), data);
	}
	
	public void removeRunnerData(RunnerRaceData data) {
		runnerData.remove(data.getRunner());
	}

	public RunnerRaceData createRunnerDataFor(Runner runner, Factory factory) {
		RunnerRaceData data = factory.createRunnerRaceData();
		data.setRunner(runner);
		data.setResult(factory.createRunnerResult());
		addRunnerData(data);
		return data;
	}
	
	public List<Runner> getRunnersFromCategory(Category cat) {
		return runnersByCategory.get(cat);
	}

	public List<Runner> getRunnersFromCategory(String catName) {
		return runnersByCategory.get(findCategory(catName));
	}

	public List<Runner> getRunnersFromCourse(Course course) {
		return runnersByCourse.get(course);
	}
	
	public List<Runner> getRunnersFromCourse(String courseName) {
		return runnersByCourse.get(findCourse(courseName));
	}

	
	public Collection<HeatSet> getHeatSets() {
		return heatsets.values();
	}

	public Vector<String> getHeatSetnames() {
		return new Vector<String>(heatsets.keySet());
	}
	
	public HeatSet findHeatSet(String name) {
		return heatsets.get(name);
	}
	
	public void addHeatSet(HeatSet heatset) {
		heatsets.put(heatset.getName(), heatset);
	}

	public void removeHeatset(HeatSet heatSet) {
		heatsets.remove(heatSet);
	}


	/**
	 * @return
	 */
	public Integer[] collectStartnumbers() { // TODO: maybe use the Integer type for startnumber, it will save some code
		Integer[] startnums = new Integer[runnersByChip.size()];
		int i = 0;
		for (Runner runner : runnersByChip.values()) {
			startnums[i] = runner.getStartnumber();
			i++;
		}
		Arrays.sort(startnums);
		return startnums;
	}
	
	public Integer detectMaxStartnumber() {
		int max = 0;
		for (Runner runner : runnersByChip.values()) {
			max = Math.max(max, runner.getStartnumber());
		}
		return max;
	}

	public Integer detectMaxChipnumber() { // Bad smell...
		int max = 0;
		for (String chip : runnersByChip.keySet()) {
			max = Math.max(max, new Integer(chip));
		}
		return max;
	}
	
	public String[] collectChipnumbers() {
		String[] chipnumbers = runnersByChip.keySet().toArray(new String[0]);
		Arrays.sort(chipnumbers);
		return chipnumbers;
	}
	
	public void checkGecoData(Factory factory, PenaltyChecker checker) {
		checkNoDataRunners(factory);
		// compute trace for data
		for (RunnerRaceData raceData : getRunnersData()) {
			checker.buildTrace(raceData);	
		}		
	}
	
	public void checkOrData(Factory factory, PenaltyChecker checker) {
		checkNoDataRunners(factory);
		// compute status and trace for data
		for (RunnerRaceData raceData : getRunnersData()) {
			 // Special runner status (DNS) should have been set before this point
			if( raceData.getResult()==null ) {
				checker.check(raceData);	
			}
		}
	}
	
	public void checkNoDataRunners(Factory factory) {
		for (Runner runner : getRunners()) {
			if( findRunnerData(runner) == null ) {
				createRunnerDataFor(runner, factory);
			}
		}
	}

	public Map<String, Integer> coursesCounts() {
		HashMap<String,Integer> map = new HashMap<String, Integer>();
		for (Course c : getCourses()) {
			map.put(c.getName(), getRunnersFromCourse(c).size());
			int running = 0;
			for (Runner runner : getRunnersFromCourse(c)) {
				if( findRunnerData(runner).getResult().getStatus().equals(Status.Unknown) ) {
					running++;
				}
			}
			map.put(c.getName() + "running", running);
		}
		return map;
	}

	
	public Map<String, Integer> statusCounts() {
		HashMap<String,Integer> map = new HashMap<String, Integer>();
		for (Status s : Status.values()) {
			map.put(s.toString(), 0);
		}
		for (RunnerRaceData data : getRunnersData()) {
			inc(data.getResult().getStatus().toString(), map);
		}
		map.put("total", getRunners().size());
		map.put("actual", getRunners().size() - map.get("DNS"));
		map.put("finished", getRunners().size() - map.get("DNS") - map.get("Unknown"));
		return map;
	}

	/**
	 * @param status
	 * @param map
	 */
	private void inc(String status, HashMap<String, Integer> map) {
		map.put(status, map.get(status) + 1);
	}


}
