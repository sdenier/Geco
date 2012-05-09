/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import net.geco.basics.TimeManager;
import net.geco.model.RankedRunner;
import net.geco.model.RunnerRaceData;

public class RankedRunnerImpl implements RankedRunner {

	private RunnerRaceData runnerData;

	private int rank;
	
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

	public String formatDiffTime(long bestTime) {
		return TimeManager.time(runnerData.getRacetime() - bestTime);
	}
}