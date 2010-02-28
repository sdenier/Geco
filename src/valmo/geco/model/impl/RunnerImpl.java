/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
import valmo.geco.model.Runner;


/**
 * @author Simon Denier
 * @since Nov 22, 2008
 *
 */
public class RunnerImpl implements Runner {
	
	private String firstname;
	
	private String lastname;
	
	private Club club;
	
	private int startnumber;
	
	private String chipnumber;
	
	private Category category;
	
	private Course course;
	
	private boolean NC;
	
//	private TimeSlot starttime;

	public String getName() {
		return firstname + " " + lastname;
	}
	
	public String getNameR() {
		return lastname + " " + firstname;
	}
	
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Club getClub() {
		return club;
	}

	public void setClub(Club club) {
		this.club = club;
	}

	public int getStartnumber() {
		return startnumber;
	}

	public void setStartnumber(int startnumber) {
		this.startnumber = startnumber;
	}

	public String getChipnumber() {
		return chipnumber;
	}

	public void setChipnumber(String chipnumber) {
		this.chipnumber = chipnumber;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

//	public TimeSlot getStarttime() {
//		return starttime;
//	}
//
//	public void setStarttime(TimeSlot starttime) {
//		this.starttime = starttime;
//	}

	public boolean isNC() {
		return NC;
	}

	public void setNC(boolean nc) {
		NC = nc;
	}
	
	@Override
	public String toString() {
		return getNameR() + ", " + getChipnumber() + ", " + getCourse().getName();
//		return getStartnumber() + "," + getChipnumber() + "," +getFirstname() + "," + getLastname()
//		+ "," + getClub().getName() + ", " + getCategory().getShortname() + "," + getCourse().getName();
	}
	
	public String idString() {
		return getNameR() + ", " + getStartnumber() + ", " + getChipnumber();  
	}
	
	
}
