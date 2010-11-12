/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import valmo.geco.core.Announcer;
import valmo.geco.core.Messages;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
import valmo.geco.model.HeatSet;
import valmo.geco.model.Pool;
import valmo.geco.model.Runner;

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
		return createClub("Club" + (registry().getClubs().size() + 1), ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void updateName(Club club, String newName) {
		if( !club.getName().equals(newName) && registry().findClub(newName)==null ) {
			registry().updateClubname(club, newName);
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

	public Course createCourse() {
		Course course = factory().createCourse();
		course.setName("Course" + (registry().getCourses().size() + 1)); //$NON-NLS-1$
		course.setCodes(new int[0]);
		registry().addCourse(course);
		announcer().announceCoursesChanged();
		return course;
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
	
	public void updateName(Course course, String newName) {
		if( !course.getName().equals(newName) && registry().findCourse(newName)==null ) {
			registry().updateCoursename(course, newName);
			announcer().announceCoursesChanged();
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
		return createCategory("Category" + (registry().getCategories().size() + 1), ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param cat
	 * @throws Exception 
	 */
	public boolean removeCategory(Category cat) throws Exception {
		if( canRemoveCategory(cat) ) {
			stage().registry().removeCategory(cat);
			announcer().announceCategoriesChanged();
			return true;
		}
		return false;		
	}

	/**
	 * @param cat
	 * @return
	 * @throws Exception 
	 */
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

	/**
	 * @param cat
	 * @param value
	 */
	public void updateShortname(Category cat, String newName) {
		if( !cat.getShortname().equals(newName) && registry().findCategory(newName)==null ) {
			registry().updateCategoryname(cat, newName);
			announcer().announceCategoriesChanged();
		}		
	}

	/**
	 * @param cat
	 * @param value
	 */
	public void updateName(Category cat, String newName) {
		if( !cat.getLongname().equals(newName) ) {
			cat.setLongname(newName);
			announcer().announceCategoriesChanged();
		}
	}
	
}
