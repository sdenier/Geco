/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import net.geco.basics.TimeManager;
import net.geco.control.RunnerControl;
import net.geco.control.ecardmodes.CopyRunnerHandler;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.model.Course;
import net.geco.model.Runner;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/**
 * @author Simon Denier
 * @since Mar 23, 2012
 *
 */
public class CopyRunnerHandlerTest extends ECardModeSetup {

	private Runner runner;
	private Course newCourse;
	@Mock private CourseDetector detector;
	@Mock private RunnerControl runnerControl;
	
	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();

		newCourse = factory.createCourse();
		when(registry.nextStartId()).thenReturn(314);
		when(detector.detectCourse(fullRunnerData)).thenReturn(newCourse);
		when(runnerControl.deriveUniqueEcard("999")).thenReturn("999a");
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);
		
		runner = factory.createRunner();
		runner.setEcard("999");
	}

	@Test
	public void handleDuplicate_callsDetector() {
		new CopyRunnerHandler(gecoControl, detector).handleDuplicate(fullRunnerData, runner);
		verify(detector).detectCourse(fullRunnerData);
	}
	
	@Test
	public void handleDuplicate_copyRunner() {
		Runner mockRunner = mock(Runner.class);
		when(mockRunner.getEcard()).thenReturn("999");
		when(mockRunner.copyWith(anyInt(), any(String.class), any(Course.class))).thenReturn(factory.createRunner());
		new CopyRunnerHandler(gecoControl, detector).handleDuplicate(fullRunnerData, mockRunner);
		verify(mockRunner).copyWith(314, "999a", newCourse);
	}

	@Test
	public void handleDuplicate_resetRunnerRegisteredStart() {
		runner.setRegisteredStarttime(new Date(1));
		ArgumentCaptor<Runner> newRunner = ArgumentCaptor.forClass(Runner.class);
		new CopyRunnerHandler(gecoControl, detector).handleDuplicate(fullRunnerData, runner);
		verify(runnerControl).registerRunner(newRunner.capture(), eq(fullRunnerData));
		assertEquals(TimeManager.NO_TIME, newRunner.getValue().getRegisteredStarttime());
	}
	
	@Test
	public void handleDuplicate_registerNewRunner() {
		ArgumentCaptor<Runner> newRunner = ArgumentCaptor.forClass(Runner.class);
		new CopyRunnerHandler(gecoControl, detector).handleDuplicate(fullRunnerData, runner);
		verify(runnerControl).registerRunner(newRunner.capture(), eq(fullRunnerData));
		assertEquals("999a", newRunner.getValue().getEcard());
	}
	
	@Test
	public void handleDuplicate_returnsId() {
		String returnedEcard = new CopyRunnerHandler(gecoControl, detector).handleDuplicate(fullRunnerData, runner);
		assertEquals("999a", returnedEcard);
	}

}
