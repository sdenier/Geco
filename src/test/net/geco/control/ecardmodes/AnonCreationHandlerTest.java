/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.control.RunnerControl;
import net.geco.control.RunnerCreationException;
import net.geco.control.ecardmodes.AnonCreationHandler;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.model.Runner;
import net.geco.model.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class AnonCreationHandlerTest extends ECardModeSetup {
	
	@Mock protected RunnerControl runnerControl;
	@Mock protected CourseDetector detector;

	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		fullRunner.setEcard("500");
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);
		when(runnerControl.deriveUniqueEcard("500")).thenReturn("500");
		when(detector.detectCourse(fullRunnerData)).thenReturn(testCourse);
	}

	protected AnonCreationHandler subject() {
		return new AnonCreationHandler(gecoControl, detector);
	}
	
	@Test
	public void handleUnregisteredCallsDetector() {
		subject().handleUnregistered(fullRunnerData, "500");
		verify(detector).detectCourse(fullRunnerData);
	}
	
	@Test
	public void handleUnregisteredDontChangeStatus() {
		fullRunnerData.getResult().setStatus(Status.OK);
		subject().handleUnregistered(fullRunnerData, "500");
		assertEquals(Status.OK, fullRunnerData.getStatus());
	}

	@Test
	public void handleUnregisteredCreatesNewRunner() {
		subject().handleUnregistered(fullRunnerData, "500");
		try {
			verify(runnerControl).buildAnonymousRunner("500", testCourse);
		} catch (RunnerCreationException e) { fail(); }
	}

	@Test
	public void handleUnregisteredRegisterNewRunner() {
		Runner newRunner = factory.createRunner();
		try {
			when(runnerControl.buildAnonymousRunner("500", testCourse)).thenReturn(newRunner);
		} catch (RunnerCreationException e) { fail(); }
		subject().handleUnregistered(fullRunnerData, "500");
		verify(runnerControl).registerRunner(newRunner, fullRunnerData);
	}
	
	@Test
	public void handleUnregisteredReturnsCardId() {
		String returnedEcard = subject().handleUnregistered(fullRunnerData, "500");
		assertEquals("500", returnedEcard);
	}

	@Test
	public void handleUnregisteredReturnsNullWhenCreationGoesWrong() {
		try {
			when(runnerControl.buildAnonymousRunner("500", testCourse)).thenThrow(new RunnerCreationException("Error"));
		} catch (RunnerCreationException e) { fail(); }
		String returnedEcard = subject().handleUnregistered(fullRunnerData, "500");
		assertNull(returnedEcard);
	}

	@Test
	public void registerAnonymousRunnerCreatesNewRunner() {
		try {
			subject().registerAnonymousRunner(fullRunnerData, testCourse, "5000");
			verify(runnerControl).buildAnonymousRunner("5000", testCourse);
		} catch (RunnerCreationException e) { fail(); }
	}

	@Test
	public void registerAnonymousRunnerRegisterNewRunner() {
		Runner newRunner = factory.createRunner();
		try {
			when(runnerControl.buildAnonymousRunner("5000", testCourse)).thenReturn(newRunner);
			subject().registerAnonymousRunner(fullRunnerData, testCourse, "5000");
		} catch (RunnerCreationException e) { fail(); }
		verify(runnerControl).registerRunner(newRunner, fullRunnerData);
	}
	
}
