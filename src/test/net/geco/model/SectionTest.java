/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static test.net.geco.testfactory.CourseFactory.createCourse;
import static test.net.geco.testfactory.CourseFactory.createSection;

import java.util.List;

import net.geco.model.Course;
import net.geco.model.Section;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Jul 22, 2013
 *
 */
public class SectionTest {

	private Course course;
	
	private List<Section> sections;

	@Before
	public void setUp() {
		course = createCourse("A", new int[] {28, 31, 32, 33, 34, 31, 35, 36, 37, 38, 31, 39, 40, 41});
		course.putSection(createSection("CO", 1));
		course.putSection(createSection("Finish", 11));
		course.putSection(createSection("VTTO", 5));
		sections = course.getSections();
	}
	
	@Test
	public void testSectionOrder() {
		String [] sectionNames = new String[] { "CO", "VTTO", "Finish" };
		for (int i = 0; i < sections.size(); i++) {
			assertThat(sections.get(i).getName(), equalTo(sectionNames[i]));
		}
	}

	@Test
	public void testSectionCodes() {
		int[][] sectionCodes = new int[][] {
				new int[] {31, 32, 33, 34},
				new int[] {31, 35, 36, 37, 38, 31},
				new int[] {39, 40, 41}};
		course.refreshSectionCodes();
		
		for (int i = 0; i < sections.size(); i++) {
			assertThat(sections.get(i).getCodes(), equalTo(sectionCodes[i]));
		} 

	}

	@Test
	public void testSectionCodes_withOneSectionMatchingCourse() {
		Course courseB = createCourse("B", new int[] {120, 100, 110});
		Section section = createSection("S", 0);
		courseB.putSection(section);
		courseB.refreshSectionCodes();
		assertThat(section.getCodes(), equalTo(courseB.getCodes()));
	}
	
}
