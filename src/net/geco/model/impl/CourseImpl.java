/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.geco.basics.TimeManager;
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
	
	private Date massStartTime = TimeManager.NO_TIME;
	
	private int[] codes;
	
	private Map<Integer, Section> sectionsMap = new TreeMap<Integer, Section>();

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

	public Date getMassStartTime() {
		return massStartTime;
	}

	public void setMassStartTime(Date massStartTime) {
		this.massStartTime = massStartTime;
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

	public List<Section> getSections() {
		return new ArrayList<Section>(sectionsMap.values());
	}

	public Section getSectionAt(int index) {
		return sectionsMap.get(index);
	}

	public void putSection(Section section) {
		sectionsMap.put(section.getStartIndex(), section);
	}

	public void removeSection(Section section) {
		sectionsMap.remove(section.getStartIndex());
	}

	public void refreshSectionCodes() {
		List<Section> sections = getSections();
		if( ! sections.isEmpty() ) {
			for (int i = 0; i < sections.size() - 1; i++) {
				sections.get(i).setCodes(codes, sections.get(i + 1).getStartIndex());
			}
			sections.get(sections.size() - 1).setCodes(codes, codes.length);
		}
	}

}
