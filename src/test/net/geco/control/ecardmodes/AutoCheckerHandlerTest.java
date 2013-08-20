/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.control.RunnerControl;
import net.geco.control.StageControl;
import net.geco.control.checking.Checker;
import net.geco.control.ecardmodes.AutoCheckerHandler;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.model.Course;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class AutoCheckerHandlerTest extends ECardModeSetup {
	
	@Mock private Checker checker;
	@Mock private StageControl stageControl;
	@Mock private RunnerControl runnerControl;
	@Mock private CourseDetector detector;

	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		when(gecoControl.checker()).thenReturn(checker);
		when(gecoControl.getService(StageControl.class)).thenReturn(stageControl);
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);
	}

	private Course setUpRunnerWithAutoCourse() {
		Course auto = factory.createCourse();
		auto.setName("[Auto]");
		auto.setCodes(new int[0]);
		when(registry.autoCourse()).thenReturn(auto);
		fullRunner.setCourse(auto);
		return auto;
	}

	@Test
	public void handleFinishChecksData() {
		new AutoCheckerHandler(gecoControl, detector).handleFinish(fullRunnerData);
		verify(checker).check(fullRunnerData);
	}

	@Test
	public void handleFinishCallsDetectorWhenAutoCourse() {
		setUpRunnerWithAutoCourse();
		new AutoCheckerHandler(gecoControl, detector).handleFinish(fullRunnerData);
		verify(detector).detectCourse(fullRunnerData);
	}

	@Test
	public void handleFinishUpdatesDetectedCourse() {
		Course autoCourse = setUpRunnerWithAutoCourse();
		when(detector.detectCourse(fullRunnerData)).thenReturn(testCourse);
		new AutoCheckerHandler(gecoControl, detector).handleFinish(fullRunnerData);
		verify(runnerControl).updateCourse(fullRunner, autoCourse, testCourse);
	}
	
	@Test
	public void handleFinishReturnsId() {
		String returnedEcard = new AutoCheckerHandler(gecoControl, detector).handleFinish(fullRunnerData);
		assertEquals(fullRunnerData.getRunner().getEcard(), returnedEcard);
	}

}
