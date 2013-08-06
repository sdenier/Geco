/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Map;
import java.util.TreeMap;

import net.geco.model.SectionTraceData;

/**
 * @author Simon Denier
 * @since Aug 6, 2013
 *
 */
public class SectionTraceDataImpl extends TraceDataImpl implements SectionTraceData {

	private Map<Integer, String> sectionsMap = new TreeMap<Integer, String>();

	@Override
	public void putSectionAt(String sectionName, int index) {
		sectionsMap.put(index, sectionName);
	}
	
	@Override
	public String sectionLabelAt(int index) {
		String section = sectionsMap.get(index);
		return section != null ? section : "";
	}

}
