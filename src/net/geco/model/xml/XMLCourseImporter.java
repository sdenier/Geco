/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.xml;

import java.util.List;

import net.geco.model.Course;

/**
 * @author Simon Denier
 * @since Jun 26, 2012
 *
 */
public interface XMLCourseImporter {

	public List<Course> importFromXml(String xmlFile) throws Exception;

}