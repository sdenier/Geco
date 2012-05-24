/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.testfactory;

import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since May 24, 2012
 *
 */
public class CourseFactory {

	private static Factory factory = new POFactory();
	
	public static Course create(String name) {
		Course course = factory.createCourse();
		course.setName(name);
		course.setCodes(new int[0]);
		return course;
	}

}
