/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static test.net.geco.GecoFixtures.punch;

import java.util.Collection;

import net.geco.app.ClassicAppBuilder;
import net.geco.control.GecoControl;
import net.geco.control.InlineTracer;
import net.geco.control.PenaltyChecker;
import net.geco.functions.LegNeutralizationFunction;
import net.geco.model.Category;
import net.geco.model.Course;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import test.net.geco.GecoFixtures;

/**
 * @author Simon Denier
 * @since Sep 18, 2011
 *
 */
public class LegNeutralizationFunctionTest {
	
	private POFactory factory;
	private Course courseA;

	@Before
	public void setUp() {
		factory = new POFactory();
		courseA = factory.createCourse();
		courseA.setName("A");
		courseA.setCodes(new int[]{ 42, 43, 45, 31, 45, 32 });
	}
	
	@Test
	public void shouldDetectLegInCourse() {
		assertFalse("should find no start control matching leg", courseA.hasLeg(100, 101));
		assertTrue("should find leg", courseA.hasLeg(45, 31));
		assertFalse("should find no end control matching leg", courseA.hasLeg(45, 101));
		assertFalse("should find no end control matching leg", courseA.hasLeg(32, 101));
		assertTrue("should find second leg", courseA.hasLeg(45, 32));
	}
	
	@Test
	public void shouldSelectCoursesWithNeutralizedLeg() {
		Course courseB = factory.createCourse();
		courseB.setName("B");
		courseB.setCodes(new int[]{ 42, 43, 31, 45, 32 });
		Course courseC = factory.createCourse();
		courseC.setName("C");
		courseC.setCodes(new int[]{ 31, 45, 32, 45, 31 });
		Registry registry = new Registry();
		registry.addCourse(courseA);
		registry.addCourse(courseB);
		registry.addCourse(courseC);
		
		GecoControl geco = GecoFixtures.mockGecoControlWithRegistry(registry);
		Mockito.when(geco.registry()).thenReturn(registry);
		LegNeutralizationFunction function = new LegNeutralizationFunction(geco);
		function.setNeutralizedLeg(45, 31);
		Collection<Course> courses = function.selectCoursesWithNeutralizedLeg();
		assertEquals("should select 2 out of 3 courses", 2, courses.size());
		assertTrue(courses.contains(courseA));
		assertTrue(courses.contains(courseC));
		assertFalse(courses.contains(courseB));
	}

	@Test
	public void testRunnerClearTrace() {
		PenaltyChecker checker = new PenaltyChecker(factory, new InlineTracer(factory));

		RunnerRaceData data = GecoFixtures.createRunnerData(courseA, null); 
		data.setPunches(new Punch[] { punch(42), punch(45), punch(31), punch(45), punch(32) });
		checker.check(data);
		assertEquals("42,-43,45,31,45,32", data.getResult().formatClearTrace());

		data.setPunches(new Punch[0]);
		data.setResult(factory.createRunnerResult());
		assertEquals("", data.getResult().formatClearTrace());
		
		data.setPunches(new Punch[] { punch(42), punch(43), punch(45), punch(45), punch(32) });
		checker.check(data);
		assertEquals("42,43,45,-31,45,32", data.getResult().formatClearTrace());
		
		data.setPunches(new Punch[] { punch(42), punch(45), punch(64), punch(31), punch(45), punch(32) });
		checker.check(data);
		assertEquals("42,-43,45,31,45,32", data.getResult().formatClearTrace());
		
		data.setPunches(new Punch[] { punch(42), punch(45), punch(64), punch(32) });
		checker.check(data);
		assertEquals("42,-43,45,-31,-45+64,32", data.getResult().formatClearTrace());
	}
	
	@Test
	public void shouldDetectRunnerWithLegToNeutralize() {
		Category cat = factory.createCategory();
		PenaltyChecker checker = new PenaltyChecker(factory, new InlineTracer(factory));

		RunnerRaceData dataSelected = GecoFixtures.createRunnerData(courseA, cat); 
		dataSelected.setPunches(new Punch[] { punch(42), punch(45), punch(31), punch(45), punch(32) });
		checker.check(dataSelected);
		assertTrue("runner with partial trace and matching leg should be accepted", dataSelected.hasLeg(45, 31) );
		
		RunnerRaceData dataRejected = GecoFixtures.createRunnerData(courseA, cat);
		dataRejected.setPunches(new Punch[0]);
		dataRejected.setResult(factory.createRunnerResult());
		assertFalse("runner without punch should be rejected", dataRejected.hasLeg(45, 31) );
		
		dataRejected.setPunches(new Punch[] { punch(42), punch(43), punch(45), punch(45), punch(32) });
		checker.check(dataSelected);
		assertFalse("runner with missing leg punch should be rejected", dataRejected.hasLeg(45, 31) );
		
		dataSelected.setPunches(new Punch[] { punch(42), punch(45), punch(64), punch(31), punch(45), punch(32) });
		checker.check(dataSelected);
		assertTrue("runner with added punch between matching leg should be accepted", dataSelected.hasLeg(45, 31) );
		
		dataRejected.setPunches(new Punch[] { punch(42), punch(45), punch(64), punch(32) });
		checker.check(dataRejected);
		System.out.println(dataRejected.getResult().formatTrace());
		assertFalse("runner with missing leg punch should be rejected", dataRejected.hasLeg(45, 32) );
	}
	
	@Test
	public void shouldSelectRunnersWithLegToNeutralize() {
		Category cat = factory.createCategory();
		RunnerRaceData dataSelected = GecoFixtures.createRunnerData(courseA, cat); 
		dataSelected.setPunches(new Punch[] { punch(42), punch(45), punch(31), punch(45), punch(32) });
		RunnerRaceData dataRejected = GecoFixtures.createRunnerData(courseA, cat); 
		dataRejected.setPunches(new Punch[] { punch(42), punch(43), punch(45), punch(45), punch(32) });
		
		Registry registry = new Registry();
		registry.addCourse(courseA);
		registry.addCategory(cat);
		registry.addRunnerWithoutId(dataSelected.getRunner());
		registry.addRunnerWithoutId(dataRejected.getRunner());
		
		GecoControl geco = GecoFixtures.mockGecoControlWithRegistry(registry);
		Mockito.when(geco.registry()).thenReturn(registry);
		LegNeutralizationFunction function = new LegNeutralizationFunction(geco);
		function.setNeutralizedLeg(45, 31);
		Collection<Runner> runners = function.selectRunnersWithLegToNeutralize(courseA);
		
		assertEquals(1, runners.size());
	}
	
	@Test
	public void testSetNeutralizedLeg() {
		
	}

	@Test
	public void testComputeOfficialTimeWithNeutralizedLegs() {
		GecoControl mullaghmeen = GecoFixtures.loadFixtures("testData/mullaghmeen", new ClassicAppBuilder());
		LegNeutralizationFunction function = new LegNeutralizationFunction(mullaghmeen);
		function.setNeutralizedLeg(156, 157);
		Collection<Course> courses = function.selectCoursesWithNeutralizedLeg();
		assertEquals(3, courses.size());
		assertTrue(courses.contains(mullaghmeen.registry().findCourse("Orange")));
		assertTrue(courses.contains(mullaghmeen.registry().findCourse("White")));
		assertTrue(courses.contains(mullaghmeen.registry().findCourse("Yellow")));		
	}

	@Test
	public void testResetNeutralizedLegs() {
		
	}
	
	@Test
	public void testResetAllOfficialTimes() {
		
	}

}
