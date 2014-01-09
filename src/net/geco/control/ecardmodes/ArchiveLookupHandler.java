/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.ArchiveManager;
import net.geco.control.GecoControl;
import net.geco.control.RunnerCreationException;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Mar 22, 2012
 *
 */
public class ArchiveLookupHandler extends AbstractHandlerWithCourseDetector implements ECardHandler {

	private AnonCreationHandler anonHandler;

	private ArchiveManager archiveManager;
	
	private boolean foundInArchive = false;

	public ArchiveLookupHandler(GecoControl gecoControl, CourseDetector detector, AnonCreationHandler anonHandler) {
		super(gecoControl, detector);
		this.anonHandler = anonHandler;
		this.archiveManager = getService(ArchiveManager.class);
	}
	
	@Override
	public String handleFinish(RunnerRaceData data) {return null;}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {return null;}

	@Override
	public String handleUnregistered(RunnerRaceData data, String cardId) {
		Runner runner = archiveManager.findAndBuildRunner(cardId);
		if( runner != null ) {
			foundInArchive = true;
			runner.setCourse(courseDetector.detectCourse(data, runner.getCategory()));
			runnerControl.registerRunner(runner, data);
			geco().log("Insertion " + data.infoString()); //$NON-NLS-1$
		} else {
			try {
				foundInArchive = false;
				this.anonHandler.registerAnonymousRunner(data, courseDetector.detectCourse(data), cardId);
			} catch (RunnerCreationException e) {
				geco().log(e.getLocalizedMessage());
				return null;
			}
		}
		return cardId;
	}

	@Override
	public boolean foundInArchive() {
		return foundInArchive;
	}

}
