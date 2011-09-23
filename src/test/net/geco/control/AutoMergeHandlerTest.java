/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static test.net.geco.GecoFixtures.punch;

import java.util.Date;

import net.geco.control.AutoMergeHandler;
import net.geco.control.FreeOrderTracer;
import net.geco.control.GecoControl;
import net.geco.control.PenaltyChecker;
import net.geco.control.RunnerControl;
import net.geco.model.Course;
import net.geco.model.Punch;
import net.geco.model.Registry;
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
public class AutoMergeHandlerTest {
	
	private POFactory factory;
	private Course courseA;
	private Course courseB;
	private Course courseC;
	private GecoControl geco;
	private RunnerRaceData raceData;

	@Before
	public void setUp() {
		factory = new POFactory();
		courseA = GecoFixtures.createCourse("A", 31, 34, 31, 33, 31, 32, 31);
		courseB = GecoFixtures.createCourse("B", 31, 33, 31, 32, 31, 34, 31);
		courseC = GecoFixtures.createCourse("C", 31, 33, 31, 32, 31, 34, 31, 35, 36);
		Registry registry = new Registry();
		registry.addCourse(courseA);
		registry.addCourse(courseB);
		registry.addCourse(courseC);
		geco = GecoFixtures.mockGecoControlWithRegistry(registry);
		when(geco.factory()).thenReturn(factory);
		RunnerControl runnerControl = new RunnerControl(geco);
		when(geco.getService(RunnerControl.class)).thenReturn(runnerControl);
		
		raceData = factory.createRunnerRaceData();
		raceData.setStarttime(new Date(10000));
		raceData.setFinishtime(new Date(20000));
	}
	
	@Test
	public void testInlineCourseWithOKTrace() {
		raceData.setPunches(new Punch[]{
				punch(31), punch(33), punch(31), punch(32), punch(31), punch(34), punch(31), punch(35), punch(36)
		});
		when(geco.checker()).thenReturn(new PenaltyChecker(factory));
		AutoMergeHandler handler = new AutoMergeHandler(geco);
		assertEquals("should find perfect match with OK trace", courseC, handler.detectCourse(raceData));
		assertEquals(Status.OK, raceData.getStatus());
	}
	
	@Test
	public void testInlineCourseWithMP() {
		raceData.setPunches(new Punch[]{
				punch(31), punch(31), punch(33), punch(31), punch(34), punch(31)
		});
		when(geco.checker()).thenReturn(new PenaltyChecker(factory));
		AutoMergeHandler handler = new AutoMergeHandler(geco);
		assertEquals("should find closest match with MP trace", courseA, handler.detectCourse(raceData));
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
		AutoMergeHandler handler = new AutoMergeHandler(geco);
		
		assertEquals("should find perfect match with OK trace", courseB, handler.detectCourse(raceData));
		assertEquals(Status.OK, raceData.getStatus());

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
		AutoMergeHandler handler = new AutoMergeHandler(geco);
		
		Assert.assertEquals("should find closest match with MP trace", courseC, handler.detectCourse(raceData));

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
		AutoMergeHandler handler = new AutoMergeHandler(geco);
		assertEquals("should find perfect match with OK trace", courseC, handler.detectCourse(raceData));
		assertEquals(Status.OK, raceData.getStatus());
	}

	@Test
	public void testFreeorderCourseWithMP() {
		raceData.setPunches(new Punch[]{
				 punch(35), punch(36), punch(31), punch(31), punch(33), punch(31), punch(32), punch(31), punch(37)
		});
		when(geco.checker()).thenReturn(new PenaltyChecker(factory, new FreeOrderTracer(factory)));
		AutoMergeHandler handler = new AutoMergeHandler(geco);
		assertEquals("should find closets match with MP trace", courseC, handler.detectCourse(raceData));
		assertEquals(Status.MP, raceData.getStatus());
	}
	
}
