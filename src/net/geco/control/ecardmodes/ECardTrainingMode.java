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
 * @since Mar 14, 2012
 *
 */
public class ECardTrainingMode extends AbstractEcardReadingMode {

	public ECardTrainingMode(GecoControl geco, CourseDetector detector) {
		this(geco, detector, true);
	}
	
	public ECardTrainingMode(GecoControl geco, CourseDetector detector, boolean register) {
		super(geco, detector);
		if( register ) {
			geco.registerService(ECardTrainingMode.class, this);
		}
	}
	
	@Override
	public void enableAutoHandler(boolean archiveLookupOn, boolean copyDuplicateOn) {
		toggleArchiveLookup(archiveLookupOn);
		toggleCopyDuplicate(copyDuplicateOn);
	}
	
	public ECardTrainingMode toggleArchiveLookup(boolean toggle) {
		if( toggle ){
			unregisteredHandler = new ArchiveLookupHandler(geco(), detector,
										new AnonCreationHandler(geco(), detector));
		} else {
			unregisteredHandler = new AnonCreationHandler(geco(), detector); 
		}
		return this;
	}

	public void toggleCopyDuplicate(boolean toggle) {
		if( toggle ){
			duplicateHandler = new CopyRunnerHandler(geco(), detector);
		} else {
			duplicateHandler = new ArchiveLookupHandler(geco(), detector,
									new AnonCreationHandler(geco(), detector));
		}
	}

	@Override
	public void handleDuplicate(RunnerRaceData runnerData, Runner runner) {
		String returnedCard = duplicateHandler.handleDuplicate(runnerData, runner);
		if( returnedCard!=null ) {
			geco().announcer().announceCardRead(returnedCard);
		}
	}

	@Override
	public void handleUnregistered(RunnerRaceData runnerData, String cardId) {
		String returnedCard = unregisteredHandler.handleUnregistered(runnerData, cardId);
		if( returnedCard!=null ) {
			geco().announcer().announceCardRead(returnedCard);
		}
	}

}
