/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;


import net.geco.basics.GecoRequestHandler;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Mar 11, 2012
 *
 */
public class ECardReadingMode extends AbstractECardMode {

	private GecoRequestHandler requestHandler;
	
	public ECardReadingMode(GecoControl geco) {
		super(ECardReadingMode.class, geco);
	}
	
	public void setRequestHandler(GecoRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	@Override
	public void handleFinished(RunnerRaceData runnerData) {
		Status oldStatus = runnerData.getResult().getStatus();
		geco().checker().check(runnerData);
		geco().log("READING " + runnerData.infoString()); //$NON-NLS-1$
		if( runnerData.getResult().getNbMPs() > 0 ) {
			geco().announcer().dataInfo(
					runnerData.getResult().formatMpTrace()
					+ " (" + runnerData.getResult().getNbMPs() + " MP)"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		geco().announcer().announceCardRead(runnerData.getRunner().getEcard());
		geco().announcer().announceStatusChange(runnerData, oldStatus);
	}

	@Override
	public void handleDuplicate(RunnerRaceData runnerData, String cardId, Runner runner) {
		geco().log("READING AGAIN " + cardId); //$NON-NLS-1$
		String returnedCard = requestHandler.requestMergeExistingRunner(runnerData, runner);
		if( returnedCard!=null ) {
			geco().announcer().announceCardReadAgain(returnedCard);
		}
	}

	@Override
	public void handleUnknown(RunnerRaceData runnerData, String cardId) {
		geco().log("READING UNKNOWN " + cardId); //$NON-NLS-1$
		String returnedCard = requestHandler.requestMergeUnknownRunner(runnerData, cardId);
		if( returnedCard!=null ) {
			geco().announcer().announceUnknownCardRead(returnedCard);
		}
	}
	
}
