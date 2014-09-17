/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.testfactory;

import java.util.Date;

import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Section;
import net.geco.model.Section.SectionType;
import net.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since Jul 22, 2013
 *
 */
public class CourseFactory {

	private static Factory factory = new POFactory();

	public static Course createCourse(String name, int... codes) {
		Course course = factory.createCourse();
		course.setName(name);
		course.setCodes(codes);
		return course;
	}

	public static Course createCourse(String name) {
		return createCourse(name, 31, 32, 33);
	}
	
	public static Course createCourseWithMassStartTime(String name, Date time) {
		Course course = createCourse(name);
		course.setMassStartTime(time);
		return course;
	}
	
	public static Section createSection(String name, int index, SectionType type) {
		Section section = factory.createSection();
		section.setName(name);
		section.setStartIndex(index);
		section.setType(type);
		return section;
	}

	public static Section createSection(String name, int index) {
		return createSection(name, index, SectionType.INLINE);
	}
	
}
