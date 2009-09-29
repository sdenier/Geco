/**
 * Copyright (c) 2009 Simon Denier
 */
package test.valmo.geco.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import valmo.geco.model.Course;
import valmo.geco.model.Registry;

/**
 * @author Simon Denier
 * @since Jan 20, 2009
 *
 */
public class OrRaceConfigTest {
	
	private Registry registryB;
	
	private Registry registryM;
	
	@Before
	public void setUp() {
		OrFixture fixture = new OrFixture();
		registryB = fixture.importBelfieldData(false);
		registryM = fixture.importMullaghmeenData(false);
	}
	
	@Test
	public void testImportClubs() {
		assertEquals(13, registryB.getClubs().size()); // TODO: remove club with empty name
		assertEquals("[None]", registryB.findClub("[None]").getName());
		assertEquals("3ROC", registryB.findClub("3ROC").getName());
		assertEquals("CORKO", registryB.findClub("CORKO").getName());
	}
	
	@Test
	public void testImportCategories() {
		assertEquals(32, registryB.getCategories().size());
		assertEquals("[None]", registryB.findCategory("[None]").getShortname());
		assertEquals("M10", registryB.findCategory("M10").getShortname());
		assertEquals("M75", registryB.findCategory("M75").getShortname());
		assertEquals("W70", registryB.findCategory("W70").getShortname());
		
		assertEquals(31, registryM.getCategories().size());
		assertEquals("M21", registryM.findCategory("M21").getShortname());
		assertEquals("W21", registryM.findCategory("W21").getShortname());
	}
	
	@Test
	public void testImportBelfieldCourses() {
		assertEquals(2, registryB.getCourses().size());
		
		Course longCourse = registryB.findCourse("Long Course");
		testCourse(longCourse, 
				"Long Course", 
				3930, 
				0, 
				new int[] { 162,164,165,166,167,168,169,170,171,172,173,174,175
				,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190 });

		Course shortCourse = registryB.findCourse("Short Course");
		testCourse(shortCourse,
				"Short Course", 
				1620, 
				0, 
				new int[] { 187,186,177,174,175,176,182,163,184,185,161,162,190 }
		);
	}

	private void testCourse(Course course, String name, int length, int climb, int[] codes) {
		assertEquals(name, course.getName());
		assertEquals(length, course.getLength());
		assertEquals(climb, course.getClimb());
		assertEquals(codes.length, course.getCodes().length);
		assertArrayEquals(codes, course.getCodes());
	}
	
	@Test
	public void testImportMullaghmeenCourses() {
		Collection<Course> courses = registryM.getCourses();
		assertEquals(8, courses.size());
		
		Collection<String> names = new HashSet<String>();
		for (Course course : courses) {
			names.add(course.getName());
		}
		Collection<String> expectedNames = new HashSet<String>(Arrays.asList("Blue", "Brown", "Green", "Light Green", "Orange", "Red", "White", "Yellow"));
		assertEquals(expectedNames, names);
		
	}
	
	@Test
	public void testMullaghmeenBrownCourse() {
		Course course = registryM.findCourse("Brown");
		testCourse(course, 
				"Brown", 
				9350, 
				300, 
				new int[] { 164,171,166,167,169,162,173,174,178,168,186,188,181
							,184,182,183,185,177,175,180,172,179,158 });
		
	}
	
	@Test
	public void testMullaghmeenLightgreenCourse() {
		Course course = registryM.findCourse("Light Green");
		testCourse(course, 
				"Light Green", 
				3210, 
				90, 
				new int[] { 179,190,156,180,175,154,172,189,191,171,166,192,169,193 });
	}
	
	@Test
	public void testMullaghmeenRedCourse() {
		Course course = registryM.findCourse("Red");
		testCourse(course,
				"Red",
				4300,
				120,
				new int[] { 151,174,163,165,194,159,154,155,156,190,158 });
	}
	
}
