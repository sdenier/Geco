/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import net.geco.control.Checker;
import net.geco.control.PenaltyChecker;
import net.geco.control.RunnerControl;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.control.ecardmodes.ECardRacingMode;
import net.geco.model.Course;
import net.geco.model.Status;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Mar 12, 2012
 *
 */
public class ECardRacingModeTest extends ECardModeSetup {

	private ECardRacingMode ecardMode;

	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		
		Checker checker = new PenaltyChecker(factory);
		RunnerControl runnerControl = new RunnerControl(gecoControl);
		CourseDetector detector = new CourseDetector(gecoControl, runnerControl);
		when(gecoControl.checker()).thenReturn(checker);
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);

		when(registry.getCourses()).thenReturn(Arrays.asList(new Course[]{ testCourse }));
		when(registry.noClub()).thenReturn(factory.createClub());
		when(registry.noCategory()).thenReturn(factory.createCategory());
		
		ecardMode = new ECardRacingMode(gecoControl, detector);
	}

	@Test
	public void handleFinishedCallsChecker() {
		Checker mockChecker = mock(Checker.class);
		when(gecoControl.checker()).thenReturn(mockChecker);
		ecardMode.handleFinished(fullRunnerData);
		verify(mockChecker).check(fullRunnerData);
	}

	@Test
	public void handleFinishedCallsAnnouncer() {
		Status oldStatus = fullRunnerData.getStatus();
		ecardMode.handleFinished(fullRunnerData);
		verify(announcer).announceCardRead("999");
		verify(announcer).announceStatusChange(fullRunnerData, oldStatus);
	}
	
	@Test
	public void handleDuplicateSetsDuplicateStatus() {
		ecardMode.handleDuplicate(danglingRunnerData, fullRunnerData.getRunner());
		assertEquals(Status.DUP, danglingRunnerData.getStatus());
	}
	
	@Test
	public void handleDuplicateCallsAnnouncer() {
		when(registry.getEcards()).thenReturn(new HashSet<String>(Arrays.asList(new String[]{ "999" })));
		ecardMode.handleDuplicate(danglingRunnerData, fullRunnerData.getRunner());
		verify(announcer).announceCardReadAgain("999a");
	}
	
	@Test
	public void handleUnregisteredInsertsFromArchive() {
		ecardMode.handleUnregistered(danglingRunnerData, "997");
		assertEquals(Status.DUP, danglingRunnerData.getStatus());
	}
	
	@Test
	public void handleUnregisteredCallsAnnouncer() {
		ecardMode.handleUnregistered(danglingRunnerData, "997");
		verify(announcer).announceUnknownCardRead("997");
	}
	
	@Test
	public void handleUnregisteredSetsUnkownStatusOtherwise() {
		ecardMode.handleUnregistered(danglingRunnerData, "997");
		assertEquals(Status.UNK, danglingRunnerData.getStatus());
	}
	
}
