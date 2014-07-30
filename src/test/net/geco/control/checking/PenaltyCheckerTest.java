/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import net.geco.control.checking.PenaltyChecker;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;


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
		factory = new POFactory();
		checker = new PenaltyChecker(factory);
		checker.setMPPenalty(30000);
		checker.setMPLimit(4);
		checker.setExtraPenalty(45000);
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
		assertTrue(data.getResult().getResultTime() == 20000000);
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
		assertTrue(data.getResult().getResultTime() == 630000);
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
		assertTrue(data.getResult().getResultTime() == 630000 + checker.mpTimePenalty(1));
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
		assertTrue(data.getResult().getResultTime() == 630000 + checker.mpTimePenalty(2));
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
		assertTrue(data.getResult().getResultTime() == 630000 + checker.mpTimePenalty(3));
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
		assertEquals(630000 + checker.mpTimePenalty(5) + checker.extraTimePenalty(1), data.getResult().getResultTime());
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
		assertTrue(data.getResult().getResultTime() == 630000);
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
		assertTrue(data.getResult().getResultTime() == 630000);
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
		assertTrue(data.getResult().getResultTime() == 630000 + checker.mpTimePenalty(2));
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
		assertTrue(data.getResult().getResultTime() == 630000 + checker.mpTimePenalty(2));
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
		assertEquals(630000 + checker.mpTimePenalty(3) + checker.extraTimePenalty(1), data.getResult().getResultTime());
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
		assertEquals(Status.MP, data.getResult().getStatus());
		assertTrue(data.getResult().getResultTime() == 630000 + checker.mpTimePenalty(6));
	}
	
	@Test
	public void testFullPenalties() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] { });
		checker.check(data);
		assertEquals(Status.MP, data.getResult().getStatus());
		long maxPenalties = 630000 + checker.mpTimePenalty(data.getCourse().nbControls());
		assertTrue(data.getResult().getResultTime() == maxPenalties);
	}

	@Test
	public void testSimpleCourseTwoExtraneousPenalty() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(205), punch(121), punch(122), punch(34), punch(33), punch(34), punch(204), punch(45), punch(46), punch(47)
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertEquals(630000 + checker.extraTimePenalty(2), data.getResult().getResultTime());
	}

	@Test
	public void testSimpleCourseMixedPenalties() {
		createSimpleCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[] {
				punch(205), punch(121), punch(33), punch(122), punch(34), punch(204), punch(45), punch(46)
			});
		checker.check(data);
		assertEquals(Status.OK, data.getResult().getStatus());
		assertEquals(630000 + checker.mpTimePenalty(2) + checker.extraTimePenalty(2), data.getResult().getResultTime());
	}

}
