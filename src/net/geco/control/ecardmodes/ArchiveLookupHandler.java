/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.ArchiveManager;
import net.geco.control.GecoControl;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Mar 22, 2012
 *
 */
public class ArchiveLookupHandler extends AbstractHandlerWithCourseDetector implements ECardHandler {

	private ECardHandler fallbackHandler;

	private ArchiveManager archiveManager;
	
	private boolean foundInArchive = false;

	public ArchiveLookupHandler(GecoControl gecoControl, CourseDetector detector, ECardHandler fallbackHandler) {
		super(gecoControl, detector);
		this.fallbackHandler = fallbackHandler;
		this.archiveManager = getService(ArchiveManager.class);
	}
	
	@Override
	public String handleFinish(RunnerRaceData data) {return null;}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {
		Runner archiveRunner = archiveManager.findAndBuildRunner(runner.getEcard());
		if( archiveRunner != null ) {
			insertFromArchive(data, archiveRunner);
		} else {
			foundInArchive = false;
			return this.fallbackHandler.handleDuplicate(data, runner);
		}
		return archiveRunner.getEcard();
	}

	@Override
	public String handleUnregistered(RunnerRaceData data, String cardId) {
		Runner runner = archiveManager.findAndBuildRunner(cardId);
		if( runner != null ) {
			insertFromArchive(data, runner);
		} else {
			foundInArchive = false;
			return this.fallbackHandler.handleUnregistered(data, cardId);
		}
		return cardId;
	}

	private void insertFromArchive(RunnerRaceData data, Runner archiveRunner) {
		foundInArchive = true;
		archiveRunner.setCourse(courseDetector.detectCourse(data, archiveRunner.getCategory()));
		runnerControl.registerRunner(archiveRunner, data);
		geco().log("Insertion " + data.infoString()); //$NON-NLS-1$
	}

	@Override
	public boolean foundInArchive() {
		return foundInArchive;
	}

}
