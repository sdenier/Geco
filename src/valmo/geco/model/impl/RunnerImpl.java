/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import valmo.geco.model.Course;
import valmo.geco.model.Runner;


/**
 * @author Simon Denier
 * @since Nov 22, 2008
 *
 */
public class RunnerImpl extends AbstractRunnerImpl implements Runner {
	
	private int startnumber;
	
	private Course course;
	
	private boolean NC;

	
	public int getStartnumber() {
		return startnumber;
	}

	public void setStartnumber(int startnumber) {
		this.startnumber = startnumber;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public boolean isNC() {
		return NC;
	}

	public void setNC(boolean nc) {
		NC = nc;
	}
	
	@Override
	public String toString() {
		return getNameR() + ", " + getChipnumber() + ", " + getCourse().getName(); //$NON-NLS-1$ //$NON-NLS-2$
//		return getStartnumber() + "," + getChipnumber() + "," +getFirstname() + "," + getLastname()
//		+ "," + getClub().getName() + ", " + getCategory().getShortname() + "," + getCourse().getName();
	}
	
	public String idString() {
		return getNameR() + ", " + getStartnumber() + ", " + getChipnumber();   //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
}
