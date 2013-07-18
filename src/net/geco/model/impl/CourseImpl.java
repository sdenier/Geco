/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Section;



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
	
	private Map<Integer, Section> sections = new TreeMap<Integer, Section>();

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
	
	public int nbControls() {
		return codes.length;
	}

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

	public String toString() {
		return Messages.getString("CourseImpl.CourseLabel")	 //$NON-NLS-1$
				+ name + ", "			 //$NON-NLS-1$
				+ codes.length + "p ("	 //$NON-NLS-1$
				+ length +"m, "			 //$NON-NLS-1$
				+ climb + "m): "		 //$NON-NLS-1$
				+ Arrays.toString(codes);
	}

	public boolean hasLeg(int start, int end) {
		for (int i = 0; i < codes.length - 1; i++) {
			if( codes[i]==start && codes[i+1]==end ){
				return true;
			}
		}
		return false;
	}

	public String formatDistanceClimb() {
		return length + "m, " + climb + "m"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean hasDistance() {
		return length > 0;
	}

	public Collection<Section> getSections() {
		return sections.values();
	}

	public Section getSectionAt(int index) {
		return sections.get(index);
	}

	public void putSection(Section section) {
		sections.put(section.getStartIndex(), section);
	}

	public void removeSection(Section section) {
		sections.remove(section.getStartIndex());
	}
	
}
