/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import net.geco.control.RunnerControl;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.impl.RunnerImpl;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Jun 6, 2011
 *
 */
public class RunnerControlTest extends MockControlSetup {
	
	private RunnerControl runnerControl;

	@Before
	public void setUp(){
		setUpMockControls();
		runnerControl = new RunnerControl(gecoControl);		
	}
	
	@Test
	public void testValidateStartId(){
		HashSet<Integer> startids = new HashSet<Integer>(Arrays.asList( new Integer[]{ 3 } ));
		when(registry.getStartIds()).thenReturn(startids);
		RunnerImpl runner = new RunnerImpl();
		runner.setStartId(1);
		
		assertTrue(runnerControl.validateStartId(runner, "1"));
		assertEquals(1, runner.getStartId().intValue());
		assertTrue(runnerControl.validateStartId(runner, "2"));
		assertEquals(2, runner.getStartId().intValue());
		assertFalse(runnerControl.validateStartId(runner, ""));
		assertEquals(2, runner.getStartId().intValue());
		assertFalse(runnerControl.validateStartId(runner, "3"));
		assertEquals(2, runner.getStartId().intValue());
	}

	@Test
	public void testValidateEcard(){
		HashSet<String> ecards = new HashSet<String>(Arrays.asList( new String[]{"3"} ));
		when(registry.getEcards()).thenReturn(ecards);
		RunnerImpl runner = new RunnerImpl();
		runner.setEcard("1");
		
		assertTrue(runnerControl.validateEcard(runner, "1"));
		assertEquals("1", runner.getEcard());
		assertTrue(runnerControl.validateEcard(runner, "2"));
		assertEquals("2", runner.getEcard());
		assertTrue(runnerControl.validateEcard(runner, "2a"));
		assertEquals("2a", runner.getEcard());
		assertFalse(runnerControl.validateEcard(runner, "3"));
		assertEquals("2a", runner.getEcard());
		assertTrue(runnerControl.validateEcard(runner, ""));
		assertEquals("", runner.getEcard());
	}
	
	@Test
	public void testDeriveUniqueEcard(){
		HashSet<String> ecards = new HashSet<String>(Arrays.asList( new String[]{"3"} ));
		when(registry.getEcards()).thenReturn(ecards);

		assertEquals("", runnerControl.deriveUniqueEcard(""));
		assertEquals("2", runnerControl.deriveUniqueEcard("2"));
		assertEquals("3a", runnerControl.deriveUniqueEcard("3"));

		ecards = new HashSet<String>(Arrays.asList( new String[]{"3", "3a"} ));
		when(registry.getEcards()).thenReturn(ecards);
		
		assertEquals("3b", runnerControl.deriveUniqueEcard("3"));
		
		for (int i = 0; i < 27; i++) {
			ecards.add(runnerControl.deriveUniqueEcard("3"));
		}
		assertEquals("3ac", runnerControl.deriveUniqueEcard("3b"));
		
	}

	@Test
	public void updateCourse() {
		Runner runner = factory.createRunner();
		Course oldCourse = factory.createCourse();
		Course newCourse = factory.createCourse();
		runnerControl.updateCourse(runner, oldCourse, newCourse);

		assertEquals(newCourse, runner.getCourse());
		verify(registry).updateRunnerCourse(oldCourse, runner);
		verify(announcer).announceCourseChange(runner, oldCourse);
	}
	
}
