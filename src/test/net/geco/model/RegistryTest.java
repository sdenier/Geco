/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.geco.model.Category;
import net.geco.model.Course;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Jun 6, 2011
 *
 */
public class RegistryTest {
	
	private POFactory factory;

	private Registry registry;

	private Runner runner1;
	
	private Category cat;

	private Course cou;


	@Before
	public void setUp(){
		factory = new POFactory();
		registry = new Registry();
		cat = categoryFactory("cat");
		registry.addCategory(cat);
		cou = courseFactory("course");
		registry.addCourse(cou);
		runner1 = factory(1);
	}

	public Runner factory(){
		Runner runner = factory.createRunner();
		runner.setCategory(cat);
		runner.setCourse(cou);
		return runner;
	}
	
	public Runner factory(int i){
		Runner runner = factory();
		runner.setStartId(Integer.valueOf(i));
		return runner;
	}

	public Category categoryFactory(String name){
		Category category = factory.createCategory();
		category.setShortname(name);
		return category;
	}
	
	public Course courseFactory(String name){
		Course course = factory.createCourse();
		course.setName(name);
		return course;
	}

	@Test
	public void testAddCourseInRegistry(){
		Course red = courseFactory("Red");
		registry.addCourse(red);
		assertTrue(registry.getRunnersFromCourse(red).isEmpty());
		
		runner1.setCourse(red);
		registry.addRunner(runner1);
		assertTrue(registry.getRunnersFromCourse("Red").contains(runner1));
	}

	@Test
	public void testRemoveCourseInRegistry(){
		Course red = courseFactory("Red");
		registry.addCourse(red);
		assertTrue(registry.getRunnersFromCourse(red).isEmpty());
		
		registry.removeCourse(red);
		assertNull(registry.getRunnersFromCourse(red));
	}

	@Test
	public void testAddCategoryInRegistry(){
		Category h60 = categoryFactory("H60");
		registry.addCategory(h60);
		assertTrue(registry.getRunnersFromCategory("H60").isEmpty());
		
		runner1.setCategory(h60);
		registry.addRunner(runner1);
		assertTrue(registry.getRunnersFromCategory(h60).contains(runner1));
	}

	@Test
	public void testRemoveCategoryInRegistry(){
		Category h60 = categoryFactory("H60");
		registry.addCategory(h60);
		assertTrue(registry.getRunnersFromCategory("H60").isEmpty());
		
		registry.removeCategory(h60);
		assertNull(registry.getRunnersFromCategory(h60));
	}

}
