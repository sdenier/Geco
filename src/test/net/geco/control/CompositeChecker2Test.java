/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import net.geco.control.CompositeChecker2;
import net.geco.control.FreeOrderTracer;
import net.geco.control.InlineTracer;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Status;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Sep 4, 2011
 *
 */
public class CompositeChecker2Test {

	private Factory factory;
	private CompositeChecker2 checker;
	
	private Course course;
	private RunnerRaceData data;

	@Before
	public void setUp() {
		factory = new POFactory();		
		checker = new CompositeChecker2(factory);
		// TODO: conf with MultiCourse
		checker.startWith(new FreeOrderTracer(factory));
		checker.joinRight(34, new InlineTracer(factory));
		checker.setMPPenalty(1800000);
		checker.disableMPLimit();
		course = factory.createCourse();
		data = factory.createRunnerRaceData();
		Runner runner = factory.createRunner();
		runner.setCourse(course);
		data.setRunner(runner);
	}
	
	public void createMixedCourse(Course course) {
		course.setCodes(new int[] { 31, 32, 33, 34, 121, 122, 121, 123, 124, 121 });
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
	public void testNoPunch() {
		createMixedCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[0]);
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("-31,-32,-33,-34,-121,-122,-121,-123,-124,-121", result.formatTrace());
		assertEquals(10, result.getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(18630000, result.getRacetime());
	}

	@Test
	public void testOKTrace() {
		createMixedCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(33), punch(31), punch(32), punch(34), punch(121), punch(122),
			punch(121), punch(123), punch(124), punch(121)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("33,31,32,34,121,122,121,123,124,121", result.formatTrace());
		assertEquals(0, result.getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(630000, result.getRacetime());
	}

	@Test
	public void testOneFreeOrderPenalty() {
		createMixedCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(33), punch(32), punch(34), punch(121), punch(122),
			punch(121), punch(123), punch(124), punch(121)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("33,32,-31,34,121,122,121,123,124,121", result.formatTrace());
		assertEquals(1, result.getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(2430000, result.getRacetime());
	}

	@Test
	public void testOneInlinePenalty() {
		createMixedCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(31), punch(33), punch(32), punch(34), punch(121), punch(122),
			punch(123), punch(124), punch(121)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("31,33,32,34,121,122,-121,123,124,121", result.formatTrace());
		assertEquals(1, result.getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(2430000, result.getRacetime());
	}

	@Test
	public void testMissingJointPunch() {
		createMixedCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(31), punch(33), punch(121), punch(122), punch(123), punch(124),
			punch(121)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("31,33,-32,-34,121,122,-121,123,124,121", result.formatTrace());
		assertEquals(3, result.getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(6030000, result.getRacetime());
	}

	@Test
	public void testFirstCourseOnly() {
		createMixedCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(31), punch(33), punch(32)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("31,33,32,-34,-121,-122,-121,-123,-124,-121", result.formatTrace());
		assertEquals(7, result.getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(13230000, result.getRacetime());
	}
	
	@Test
	public void testMultiplePenalties() {
		createMixedCourse(course);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(32), punch(31), punch(34), punch(122), punch(121),
			punch(124), punch(121), punch(123)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("32,31,-33,34,-121,122,121,-123,124,121,+123", result.formatTrace());
		assertEquals(3, result.getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(6030000, result.getRacetime());
	}
	
}
