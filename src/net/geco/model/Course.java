/**
 * Copyright (c) 2008 Simon Denier
 */
package net.geco.model;

import java.util.Collection;


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

	public int[] getCodes();

	public void setCodes(int[] codes);
	
	public int nbControls();

	public boolean hasLeg(int start, int end);

	public Collection<Section> getSections();
	
	public Section getSectionAt(int index);
	
	public void putSection(Section section);

	public void removeSection(Section targetSection);
	
}
