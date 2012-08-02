/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;



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
	
	private RunnerRegistry runnerRegistry;
	
	private GroupRegistry<Course> courseRegistry;
	
	private GroupRegistry<Club> clubRegistry;

	private GroupRegistry<Category> categoryRegistry;
	
	private GroupRegistry<HeatSet> heatsetRegistry;

	private Course autoCourse;

	
	public Registry() {
		runnerRegistry = new RunnerRegistry();
		courseRegistry = new GroupRegistry<Course>();
		clubRegistry = new GroupRegistry<Club>();
		categoryRegistry = new GroupRegistry<Category>();
		heatsetRegistry = new GroupRegistry<HeatSet>();
	}
	
	
	/*
	 * Courses
	 */
	
	public Collection<Course> getCourses() {
		return courseRegistry.getGroups();
	}
	
	public List<Course> getSortedCourses() {
		return courseRegistry.getSortedGroups();
	}

	public List<String> getCourseNames() {
		return courseRegistry.getNames();
	}

	public List<String> getSortedCourseNames() {
		return courseRegistry.getSortedNames();
	}
	
	public Course findCourse(String name) {
		return courseRegistry.find(name);
	}
	
	public void addCourse(Course course) {
		courseRegistry.add(course);
		runnerRegistry.courseCreated(course);
	}

	public void removeCourse(Course course) {
		courseRegistry.remove(course);
		runnerRegistry.courseDeleted(course);
	}
	
	public void updateCourseName(Course course, String newName) {
		courseRegistry.updateName(course, newName);
	}
	
	public Course autoCourse() {
		return this.autoCourse;
	}
	
	public Course ensureAutoCourse(Factory factory) {
		this.autoCourse = findCourse(autoCourseName());
		if( autoCourse==null ){
			autoCourse = createCourse(autoCourseName(), factory);
		}
		return this.autoCourse;
	}
	
	private Course createCourse(String name, Factory factory) {
		Course course = factory.createCourse();
		course.setName(name);
		course.setCodes(new int[0]);
		addCourse(course);
		return course;
	}

	public static String autoCourseName() {
		return Messages.getString("Registry.AutoCourseName"); //$NON-NLS-1$
	}

	
	/*
	 * Clubs
	 */

	public Collection<Club> getClubs() {
		return clubRegistry.getGroups();
	}

	public List<Club> getSortedClubs() {
		return clubRegistry.getSortedGroups();
	}
	
	public List<String> getClubNames() {
		return clubRegistry.getNames();
	}
	
	public List<String> getSortedClubNames() {
		return clubRegistry.getSortedNames();
	}
	
	public Club findClub(String name) {
		return clubRegistry.find(name);
	}
	
	public void addClub(Club club) {
		clubRegistry.add(club);
	}
	
	public void removeClub(Club club) {
		clubRegistry.remove(club);
	}
	
	public void updateClubName(Club club, String newName) {
		clubRegistry.updateName(club, newName);
	}
	
	public Club noClub() {
		return findClub(Messages.getString("Registry.NoClubLabel")); //$NON-NLS-1$
	}

	public Club anyClub() {
		try {
			return clubRegistry.any();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	
	/*
	 * Categories
	 */
	
	public Collection<Category> getCategories() {
		return categoryRegistry.getGroups();
	}
	
	public List<Category> getSortedCategories() {
		return categoryRegistry.getSortedGroups();
	}

	public List<String> getCategoryNames() {
		return categoryRegistry.getNames();
	}

	public List<String> getSortedCategoryNames() {
		return categoryRegistry.getSortedNames();
	}

	public Category findCategory(String name) {
		return categoryRegistry.find(name);
	}
	
	public void addCategory(Category cat) {
		categoryRegistry.add(cat);
		runnerRegistry.categoryCreated(cat);
	}
	
	public void removeCategory(Category cat) {
		categoryRegistry.remove(cat);
		runnerRegistry.categoryDeleted(cat);
	}

	public void updateCategoryName(Category cat, String newName) {
		categoryRegistry.updateName(cat, newName);
	}
	
	public Category noCategory() {
		return findCategory(Messages.getString("Registry.NoCategoryLabel")); //$NON-NLS-1$
	}
	
	public Category anyCategory() {
		try {
			return categoryRegistry.any();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	public Course getDefaultCourseOrAutoFor(Category cat) {
		return ( cat.getCourse()!=null ) ?
				cat.getCourse() :
				autoCourse();
	}

	
	/*
	 * Runners
	 */

	public Set<Integer> getStartIds() {
		return runnerRegistry.getStartIds();
	}
	
	public Integer nextStartId() {
		return runnerRegistry.maxStartId() + 1;
	}
	
	public Set<String> getEcards() {
		return runnerRegistry.getEcards();
	}

	public int detectMaxEcardNumber() {
		int max = 0;
		for (String ecard : runnerRegistry.getEcards()) {
			try {
				Integer ecardi = Integer.valueOf(ecard);
				max = Math.max(max, ecardi);
			} catch (NumberFormatException e) {
				// bypass ecard number xxxxa (cloned entries)
			}
		}
		return max;
	}

	public Collection<Runner> getRunners() {
		return runnerRegistry.getRunners();
	}

	public Runner findRunnerById(Integer startId) {
		return runnerRegistry.findRunnerById(startId);
	}
	
	public Runner findRunnerByEcard(String ecard) {
		return runnerRegistry.findRunnerByEcard(ecard);
	}
	
	public void addRunner(Runner runner) {
		runnerRegistry.addRunner(runner);
	}

	public void addRunnerWithoutId(Runner runner) {
		runnerRegistry.addRunnerWithoutId(runner);
	}
	
	public void addRunnerSafely(Runner runner) {
		runnerRegistry.addRunnerSafely(runner);
	}
	
	public void updateRunnerStartId(Integer oldId, Runner runner) {
		runnerRegistry.updateRunnerStartId(oldId, runner);
	}
	
	public void updateRunnerEcard(String oldEcard, Runner runner) {
		runnerRegistry.updateRunnerEcard(oldEcard, runner);
	}

	public void updateRunnerCourse(Course oldCourse, Runner runner) {
		runnerRegistry.updateRunnerCourse(oldCourse, runner);
	}

	public void updateRunnerCategory(Category oldCat, Runner runner) {
		runnerRegistry.updateRunnerCategory(oldCat, runner);
	}
	
	public void removeRunner(Runner runner) {
		runnerRegistry.removeRunner(runner);
	}

	/*
	 * Runner Race Data
	 */
	
	public Collection<RunnerRaceData> getRunnersData() {
		return runnerRegistry.getRunnersData();
	}
	
	public RunnerRaceData findRunnerData(Runner runner) {
		return runnerRegistry.findRunnerData(runner);
	}

	public RunnerRaceData findRunnerData(Integer startId) {
		return runnerRegistry.findRunnerData(startId);
	}

	public RunnerRaceData findRunnerData(String ecard) {
		return runnerRegistry.findRunnerData(ecard);
	}
	
	public void addRunnerData(RunnerRaceData data) {
		runnerRegistry.addRunnerData(data);
	}
	
	public void removeRunnerData(RunnerRaceData data) {
		runnerRegistry.removeRunnerData(data);
	}
	
	public List<Runner> getRunnersFromCategory(Category cat) {
		return runnerRegistry.getRunnersFromCategory(cat);
	}

	public List<Runner> getRunnersFromCategory(String catName) {
		return getRunnersFromCategory(findCategory(catName));
	}
	
	public Map<Course, List<Runner>> getRunnersByCourseFromCategory(String catName) {
		return runnerRegistry.getRunnersByCourseFromCategory(findCategory(catName));
	}

	public List<Runner> getRunnersFromCourse(Course course) {
		return runnerRegistry.getRunnersFromCourse(course);
	}
	
	public List<Runner> getRunnersFromCourse(String courseName) {
		return getRunnersFromCourse(findCourse(courseName));
	}

	public List<RunnerRaceData> getRunnerDataFromCourse(Course course) {
		return runnerRegistry.getRunnerDataFromCourse(course);
	}
	
	/*
	 * HeatSet
	 * TODO: add updateHeatSetName
	 */
	
	public Collection<HeatSet> getHeatSets() {
		return heatsetRegistry.getGroups();
	}

	public void addHeatSet(HeatSet heatset) {
		heatsetRegistry.add(heatset);
	}

	public void removeHeatset(HeatSet heatSet) {
		heatsetRegistry.remove(heatSet);
	}

}
