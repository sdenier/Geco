/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;

/**
 * @author Simon Denier
 * @since Jun 30, 2009
 *
 */
public interface Runner extends IRunner {

	public int getStartnumber();

	public void setStartnumber(int startnumber);

	public Course getCourse();

	public void setCourse(Course course);

	public boolean isNC();

	public void setNC(boolean nc);

}