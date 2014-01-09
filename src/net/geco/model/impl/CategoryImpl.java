/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import net.geco.model.Category;
import net.geco.model.Course;
import net.geco.model.CourseSet;


/**
 * @author Simon Denier
 * @since Nov 22, 2008
 *
 */
public class CategoryImpl implements Category {
	
	private Course course;
	
	private String longname;
	
	private String shortname;

	private CourseSet courseset;

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public String getLongname() {
		return longname;
	}

	public String getShortname() {
		return shortname;
	}

	public void setLongname(String longname) {
		this.longname = longname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getName() {
		return getShortname();
	}

	public void setName(String name) {
		setShortname(name);
	}

	public CourseSet getCourseSet() {
		return courseset;
	}

	public void setCourseSet(CourseSet courseset) {
		this.courseset = courseset;
	}
	
}
