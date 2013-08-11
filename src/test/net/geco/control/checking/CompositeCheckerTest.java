/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import net.geco.control.checking.CompositeChecker;
import net.geco.control.checking.CompositeTracer;
import net.geco.control.checking.FreeOrderTracer;
import net.geco.control.checking.InlineTracer;
import net.geco.control.checking.MultiCourse;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Status;
import net.geco.model.impl.POFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author Simon Denier
 * @since Sep 4, 2011
 *
 */
public class CompositeCheckerTest {

	private Factory factory;
	private CompositeChecker checker;
	
	private Course course1;
	private Course course2;

	private Runner runner;
	private RunnerRaceData data;

	@Before
	public void setUp() {
		factory = new POFactory();		
		checker = new CompositeChecker(factory);
		checker.setMPPenalty(1800000);
		checker.disableMPLimit();
		course1 = factory.createCourse();
		course2 = factory.createCourse();
		checker.registerMultiCourse(createMixedCourse(course1));
		checker.registerMultiCourse(createShortCourse(course2));
		
		data = factory.createRunnerRaceData();
		runner = factory.createRunner();
		runner.setCourse(course1);
		data.setRunner(runner);
	}
	
	public MultiCourse createMixedCourse(Course course) {
		course.setCodes(new int[] { 31, 32, 33, 34, 121, 122, 121, 123, 124, 121 });
		MultiCourse multiCourse = new MultiCourse(course);
		multiCourse.startWith(new FreeOrderTracer(factory));
		try {
			multiCourse.joinRight(34, new InlineTracer(factory));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		return multiCourse;
	}

	public MultiCourse createShortCourse(Course course) {
		course.setCodes(new int[] { 61, 62, 63, 64, 65 });
		MultiCourse multiCourse = new MultiCourse(course);
		multiCourse.startWith(new FreeOrderTracer(factory));
		try {
			multiCourse.joinRight(63, new InlineTracer(factory));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		return multiCourse;
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
	public void testImportMultiCourse() {
		Registry mockRegistry = Mockito.mock(Registry.class);
		Course course = factory.createCourse();
		course.setName("Course A");
		createMixedCourse(course);
		Mockito.when(mockRegistry.findCourse("Course A")).thenReturn(course);
		
		MultiCourse multiCourse = checker.importMultiCourse(new String[] {"Course A", "121"}, mockRegistry);
		assertEquals(course, multiCourse.getCourse());
		Assert.assertArrayEquals(new int[]{ 31, 32, 33, 34 }, multiCourse.firstSection().codes);
		Assert.assertTrue(multiCourse.firstSection().tracer instanceof FreeOrderTracer);
		Assert.assertArrayEquals(new int[]{ 121, 122, 121, 123, 124, 121 },
								 multiCourse.secondSection().codes);
		Assert.assertTrue(multiCourse.secondSection().tracer instanceof InlineTracer);
	}
	
	@Test
	public void checkerShouldSetUpTracerWithMultiCourse() {
		MultiCourse multiCourse = new MultiCourse(course1);
		CompositeTracer tracer = Mockito.mock(CompositeTracer.class);
		Mockito.when(tracer.computeTrace(course1.getCodes(), new Punch[0])).thenReturn(factory.createTraceData());
		CompositeChecker checker = new CompositeChecker(factory, tracer);
		checker.registerMultiCourse(multiCourse);
		runner.setCourse(course1);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[0]);
		checker.check(data);
		Mockito.verify(tracer).setMultiCourse(Matchers.same(multiCourse));
	}
	
	@Test
	public void testShortCourse() {
		runner.setCourse(course2);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(100000));
		data.setPunches(new Punch[]{ punch(62), punch(61), punch(64), punch(65) });
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("62,61,-63,64,65", data.getTraceData().formatTrace());
		assertEquals(1, data.getTraceData().getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(1900000, result.getRacetime());
	}
	
	@Test
	public void testNoPunch() {
		runner.setCourse(course1);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[0]);
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("-31,-32,-33,-34,-121,-122,-121,-123,-124,-121", data.getTraceData().formatTrace());
		assertEquals(10, data.getTraceData().getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(18630000, result.getRacetime());
	}

	@Test
	public void testOKTrace() {
		runner.setCourse(course1);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(33), punch(31), punch(32), punch(34), punch(121), punch(122),
			punch(121), punch(123), punch(124), punch(121)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("33,31,32,34,121,122,121,123,124,121", data.getTraceData().formatTrace());
		assertEquals(0, data.getTraceData().getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(630000, result.getRacetime());
	}

	@Test
	public void testOneFreeOrderPenalty() {
		runner.setCourse(course1);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(33), punch(32), punch(34), punch(121), punch(122),
			punch(121), punch(123), punch(124), punch(121)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("33,32,-31,34,121,122,121,123,124,121", data.getTraceData().formatTrace());
		assertEquals(1, data.getTraceData().getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(2430000, result.getRacetime());
	}

	@Test
	public void testOneInlinePenalty() {
		runner.setCourse(course1);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(31), punch(33), punch(32), punch(34), punch(121), punch(122),
			punch(123), punch(124), punch(121)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("31,33,32,34,121,122,-121,123,124,121", data.getTraceData().formatTrace());
		assertEquals(1, data.getTraceData().getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(2430000, result.getRacetime());
	}

	@Test
	public void testMissingJointPunch() {
		runner.setCourse(course1);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(31), punch(33), punch(121), punch(122), punch(123), punch(124),
			punch(121)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("31,33,-32,-34,121,122,-121,123,124,121", data.getTraceData().formatTrace());
		assertEquals(3, data.getTraceData().getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(6030000, result.getRacetime());
	}

	@Test
	public void testFirstCourseOnly() {
		runner.setCourse(course1);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(31), punch(33), punch(32)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("31,33,32,-34,-121,-122,-121,-123,-124,-121", data.getTraceData().formatTrace());
		assertEquals(7, data.getTraceData().getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(13230000, result.getRacetime());
	}
	
	@Test
	public void testMultiplePenalties() {
		runner.setCourse(course1);
		data.setStarttime(new Date(0));
		data.setFinishtime(new Date(630000));
		data.setPunches(new Punch[]{
			punch(32), punch(31), punch(34), punch(122), punch(121),
			punch(124), punch(121), punch(123)
		});
		checker.check(data);
		RunnerResult result = data.getResult();
		assertEquals("32,31,-33,34,-121,122,121,-123,124,121,+123", data.getTraceData().formatTrace());
		assertEquals(3, data.getTraceData().getNbMPs());
		assertEquals(Status.OK, result.getStatus());
		assertEquals(6030000, result.getRacetime());
	}
	
}
