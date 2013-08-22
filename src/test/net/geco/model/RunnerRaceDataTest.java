/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import net.geco.basics.TimeManager;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.impl.POFactory;

import org.junit.Test;

import test.net.geco.testfactory.CourseFactory;
import test.net.geco.testfactory.RunnerFactory;

/**
 * @author Simon Denier
 * @since Apr 25, 2012
 *
 */
public class RunnerRaceDataTest {

	@Test
	public void getOfficialStartTime() {
		RunnerRaceData runnerData = RunnerFactory.create("111");
		assertThat("should return 'no time' when nothing is set",
				runnerData.getOfficialStarttime(),
				equalTo(TimeManager.NO_TIME));

		Course course = CourseFactory.createCourseWithMassStartTime("A", new Date(1000000));
		runnerData.getRunner().setCourse(course);
		assertThat("Course time for a mass start should be used when it is the only option",
				runnerData.getOfficialStarttime(),
				equalTo(new Date(1000000)));
		
		runnerData.getRunner().setRegisteredStarttime(new Date(2000000));
		assertThat("Runner's registered start time primes over course start time when it is set",
				runnerData.getOfficialStarttime(),
				equalTo(new Date(2000000)));
		
		runnerData.setStarttime(new Date(3000000));
		assertThat("Ecard start time has top priority over all other start times",
				runnerData.getOfficialStarttime(),
				equalTo(new Date(3000000)));
	}
	
	@Test
	public void test_getPace() {
		POFactory factory = new POFactory();
		
		RunnerRaceData data = factory.createRunnerRaceData();
		RunnerResult result = factory.createRunnerResult();
		data.setResult(result);
		result.setResultTime(960000);
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
		result.setResultTime(960000);
		Runner runner = factory.createRunner();
		Course course = factory.createCourse();
		course.setLength(0);
		runner.setCourse(course);
		data.setRunner(runner);
		
		assertTrue(Float.isInfinite(data.getMillisecondPace()));
	}

}
