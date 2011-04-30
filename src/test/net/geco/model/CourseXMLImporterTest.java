/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Vector;

import net.geco.model.Course;
import net.geco.model.impl.POFactory;
import net.geco.model.xml.CourseSaxImporter;

import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Simon Denier
 * @since Mar 5, 2010
 *
 */
public class CourseXMLImporterTest {

	private static Vector<Course> courses1;
	private static Vector<Course> courses2;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		courses1 = CourseSaxImporter.importFromXml("testData/IOFdata-2.0.3/CourseData_example1.xml", new POFactory());
		courses2 = CourseSaxImporter.importFromXml("testData/IOFdata-2.0.3/CourseData_example2.xml", new POFactory());
	}

	@Test
	public void testCourse1() {
		assertEquals(2, courses1.size());
		
		Course bane1 = courses1.firstElement();
		assertEquals("Bane 01", bane1.getName());
		assertEquals(7600, bane1.getLength());
		assertEquals(0, bane1.getClimb());
		int[] codes = bane1.getCodes();
		assertEquals(17, bane1.nbControls());
		assertArrayEquals(
				new int[] {40, 46, 51, 56, 57, 62, 64, 75, 81, 999, 125, 134, 138, 145, 149, 154, 200}, 
				codes);
	}

	@Test
	public void testCourse2() {
		assertEquals(2, courses1.size());
		
		Course bane2 = courses1.lastElement();
		assertEquals("Bane 02", bane2.getName());
		assertEquals(10950, bane2.getLength());
		assertEquals(0, bane2.getClimb());
		int[] codes = bane2.getCodes();
		assertEquals(25, bane2.nbControls());
		assertArrayEquals(
				new int[] {36, 52, 44, 47, 50, 54, 55, 57, 60, 78, 74, 72, 81,
						999, 102, 91, 93, 99, 110, 135, 138, 145, 148, 154, 200}, 
				codes);
	}

	@Test
	public void testCourseAAA() {
		assertEquals(64, courses2.size());
		
		// For unknown reasons, course order is not text order
		Course aaa = null;
		Course ddd = null;
		for (Course c : courses2) {
			if( c.getName().equals("Herre 3. tur AAA")) {
				aaa = c;
			}
			if( c.getName().equals("Herre 3. tur DDD")) {
				ddd = c;
			}
		}
		
		assertNotNull(aaa);		
		assertEquals("Herre 3. tur AAA", aaa.getName());
		assertEquals(6625, aaa.getLength());
		assertEquals(0, aaa.getClimb());
		int[] codes = aaa.getCodes();
		assertEquals(15, aaa.nbControls());
		assertArrayEquals(
				new int[] {205, 209, 158, 153, 152, 108, 159, 154, 155, 55,
							151, 202, 201, 160, 200}, 
				codes);
		
		assertNotNull(ddd);
		assertEquals("Herre 3. tur DDD", ddd.getName());
		assertEquals(6825, ddd.getLength());
		assertEquals(0, ddd.getClimb());
		codes = ddd.getCodes();
		assertEquals(15, ddd.nbControls());
		assertArrayEquals(
				new int[] {208, 209, 158, 39, 54, 162, 159, 154, 155, 53, 151,
							150, 201, 160, 200}, 
				codes);

	}

	
}
