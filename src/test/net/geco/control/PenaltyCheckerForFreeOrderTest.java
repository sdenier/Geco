/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Properties;

import net.geco.control.FreeOrderTracer;
import net.geco.control.PenaltyChecker;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Simon Denier
 * @since Aug 5, 2011
 *
 */
public class PenaltyCheckerForFreeOrderTest {
	
	private Factory factory;
	private PenaltyChecker checker;
	
	private Course course;
	private RunnerRaceData data;

	@Before
	public void setUp() {
		factory = new POFactory();
		checker = new PenaltyChecker(factory, new FreeOrderTracer(factory));
		course = factory.createCourse();
		data = factory.createRunnerRaceData();
		Runner runner = factory.createRunner();
		runner.setCourse(course);
		data.setRunner(runner);
	}
	
	public void createSimpleCourse(Course course) {
		course.setCodes(new int[] { 121, 122, 34, 33, 45});
	}
	
	public Punch punch(Date time, int code) {
		Punch punch = factory.createPunch();
		punch.setTime(time);
		punch.setCode(code);
		return punch;
	}
	
	public Punch punch(int code) {
		return punch(new Date(), code);
	}
	
	@Test
	public void testNoPunchMP() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[0]);
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals(Status.MP, result.getStatus());
		assertTrue(result.getRacetime() == 630000);
		assertEquals(5, data.getTraceData().getNbMPs());
		assertEquals("-121,-122,-34,-33,-45", data.getTraceData().formatTrace());
	}
	
	@Test
	public void testSimpleCourseOK() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(34), punch(33), punch(45),
			});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals(Status.OK, result.getStatus());
		assertTrue(result.getRacetime() == 630000);
		assertEquals(0, data.getTraceData().getNbMPs());
		assertEquals("121,122,34,33,45", data.getTraceData().formatTrace());
	}

	@Test
	public void testSimpleCourseFreeOrderOK() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(45), punch(122), punch(34), punch(33), punch(121)
			});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals(Status.OK, result.getStatus());
		assertTrue(result.getRacetime() == 630000);
		assertEquals(0, data.getTraceData().getNbMPs());
		assertEquals("45,122,34,33,121", data.getTraceData().formatTrace());
	}

	@Test
	public void testSimpleCourseDuplicateOK() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				 punch(45), punch(122), punch(33), punch(45), punch(121), punch(34), punch(33),
			});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals(Status.OK, result.getStatus());
		assertTrue(result.getRacetime() == 630000);
		assertEquals(0, data.getTraceData().getNbMPs());
		assertEquals("45,122,33,+45,121,34,+33", data.getTraceData().formatTrace());
	}

	@Test
	public void testSimpleCourseMP() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(34), punch(33), punch(45),
			});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals(Status.MP, result.getStatus());
		assertTrue(result.getRacetime() == 630000);
		assertEquals(1, data.getTraceData().getNbMPs());
		assertEquals("121,34,33,45,-122", data.getTraceData().formatTrace());
	}

	@Test
	public void testSimpleCourseMPs() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(34), punch(33), punch(45),
			});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals(Status.MP, result.getStatus());
		assertTrue(result.getRacetime() == 630000);
		assertEquals(2, data.getTraceData().getNbMPs());
		assertEquals("34,33,45,-121,-122", data.getTraceData().formatTrace());
	}

	@Test
	public void testSimpleCourseReplacePunch() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(34), punch(33), punch(46),
			});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals(Status.MP, result.getStatus());
		assertTrue(result.getRacetime() == 630000);
		assertEquals(1, data.getTraceData().getNbMPs());
		assertEquals("121,122,34,33,+46,-45", data.getTraceData().formatTrace());
	}

	@Test
	public void testSimpleCourseWithTwoPenalties() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(33), punch(45),
			});
		checker.setMPPenalty(1800);
		checker.setMPLimit(2);
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals(Status.OK, result.getStatus());
		assertTrue(result.getRacetime() == 630000 + checker.timePenalty(2));
		assertEquals(2, data.getTraceData().getNbMPs());
		assertEquals("121,33,45,-122,-34", data.getTraceData().formatTrace());
	}

	@Test
	public void testSimpleCourseMPAboveLimit() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(34)
			});
		checker.setMPPenalty(1800);
		checker.setMPLimit(2);
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals(Status.MP, result.getStatus());
		assertTrue(result.getRacetime() == 630000 + checker.timePenalty(3));
		assertEquals(3, data.getTraceData().getNbMPs());
		assertEquals("121,34,-122,-33,-45", data.getTraceData().formatTrace());
	}
	
	@Test
	public void testNoMPLimit() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(34), punch(33), punch(45),
			});
		checker.disableMPLimit();
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals(Status.OK, result.getStatus());
		assertTrue(result.getRacetime() == 630000);
		assertEquals(1, data.getTraceData().getNbMPs());
	}
	
	@Test
	public void testNoMPLimitSaveConfig() {
		Properties prop = new Properties();
		checker.saving(null, prop);
		assertFalse(checker.noMPLimit());
		assertEquals("0", prop.getProperty(PenaltyChecker.mpLimitProperty()));
		
		checker.disableMPLimit();
		assertTrue(checker.noMPLimit());
		checker.saving(null, prop);
		assertEquals("-1", prop.getProperty(PenaltyChecker.mpLimitProperty()));
		
		checker.enableMPLimit();
		checker.setMPLimit(2);
		assertFalse(checker.noMPLimit());
		checker.saving(null, prop);
		assertEquals("2", prop.getProperty(PenaltyChecker.mpLimitProperty()));
	}
	
	@Test
	public void testNoMPLimitLoadConfig() {
		Properties prop = new Properties();
		Stage stage = Mockito.mock(Stage.class);
		Mockito.when(stage.getProperties()).thenReturn(prop);
		
		checker.postInitialize(stage);
		assertFalse(checker.noMPLimit());
		assertEquals(checker.defaultMPLimit(), checker.getMPLimit());
		
		prop.setProperty(PenaltyChecker.mpLimitProperty(), "-1");
		checker.postInitialize(stage);
		assertTrue(checker.noMPLimit());
		assertEquals(checker.defaultMPLimit(), checker.getMPLimit());

		prop.setProperty(PenaltyChecker.mpLimitProperty(), "3");
		checker.postInitialize(stage);
		assertFalse(checker.noMPLimit());
		assertEquals(3, checker.getMPLimit());
	}

}
