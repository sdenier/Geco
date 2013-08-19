/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.context;

import net.geco.model.Course;

/**
 * @author Simon Denier
 * @since Aug 19, 2013
 *
 */
public class CourseContext extends GenericContext {

	public CourseContext(Course course, int nbRunners) {
		put("geco_CourseName", course.getName()); //$NON-NLS-1$
		put("geco_NbRunners", nbRunners); //$NON-NLS-1$
		put("geco_CourseLength", course.getLength()); //$NON-NLS-1$
		put("geco_CourseClimb", course.getClimb()); //$NON-NLS-1$
	}

}
