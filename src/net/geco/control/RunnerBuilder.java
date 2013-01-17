/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Aug 21, 2009
 *
 */
public class RunnerBuilder extends BasicControl {
	
	public RunnerBuilder(Factory factory) {
		super(factory);
	}

	public RunnerRaceData buildRunnerData() {
		RunnerRaceData data = factory().createRunnerRaceData();
		data.setResult(factory().createRunnerResult());
		return data;
	}
	
	public RunnerRaceData registerRunnerDataFor(Registry registry, Runner runner, RunnerRaceData runnerData) {
		runnerData.setRunner(runner);
		registry.addRunnerData(runnerData);
		return runnerData;
	}
	
}
