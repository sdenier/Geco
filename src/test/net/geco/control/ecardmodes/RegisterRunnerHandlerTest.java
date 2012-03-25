/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.control.ArchiveManager;
import net.geco.control.RunnerControl;
import net.geco.control.RunnerCreationException;
import net.geco.control.StageControl;
import net.geco.control.ecardmodes.RegisterRunnerHandler;
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
public class RegisterRunnerHandlerTest extends ECardModeSetup {

	private Course autoCourse;

	@Mock private ArchiveManager archive;
	@Mock private StageControl stageControl;
	@Mock private RunnerControl runnerControl;

	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		
		autoCourse = factory.createCourse();
		autoCourse.setName("[Auto]");
		autoCourse.setCodes(new int[0]);
		when(stageControl.getAutoCourse()).thenReturn(autoCourse);
		when(gecoControl.getService(ArchiveManager.class)).thenReturn(archive);
		when(gecoControl.getService(StageControl.class)).thenReturn(stageControl);
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);
	}

	private Runner dummyRunner(String ecard) {
		Runner runner = factory.createRunner();
		runner.setStartId(32);
		runner.setCourse(autoCourse);
		return runner;
	}
	
	@Test
	public void handleUnregisteredCallsArchiveManager() {
		when(archive.findAndCreateRunner("600", autoCourse)).thenReturn(dummyRunner("600"));
		new RegisterRunnerHandler(gecoControl).handleUnregistered(null, "600");
		verify(archive).findAndCreateRunner("600", autoCourse);
	}

	@Test
	public void handleUnregisteredRegistersRunnerFromArchiveIfFound() {
		Runner runner = dummyRunner("600");
		when(archive.findAndCreateRunner("600", autoCourse)).thenReturn(runner);
		ArgumentCaptor<RunnerRaceData> data = ArgumentCaptor.forClass(RunnerRaceData.class);
		new RegisterRunnerHandler(gecoControl).handleUnregistered(null, "600");
		
		verify(runnerControl).registerRunner(eq(runner), data.capture());
		assertEquals(autoCourse, runner.getCourse());
		assertEquals(Status.RUN, data.getValue().getStatus());
	}

	@Test
	public void handleUnregisteredCreatesNewRunner() {
		try {
			when(runnerControl.buildAnonymousRunner("600", autoCourse)).thenReturn(dummyRunner("600"));
		} catch (RunnerCreationException e) { fail(); }
		new RegisterRunnerHandler(gecoControl).handleUnregistered(null, "600");
		try {
			verify(runnerControl).buildAnonymousRunner("600", autoCourse);
		} catch (RunnerCreationException e) { fail(); }		
	}

	@Test
	public void handleUnregisteredRegisterNewRunner() {
		Runner newRunner = dummyRunner("600");
		ArgumentCaptor<RunnerRaceData> data = ArgumentCaptor.forClass(RunnerRaceData.class);
		try {
			when(runnerControl.buildAnonymousRunner("600", autoCourse)).thenReturn(newRunner);
		} catch (RunnerCreationException e) { fail(); }
		new RegisterRunnerHandler(gecoControl).handleUnregistered(null, "600");

		verify(runnerControl).registerRunner(eq(newRunner), data.capture());
		assertEquals(autoCourse, newRunner.getCourse());
		assertEquals(Status.RUN, data.getValue().getStatus());
	}

	@Test
	public void handleUnregisteredReturnsId() {
		when(archive.findAndCreateRunner("600", autoCourse)).thenReturn(dummyRunner("600"));
		String returnedEcard = new RegisterRunnerHandler(gecoControl).handleUnregistered(null, "600");
		assertEquals("600", returnedEcard);
	}

	@Test
	public void handleUnregisteredReturnsNullWhenCreationGoesWrong() {
		try {
			when(runnerControl.buildAnonymousRunner("600", autoCourse)).thenThrow(new RunnerCreationException("Error"));
		} catch (RunnerCreationException e) { fail(); }
		String returnedEcard = new RegisterRunnerHandler(gecoControl).handleUnregistered(null, "600");
		assertNull(returnedEcard);
	}
	
}
