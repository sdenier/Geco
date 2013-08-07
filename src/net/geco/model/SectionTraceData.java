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

	public void putSectionAt(Section section, int index);
	
	public String sectionLabelAt(int rowIndex);

}
