/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import net.geco.basics.Announcer;
import net.geco.basics.GecoRequestHandler;
import net.geco.basics.TimeManager;
import net.geco.control.Checker;
import net.geco.control.ECardHandler;
import net.geco.control.GecoControl;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;
import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.PunchRecordData;
import org.martin.sireader.server.IResultData;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Simon Denier
 * @since Mar 12, 2012
 *
 */
public class ECardHandlerTest {

	private ECardHandler ecardHandler;
	private Factory factory = new POFactory();

	@Mock private GecoRequestHandler requestHandler;
	@Mock private GecoControl gecoControl;
	@Mock private Registry registry;
	@Mock private Checker checker;
	@Mock private Announcer announcer;
	
	@Mock private IResultData<PunchObject, PunchRecordData> card;
	private RunnerRaceData danglingRunnerData;
	private RunnerRaceData fullRunnerData;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		setUpMockRegistry(gecoControl, registry);
		when(gecoControl.factory()).thenReturn(factory);
		when(gecoControl.checker()).thenReturn(checker);
		when(gecoControl.announcer()).thenReturn(announcer);
		
		ecardHandler = new ECardHandler(gecoControl);
		ecardHandler.setRequestHandler(requestHandler);
		
		setUpCardPunches();
		danglingRunnerData = factory.createRunnerRaceData();
		fullRunnerData = factory.createRunnerRaceData();
		fullRunnerData.setRunner(createFullRunner());
		fullRunnerData.setResult(factory.createRunnerResult());
	}

	private void setUpMockRegistry(GecoControl control, Registry registry) {
		Stage stage = mock(Stage.class);
		when(stage.registry()).thenReturn(registry);
		when(control.stage()).thenReturn(stage);
	}
	
	private void setUpCardPunches() {
		ArrayList<PunchObject> punchArray = new ArrayList<PunchObject>();
		punchArray.add(new PunchObject(31, 2000));
		punchArray.get(0).evaluateTime(0);
		when(card.getPunches()).thenReturn(punchArray);
	}
	
	private Runner createFullRunner() {
		Runner runner = factory.createRunner();
		runner.setStartId(1);
		runner.setEcard("999");
		runner.setCourse(factory.createCourse());
		return runner;
	}

	private Runner setUpRegistryForRegisteredEcard(String ecardId) {
		Runner runner = fullRunnerData.getRunner();
		when(card.getSiIdent()).thenReturn(ecardId);
		when(registry.findRunnerByEcard(ecardId)).thenReturn(runner);
		when(registry.findRunnerData(runner)).thenReturn(fullRunnerData);
		return runner;
	}
	
	@Test
	public void handleEcardNominalCase() {
		setUpRegistryForRegisteredEcard("999");
		ecardHandler.handleECard(card);
		verify(announcer).announceCardRead("999");
	}

	@Test
	public void handleEcardRentedCase() {
		Runner runner = setUpRegistryForRegisteredEcard("999");
		runner.setRentedEcard(true);
		ecardHandler.handleECard(card);
		verify(announcer).announceRentedCard("999");
	}
	
	@Test
	public void handleEcardDuplicateCase() {
		Runner runner = setUpRegistryForRegisteredEcard("999");
		fullRunnerData.getResult().setStatus(Status.OK);
		ecardHandler.handleECard(card);
		verify(requestHandler).requestMergeExistingRunner(any(RunnerRaceData.class), eq(runner));
	}

	@Test
	public void handleEcardUnknownCase() {
		when(card.getSiIdent()).thenReturn("999");
		when(registry.findRunnerByEcard("999")).thenReturn(null);
		ecardHandler.handleECard(card);
		verify(requestHandler).requestMergeUnknownRunner(any(RunnerRaceData.class), eq("999"));
	}

	@Test
	public void handleFinishedCallsChecker() {
		ecardHandler.handleFinished(fullRunnerData, card);
		verify(checker).check(fullRunnerData);
	}

	@Test
	public void handleFinishedUpdatesPunches() {
		ecardHandler.handleFinished(fullRunnerData, card);
		assertEquals(card.getPunches().size(), fullRunnerData.getPunches().length);
	}
	
	@Test
	public void handleFinishedCallsAnnouncer() {
		Status oldStatus = fullRunnerData.getStatus();
		ecardHandler.handleFinished(fullRunnerData, card);
		verify(announcer).announceCardRead("999");
		verify(announcer).announceStatusChange(fullRunnerData, oldStatus);
	}
	
	@Test
	public void handleDuplicateRequestsMerge() {
		Runner runner = factory.createRunner();
		ecardHandler.handleDuplicate(card, "998", runner);
		verify(requestHandler).requestMergeExistingRunner(any(RunnerRaceData.class), eq(runner));
	}

	@Test
	public void handleDuplicateCallsAnnouncer() {
		when(requestHandler.requestMergeExistingRunner(any(RunnerRaceData.class), any(Runner.class))).thenReturn("998a");
		Runner runner = factory.createRunner();
		ecardHandler.handleDuplicate(card, "998", runner);
		verify(announcer).announceCardReadAgain("998a");
	}
	
	@Test
	public void handleUnknownRequestsMerge() {
		ecardHandler.handleUnknown(card, "997");
		verify(requestHandler).requestMergeUnknownRunner(any(RunnerRaceData.class), eq("997"));
	}

	@Test
	public void handleUnknownCallsAnnouncer() {
		when(requestHandler.requestMergeUnknownRunner(any(RunnerRaceData.class), anyString())).thenReturn("997");
		ecardHandler.handleUnknown(card, "997");
		verify(announcer).announceUnknownCardRead("997");
	}
	
	@Test
	public void updateRaceDataStampsReadtime() {
		RunnerRaceData mockData = mock(RunnerRaceData.class);
		ecardHandler.updateRaceDataWith(mockData, card);
		verify(mockData).stampReadtime();
	}

	@Test
	public void updateRaceDataWithEcardTimes() {
		when(card.getClearTime()).thenReturn(15000l);
		when(card.getCheckTime()).thenReturn(20000l);
		when(card.getStartTime()).thenReturn(25000l);
		when(card.getFinishTime()).thenReturn(30000l);
		ecardHandler.updateRaceDataWith(fullRunnerData, card);
		assertEquals(new Date(15000), fullRunnerData.getErasetime());
		assertEquals(new Date(20000), fullRunnerData.getControltime());
		assertEquals(new Date(25000), fullRunnerData.getStarttime());
		assertEquals(new Date(30000), fullRunnerData.getFinishtime());
	}
	
	@Test
	public void updateRaceDataWithPunches() {
		ecardHandler.updateRaceDataWith(fullRunnerData, card);
		Punch[] punches = fullRunnerData.getPunches();

		assertEquals(card.getPunches().size(), punches.length);
		assertEquals(31, punches[0].getCode());
		assertEquals(new Date(2000), punches[0].getTime());
	}
	
	@Test
	public void safeTime() {
		assertEquals(TimeManager.NO_TIME, ecardHandler.safeTime(PunchObject.INVALID));
		assertEquals(new Date(1000), ecardHandler.safeTime(1000));
	}

	@Test
	public void handleStarttime() {
		when(card.getStartTime()).thenReturn(1000l, PunchObject.INVALID);
		
		ecardHandler.handleStarttime(danglingRunnerData, card);
		assertEquals("Set Start time", new Date(1000), danglingRunnerData.getStarttime());
		
		ecardHandler.handleStarttime(danglingRunnerData, card);
		assertEquals("Set no time when invalid", TimeManager.NO_TIME, danglingRunnerData.getStarttime());
		verify(gecoControl, times(1)).log(anyString());
	}

	@Test
	public void handleStarttimeWithRegisteredStarttime() {
		when(card.getStartTime()).thenReturn(1000l, PunchObject.INVALID);

		Runner runner = fullRunnerData.getRunner();
		runner.setRegisteredStarttime(new Date(5));
		
		ecardHandler.handleStarttime(fullRunnerData, card);
		assertEquals("Set Start time even with registered start time",
							new Date(1000), fullRunnerData.getStarttime());
		
		ecardHandler.handleStarttime(fullRunnerData, card);
		assertEquals("Set no time when invalid even with registered start time",
							TimeManager.NO_TIME, fullRunnerData.getStarttime());
		verify(gecoControl, never()).log(anyString());

		runner.setRegisteredStarttime(TimeManager.NO_TIME);
		ecardHandler.handleStarttime(fullRunnerData, card);
		assertEquals("Set no time when invalid even with registered start time",
							TimeManager.NO_TIME, fullRunnerData.getStarttime());
		verify(gecoControl, times(1)).log(anyString());
	}
	
	@Test
	public void handleFinishtime() {
		when(card.getFinishTime()).thenReturn(1000l, PunchObject.INVALID);
		
		ecardHandler.handleFinishtime(fullRunnerData, card);
		assertEquals("Set Finish time", new Date(1000), fullRunnerData.getFinishtime());
		
		ecardHandler.handleFinishtime(fullRunnerData, card);
		assertEquals("Set no time when invalid", TimeManager.NO_TIME, fullRunnerData.getFinishtime());
	}
}
