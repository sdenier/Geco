/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.GecoControl;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Jul 15, 2012
 *
 */
public abstract class AbstractEcardReadingMode extends AbstractECardMode {

	protected CourseDetector detector;

	public AbstractEcardReadingMode(GecoControl geco, CourseDetector detector) {
		super(geco);
		this.detector = detector;
		finishHandler = new AutoCheckerHandler(geco, detector);
		enableAutoHandler(true);
	}
	
	public void enableManualHandler() {
		ManualHandler manualHandler = new ManualHandler(geco(), detector);
		duplicateHandler = manualHandler;
		unregisteredHandler = manualHandler;
	}

	public void enableAutoHandler(boolean archiveLookupOn) {	}
	
	@Override
	public void handleRegistered(RunnerRaceData runnerData) {
		Status oldStatus = runnerData.getResult().getStatus();
		finishHandler.handleFinish(runnerData);
		geco().log("READING " + runnerData.infoString()); //$NON-NLS-1$
		if( runnerData.getTraceData().getNbMPs() > 0 ) {
			geco().announcer().dataInfo(
					runnerData.getResult().formatMpTrace()
					+ " (" + runnerData.getTraceData().getNbMPs() + " MP)"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		geco().announcer().announceCardRead(runnerData.getRunner().getEcard());
		geco().announcer().announceStatusChange(runnerData, oldStatus);
	}

}