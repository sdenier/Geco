/**
 * Copyright (c) 2008 Simon Denier
 */
package net.geco.model;

import java.util.Date;
import java.util.List;


/**
 * @author Simon Denier
 * @since Nov 22, 2008
 *
 */
public interface Course extends Pool, Group {

	public String getName();

	public void setName(String name);

	public int getLength();

	public void setLength(int length);

	public int getClimb();

	public void setClimb(int climb);

	public boolean hasDistance();

	public String formatDistanceClimb();

	public Date getMassStartTime();
	
	public void setMassStartTime(Date time);
	
	public int[] getCodes();

	public void setCodes(int[] codes);
	
	public int nbControls();

	public boolean hasLeg(int start, int end);

	public List<Section> getSections();
	
	public Section getSectionAt(int index);
	
	public void putSection(Section section);

	public void removeSection(Section targetSection);

	public void refreshSectionCodes();

	public CourseSet getCourseSet();

	public void setCourseSet(CourseSet courseset);
	
}
