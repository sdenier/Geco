/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.functions;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import net.geco.control.ArchiveManager;
import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.control.functions.StationLogChecker;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import test.net.geco.testfactory.MockControls;
import test.net.geco.testfactory.RunnerFactory;

/**
 * @author Simon Denier
 * @since May 24, 2012
 *
 */
public class StationLogCheckerTest {

	private GecoControl geco;

	@Mock
	private Registry registry;

	@Mock
	private RunnerControl runnerControl;
	
	protected StationLogChecker subject() {
		return new StationLogChecker(geco, false);
	}
	
	protected StationLogChecker subjectForSimulation() {
		return new StationLogChecker(geco, true);
	}	
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		geco = MockControls.mockGecoControlWithRegistry(registry);
		when(geco.getService(RunnerControl.class)).thenReturn(runnerControl);
	}
	
	private void setUpRegistry(RunnerRaceData runnerData) {
		Runner runner = runnerData.getRunner();
		when(registry.findRunnerByEcard(runner.getEcard())).thenReturn(runner);
		when(registry.findRunnerData(runner)).thenReturn(runnerData);
	}
	
	@Test
	public void checkECardStatus_dontChangeFinishedEntry() {
		RunnerRaceData runnerData = RunnerFactory.createWithStatus("1000000", Status.OK);
		setUpRegistry(runnerData);
		subject().checkECardStatus("1000000", false);
		verify(runnerControl, never()).validateStatus(runnerData, Status.RUN);
	}

	@Test
	public void checkECardStatus_dontChangeNotStartedEntriesNotInLog() {
		RunnerRaceData runnerData = RunnerFactory.createWithStatus("5000", Status.NOS);
		setUpRegistry(runnerData);
		when(registry.findRunnerByEcard("1000000")).thenReturn(null);
		subject().checkECardStatus("1000000", false);
		verify(runnerControl, never()).validateStatus(runnerData, Status.RUN);
	}
	
	@Test
	public void checkECardStatus_setNotStartedEntryToRunning() {
		RunnerRaceData runnerData = RunnerFactory.createWithStatus("1000000", Status.NOS);
		setUpRegistry(runnerData);
		subject().checkECardStatus("1000000", false);
		verify(runnerControl).validateStatus(runnerData, Status.RUN);
	}
	
	@Test
	public void checkECardStatus_reportInconsistentDNSEntryFoundInLog() {
		RunnerRaceData runnerData = RunnerFactory.createWithStatus("1000000", Status.DNS);
		setUpRegistry(runnerData);
		subject().checkECardStatus("1000000", false);
		verify(geco).log("WARNING: " + runnerData.getRunner().idString() + " found in running log, but set as DNS in registry");
	}

	@Test
	public void checkECardStatus_reportUnregisteredECardFoundInLog() {
		when(registry.findRunnerByEcard("1000000")).thenReturn(null);
		subject().checkECardStatus("1000000", false);
		verify(geco.announcer()).dataInfo("WARNING: ecard 1000000 is unregistered, yet found in running log");
		verify(runnerControl, never()).registerRunner(any(Runner.class), any(RunnerRaceData.class));
	}
	@Test
	public void checkECardStatus_registerUnregisteredECardOnDemand() {
		ArchiveManager archive = mock(ArchiveManager.class);
		Runner runner = mock(Runner.class);
		when(archive.findAndCreateRunner("1000000")).thenReturn(runner);
		when(geco.getService(ArchiveManager.class)).thenReturn(archive);
		when(registry.findRunnerByEcard("1000000")).thenReturn(null);
		subject().checkECardStatus("1000000", true);
		verify(runnerControl).registerRunner(any(Runner.class), any(RunnerRaceData.class));
	}

	@Test
	public void markNotStartedEntriesAsDNS() {
		RunnerRaceData okRunner = RunnerFactory.createWithStatus("1", Status.OK);
		RunnerRaceData dnsRunner = RunnerFactory.createWithStatus("2", Status.NOS);
		when(registry.getRunnersData()).thenReturn(Arrays.asList(new RunnerRaceData[]{okRunner, dnsRunner}));
		subject().markNotStartedEntriesAsDNS(Collections.<String> emptySet());
		verify(runnerControl).validateStatus(dnsRunner, Status.DNS);
		verify(runnerControl, never()).validateStatus(okRunner, Status.DNS);
	}
	
	@Test
	public void checkECardStatus_simulationDontChangeStatus() {
		RunnerRaceData runnerData = RunnerFactory.createWithStatus("1000000", Status.NOS);
		setUpRegistry(runnerData);
		subjectForSimulation().checkECardStatus("1000000", false);
		verify(runnerControl, never()).validateStatus(runnerData, Status.RUN);
	}

	@Test
	public void checkECardStatus_simulationDontRegisterNewECard() {
		when(registry.findRunnerByEcard("1000000")).thenReturn(null);
		subjectForSimulation().checkECardStatus("1000000", true);
		verify(runnerControl, never()).registerRunner(any(Runner.class), any(RunnerRaceData.class));
	}

	@Test
	public void markNotStartedEntriesAsDNS_simulationDontChangeStatus() {
		RunnerRaceData runnerData = RunnerFactory.createWithStatus("1000000", Status.NOS);
		when(registry.getRunnersData()).thenReturn(Arrays.asList(new RunnerRaceData[]{runnerData}));
		subjectForSimulation().markNotStartedEntriesAsDNS(Collections.<String> emptySet());
		verify(runnerControl, never()).validateStatus(runnerData, Status.DNS);
	}
	
}
