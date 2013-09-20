/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.model.Course;
import net.geco.model.Section;


/**
 * @author Simon Denier
 * @since Jul 17, 2013
 *
 */
public class SectionService extends Control {

	public SectionService(GecoControl gecoControl) {
		super(SectionService.class, gecoControl);
	}

	public Section findOrCreateSection(Course course, int index) {
		Section section = course.getSectionAt(index);
		if( section == null ) {
			section = factory().createSection();
			section.setStartIndex(index);
		}
		return section;
	}

	public Section findSection(Course course, int index) {
		Section section = course.getSectionAt(index);
		if( section == null ) {
			return Section.NULL_SECTION;
		} else {
			return section;
		}
	}

	public void put(Course course, Section section) {
		course.putSection(section);
		course.refreshSectionCodes();
	}

	public void remove(Course course, Section section) {
		course.removeSection(section);
		course.refreshSectionCodes();
	}
	
}
