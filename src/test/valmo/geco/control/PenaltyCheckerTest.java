/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.valmo.geco.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import valmo.geco.control.GecoControl;
import valmo.geco.control.PenaltyChecker;
import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class PenaltyCheckerTest {
	
	private Factory factory;
	private PenaltyChecker checker;
	
	private Course course;
	private RunnerRaceData data;

	@Before
	public void setUp() {
		GecoControl geco = new GecoControl();
		factory = geco.factory();
		checker = new PenaltyChecker(geco);
		checker.setMPPenalty(30000);
		checker.setMPLimit(4);
		course = factory.createCourse();
		data = factory.createRunnerRaceData();
		Runner runner = factory.createRunner();
		runner.setCourse(course);
		data.setRunner(runner);
	}
	
	/**
	 * @param course2
	 */
	public void createSimpleCourse(Course course) {
		course.setCodes(new int[] { 121, 122, 34, 33, 45, 46, 47});
	}
	
	public void createButterflyCourse(Course course) {
		course.setCodes(new int[] { 121, 122, 121, 123, 121, 124, 125, 126, 121, 45});
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
	public void testSimpleCourseOK() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(20000000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(34), punch(33), punch(45), punch(46), punch(47)
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 20000000);
	}

	@Test
	public void testSimpleCourseOKWithLoopBack() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(33), punch(45), punch(34), punch(33), punch(45), punch(46), 
				punch(47)
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}


	@Test
	public void testSimpleCourseOnePenalty() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(34), punch(33), punch(45), punch(46), punch(47)
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000 + checker.timePenalty(1));
	}

	@Test
	public void testSimpleCourseTwoPenalties() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(34), punch(46), punch(47), punch(33)
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000 + checker.timePenalty(2));
	}
	
	@Test
	public void testSimpleCourseThreePenalties() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(122), punch(121), punch(34), punch(33), punch(47)
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000 + checker.timePenalty(3));
	}

	@Test
	public void testSimpleCourseMP() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(122), punch(31), punch(33)
			});
		checker.check(data);
		assertEquals(Status.MP, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == (630000 + checker.timePenalty(5)));
	}

	
	@Test
	public void testCourseWithButterlyOK() {
		createButterflyCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(121), punch(123), punch(121), punch(124), punch(125), punch(126),
				punch(121), punch(45), 
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}

	@Test
	public void testCourseWithButterlyOKDoubleLoop() {
		createButterflyCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(123), punch(121), punch(122), punch(121), punch(121), punch(123), 
				punch(121), punch(124), punch(125), punch(126), punch(121), punch(45), 
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000);
	}

	
	@Test
	public void testCourseWithButterflyTwoPenalties() {
		createButterflyCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(123), punch(121), punch(124), punch(126), punch(121),
				punch(45), 
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000 + checker.timePenalty(2));
	}

	@Test
	public void testCourseButterflyWithInversedLoops() {
		/*
		 * on compte les erreurs (oublis), mais on ne compte pas le coût de remédiation.
		 * (refaire les 2 boucles dans l'ordre) -> trop complexe
		 * et même, il faudrait tout refaire à partir de l'erreur en poussant la logique au bout.
		 */
		createButterflyCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(122), punch(121), punch(124), punch(125), punch(126), punch(121), punch(123),  
				punch(121),	punch(45), 
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000 + checker.timePenalty(2));
	}

	@Test
	public void testCourseButterflyWithLoopInReversedOrder() {
		createButterflyCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(43), punch(122), punch(121), punch(123), punch(121), punch(126), punch(125), punch(124),  
				punch(121),	punch(45), 
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000 + checker.timePenalty(3));
	}

	@Test
	public void testCourseWithButterflyMP() {
		createButterflyCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(121), punch(123), punch(121), punch(122), punch(121),
			});
		checker.check(data);
		checker.explainTrace(course.getCodes(), data.getPunches(), false);
		assertEquals(Status.MP, data.getResult().getStatus());
		assertTrue(data.getResult().getRacetime() == 630000 + checker.timePenalty(6));
	}
	
	@Test
	public void testFullPenalties() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] { });
		checker.check(data);
		assertEquals(Status.MP, data.getResult().getStatus());
		long maxPenalties = 630000 + checker.timePenalty(data.getCourse().getCodes().length);
		assertTrue(data.getResult().getRacetime() == maxPenalties);
	}

	 
}
