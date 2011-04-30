/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.model;

import java.util.Date;

/**
 * @author Simon Denier
 * @since Jun 30, 2009
 *
 */
public interface Runner extends AbstractRunner {

	public int getStartnumber();

	public void setStartnumber(int startnumber);
	
	public Date getRegisteredStarttime();
	
	public void setRegisteredStarttime(Date time);

	public Course getCourse();

	public void setCourse(Course course);

	public boolean isNC();

	public void setNC(boolean nc);
	
	public boolean rentedEcard();
	
	public void setRentedEcard(boolean rented);

}