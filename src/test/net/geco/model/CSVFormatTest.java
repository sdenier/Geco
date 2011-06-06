/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.impl.POFactory;
import net.geco.model.iocsv.RunnerIO;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Jun 7, 2011
 *
 */
public class CSVFormatTest {

	private Factory factory;
	
	private Registry registry;

	private Club club;

	private Course course;

	private Category category;

	@Before
	public void setUp(){
		factory = new POFactory();
		registry = new Registry();
		club = factory.createClub();
		club.setName("Cl");
		registry.addClub(club);
		course = factory.createCourse();
		course.setName("Course");
		registry.addCourse(course);
		category = factory.createCategory();
		category.setName("H60");
		registry.addCategory(category);
	}
	
	@Test
	public void testRunnerImport(){
		RunnerIO runnerIO = new RunnerIO(factory, null, null, registry, 0);
		Runner runner = runnerIO.importTData(new String[]{
			"100", "203a", "John", "Doe", "Cl", "Course", "true", "H60", "1:00:00", "", "", "true", "50000"
//		id,Ecard,First Name,Last Name,Club,Course,Rented,Class,Start Time,Finish Time,Status,NC,Archive
		});
		
		assertEquals("Doe", runner.getLastname());
		assertEquals("John", runner.getFirstname());
		assertEquals(100, runner.getStartId().intValue());
		assertEquals(50000, runner.getArchiveId().intValue());
		assertEquals("203a", runner.getEcard());
		assertTrue(runner.rentedEcard());
		assertTrue(runner.isNC());
		assertEquals(new Date(3600000), runner.getRegisteredStarttime());
	}
	
	@Test
	public void testRunnerExport(){
		RunnerIO runnerIO = new RunnerIO(factory, null, null, registry, 0);
		Runner runner = factory.createRunner();
		runner.setArchiveId(50000);
		runner.setCategory(category);
		runner.setClub(club);
		runner.setCourse(course);
		runner.setEcard("203a");
		runner.setFirstname("John");
		runner.setLastname("Doe");
		runner.setNC(true);
		runner.setRentedEcard(true);
		runner.setStartId(100);
		runner.setRegisteredStarttime(new Date(3600000));
		String[] data = runnerIO.exportTData(runner);
		
		Assert.assertArrayEquals(
				new String[]{"100", "203a", "John", "Doe", "Cl", "Course", "true", "H60", "1:00:00", "", "", "true", "50000"},
				data);
	}
	
	@Test
	public void testCardDataImport(){
		Assert.fail();
	}

	@Test
	public void testCardDataExport(){
		Assert.fail();
	}
	
	@Test
	public void testResultDataImport(){
		Assert.fail();
	}

	@Test
	public void testResultDataExport(){
		Assert.fail();
	}

}
