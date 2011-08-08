/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import net.geco.basics.Util;
import net.geco.control.FreeOrderChecker;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Status;
import net.geco.model.Trace;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Aug 5, 2011
 *
 */
public class FreeOrderCheckerTest {
	
	private Factory factory;
	private FreeOrderChecker checker;
	
	private Course course;
	private RunnerRaceData data;

	@Before
	public void setUp() {
		factory = new POFactory();
		checker = new FreeOrderChecker(factory);
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
	
	public String traceToString(Trace[] trace) {
		return Util.join(trace, ",", new StringBuffer());
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
		assertEquals(5, result.getNbMPs());
		assertEquals("-121,-122,-34,-33,-45", traceToString(result.getTrace()));
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
		assertEquals(0, result.getNbMPs());
		assertEquals("121,122,34,33,45", traceToString(result.getTrace()));
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
		assertEquals(0, result.getNbMPs());
		assertEquals("45,122,34,33,121", traceToString(result.getTrace()));
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
		assertEquals(0, result.getNbMPs());
		assertEquals("45,122,33,+45,121,34,+33", traceToString(result.getTrace()));
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
		assertEquals(1, result.getNbMPs());
		assertEquals("121,34,33,45,-122", traceToString(result.getTrace()));
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
		assertEquals(2, result.getNbMPs());
		assertEquals("34,33,45,-121,-122", traceToString(result.getTrace()));
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
		assertEquals(1, result.getNbMPs());
		assertEquals("121,122,34,33,+46,-45", traceToString(result.getTrace()));
	}

}
