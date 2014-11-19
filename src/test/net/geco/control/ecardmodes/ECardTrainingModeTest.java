/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import net.geco.control.ArchiveManager;
import net.geco.control.RunnerControl;
import net.geco.control.checking.Checker;
import net.geco.control.checking.PenaltyChecker;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.control.ecardmodes.ECardTrainingMode;
import net.geco.model.Course;
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

	@Mock private ArchiveManager archive;
	
	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		
		Checker checker = new PenaltyChecker(factory);
		RunnerControl runnerControl = new RunnerControl(gecoControl);
		when(gecoControl.checker()).thenReturn(checker);
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);
		when(gecoControl.getService(ArchiveManager.class)).thenReturn(archive);
		
		when(registry.getCourses()).thenReturn(Arrays.asList(new Course[]{ testCourse }));
		when(registry.noClub()).thenReturn(factory.createClub());
		when(registry.noCategory()).thenReturn(factory.createCategory());
		
		CourseDetector detector = new CourseDetector(gecoControl);
		ecardMode = new ECardTrainingMode(gecoControl, detector);
	}

	@Test
	public void handleRegisteredCallsChecker() {
		Checker mockChecker = mock(Checker.class);
		when(gecoControl.checker()).thenReturn(mockChecker);
		ecardMode.handleRegistered(fullRunnerData);
		verify(mockChecker).check(fullRunnerData);	
	}
	
	@Test
	public void handleRegisteredCallsAnnouncer() {
		Status oldStatus = fullRunnerData.getStatus();
		ecardMode.handleRegistered(fullRunnerData);
		verify(announcer).announceCardRead("999");
		verify(announcer).announceStatusChange(fullRunnerData, oldStatus);
	}
	
	@Test
	public void handleDuplicateCallsCopyRunner() {
		Runner mockRunner = mock(Runner.class);
		when(mockRunner.getEcard()).thenReturn("999");
		when(mockRunner.copyWith(anyInt(), anyString(), any(Course.class))).thenReturn(fullRunner);
		when(registry.nextStartId()).thenReturn(100);
		when(registry.getEcards()).thenReturn(new HashSet<String>(Arrays.asList(new String[]{ "999" })));
		ecardMode.handleDuplicate(danglingRunnerData, mockRunner);
		verify(mockRunner).copyWith(100, "999a", testCourse);
	}

	@Test
	public void handleDuplicateInsertsFromArchive() {
		ecardMode.toggleCopyDuplicate(false);
		Runner mockRunner = mock(Runner.class);
		when(mockRunner.getEcard()).thenReturn("999");
		when(archive.findAndBuildRunner("999")).thenReturn(fullRunner);
		ecardMode.handleDuplicate(danglingRunnerData, mockRunner);
		assertThat(danglingRunnerData.getRunner(), equalTo(fullRunner));
	}

	@Test
	public void handleDuplicateSetsResolvedStatus() {
		ecardMode.handleDuplicate(danglingRunnerData, fullRunner);
		assertTrue(danglingRunnerData.getStatus().isResolved());
	}

	@Test
	public void handleDuplicateCallsAnnouncer() {
		when(registry.getEcards()).thenReturn(new HashSet<String>(Arrays.asList(new String[]{ "999" })));
		ecardMode.handleDuplicate(danglingRunnerData, fullRunner);
		verify(announcer).announceCardRead("999a");
	}

	@Test
	public void handleUnregisteredInsertsFromArchive() {
		ecardMode.handleUnregistered(danglingRunnerData, "997");
		verify(archive).findAndBuildRunner("997");
	}

	@Test
	public void handleUnregisteredCallsAnnouncer() {
		when(archive.findAndBuildRunner("997")).thenReturn(fullRunnerData.getRunner());
		ecardMode.handleUnregistered(danglingRunnerData, "997");
		verify(announcer).announceCardRead("997");

		ecardMode.handleUnregistered(danglingRunnerData, "998");
		verify(announcer).announceCardRead("998");
	}
	
	@Test
	public void handleUnregisteredSetsResolvedStatus() {
		ecardMode.handleUnregistered(danglingRunnerData, "997");
		assertTrue(danglingRunnerData.getStatus().isResolved());
	}
	
}
