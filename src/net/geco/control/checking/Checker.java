/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Aug 12, 2011
 *
 */
public interface Checker {

	public void postInitialize(Stage newStage);

	public void check(RunnerRaceData runnerData);

	public Status computeStatus(RunnerRaceData raceData);

	public long computeOfficialRaceTime(RunnerRaceData raceData);

	public void resetRaceTime(RunnerRaceData runnerData);

}
