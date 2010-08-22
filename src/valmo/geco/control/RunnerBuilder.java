/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import valmo.geco.model.Factory;
import valmo.geco.model.Registry;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;

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
	
	public void checkGecoData(Stage currentStage, PenaltyChecker checker) {
		checkNoDataRunners(currentStage.registry());
		// compute trace for data
		for (RunnerRaceData raceData : currentStage.registry().getRunnersData()) {
			checker.computeStatus(raceData);	
		}
	}
	
	public void checkOrData(Stage currentStage, PenaltyChecker checker) {
		checkNoDataRunners(currentStage.registry());
		// compute status and trace for data
		for (RunnerRaceData raceData : currentStage.registry().getRunnersData()) {
			// Special runner status (DNS) should have been set before this point
			if( raceData.getResult()==null ) {
				checker.check(raceData);	
			}
		}
	}
	
	public void checkNoDataRunners(Registry registry) {
		for (Runner runner : registry.getRunners()) {
			if( registry.findRunnerData(runner) == null ) {
				registerRunnerDataFor(registry, runner, buildRunnerData());
			}
		}
	}

}
