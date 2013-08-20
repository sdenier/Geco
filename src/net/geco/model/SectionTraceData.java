/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Simon Denier
 * @since Aug 6, 2013
 *
 */
public interface SectionTraceData extends TraceData {
	
	public Set<Entry<Integer, Section>> getSectionData();
	
	public void putSectionAt(Section section, int index);
	
	public String sectionLabelAt(int rowIndex);

	public long[] sectionsFinishTimes(long raceTime);

	public long computeSectionTime(Integer sectionIndex, Date startTime, Date finishTime);

}
