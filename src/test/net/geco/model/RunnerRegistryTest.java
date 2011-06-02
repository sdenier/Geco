/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import net.geco.model.Category;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerRegistry;
import net.geco.model.impl.POFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Jun 1, 2011
 *
 */
public class RunnerRegistryTest {
	
	private POFactory factory;

	private RunnerRegistry registry;

	private Runner runner1;
	
	private Category cat;

	private Category h60;

	private Course cou;

	private Course red;


	@Before
	public void setUp(){
		factory = new POFactory();
		registry = new RunnerRegistry();
		cat = categoryFactory("cat");
		h60 = categoryFactory("H60");
		cou = courseFactory("course");
		red = courseFactory("Red");
		runner1 = factory(1);
	}
	
	/*
	 * Basic runner registry
	 */

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
		registry.categoryCreated(category);
		return category;
	}
	
	public Course courseFactory(String name){
		Course course = factory.createCourse();
		course.setName(name);
		registry.courseCreated(course);
		return course;
	}
	
	@Test
	public void addOneRunnerWithId(){
		registry.addRunner(runner1);
		assertEquals(runner1.getStartId(), registry.findRunnerById(Integer.valueOf(1)).getStartId());
		assertEquals(runner1, registry.findRunnerById(1));
	}

	@Test
	public void removeRunner(){
		addOneRunnerWithId();
		registry.removeRunner(runner1);
		Assert.assertTrue(registry.getRunners().isEmpty());
	}

	@Test
	public void addRunnerWithoutId(){
		Runner runner = factory();
		registry.addRunnerWithoutId(runner);
		assertEquals(runner, registry.findRunnerById(1));
		
		registry.addRunner(factory(2));
		
		runner = factory();
		registry.addRunnerWithoutId(runner);
		assertEquals(runner, registry.findRunnerById(3));
		
		assertEquals(3, registry.getRunners().size());
	}
	
	@Test
	public void addRunnersSafely(){
		registry.addRunnerSafely(runner1);
		Runner runner = factory();
		registry.addRunnerSafely(runner);
		
		assertEquals(runner1, registry.findRunnerById(1));
		assertEquals(runner, registry.findRunnerById(2));
		
		assertEquals(2, registry.getRunners().size());
	}
	
	@Test
	public void overwriteRunnerId(){
		addOneRunnerWithId();
		
		Runner runner = factory(1);
		registry.addRunnerWithoutId(runner);
		assertEquals(2, runner.getStartId().intValue());
		
		runner = factory(2);
		registry.addRunnerSafely(runner);
		assertEquals(3, runner.getStartId().intValue());		
	}
	
	@Test
	public void updateRunnerStartId(){
		addOneRunnerWithId();
		runner1.setStartId(6);
		registry.updateRunnerStartId(1, runner1);
		assertEquals(null, registry.findRunnerById(1));
		assertEquals(runner1, registry.findRunnerById(6));
		assertEquals(1, registry.getRunners().size());
	}
	
	@Test
	public void availableStardId(){
		addOneRunnerWithId();
		assertFalse(registry.availableStartId(null));
		assertFalse(registry.availableStartId(1));
		assertTrue(registry.availableStartId(2));
	}
	
	@Test
	public void testMaxStartId(){
		assertEquals(0, registry.maxStartId());
		registry.addRunner(runner1);
		assertEquals(1, registry.maxStartId());
		registry.addRunner(factory(2));
		assertEquals(2, registry.maxStartId());
		Runner runner5 = factory(5);
		registry.addRunner(runner5);
		assertEquals(5, registry.maxStartId());
		registry.addRunner(factory(3));
		
		registry.removeRunner(runner1);
		assertEquals(5, registry.maxStartId());
		registry.removeRunner(runner5);
		assertEquals(3, registry.maxStartId());
		
		runner1.setStartId(6);
		registry.updateRunnerStartId(1, runner1);
		assertEquals(6, registry.maxStartId());
	}

	/*
	 * Runners and ecards
	 */
	
	@Test
	public void findRunnerByEcard(){
		runner1.setEcard("100");
		registry.addRunner(runner1);
		assertEquals(runner1, registry.findRunnerByEcard("100"));
	}
	
	@Test
	public void updateRunnerEcard(){
		runner1.setEcard("100");
		registry.addRunner(runner1);
		runner1.setEcard("200");
		registry.updateRunnerEcard("100", runner1);
		assertNull(registry.findRunnerByEcard("100"));
		assertEquals(runner1, registry.findRunnerByEcard("200"));
	}
	
	@Test
	public void deleteRunnerEcard(){
		runner1.setEcard("100");
		registry.addRunner(runner1);
		
		runner1.setEcard(null);
		registry.updateRunnerEcard("100", runner1);
		assertNull(registry.findRunnerByEcard("100"));
		assertNull(registry.findRunnerByEcard(null));
		
		Runner runner = factory();
		runner.setEcard("200");
		registry.addRunnerSafely(runner);
		assertEquals(runner, registry.findRunnerByEcard("200"));
		
		runner.setEcard("");
		registry.updateRunnerEcard("200", runner);
		assertNull(registry.findRunnerByEcard("200"));
		assertNull(registry.findRunnerByEcard(""));
	}
	
	@Test
	public void updateRunnerWithoutEcard(){
		registry.addRunner(runner1);
		assertNull(registry.findRunnerByEcard(runner1.getEcard()));
		
		runner1.setEcard("1000");
		registry.updateRunnerEcard(null, runner1);
		assertEquals(runner1, registry.findRunnerByEcard(runner1.getEcard()));
	}
	
	@Test
	public void removeRunnerAlsoRemoveFromEcardList(){
		runner1.setEcard("100");
		registry.addRunner(runner1);
		assertEquals(runner1, registry.findRunnerByEcard("100"));

		registry.removeRunner(runner1);
		assertNull(registry.findRunnerByEcard("100"));
	}
	
	/*
	 * Runners and courses
	 */
	
	@Test
	public void addRunnerInRedCourse(){
		runner1.setCourse(red);
		registry.addRunner(runner1);
		assertEquals(1, registry.getRunnersFromCourse(red).size());
		assertTrue( registry.getRunnersFromCourse(red).contains(runner1) );
	}
	
	@Test
	public void updateRunnerCourseToYellow(){
		addRunnerInRedCourse();
		Course yellow = courseFactory("Yellow");
		runner1.setCourse(yellow);
		registry.updateRunnerCourse(red, runner1);
		
		assertFalse( registry.getRunnersFromCourse(red).contains(runner1) );
		assertTrue( registry.getRunnersFromCourse(yellow).contains(runner1) );
	}
	
	@Test
	public void removeRunnerAlsoRemoveFromCourseList(){
		addRunnerInRedCourse();
		Runner runner = factory();
		runner.setCourse(red);
		registry.addRunnerSafely(runner);
		
		Collection<Runner> runners = registry.getRunnersFromCourse(red);
		assertEquals(2, runners.size());
		assertTrue(runners.contains(runner1));
		assertTrue(runners.contains(runner));
		
		registry.removeRunner(runner1);

		runners = registry.getRunnersFromCourse(red);
		assertEquals(1, runners.size());
		assertTrue(runners.contains(runner));
		assertFalse(runners.contains(runner1));

		
	}
	
	/*
	 * Runners and categories
	 */
	
	@Test
	public void addRunnerInH60Category(){
		runner1.setCategory(h60);
		registry.addRunner(runner1);
		Collection<Runner> runners = registry.getRunnersFromCategory(h60);
		assertEquals(1, runners.size());
		assertEquals(runner1, runners.iterator().next());
	}
	
	@Test
	public void updateRunnerCategoryToH55(){
		addRunnerInH60Category();
		Category h55 = categoryFactory("H55");
		runner1.setCategory(h55);
		registry.updateRunnerCategory(h60, runner1);

		Collection<Runner> runners = registry.getRunnersFromCategory(h60);
		assertTrue(runners.isEmpty());

		runners = registry.getRunnersFromCategory(h55);
		assertEquals(1, runners.size());
		assertEquals(runner1, runners.iterator().next());
	}
	
	@Test
	public void removeRunnerAlsoRemoveFromCategoryList(){
		addRunnerInH60Category();
		Runner runner = factory();
		runner.setCategory(h60);
		registry.addRunnerSafely(runner);
		
		Collection<Runner> runners = registry.getRunnersFromCategory(h60);
		assertEquals(2, runners.size());
		assertTrue(runners.contains(runner1));
		assertTrue(runners.contains(runner));
		
		registry.removeRunner(runner);

		runners = registry.getRunnersFromCategory(h60);
		assertEquals(1, runners.size());
		assertTrue(runners.contains(runner1));
		assertFalse(runners.contains(runner));
	}

	/*
	 * Runners and data
	 */
	
	@Test
	public void addRunnerData(){
		RunnerRaceData data = factory.createRunnerRaceData();
		data.setRunner(runner1);
		registry.addRunnerData(data);
		assertTrue( registry.getRunnersData().contains(data));
	}
	
	@Test
	public void findRunnerData(){
		RunnerRaceData data = factory.createRunnerRaceData();
		data.setRunner(runner1);
		runner1.setEcard("100");
		registry.addRunner(runner1);
		registry.addRunnerData(data);
		assertEquals( data, registry.findRunnerData(runner1) );
		assertEquals( data, registry.findRunnerData("100") );
	}

	@Test
	public void removeRunnerData(){
		RunnerRaceData data = factory.createRunnerRaceData();
		data.setRunner(runner1);
		registry.addRunnerData(data);
		
		registry.removeRunnerData(data);
		assertFalse(registry.getRunnersData().contains(data));
	}

}
