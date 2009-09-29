/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;

/**
 * @author Simon Denier
 * @since Jun 30, 2009
 *
 */
public interface Runner {

	public String getName();

	public String getNameR();

	public String getFirstname();

	public void setFirstname(String firstname);

	public String getLastname();

	public void setLastname(String lastname);

	public Club getClub();

	public void setClub(Club club);

	public int getStartnumber();

	public void setStartnumber(int startnumber);

	public String getChipnumber();

	public void setChipnumber(String chipnumber);

	public Category getCategory();

	public void setCategory(Category category);

	public Course getCourse();

	public void setCourse(Course course);

	public boolean isNC();

	public void setNC(boolean nc);

	public String toString();

	public String idString();

}