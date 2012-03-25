/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.basics.GService;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.PunchRecordData;
import org.martin.sireader.server.IResultData;

/**
 * @author Simon Denier
 * @since Mar 14, 2012
 *
 */
public interface ECardMode extends GService {

	/**
	 * Entry point for the handler.
	 * 
	 * @param card
	 */
	public void processECard(IResultData<PunchObject, PunchRecordData> card);

	public void handleRegistered(RunnerRaceData runnerData);

	public void handleDuplicate(RunnerRaceData runnerData, Runner runner);

	public void handleUnregistered(RunnerRaceData runnerData, String cardId);

}