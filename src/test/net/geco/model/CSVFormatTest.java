/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import net.geco.basics.TimeManager;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;
import net.geco.model.impl.POFactory;
import net.geco.model.iocsv.CardDataIO;
import net.geco.model.iocsv.ResultDataIO;
import net.geco.model.iocsv.RunnerIO;

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
	public void testMigr12EcardToStartid(){ // MIGR12
		RunnerIO runnerIO = new RunnerIO(null, null, null, null, 0);
		assertEquals(1, runnerIO.uniqueStartId("1"));
		assertEquals(10001, runnerIO.uniqueStartId("1a"));
		assertEquals(100001, runnerIO.uniqueStartId("10a"));
		assertEquals(1000001, runnerIO.uniqueStartId("100a"));
		assertEquals(100011, runnerIO.uniqueStartId("1aa"));
		assertEquals(10000111, runnerIO.uniqueStartId("10aaa"));
		assertEquals(10000011, runnerIO.uniqueStartId("100aa"));
	}
	
	@Test
	public void testRunnerImportMigr12(){ // MIGR12
		RunnerIO runnerIO = new RunnerIO(factory, null, null, registry, 0);
		Runner runner = runnerIO.importTData(new String[]{
			"100a", "203a", "John", "Doe", "Cl", "Course", "true", "H60", "1:00:00", "", "", "true", "50000"
		});
		
		assertEquals("Doe", runner.getLastname());
		assertEquals("John", runner.getFirstname());
		assertEquals(1000001, runner.getStartId().intValue());
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
		
		assertArrayEquals(
				new String[]{"100", "203a", "John", "Doe", "Cl", "Course", "true", "H60", "1:00:00", "", "", "true", "50000"},
				data);
	}
	
	@Test
	public void testRunnerRoundtrip(){
		RunnerIO runnerIO = new RunnerIO(factory, null, null, registry, 0);
		String[] record = new String[]{
			"100", "203a", "John", "Doe", "Cl", "Course", "true", "H60", "1:00:00", "", "", "true", "50000"
		};
		assertArrayEquals(record, runnerIO.exportTData(runnerIO.importTData(record)));
	}
	
	private Runner runnerFactory(Integer startId){
		Runner runner = factory.createRunner();
		runner.setStartId(startId);
		runner.setCategory(category);
		runner.setCourse(course);
		registry.addRunner(runner);
		return runner;
	}
	
	@Test
	public void testCardDataImport(){
		runnerFactory(66);
		CardDataIO cardDataIO = new CardDataIO(factory, null, null, registry);
		RunnerRaceData runnerData = cardDataIO.importTData(new String[]{
			"66", "--:--", "1:00:00", "1:00:15", "1:05:00", "2:10:05", "31", "1:06:15", "32", "1:32:42"				
//		Start id,Read time,Clear time,Check time,Start time,Finish time,Control,Split time, ...
		});
		cardDataIO.register(runnerData, registry);
		assertEquals(TimeManager.NO_TIME, runnerData.getReadtime());
		assertEquals(TimeManager.safeParse("1:00:00"), runnerData.getErasetime());
		assertEquals(TimeManager.safeParse("1:00:15"), runnerData.getControltime());
		assertEquals(TimeManager.safeParse("1:05:00"), runnerData.getStarttime());
		assertEquals(TimeManager.safeParse("2:10:05"), runnerData.getFinishtime());
		Punch[] punches = runnerData.getPunches();
		assertEquals(2, punches.length);
		assertEquals(31, punches[0].getCode());
		assertEquals(TimeManager.safeParse("1:06:15"), punches[0].getTime());
		assertEquals(32, punches[1].getCode());
		assertEquals(TimeManager.safeParse("1:32:42"), punches[1].getTime());
	}

	@Test
	public void testCardDataRoundtrip(){
		runnerFactory(66);
		CardDataIO cardDataIO = new CardDataIO(factory, null, null, registry);
		String[] record = new String[]{
			"66", "--:--", "1:00:00", "1:00:15", "1:05:00", "2:10:05", "31", "1:06:15", "32", "1:32:42"				
		};
		RunnerRaceData runnerData = cardDataIO.importTData(record);
		cardDataIO.register(runnerData, registry);
		assertArrayEquals(record, cardDataIO.exportTData(runnerData));
	}

	@Test
	public void testResultDataImport(){
		testCardDataImport();
		ResultDataIO resultDataIO = new ResultDataIO(factory, null, null, registry);
		RunnerRaceData raceData = resultDataIO.importTData(new String[]{
			"66", "NOS", "0:59:25" 
			// start id, status, racetime
		});
		assertEquals(Status.NOS, raceData.getStatus());
		assertEquals(TimeManager.safeParse("0:59:25").getTime(), raceData.getResult().getRacetime());
	}

	@Test
	public void testResultDataRoundtrip(){
		testCardDataImport();
		ResultDataIO resultDataIO = new ResultDataIO(factory, null, null, registry);
		String[] record = new String[]{"66", "NOS", "0:59:25"};
		assertArrayEquals(record, resultDataIO.exportTData(resultDataIO.importTData(record)));

	}
}
