/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.BufferedReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import net.geco.basics.Announcer;
import net.geco.basics.GecoResources;
import net.geco.basics.TimeManager;
import net.geco.basics.Util;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.HeatSet;
import net.geco.model.Messages;
import net.geco.model.Pool;
import net.geco.model.Registry;
import net.geco.model.Runner;

/**
 * @author Simon Denier
 * @since Aug 21, 2009
 *
 */
public class StageControl extends Control {
	
	public StageControl(GecoControl gecoControl) {
		super(StageControl.class, gecoControl);
	}
	
	private Announcer announcer() {
		return geco().announcer();
	}
	
	public Club createClub(String name, String shortName) {
		Club club = factory().createClub();
		club.setName(name);
		club.setShortname(shortName);
		registry().addClub(club);
		announcer().announceClubsChanged();
		return club;
	}
	
	public Club createClub() {
		return createClub(Messages.getString("StageControl.ClubLabel") //$NON-NLS-1$
							+ (registry().getClubs().size() + 1), ""); //$NON-NLS-1$
	}

	public void updateName(Club club, String newName) {
		if( !club.getName().equals(newName) && registry().findClub(newName)==null ) {
			registry().updateClubName(club, newName);
			announcer().announceClubsChanged();
		}
	}
	
	public void updateShortname(Club club, String newName) {
		if( !club.getShortname().equals(newName) ) {
			club.setShortname(newName);
			announcer().announceClubsChanged();
		}
	}
	
	public boolean removeClub(Club club) {
		if( canRemoveClub(club) ) {
			stage().registry().removeClub(club);
			announcer().announceClubsChanged();
			return true;
		}
		return false;
	}
	
	public boolean canRemoveClub(Club club) {
		boolean clubHasRunners = false;
		for (Runner runner : registry().getRunners()) {
			clubHasRunners |= (runner.getClub() == club);
		}
		return !clubHasRunners;
	}

	public void removeAllClubs() {
		geco().log(Messages.getString("StageControl.RemovingAllClubsLabel")); //$NON-NLS-1$
		ArrayList<Club> clubs = new ArrayList<Club>(registry().getClubs());
		for (Club club : clubs) {
			if( ! removeClub(club) ){
				geco().info(Messages.getString("StageControl.RunnersBelongToClubWarning") + club.getName(), true); //$NON-NLS-1$
			}
		}
	}

	public Course createCourse(String name) {
		Course course = factory().createCourse();
		course.setName(name);
		course.setCodes(new int[0]);
		registry().addCourse(course);
		announcer().announceCoursesChanged();
		return course;
	}

	public Course createCourse() {
		return createCourse(Messages.getString("CourseImpl.CourseLabel") //$NON-NLS-1$
							+ (registry().getCourses().size() + 1));
	}

	public Course addCourse(Course course) {
		Course previousCourse = registry().findCourse(course.getName());
		registry().addCourse(course);
		if( previousCourse!=null ) {
			changeCourse(previousCourse, course);
		}
		announcer().announceCoursesChanged();
		return previousCourse;
	}

	private void changeCourse(Course previousCourse, Course newCourse) {
		for (Category cat : registry().getCategories()) {
			if( cat.getCourse() == previousCourse ) {
				cat.setCourse(newCourse);
			}
		}
		for (Runner runner : registry().getRunners()) {
			if( runner.getCourse() == previousCourse ) {
				runner.setCourse(newCourse);
			}
		}
		for (HeatSet set : registry().getHeatSets()) {
			if( set.isCourseType() ) {
				Pool[] pools = set.getSelectedPools();
				for (int i = 0; i < pools.length; i++) {
					if( pools[i]==previousCourse ) {
						pools[i] = newCourse;
					}
				}
			}
		}
	}

	public boolean removeCourse(Course course) throws Exception {
		if( canRemoveCourse(course) ) {
			stage().registry().removeCourse(course);
			announcer().announceCoursesChanged();
			return true;
		}
		return false;
	}

	public boolean canRemoveCourse(Course course) throws Exception {
		if( course == registry().autoCourse() ){
			throw new Exception(Registry.autoCourseName() + Messages.getString("StageControl.UneditableCourseWarning")); //$NON-NLS-1$
		}
		for (Category cat : registry().getCategories()) {
			if( cat.getCourse() == course ) {
				throw new Exception(Messages.getString("StageControl.CategoryUseCourseWarning")); //$NON-NLS-1$
			}
		}
		for (Runner runner : registry().getRunners()) {
			if( runner.getCourse() == course ) {
				throw new Exception(Messages.getString("StageControl.RunnerUseCourseWarning")); //$NON-NLS-1$
			}
		}
		for (HeatSet set : registry().getHeatSets()) {
			if( set.isCourseType() ) {
				for (Pool pool : set.getSelectedPools()) {
					if( pool == course ) {
						throw new Exception(Messages.getString("StageControl.HeatsetUseCourseWarning")); //$NON-NLS-1$
					}
				}
			}
		}
		return true;
	}

	public void removeAllCourses() {
		geco().log(Messages.getString("StageControl.RemovingAllCoursesLabel")); //$NON-NLS-1$
		ArrayList<Course> courses = new ArrayList<Course>(registry().getCourses());
		for (Course course : courses) {
			try {
				if( course != registry().autoCourse() ){
					removeCourse(course);
				}
			} catch (Exception e) {
				geco().info(String.format("%s %s", e.getLocalizedMessage(), course.getName()), true); //$NON-NLS-1$
			}
		}
	}
	
