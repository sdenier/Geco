/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.testfactory;

import net.geco.model.Factory;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;
import net.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since May 24, 2012
 *
 */
public class RunnerFactory {

	private static Factory factory = new POFactory();

	public static RunnerRaceData create(String ecard) {
		Runner runner = factory.createRunner();
		runner.setStartId(1);
		runner.setEcard(ecard);
		runner.setCourse(GroupFactory.createCourse("Dummy"));
		RunnerRaceData runnerRaceData = factory.createRunnerRaceData();
		runnerRaceData.setRunner(runner);
		runnerRaceData.setTraceData(factory.createTraceData());
		runnerRaceData.setResult(factory.createRunnerResult());
		return runnerRaceData;
	}

	public static RunnerRaceData createWithStatus(String ecard, Status status) {
		RunnerRaceData runnerRaceData = create(ecard);
		runnerRaceData.getResult().setStatus(status);
		return runnerRaceData;
	}

}
