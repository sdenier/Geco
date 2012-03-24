/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;


import net.geco.control.GecoControl;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Mar 11, 2012
 *
 */
public class ECardRacingMode extends AbstractECardMode {

	public ECardRacingMode(GecoControl geco, CourseDetector detector) {
		super(ECardRacingMode.class, geco);
		finishHandler = new AutoCheckerHandler(geco);
		duplicateHandler = new AnonCreationHandler(geco, detector);
		unregisteredHandler = new ArchiveLookupHandler(geco, detector, Status.UNK);
	}
	
	@Override
	public void handleFinished(RunnerRaceData runnerData) {
		defaultHandle(runnerData);
	}

	@Override
	public void handleDuplicate(RunnerRaceData runnerData, Runner runner) {
		geco().log("READING AGAIN " + runner.getEcard()); //$NON-NLS-1$
		String returnedCard = duplicateHandler.handleDuplicate(runnerData, runner);
		if( returnedCard!=null ) {
			geco().announcer().announceCardReadAgain(returnedCard);
		}
	}

	@Override
	public void handleUnregistered(RunnerRaceData runnerData, String cardId) {
		geco().log("READING UNKNOWN " + cardId); //$NON-NLS-1$
		String returnedCard = unregisteredHandler.handleUnregistered(runnerData, cardId);
		if( returnedCard!=null ) {
			if( unregisteredHandler.foundInArchive() ){
				geco().announcer().announceCardRead(returnedCard);
			} else {
				geco().announcer().announceUnknownCardRead(returnedCard);
			}
		}
	}
	
}
