/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Map;
import java.util.TreeMap;

import net.geco.model.Section;
import net.geco.model.SectionTraceData;

/**
 * @author Simon Denier
 * @since Aug 6, 2013
 *
 */
public class SectionTraceDataImpl extends TraceDataImpl implements SectionTraceData {

	private Map<Integer, Section> sectionsMap = new TreeMap<Integer, Section>();

	@Override
	public void putSectionAt(Section section, int index) {
		sectionsMap.put(index, section);
	}
	
	@Override
	public String sectionLabelAt(int index) {
		Section section = sectionsMap.get(index);
		return section != null ? section.getName() : "";
	}

}
