/**
 * Copyright (c) 2008 Simon Denier
 */
package net.geco.model;


/**
 * @author Simon Denier
 * @since Nov 22, 2008
 *
 */
public interface Course extends Pool {

	public String getName();

	public void setName(String name);

	public int getLength();

	public void setLength(int length);

	public int getClimb();

	public void setClimb(int climb);

	public int[] getCodes();

	public void setCodes(int[] codes);
	
	public int nbControls();

}
