/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Aug 12, 2011
 *
 */
public interface Checker {

	public void postInitialize(Stage newStage);

	public void check(RunnerRaceData raceData);

	public TraceData computeTraceData(RunnerRaceData runnerData);

	public Status computeStatus(RunnerRaceData raceData);

	public long computeRaceTime(RunnerRaceData runnerData);
	
	public long computeResultTime(RunnerRaceData raceData);

	public long computeTimePenalty(RunnerRaceData raceData);
	
	public void setResult(RunnerRaceData runnerData);

	public void setTraceData(RunnerRaceData runnerData);

	public void resetRaceTime(RunnerRaceData raceData);

}
