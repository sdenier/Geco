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
public class ECardTrainingMode extends AbstractECardMode {

	private CourseDetector detector;
	
	public ECardTrainingMode(GecoControl gecoControl, CourseDetector detector) {
		this(gecoControl, detector, true);
	}
	
	public ECardTrainingMode(GecoControl gecoControl, CourseDetector detector, boolean register) {
		super(gecoControl);
		if( register ) {
			gecoControl.registerService(ECardTrainingMode.class, this);
		}
		this.detector = detector;
		finishHandler = new AutoCheckerHandler(gecoControl, detector);
		enableAutoHandler(true);
	}
	
	public void enableManualHandler() {
		ManualHandler manualHandler = new ManualHandler(geco(), detector);
		duplicateHandler = manualHandler;
		unregisteredHandler = manualHandler;
	}
	
	public void enableAutoHandler(boolean archiveLookupOn) {
		duplicateHandler = new CopyRunnerHandler(geco(), detector);
		toggleArchiveLookup(archiveLookupOn);
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

	@Override
	public void handleRegistered(RunnerRaceData runnerData) {
		defaultHandle(runnerData);
	}

	@Override
	public void handleDuplicate(RunnerRaceData runnerData, Runner runner) {
		String returnedCard = duplicateHandler.handleDuplicate(runnerData, runner);
		if( returnedCard!=null ){
			geco().announcer().announceCardReadAgain(returnedCard);
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
