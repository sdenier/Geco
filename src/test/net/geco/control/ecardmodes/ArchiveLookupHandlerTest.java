/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.control.ArchiveManager;
import net.geco.control.RunnerControl;
import net.geco.control.ecardmodes.ArchiveLookupHandler;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.control.ecardmodes.ECardHandler;
import net.geco.model.Runner;

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
	@Mock private ECardHandler fallbackHandler;

	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		when(gecoControl.getService(ArchiveManager.class)).thenReturn(archive);
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);
		when(detector.detectCourse(fullRunnerData, fullRunner.getCategory())).thenReturn(testCourse);
	}

	protected ArchiveLookupHandler subject() {
		return new ArchiveLookupHandler(gecoControl, detector, fallbackHandler);
	}

	@Test
	public void handleUnregisteredCallsArchiveManager() {
		subject().handleUnregistered(fullRunnerData, "2000");
		verify(archive).findAndBuildRunner("2000");
	}

	@Test
	public void handleUnregisteredCallsCourseDetector() {
		when(archive.findAndBuildRunner("2000")).thenReturn(fullRunner);
		subject().handleUnregistered(fullRunnerData, "2000");
		verify(detector).detectCourse(fullRunnerData, fullRunner.getCategory());
	}

	@Test
	public void handleUnregisteredRegistersRunnerFromArchiveIfFound() {
		Runner runner = factory.createRunner();
		when(archive.findAndBuildRunner("2000")).thenReturn(runner);
		subject().handleUnregistered(fullRunnerData, "2000");
		verify(runnerControl).registerRunner(runner, fullRunnerData);
	}

	@Test
	public void handleUnregisteredReturnsId() {
		when(archive.findAndBuildRunner("2000")).thenReturn(factory.createRunner());
		String returnedEcard = subject().handleUnregistered(fullRunnerData, "2000");
		assertEquals("2000", returnedEcard);
	}

	@Test
	public void foundInArchive() {
		Runner runner = factory.createRunner();
		ArchiveLookupHandler handler = subject();
		when(archive.findAndBuildRunner("2000")).thenReturn(runner, (Runner) null);
		
		handler.handleUnregistered(fullRunnerData, "2000");
		assertTrue(handler.foundInArchive());
		
		handler.handleUnregistered(fullRunnerData, "2000");
		assertFalse(handler.foundInArchive());
	}

	@Test
	public void handleUnregisteredDelegatesToFallbackCreationHandlerIfNotFoundInArchive() {
		subject().handleUnregistered(fullRunnerData, "2000");
		verify(fallbackHandler).handleUnregistered(fullRunnerData, "2000");
	}

	@Test
	public void handleUnregisteredReturnsNullWhenCreationGoesWrong() {
		when(fallbackHandler.handleUnregistered(fullRunnerData, "2000")).thenReturn(null);
		String returnedEcard = subject().handleUnregistered(fullRunnerData, "2000");
		assertNull(returnedEcard);
	}

}
