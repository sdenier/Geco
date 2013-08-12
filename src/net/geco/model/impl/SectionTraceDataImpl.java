/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import net.geco.model.Section;
import net.geco.model.SectionTraceData;

/**
 * @author Simon Denier
 * @since Aug 6, 2013
 *
 */
public class SectionTraceDataImpl extends TraceDataImpl implements SectionTraceData {

	private NavigableMap<Integer, Section> sectionsMap = new TreeMap<Integer, Section>();

	public Set<Entry<Integer,Section>> getSectionData() {
		return sectionsMap.entrySet();
	}
	
	@Override
	public void putSectionAt(Section section, int index) {
		sectionsMap.put(index, section);
	}
	
	@Override
	public String sectionLabelAt(int index) {
		Section section = sectionsMap.get(index);
		return section != null ? section.getName() : "";
	}

	@Override
	public boolean hasSectionData() {
		return true;
	}

	@Override
	public long[] sectionsFinishTimes(long raceTime) {
		long[] finishTimes = new long[sectionsMap.size()];
		Iterator<Integer> sectionIndices = sectionsMap.keySet().iterator();
		Integer nextSectionIndex = sectionIndices.next();
		for (int i = 0; i < finishTimes.length; i++) {
			if( sectionIndices.hasNext() ) {
				nextSectionIndex = sectionIndices.next();
				finishTimes[i] = trace[nextSectionIndex].getTime().getTime();
			} else { // last section
				finishTimes[i] = raceTime;
			}
		}
		return finishTimes;
	}

}
