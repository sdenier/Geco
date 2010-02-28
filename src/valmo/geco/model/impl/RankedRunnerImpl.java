/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import valmo.geco.model.RankedRunner;
import valmo.geco.model.RunnerRaceData;

public class RankedRunnerImpl implements RankedRunner {
	private RunnerRaceData runnerData;
	private int rank;
	
	// TODO: factory
	public RankedRunnerImpl(int rank, RunnerRaceData runnerData) {
		this.rank = rank;
		this.runnerData = runnerData;
	}

	public RunnerRaceData getRunnerData() {
		return runnerData;
	}
	public int getRank() {
		return rank;
	}
}