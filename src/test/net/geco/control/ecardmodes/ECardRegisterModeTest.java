/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.control.ArchiveManager;
import net.geco.control.RunnerControl;
import net.geco.control.ecardmodes.ECardRegisterMode;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/**
 * @author Simon Denier
 * @since Mar 24, 2012
 *
 */
public class ECardRegisterModeTest extends ECardModeSetup {

	private Course autoCourse;

	@Mock
	private ArchiveManager archive;

	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
	
		autoCourse = factory.createCourse();
		when(registry.autoCourse()).thenReturn(autoCourse);
		RunnerControl runnerControl = new RunnerControl(gecoControl);
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);
		when(gecoControl.getService(ArchiveManager.class)).thenReturn(archive);
		
		when(registry.noClub()).thenReturn(factory.createClub());
		when(registry.noCategory()).thenReturn(factory.createCategory());
	}
	
	@Test
	public void handleRegisteredCallsAnnouncer() {
		new ECardRegisterMode(gecoControl).handleRegistered(fullRunnerData);
		verify(announcer).announceCardRegistered(fullRunnerData.getRunner().getEcard());
	}

	@Test
	public void handleDuplicateCallsAnnouncer() {
		new ECardRegisterMode(gecoControl).handleDuplicate(null, fullRunner);
		verify(announcer).announceCardRegistered(fullRunner.getEcard());
	}

	@Test
	public void handleUnregisteredInsertsFromArchive() {
		new ECardRegisterMode(gecoControl).handleUnregistered(null, "600");
		verify(archive).findAndBuildRunner("600");
	}
	
	@Test
	public void handleUnregisteredSetsAutoCourseAndRunningStatus() {
		RunnerControl mockControl = mock(RunnerControl.class);
		when(gecoControl.getService(RunnerControl.class)).thenReturn(mockControl);
		Runner runner = factory.createRunner();
		runner.setStartId(32);
		runner.setCourse(autoCourse);
		when(archive.findAndBuildRunner("600")).thenReturn(runner);
		new ECardRegisterMode(gecoControl).handleUnregistered(null, "600");
		
		ArgumentCaptor<RunnerRaceData> runnerData = ArgumentCaptor.forClass(RunnerRaceData.class);
		verify(mockControl).registerRunner(any(Runner.class), runnerData.capture());
		assertEquals(autoCourse, runner.getCourse());
		assertEquals(Status.RUN, runnerData.getValue().getStatus());
	}

	@Test
	public void handleUnregisteredCallsAnnouncer() {
		new ECardRegisterMode(gecoControl).handleUnregistered(null, "600");
		verify(announcer).announceCardRegistered("600");		
	}

}
