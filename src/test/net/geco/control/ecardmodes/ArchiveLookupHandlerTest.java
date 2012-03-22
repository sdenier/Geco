/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.control.ArchiveManager;
import net.geco.control.RunnerControl;
import net.geco.control.RunnerCreationException;
import net.geco.control.ecardmodes.ArchiveLookupHandler;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.model.Runner;
import net.geco.model.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Simon Denier
 * @since Mar 22, 2012
 *
 */
public class ArchiveLookupHandlerTest extends ECardModeSetup {

	@Mock private ArchiveManager archive;
	@Mock private CourseDetector detector;
	@Mock private RunnerControl runnerControl;

	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		when(gecoControl.getService(ArchiveManager.class)).thenReturn(archive);
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);
		when(detector.detectCourse(fullRunnerData)).thenReturn(testCourse);
//		
//		runner = factory.createRunner();
//		runner.setEcard("500");
	}

	@Test
	public void handleUnregisteredCallsDetector() {
		new ArchiveLookupHandler(gecoControl, detector).handleUnregistered(fullRunnerData, "2000");
		verify(detector).detectCourse(fullRunnerData);
	}

	@Test
	public void handleUnregisteredCallsArchiveManager() {
		new ArchiveLookupHandler(gecoControl, detector).handleUnregistered(fullRunnerData, "2000");
		verify(archive).findAndCreateRunner("2000", testCourse);
	}

	@Test
	public void handleUnregisteredRegistersRunnerFromArchiveIfFound() {
		Runner runner = factory.createRunner();
		when(archive.findAndCreateRunner("2000", testCourse)).thenReturn(runner);
		new ArchiveLookupHandler(gecoControl, detector).handleUnregistered(fullRunnerData, "2000");
		verify(runnerControl).registerRunner(runner, fullRunnerData);
	}

	@Test
	public void foundInArchive() {
		Runner runner = factory.createRunner();
		ArchiveLookupHandler handler = new ArchiveLookupHandler(gecoControl, detector);
		when(archive.findAndCreateRunner("2000", testCourse)).thenReturn(runner, (Runner) null);
		
		handler.handleUnregistered(fullRunnerData, "2000");
		assertTrue(handler.foundInArchive());
		
		handler.handleUnregistered(fullRunnerData, "2000");
		assertFalse(handler.foundInArchive());
	}

	@Test
	public void handleUnregisteredSetsCustomStatusOtherwise() {
		new ArchiveLookupHandler(gecoControl, detector).handleUnregistered(fullRunnerData, "2000");
		assertEquals(Status.UNK, fullRunnerData.getStatus());
	}

	@Test
	public void handleUnregisteredCreatesNewRunner() {
		new ArchiveLookupHandler(gecoControl, detector).handleUnregistered(fullRunnerData, "2000");
		try {
			verify(runnerControl).buildAnonymousRunner("2000", testCourse);
		} catch (RunnerCreationException e) { fail(); }		
	}

	@Test
	public void handleUnregisteredRegisterNewRunner() {
		Runner newRunner = factory.createRunner();
		try {
			when(runnerControl.buildAnonymousRunner("2000", testCourse)).thenReturn(newRunner);
		} catch (RunnerCreationException e) { fail(); }
		new ArchiveLookupHandler(gecoControl, detector).handleUnregistered(fullRunnerData, "2000");
		verify(runnerControl).registerRunner(newRunner, fullRunnerData);
	}
	
	@Test
	public void handleUnregisteredReturnsId() {
		String returnedEcard = new ArchiveLookupHandler(gecoControl, detector).handleUnregistered(fullRunnerData, "2000");
		assertEquals("2000", returnedEcard);
	}

	@Test
	public void handleUnregisteredReturnsNullWhenCreationGoesWrong() {
		try {
			when(runnerControl.buildAnonymousRunner("2000", testCourse)).thenThrow(new RunnerCreationException("Error"));
		} catch (RunnerCreationException e) { fail(); }
		String returnedEcard = new ArchiveLookupHandler(gecoControl, detector).handleUnregistered(fullRunnerData, "2000");
		assertNull(returnedEcard);
	}

}
