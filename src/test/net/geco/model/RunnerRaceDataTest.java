/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.impl.POFactory;

import org.junit.Test;

/**
 * @author Simon Denier
 * @since Apr 25, 2012
 *
 */
public class RunnerRaceDataTest {

	@Test
	public void test_getPace() {
		POFactory factory = new POFactory();
		
		RunnerRaceData data = factory.createRunnerRaceData();
		RunnerResult result = factory.createRunnerResult();
		data.setResult(result);
		result.setRacetime(960000);
		Runner runner = factory.createRunner();
		Course course = factory.createCourse();
		course.setLength(4000);
		runner.setCourse(course);
		data.setRunner(runner);
		
		assertEquals(240000, data.getMillisecondPace(), 0.0001);
		assertEquals("4:00", data.formatPace());
	}

	@Test
	public void test_getPaceReturnsInfinityWithZeroCourseLength() {
		POFactory factory = new POFactory();
		
		RunnerRaceData data = factory.createRunnerRaceData();
		RunnerResult result = factory.createRunnerResult();
		data.setResult(result);
		result.setRacetime(960000);
		Runner runner = factory.createRunner();
		Course course = factory.createCourse();
		course.setLength(0);
		runner.setCourse(course);
		data.setRunner(runner);
		
		assertTrue(Float.isInfinite(data.getMillisecondPace()));
	}

}