	public void updateName(Course course, String newName) {
		if( course == registry().autoCourse() ){
			return;
		}
		if( !course.getName().equals(newName) && registry().findCourse(newName)==null ) {
			registry().updateCourseName(course, newName);
			announcer().announceCoursesChanged();
		}		
	}

	public boolean validateMassStartTime(Course course, String startTime) {
		try {
			Date oldTime = course.getMassStartTime();
			Date newTime = (startTime.equals("")) ? //$NON-NLS-1$
				TimeManager.NO_TIME
				:
				TimeManager.userParse(startTime);
			if( ! oldTime.equals(newTime) ) {
				course.setMassStartTime(newTime);
				geco().log("Change Mass start time for course "
						+ course.getName() + Messages.getString("RunnerControl.FromMessage") //$NON-NLS-1$
						+ TimeManager.fullTime(oldTime) + Messages.getString("RunnerControl.ToMessage") //$NON-NLS-1$
						+ TimeManager.fullTime(newTime));
			}
			return true;
		} catch (ParseException e1) {
			geco().info(Messages.getString("RunnerControl.RegisteredStartimeWarning"), true); //$NON-NLS-1$
			return false;
		}
	}

	public void updateMassStarttimes(long zeroTime, long oldTime) {
		for (Course course : registry().getCourses()) {
			Date relativeTime = TimeManager.relativeTime(course.getMassStartTime(), oldTime);
			course.setMassStartTime(TimeManager.absoluteTime(relativeTime, zeroTime));
		}
	}

	public Category createCategory(String shortName, String longName) {
		Category cat = factory().createCategory();
		cat.setShortname(shortName);
		cat.setLongname(longName);
		registry().addCategory(cat);
		announcer().announceCategoriesChanged();
		return cat;		
	}

	public Category createCategory() {
		return createCategory(Messages.getString("StageControl.CategoryLabel") //$NON-NLS-1$
								+ (registry().getCategories().size() + 1), ""); //$NON-NLS-1$
	}

	public boolean removeCategory(Category cat) throws Exception {
		if( canRemoveCategory(cat) ) {
			stage().registry().removeCategory(cat);
			announcer().announceCategoriesChanged();
			return true;
		}
		return false;		
	}

	private boolean canRemoveCategory(Category cat) throws Exception {
		for (Runner runner : registry().getRunners()) {
			if( runner.getCategory() == cat ) {
				throw new Exception(Messages.getString("StageControl.RunnerUseCategoryWarning")); //$NON-NLS-1$
			}
		}
		for (HeatSet set : registry().getHeatSets()) {
			if( set.isCategoryType() || set.isMixedType() ) {
				for (Pool pool : set.getSelectedPools()) {
					if( pool == cat ) {
						throw new Exception(Messages.getString("StageControl.HeatsetUseCategoryWarning")); //$NON-NLS-1$
					}
				}
			}
		}
		return true;
	}

	public void removeAllCategories() {
		geco().log(Messages.getString("StageControl.RemovingAllCategoriesLabel")); //$NON-NLS-1$
		ArrayList<Category> categories = new ArrayList<Category>(registry().getCategories());
		for (Category category : categories) {
			try {
				removeCategory(category);
			} catch (Exception e) {
				geco().info(String.format("%s %s", e.getLocalizedMessage(), category.getName()), true); //$NON-NLS-1$
			}
		}
	}
	
	public void updateShortname(Category cat, String newName) {
		if( !cat.getShortname().equals(newName) && registry().findCategory(newName)==null ) {
			registry().updateCategoryName(cat, newName);
			announcer().announceCategoriesChanged();
		}		
	}

	public void updateName(Category cat, String newName) {
		if( !cat.getLongname().equals(newName) ) {
			cat.setLongname(newName);
			announcer().announceCategoriesChanged();
		}
	}

	public Club ensureClubInRegistry(String clubname, String shortname) {
		Club rClub = registry().findClub(clubname);
		if( rClub==null ) {
			rClub = createClub(clubname, shortname);
		}
		return rClub;
	}

	public Course ensureCourseInRegistry(String coursename) {
		Course course = registry().findCourse(coursename);
		if( course==null ){
			course = createCourse(coursename);
		}
		return course;
	}

	public Category ensureCategoryInRegistry(String categoryname, String longname) {
		Category rCat = registry().findCategory(categoryname);
		if( rCat==null ){
			rCat = createCategory(categoryname, longname);
		}
		return rCat;
	}
	
	public void importCategoryTemplate(String filepath) throws Exception {
		BufferedReader reader = GecoResources.getSafeReaderFor(filepath);
		String line = reader.readLine();
		while( line != null ){
			String[] tokens = Util.splitAndTrim(line, ","); //$NON-NLS-1$
			if( tokens.length < 3 ){
				throw new Exception(Messages.getString("StageControl.InvalidLineWarning") + line); //$NON-NLS-1$
			}
			Category category = ensureCategoryInRegistry(tokens[0], tokens[1]);
			Course course = ensureCourseInRegistry(tokens[2]);
			category.setLongname(tokens[1]); // enforce long name
			category.setCourse(course);
			line = reader.readLine();
		}
	}
	
}
