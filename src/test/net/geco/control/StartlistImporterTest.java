/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.basics.Util;
import net.geco.control.RunnerControl;
import net.geco.control.StageControl;
import net.geco.control.StartlistImporter;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import test.net.geco.testfactory.GroupFactory;

/**
 * @author Simon Denier
 * @since Mar 15, 2012
 *
 */
public class StartlistImporterTest extends MockControlSetup {

	@Mock private RunnerControl runnerControl;
	@Mock private StageControl stageControl;
	
	private StartlistImporter startlistImporter;

	@Before
	public void setUp() {
		setUpMockControls();
		when(gecoControl.getService(RunnerControl.class)).thenReturn(runnerControl);
		when(gecoControl.getService(StageControl.class)).thenReturn(stageControl);
		
		startlistImporter = new StartlistImporter(gecoControl);
	}
	
	@Test
	public void importRunnerRecordWithoutStartId() {
		Runner runner = new POFactory().createRunner();
		runner.setStartId(Integer.valueOf(100));
		when(runnerControl.buildBasicRunner("1061511")).thenReturn(runner);
		String[] record = Util.splitAndTrim(";1061511;10869;DENIER;Simon;80;H;;;00:46:00;;;;5906;5906NO;VALMO;France;11;H21A;H21A", ";");
		startlistImporter.importRunnerRecord(record);
		verify(runnerControl).registerNewRunner(runner);
		assertEquals("Start id should be the one given at creation by registry",
					 Integer.valueOf(100), runner.getStartId());
	}

	@Test
	public void importRunnerWithCustomCourseField() {
		Runner runner = new POFactory().createRunner();
		Course courseA = GroupFactory.createCourse("Course A");
		when(runnerControl.buildBasicRunner("1061511")).thenReturn(runner);
		when(stageControl.ensureCourseInRegistry("Course A")).thenReturn(courseA);
		String[] record = Util.splitAndTrim(";1061511;10869;DENIER;Simon;80;H;;;00:46:00;;Geco-course;Course A;5906;5906NO;VALMO;France;11;H21A;H21A", ";");
		startlistImporter.importRunnerRecord(record);
		verify(runnerControl).registerNewRunner(runner);
		assertEquals("Runner should be registered with the given course when the custom Geco-course flag is set",
					courseA, runner.getCourse());
	}
	
}
