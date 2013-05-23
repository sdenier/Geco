/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.net.geco.GecoFixtures.createCourse;
import static test.net.geco.GecoFixtures.punch;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import net.geco.app.ClassicAppBuilder;
import net.geco.basics.Announcer;
import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.control.InlineTracer;
import net.geco.control.PenaltyChecker;
import net.geco.control.results.ResultBuilder;
import net.geco.functions.LegNeutralizationFunction;
import net.geco.model.Category;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.Result;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Trace;
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
		Messages.put("ui", "net.geco.ui.messages"); //$NON-NLS-1$ //$NON-NLS-2$
		factory = new POFactory();
		courseA = createCourse("A", 42, 43, 45, 31, 45, 32);
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
		Course courseB = createCourse("B", 42, 43, 31, 45, 32);
		Course courseC = createCourse("C", 31, 45, 32, 45, 31);
		Registry registry = new Registry();
		registry.addCourse(courseA);
		registry.addCourse(courseB);
		registry.addCourse(courseC);
		
		GecoControl geco = GecoFixtures.mockGecoControlWithRegistry(registry);
		when(geco.announcer()).thenReturn(new Announcer());
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
		Trace[] leg = dataSelected.retrieveLeg(45, 31);
		assertNotNull("runner with partial trace and matching leg should be accepted", leg );
		assertEquals(Integer.toString(45), leg[0].getCode());
		assertEquals(Integer.toString(31), leg[1].getCode());
		
		RunnerRaceData dataRejected = GecoFixtures.createRunnerData(courseA, cat);
		dataRejected.setPunches(new Punch[0]);
		dataRejected.setResult(factory.createRunnerResult());
		assertNull("runner without punch should be rejected", dataRejected.retrieveLeg(45, 31) );
		
		dataRejected.setPunches(new Punch[] { punch(42), punch(43), punch(45), punch(45), punch(32) });
		checker.check(dataSelected);
		assertNull("runner with missing leg punch should be rejected", dataRejected.retrieveLeg(45, 31) );
		
		dataSelected.setPunches(new Punch[] { punch(42), punch(45), punch(64), punch(31), punch(45), punch(32) });
		checker.check(dataSelected);
		leg = dataSelected.retrieveLeg(45, 31);
		assertNotNull("runner with added punch between matching leg should be accepted", leg );
		assertEquals(Integer.toString(45), leg[0].getCode());
		assertEquals(Integer.toString(31), leg[1].getCode());
		
		dataRejected.setPunches(new Punch[] { punch(42), punch(45), punch(64), punch(32) });
		checker.check(dataRejected);
		assertNull("runner with missing leg punch should be rejected", dataRejected.retrieveLeg(45, 32) );
	}
	
	@Test
	public void shouldSetNeutralizedLegAndSubtractNeutralizedTimeFromOfficialTime(){
		RunnerResult result = factory.createRunnerResult();
		result.setRacetime(100000);
		Trace endTrace = factory.createTrace("32", new Date(30000));
		RunnerRaceData raceData = mock(RunnerRaceData.class);
		when(raceData.getResult()).thenReturn(result);
		Runner runner = mock(Runner.class);
		when(raceData.getRunner()).thenReturn(runner);
		when(raceData.retrieveLeg(anyInt(), anyInt())).thenReturn(
				new Trace[]{ factory.createTrace("", new Date(20000)), endTrace });

		GecoControl geco = mock(GecoControl.class);
		Mockito.doNothing().when(geco).log(Mockito.anyString());
		LegNeutralizationFunction function = new LegNeutralizationFunction(geco);
		function.setNeutralizedLeg(31, 32);
		function.neutralizeLeg(raceData, false);
		assertEquals("should substract leg time from official time", 90000, result.getRacetime());
		assertTrue(endTrace.isNeutralized());
		
		function.neutralizeLeg(raceData, false);
		assertEquals("should not change official time again once leg is neutralized", 90000, result.getRacetime());
		assertTrue(endTrace.isNeutralized());
		
		endTrace.setNeutralized(false);
		function.neutralizeLeg(raceData, false);
		assertEquals(80000, result.getRacetime());
		assertTrue(endTrace.isNeutralized());	
	}

	@Test
	public void shouldNotNeutralizeLegWithNoTime(){
		RunnerResult result = factory.createRunnerResult();
		result.setRacetime(100000);
		Trace endTrace = factory.createTrace("32", new Date(10000));
		RunnerRaceData raceData = mock(RunnerRaceData.class);
		when(raceData.getResult()).thenReturn(result);
		when(raceData.retrieveLeg(anyInt(), anyInt())).thenReturn(
				new Trace[]{ factory.createTrace("", new Date(20000)), endTrace });

		LegNeutralizationFunction function = new LegNeutralizationFunction(null);
		function.setNeutralizedLeg(31, 32);
		function.neutralizeLeg(raceData, false);
		assertEquals("should not neutralize leg when split has no time", 100000, result.getRacetime());
		assertFalse(endTrace.isNeutralized());
	}
	
	@Test
	public void shouldNotChangeRacetimeWhenMissingLeg(){
		RunnerResult result = factory.createRunnerResult();
		result.setRacetime(100000);
		RunnerRaceData raceData = mock(RunnerRaceData.class);
		when(raceData.retrieveLeg(anyInt(), anyInt())).thenReturn(null);

		LegNeutralizationFunction function = new LegNeutralizationFunction(null);
		function.setNeutralizedLeg(31, 32);
		function.neutralizeLeg(raceData, false);
		assertEquals(100000, result.getRacetime());
	}

	@Test
	public void shouldNotChangeRacetimeWithNoTime(){
		RunnerResult result = factory.createRunnerResult();
		result.setRacetime(TimeManager.NO_TIME_l);
		Trace endTrace = factory.createTrace("32", new Date(40000));
		RunnerRaceData raceData = mock(RunnerRaceData.class);
		when(raceData.getResult()).thenReturn(result);
		when(raceData.retrieveLeg(anyInt(), anyInt())).thenReturn(
				new Trace[]{ factory.createTrace("", new Date(20000)), endTrace });

		LegNeutralizationFunction function = new LegNeutralizationFunction(null);
		function.setNeutralizedLeg(31, 32);
		function.neutralizeLeg(raceData, false);
		assertEquals(TimeManager.NO_TIME_l, result.getRacetime());
		assertTrue(endTrace.isNeutralized());
	}
	
	@Test
	public void testSimulateLegNeutralization(){
		RunnerResult result = factory.createRunnerResult();
		result.setRacetime(100000);
		Trace endTrace = factory.createTrace("32", new Date(30000));
		RunnerRaceData raceData = mock(RunnerRaceData.class);
		when(raceData.getResult()).thenReturn(result);
		Runner runner = mock(Runner.class);
		when(runner.idString()).thenReturn("Doe");
		when(raceData.getRunner()).thenReturn(runner);
		when(raceData.retrieveLeg(anyInt(), anyInt())).thenReturn(
				new Trace[]{ factory.createTrace("", new Date(20000)), endTrace });

		GecoControl geco = mock(GecoControl.class);
		Mockito.doNothing().when(geco).log(Mockito.anyString());
		Announcer announcer = mock(Announcer.class);
		when(geco.announcer()).thenReturn(announcer);
		
		LegNeutralizationFunction function = new LegNeutralizationFunction(geco);
		function.setNeutralizedLeg(31, 32);
		function.neutralizeLeg(raceData, true);
		Mockito.verify(announcer).dataInfo("Doe - split 0:10");
		assertEquals("should not change official time", 100000, result.getRacetime());
		assertFalse(endTrace.isNeutralized());
	}

	@Test
	public void testComputeRaceTimeWithNeutralizedLegs() {
		GecoControl mullaghmeen = GecoFixtures.loadFixtures("testData/mullaghmeen", new ClassicAppBuilder());
		Registry registry = mullaghmeen.registry();
		Course orange = registry.findCourse("Orange");
		LegNeutralizationFunction function = new LegNeutralizationFunction(mullaghmeen);
		function.getParametersConfig(); // init simulateCB
		function.setNeutralizedLeg(156, 157);
		
		Collection<Course> courses = function.selectCoursesWithNeutralizedLeg();
		assertEquals(3, courses.size());
		assertTrue(courses.contains(orange));
		assertTrue(courses.contains(registry.findCourse("White")));
		assertTrue(courses.contains(registry.findCourse("Yellow")));
		
		function.execute();
		assertEquals("17:28", registry.findRunnerData("51009").getResult().formatRacetime()); // Caoimhe O'Boyle
		assertEquals("26:40", registry.findRunnerData("11428").getResult().formatRacetime()); // Claire Garvey
		assertEquals("26:26", registry.findRunnerData("11444").getResult().formatRacetime()); // Aaron Clogher & Eoin Connell
		assertEquals("12:00:06", registry.findRunnerData("11477").getResult().formatRacetime()); // Laoise Brennan
		assertEquals("59:36", registry.findRunnerData("11476").getResult().formatRacetime()); // Laura Murphy

		// changed result
		Result result = new ResultBuilder(mullaghmeen).buildResultForCourse(orange);
		assertEquals("Aaron Clogher & Eoin Connell", result.getRankedRunners().get(8).getRunner().getName());
		assertEquals("Sean Kearns", result.getRankedRunners().get(9).getRunner().getName());
		assertEquals("Claire Garvey", result.getRankedRunners().get(10).getRunner().getName());
	}

	@Test
	public void testResetAllOfficialTimes() {
		RunnerResult result = factory.createRunnerResult();
		result.setRacetime(100000);
		result.setNbMPs(1);
		Trace trace = factory.createTrace("34", new Date(130000));
		trace.setNeutralized(true);
		result.setTrace(new Trace[]{ trace });
		RunnerRaceData raceData = factory.createRunnerRaceData();
		raceData.setStarttime(new Date(30000));
		raceData.setFinishtime(new Date(140000));
		raceData.setResult(result);
		
		Registry registry = mock(Registry.class);
		when(registry.getRunnersData()).thenReturn(Arrays.asList(raceData));
		GecoControl geco = GecoFixtures.mockGecoControlWithRegistry(registry);
		Mockito.doNothing().when(geco).log(Mockito.anyString());
		PenaltyChecker checker = new PenaltyChecker(factory);
		checker.setMPPenalty(15000);
		when(geco.checker()).thenReturn(checker);
		
		LegNeutralizationFunction function = new LegNeutralizationFunction(geco);
		function.resetAllOfficialTimes();
		assertEquals("should reset official time to original time", 125000, result.getRacetime());
		assertFalse("should reset neutralized leg", trace.isNeutralized());
	}
	
}
