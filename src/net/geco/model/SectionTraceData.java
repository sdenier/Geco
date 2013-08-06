/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

/**
 * @author Simon Denier
 * @since Aug 6, 2013
 *
 */
public interface SectionTraceData extends TraceData {

	public void putSectionAt(String sectionName, int index);
	
	public String sectionLabelAt(int rowIndex);

}
