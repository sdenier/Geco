/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.GecoControl;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.gecosi.SiDataFrame;

/**
 * @author Simon Denier
 * @since Mar 24, 2012
 *
 */
public class ECardRegisterMode extends AbstractECardMode {

	public ECardRegisterMode(GecoControl gecoControl) {
		super(ECardRegisterMode.class, gecoControl);
		unregisteredHandler = finishHandler = new RegisterRunnerHandler(gecoControl);
	}

	@Override
	protected void processRegistered(SiDataFrame card, RunnerRaceData data) {
		handleRegistered(data);
	}

	@Override
	protected void processDuplicate(SiDataFrame card, Runner runner) {
		handleDuplicate(null, runner);
	}

	@Override
	protected void processUnregistered(SiDataFrame card) {
		handleUnregistered(null, card.getSiNumber());
	}


	@Override
	public void handleRegistered(RunnerRaceData runnerData) {
		finishHandler.handleFinish(runnerData);
		geco().announcer().announceCardRegistered(runnerData.getRunner().getEcard());
	}

	@Override
	public void handleDuplicate(RunnerRaceData runnerData, Runner runner) {
		geco().announcer().announceCardRegistered(runner.getEcard());
	}

	@Override
	public void handleUnregistered(RunnerRaceData runnerData, String cardId) {
		String returnedCard = unregisteredHandler.handleUnregistered(runnerData, cardId);
		if( returnedCard!=null ) {
			geco().announcer().announceCardRegistered(returnedCard);
		}
	}

}
