/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model.impl;

import valmo.geco.model.Course;


/**
 * @author Simon Denier
 * @since Jan 4, 2009
 *
 */
public class CourseImpl implements Course {
	
	private String name;
	
	private int length;
	
	private int climb;
	
	private int[] codes;
	
//	private StartList startlist;

	
	public int getClimb() {
		return climb;
	}

	public int[] getCodes() {
		return codes;
	}

	public int getLength() {
		return length;
	}

	public String getName() {
		return name;
	}

//	public StartList getStartlist() {
//		return startlist;
//	}

	public void setClimb(int climb) {
		this.climb = climb;
	}

	public void setCodes(int[] codes) {
		this.codes = codes;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public void setStartlist(StartList startlist) {
//		this.startlist = startlist;
//	}

}
