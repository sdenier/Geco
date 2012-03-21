/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.basics.GecoRequestHandler;
import net.geco.control.Checker;
import net.geco.control.ecardmodes.ECardRacingMode;
import net.geco.model.Runner;
import net.geco.model.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Simon Denier
 * @since Mar 12, 2012
 *
 */
public class ECardRacingModeTest extends ECardModeSetup {

	private ECardRacingMode ecardMode;

	@Mock private GecoRequestHandler requestHandler;
	@Mock private Checker checker;
	
	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		when(gecoControl.checker()).thenReturn(checker);
		
		ecardMode = new ECardRacingMode(gecoControl);
		ecardMode.setRequestHandler(requestHandler);
	}

	@Test
	public void handleFinishedCallsChecker() {
		ecardMode.handleFinished(fullRunnerData);
		verify(checker).check(fullRunnerData);
	}

	@Test
	public void handleFinishedCallsAnnouncer() {
		Status oldStatus = fullRunnerData.getStatus();
		ecardMode.handleFinished(fullRunnerData);
		verify(announcer).announceCardRead("999");
		verify(announcer).announceStatusChange(fullRunnerData, oldStatus);
	}
	
	@Test
	public void handleDuplicateRequestsMerge() {
		Runner runner = factory.createRunner();
		ecardMode.handleDuplicate(danglingRunnerData, runner);
		verify(requestHandler).requestMergeExistingRunner(danglingRunnerData, runner);
	}

	@Test
	public void handleDuplicateSetsDuplicateStatus() {
		Runner runner = factory.createRunner();
		ecardMode.handleDuplicate(danglingRunnerData, runner);
		assertEquals(Status.DUP, danglingRunnerData.getStatus());
	}
	
	@Test
	public void handleDuplicateCallsAnnouncer() {
		Runner runner = factory.createRunner();
		when(requestHandler.requestMergeExistingRunner(danglingRunnerData, runner)).thenReturn("998a");
		ecardMode.handleDuplicate(danglingRunnerData, runner);
		verify(announcer).announceCardReadAgain("998a");
	}
	
	@Test
	public void handleUnregisteredRequestsMerge() {
		ecardMode.handleUnregistered(danglingRunnerData, "997");
		verify(requestHandler).requestMergeUnknownRunner(danglingRunnerData, "997");
	}

	@Test
	public void handleUnregisteredCallsAnnouncer() {
		when(requestHandler.requestMergeUnknownRunner(danglingRunnerData, "997")).thenReturn("997");
		ecardMode.handleUnregistered(danglingRunnerData, "997");
		verify(announcer).announceUnknownCardRead("997");
	}
	
}
