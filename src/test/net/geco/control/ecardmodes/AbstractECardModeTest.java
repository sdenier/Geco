/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.control.ecardmodes.AbstractECardMode;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;
import net.gecosi.dataframe.SiDataFrame;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Mar 12, 2012
 *
 */
public class AbstractECardModeTest extends ECardModeSetup {

	public static class DummyECardHandler extends AbstractECardMode {
		private RunnerRaceData finishData;
		private RunnerRaceData duplicateData;
		private Runner runner;
		private RunnerRaceData unregisteredData;
		public DummyECardHandler(GecoControl gecoControl) {
			super(DummyECardHandler.class, gecoControl);
		}
		public void handleRegistered(RunnerRaceData runnerData) {
			this.finishData = runnerData;
		}
		public void handleDuplicate(RunnerRaceData runnerData, Runner runner) {
			this.duplicateData = runnerData;
			this.runner = runner;
		}
		public void handleUnregistered(RunnerRaceData runnerData, String cardId) {
			this.unregisteredData = runnerData;
		}
	}
	
	private DummyECardHandler ecardMode;
	
	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		ecardMode = new DummyECardHandler(gecoControl);
	}

	private Runner setUpRegistryForRegisteredEcard(String ecardId) {
		Runner runner = fullRunnerData.getRunner();
		when(card.getSiNumber()).thenReturn(ecardId);
		when(registry.findRunnerByEcard(ecardId)).thenReturn(runner);
		when(registry.findRunnerData(runner)).thenReturn(fullRunnerData);
		return runner;
	}
	
	@Test
	public void processECardNominalCase() {
		setUpRegistryForRegisteredEcard("999");
		ecardMode.processECard(card);
		assertTrue("Should retrieve data from registry", fullRunnerData == ecardMode.finishData);
		checkCardData(card, ecardMode.finishData);
	}

	@Test
	public void processECardRentedCase() {
		Runner runner = setUpRegistryForRegisteredEcard("999");
		runner.setRentedEcard(true);
		ecardMode.processECard(card);
		verify(announcer).announceRentedCard("999");
	}
	
	@Test
	public void processECardDuplicateCase() {
		Runner runner = setUpRegistryForRegisteredEcard("999");
		fullRunnerData.getResult().setStatus(Status.OK);
		ecardMode.processECard(card);
		assertTrue("Should create new data", fullRunnerData != ecardMode.duplicateData);
		assertTrue("Should receive existing runner", runner == ecardMode.runner);
		checkCardData(card, ecardMode.duplicateData);
	}

	@Test
	public void processECardUnregisteredCase() {
		when(card.getSiNumber()).thenReturn("999");
		when(registry.findRunnerByEcard("999")).thenReturn(null);
		ecardMode.processECard(card);
		checkCardData(card, ecardMode.unregisteredData);
	}

	@Test
	public void updateRaceDataStampsReadtime() {
		RunnerRaceData mockData = mock(RunnerRaceData.class);
		ecardMode.updateRaceDataWith(mockData, card);
		verify(mockData).stampReadtime();
	}

	@Test
	public void updateRaceDataProcessCardData() {
		ecardMode.updateRaceDataWith(fullRunnerData, card);
		checkCardData(card, fullRunnerData);
	}

	@Test
	public void createUnregisteredData() {
		RunnerRaceData unregisteredData = ecardMode.createUnregisteredData(card);
		checkCardData(card, unregisteredData);
	}
	
	@Test
	public void safeTime() {
		assertEquals(TimeManager.NO_TIME, ecardMode.safeTime(SiDataFrame.NO_TIME));
		assertEquals(new Date(1000), ecardMode.safeTime(1000));
	}

	@Test
	public void processStarttime() {
		RunnerRaceData unlinkedRunnerData = factory.createRunnerRaceData();
		when(card.getStartTime()).thenReturn(1000l, SiDataFrame.NO_TIME);
		
		ecardMode.processStarttime(unlinkedRunnerData, card);
		assertEquals("Set Start time", new Date(1000), unlinkedRunnerData.getStarttime());
		
		ecardMode.processStarttime(unlinkedRunnerData, card);
		assertEquals("Set no time when invalid", TimeManager.NO_TIME, unlinkedRunnerData.getStarttime());
	}

	@Test
	public void processStarttimeWithRegisteredStarttime() {
		when(card.getStartTime()).thenReturn(1000l, SiDataFrame.NO_TIME);

		Runner runner = fullRunnerData.getRunner();
		runner.setRegisteredStarttime(new Date(5));
		
		ecardMode.processStarttime(fullRunnerData, card);
		assertEquals("Set Start time even with registered start time",
							new Date(1000), fullRunnerData.getStarttime());
		
		ecardMode.processStarttime(fullRunnerData, card);
		assertEquals("Set no time when invalid even with registered start time",
							TimeManager.NO_TIME, fullRunnerData.getStarttime());
		verify(gecoControl, never()).log(anyString());

		runner.setRegisteredStarttime(TimeManager.NO_TIME);
		ecardMode.processStarttime(fullRunnerData, card);
		assertEquals("Set no time when invalid even with registered start time",
							TimeManager.NO_TIME, fullRunnerData.getStarttime());
	}
	
	@Test
	public void processFinishtime() {
		when(card.getFinishTime()).thenReturn(1000l, SiDataFrame.NO_TIME);
		
		ecardMode.processFinishtime(fullRunnerData, card);
		assertEquals("Set Finish time", new Date(1000), fullRunnerData.getFinishtime());
		
		ecardMode.processFinishtime(fullRunnerData, card);
		assertEquals("Set no time when invalid", TimeManager.NO_TIME, fullRunnerData.getFinishtime());
	}
}
