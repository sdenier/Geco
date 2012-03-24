/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.ArchiveManager;
import net.geco.control.GecoControl;
import net.geco.control.RunnerCreationException;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Mar 22, 2012
 *
 */
public class ArchiveLookupHandler extends AnonCreationHandler implements ECardHandler {

	private ArchiveManager archiveManager;
	private Status customStatus;
	private boolean foundInArchive = false;

	public ArchiveLookupHandler(GecoControl gecoControl, CourseDetector detector) {
		super(gecoControl, detector);
		this.archiveManager = getService(ArchiveManager.class);
	}

	public ArchiveLookupHandler(GecoControl gecoControl, CourseDetector detector, Status custom) {
		this(gecoControl, detector);
		this.customStatus = custom;
	}
	
	@Override
	public String handleFinish(RunnerRaceData data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String handleUnregistered(RunnerRaceData data, String cardId) {
		Course course = courseDetector.detectCourse(data);
		Runner runner = archiveManager.findAndCreateRunner(cardId, course);
		if( runner!=null ){
			foundInArchive = true;
			runnerControl.registerRunner(runner, data);
			geco().log("Insertion " + data.infoString()); //$NON-NLS-1$
		} else {
			foundInArchive = false;
			checkCustomStatus(data);
			try {
				registerAnonymousRunner(data, course, cardId);
			} catch (RunnerCreationException e) {
				geco().log(e.getLocalizedMessage());
				return null;
			}
		}
		return cardId;
	}

	public void checkCustomStatus(RunnerRaceData data) {
		if( customStatus!=null ){
			data.getResult().setStatus(customStatus);
		}
	}

	@Override
	public boolean foundInArchive() {
		return foundInArchive;
	}

}
