/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static test.net.geco.GecoFixtures.punch;

import java.util.Date;

import net.geco.control.FreeOrderTracer;
import net.geco.control.GecoControl;
import net.geco.control.PenaltyChecker;
import net.geco.control.RunnerControl;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.model.Course;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;
import net.geco.model.impl.POFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import test.net.geco.GecoFixtures;

/**
 * @author Simon Denier
 * @since Sep 23, 2011
 *
 */
public class CourseDetectorTest {
	
	private POFactory factory;
	private Course courseA;
	private Course courseB;
	private Course courseC;
	private Course autoC;
	private RunnerRaceData raceData;

	private GecoControl geco;
	private RunnerControl runnerControl;
	private CourseDetector detector;
	private Registry registry;

	@Before
	public void setUp() {
		factory = new POFactory();
		courseA = GecoFixtures.createCourse("A", 31, 34, 31, 33, 31, 32, 31);
		courseB = GecoFixtures.createCourse("B", 31, 33, 31, 32, 31, 34, 31);
		courseC = GecoFixtures.createCourse("C", 31, 33, 31, 32, 31, 34, 31, 35, 36);
		autoC   = GecoFixtures.createCourse(Registry.autoCourseName());
		registry = new Registry();
		registry.addCourse(courseA);
		registry.addCourse(courseB);
		registry.addCourse(courseC);
		geco = GecoFixtures.mockGecoControlWithRegistry(registry);
		when(geco.factory()).thenReturn(factory);
		runnerControl = new RunnerControl(geco);
		
		detector = new CourseDetector(geco, runnerControl, autoC);
		
		raceData = factory.createRunnerRaceData();
		raceData.setStarttime(new Date(10000));
		raceData.setFinishtime(new Date(20000));
		raceData.setResult(factory.createRunnerResult());
	}
	
	@Test
	public void testInlineCourseWithOKTrace() {
		raceData.setPunches(new Punch[]{
				punch(31), punch(33), punch(31), punch(32), punch(31), punch(34), punch(31), punch(35), punch(36)
		});
		when(geco.checker()).thenReturn(new PenaltyChecker(factory));
		assertEquals("should find perfect match with OK trace", courseC, detector.detectCourse(raceData));
		assertEquals(Status.OK, raceData.getStatus());
	}
	
	@Test
	public void testInlineCourseWithMP() {
		raceData.setPunches(new Punch[]{
				punch(31), punch(31), punch(33), punch(31), punch(34), punch(31)
		});
		when(geco.checker()).thenReturn(new PenaltyChecker(factory));
		assertEquals("should find closest match with MP trace", courseA, detector.detectCourse(raceData));
		assertEquals(Status.MP, raceData.getStatus());
	}
	
	@Test
	public void testOrientshowCourseWithMultipleOK() {
		raceData.setPunches(new Punch[]{
				punch(31), punch(33), punch(31), punch(32), punch(31), punch(34), punch(31)
		});
		PenaltyChecker checker = new PenaltyChecker(factory);
		checker.setMPLimit(2);
		when(geco.checker()).thenReturn(checker);
		
		assertEquals("should find perfect match with OK trace", courseB, detector.detectCourse(raceData));
		assertEquals(Status.OK, raceData.getStatus());

		// Other OK courses
		raceData.setRunner(factory.createRunner());
		
		raceData.getRunner().setCourse(courseA);
		checker.check(raceData);
		assertEquals(Status.OK, raceData.getStatus());

		raceData.getRunner().setCourse(courseB);
		checker.check(raceData);
		assertEquals(Status.OK, raceData.getStatus());

		raceData.getRunner().setCourse(courseC);
		checker.check(raceData);
		assertEquals(Status.OK, raceData.getStatus());
	}
	
	@Test
	public void testOrientshowCourseWithMP() {
		raceData.setPunches(new Punch[]{
				punch(31), punch(33), punch(32), punch(31), punch(34), punch(31), punch(35), punch(36), punch(37)
		});
		PenaltyChecker checker = new PenaltyChecker(factory);
		checker.setMPLimit(2);
		when(geco.checker()).thenReturn(checker);
		
		Assert.assertEquals("should find closest match with MP trace", courseC, detector.detectCourse(raceData));

		// Other OK courses
		raceData.setRunner(factory.createRunner());
		
		raceData.getRunner().setCourse(courseB);
		checker.check(raceData);
		assertEquals(Status.OK, raceData.getStatus());

		raceData.getRunner().setCourse(courseA);
		checker.check(raceData);
		assertEquals(Status.MP, raceData.getStatus());

		raceData.getRunner().setCourse(courseC);
		checker.check(raceData);
		assertEquals(Status.OK, raceData.getStatus());
	}

	@Test
	public void testFreeorderCourseWithOKTrace() {
		raceData.setPunches(new Punch[]{
				 punch(35), punch(36), punch(31), punch(34), punch(31), punch(33), punch(31), punch(32), punch(31)
		});
		when(geco.checker()).thenReturn(new PenaltyChecker(factory, new FreeOrderTracer(factory)));
		assertEquals("should find perfect match with OK trace", courseC, detector.detectCourse(raceData));
		assertEquals(Status.OK, raceData.getStatus());
	}

	@Test
	public void testFreeorderCourseWithMP() {
		raceData.setPunches(new Punch[]{
				 punch(35), punch(36), punch(31), punch(31), punch(33), punch(31), punch(32), punch(31), punch(37)
		});
		when(geco.checker()).thenReturn(new PenaltyChecker(factory, new FreeOrderTracer(factory)));
		assertEquals("should find closets match with MP trace", courseC, detector.detectCourse(raceData));
		assertEquals(Status.MP, raceData.getStatus());
	}
	
	@Test
	public void detectCourse_shouldNotChangeRunnerReference() {
		Runner runner = factory.createRunner();
		raceData.setRunner(runner);
		when(geco.checker()).thenReturn(new PenaltyChecker(factory));
		detector.detectCourse(raceData);
		assertEquals(runner, raceData.getRunner());
	}

	@Test
	public void detectCourse_shouldNotReturnAutoCourseWhenOtherCoursesExist() {
		registry.addCourse(autoC);
		raceData.setPunches(new Punch[]{
				punch(31), punch(31), punch(33), punch(31), punch(34), punch(31)
		});
		when(geco.checker()).thenReturn(new PenaltyChecker(factory));
		assertEquals("should not match Auto course even if OK", courseA, detector.detectCourse(raceData));
	}

	@Test
	public void detectCourse_shouldReturnAutoCourseWhenSingle() {
		registry.addCourse(autoC);
		registry.removeCourse(courseA);
		registry.removeCourse(courseB);
		registry.removeCourse(courseC);
		assertEquals(1, registry.getCourses().size());
		
		raceData.setPunches(new Punch[]{
				punch(31), punch(31), punch(33), punch(31), punch(34), punch(31)
		});
		when(geco.checker()).thenReturn(new PenaltyChecker(factory));
		assertEquals("should return Auto course if it's the only course", autoC, detector.detectCourse(raceData));
	}

}
