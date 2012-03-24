/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.GecoControl;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.PunchRecordData;
import org.martin.sireader.server.IResultData;

/**
 * @author Simon Denier
 * @since Mar 24, 2012
 *
 */
public class ECardRegisterMode extends AbstractECardMode {

	public ECardRegisterMode(GecoControl gecoControl) {
		super(ECardRegisterMode.class, gecoControl);
		unregisteredHandler = new RegisterRunnerHandler(gecoControl);
	}

	@Override
	protected void processFinished(IResultData<PunchObject, PunchRecordData> card, RunnerRaceData data) {
		handleFinished(data);
	}

	@Override
	protected void processDuplicate(IResultData<PunchObject, PunchRecordData> card, Runner runner) {
		handleDuplicate(null, runner);
	}

	@Override
	protected void processUnregistered(IResultData<PunchObject, PunchRecordData> card) {
		handleUnregistered(null, card.getSiIdent());
	}


	@Override
	public void handleFinished(RunnerRaceData runnerData) {
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
