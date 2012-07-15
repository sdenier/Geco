/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;


import net.geco.control.GecoControl;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Mar 11, 2012
 *
 */
public class ECardRacingMode extends AbstractEcardReadingMode {

	public ECardRacingMode(GecoControl geco, CourseDetector detector) {
		this(geco, detector, true);
	}

	public ECardRacingMode(GecoControl geco, CourseDetector detector, boolean register) {
		super(geco, detector);
		if( register ) {
			geco.registerService(ECardRacingMode.class, this);
		}
	}
	
	public void enableAutoHandler(boolean archiveLookupOn) {
		duplicateHandler = new AnonCreationHandler.DuplicateCreationHandler(geco(), detector);
		toggleArchiveLookup(archiveLookupOn);
	}
	
	public ECardRacingMode toggleArchiveLookup(boolean toggle) {
		if( toggle ){
			unregisteredHandler = new ArchiveLookupHandler(geco(), detector,
										new AnonCreationHandler.UnknownCreationHandler(geco(), detector));
		} else {
			unregisteredHandler = new AnonCreationHandler.UnknownCreationHandler(geco(), detector); 
		}
		return this;
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
