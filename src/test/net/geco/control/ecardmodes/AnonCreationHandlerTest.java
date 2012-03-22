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
	
	@Mock private RunnerControl runnerControl;
	@Mock private CourseDetector detector;
	private Runner runner;

	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);
		when(runnerControl.deriveUniqueEcard("500")).thenReturn("500a");
		when(detector.detectCourse(fullRunnerData)).thenReturn(testCourse);
		
		runner = factory.createRunner();
		runner.setEcard("500");
		
	}
	
	@Test
	public void handleDuplicateCallsDetector() {
		new AnonCreationHandler(gecoControl, detector).handleDuplicate(fullRunnerData, runner);
		verify(detector).detectCourse(fullRunnerData);
	}
	
	@Test
	public void handleDuplicateSetsCustomStatus() {
		new AnonCreationHandler(gecoControl, detector).handleDuplicate(fullRunnerData, runner);
		assertEquals(Status.DUP, fullRunnerData.getStatus());
	}

	@Test
	public void handleDuplicateCreatesNewRunner() {
		new AnonCreationHandler(gecoControl, detector).handleDuplicate(fullRunnerData, runner);
		try {
			verify(runnerControl).buildAnonymousRunner("500a", testCourse);
		} catch (RunnerCreationException e) { fail(); }
	}

	@Test
	public void handleDuplicateRegisterNewRunner() {
		Runner newRunner = factory.createRunner();
		try {
			when(runnerControl.buildAnonymousRunner("500a", testCourse)).thenReturn(newRunner);
		} catch (RunnerCreationException e) { fail(); }
		new AnonCreationHandler(gecoControl, detector).handleDuplicate(fullRunnerData, runner);
		verify(runnerControl).registerRunner(newRunner, fullRunnerData);
	}
	
	@Test
	public void handleDuplicateReturnsId() {
		String returnedEcard = new AnonCreationHandler(gecoControl, detector).handleDuplicate(fullRunnerData, runner);
		assertEquals("500a", returnedEcard);
	}

	@Test
	public void handleDuplicateReturnsNullWhenCreationGoesWrong() {
		try {
			when(runnerControl.buildAnonymousRunner("500a", testCourse)).thenThrow(new RunnerCreationException("Error"));
		} catch (RunnerCreationException e) { fail(); }
		String returnedEcard = new AnonCreationHandler(gecoControl, detector).handleDuplicate(fullRunnerData, runner);
		assertNull(returnedEcard);
	}

}
