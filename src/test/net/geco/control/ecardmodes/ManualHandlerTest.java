/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.basics.MergeRequestHandler;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.control.ecardmodes.ManualHandler;
import net.geco.model.Course;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Simon Denier
 * @since Mar 23, 2012
 *
 */
public class ManualHandlerTest extends ECardModeSetup {

	@Mock private CourseDetector detector;
	@Mock private MergeRequestHandler requestHandler;
	
	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		when(gecoControl.getService(MergeRequestHandler.class)).thenReturn(requestHandler);
	}

	@Test
	public void handleDuplicateCallsCourseDetector() {
		new ManualHandler(gecoControl, detector).handleDuplicate(danglingRunnerData, fullRunner);
		verify(detector).detectCourse(danglingRunnerData);
	}
	
	@Test
	public void handleDuplicateRequestsMerge() {
		Course course = factory.createCourse();
		when(detector.detectCourse(danglingRunnerData)).thenReturn(course);
		new ManualHandler(gecoControl, detector).handleDuplicate(danglingRunnerData, fullRunner);
		verify(requestHandler).requestMergeExistingRunner(danglingRunnerData, fullRunner, course);
	}

	@Test
	public void handleUnregisteredCallsCourseDetector() {
		new ManualHandler(gecoControl, detector).handleUnregistered(danglingRunnerData, "100");
		verify(detector).detectCourse(danglingRunnerData);
	}

	@Test
	public void handleUnregisteredRequestsMerge() {
		Course course = factory.createCourse();
		when(detector.detectCourse(danglingRunnerData)).thenReturn(course);
		new ManualHandler(gecoControl, detector).handleUnregistered(danglingRunnerData, "100");
		verify(requestHandler).requestMergeUnknownRunner(danglingRunnerData, "100", course);
	}
	
}
