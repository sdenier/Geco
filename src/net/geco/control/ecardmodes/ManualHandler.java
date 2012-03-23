/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.basics.GecoRequestHandler;
import net.geco.control.GecoControl;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Mar 23, 2012
 *
 */
public class ManualHandler implements ECardHandler {

	private GecoRequestHandler requestHandler;

	public ManualHandler(GecoControl geco) {
		this.requestHandler = geco.getService(GecoRequestHandler.class);
	}
	
	@Override
	public String handleFinish(RunnerRaceData data) {
		return null;
	}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {
		return requestHandler.requestMergeExistingRunner(data, runner);
	}

	@Override
	public String handleUnregistered(RunnerRaceData data, String cardId) {
		return requestHandler.requestMergeUnknownRunner(data, cardId);
	}

	@Override
	public boolean foundInArchive() {
		return false;
	}

}
