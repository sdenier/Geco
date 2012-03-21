/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.control.Checker;
import net.geco.control.ECardTrainingMode;
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
public class ECardTrainingModeTest extends ECardModeSetup {

	private ECardTrainingMode ecardMode;

	@Mock private Checker checker;
	
	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		when(gecoControl.checker()).thenReturn(checker);
		
		ecardMode = new ECardTrainingMode(gecoControl);
	}

	@Test
	public void handleFinishedCallsChecker() {
		ecardMode.handleFinished(fullRunnerData);
		fail();
//		verify(checker).check(fullRunnerData);
	}
	
	@Test
	public void handleFinishedCallsAnnouncer() {
		Status oldStatus = fullRunnerData.getStatus();
		ecardMode.handleFinished(fullRunnerData);
		verify(announcer).announceCardRead("999");
		verify(announcer).announceStatusChange(fullRunnerData, oldStatus);
	}
	
	@Test
	public void handleDuplicateCallsCopyRunner() {
		Runner runner = factory.createRunner();
		ecardMode.handleDuplicate(danglingRunnerData, "998", runner);
		fail();
	}

	@Test
	public void handleDuplicateCallsAnnouncer() {
		Runner runner = factory.createRunner();
		ecardMode.handleDuplicate(danglingRunnerData, "998", runner);
		verify(announcer).announceCardReadAgain("998a");
	}
	
	@Test
	public void handleUnknownCallsAutoInsert() {
		ecardMode.handleUnknown(danglingRunnerData, "997");
		fail();
	}

	@Test
	public void handleUnknownCallsAnnouncer() {
		ecardMode.handleUnknown(danglingRunnerData, "997");
		verify(announcer).announceUnknownCardRead("997");
	}
	
}
