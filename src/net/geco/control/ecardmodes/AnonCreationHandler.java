/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.control.RunnerCreationException;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class AnonCreationHandler extends Control implements ECardHandler {

	protected RunnerControl runnerControl;
	protected CourseDetector courseDetector;

	public AnonCreationHandler(GecoControl gecoControl, CourseDetector detector) {
		super(gecoControl);
		runnerControl = getService(RunnerControl.class);
		courseDetector = detector;		
	}
	
	@Override
	public String handleFinish(RunnerRaceData data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {
		Course course = courseDetector.detectCourse(data);
		data.getResult().setStatus(Status.DUP);
		String uniqueEcard = runnerControl.deriveUniqueEcard(runner.getEcard());
		try {
			registerAnonymousRunner(data, course, uniqueEcard);
		} catch (RunnerCreationException e1) {
			geco().log(e1.getLocalizedMessage());
			return null;
		}
		return uniqueEcard;
	}

	protected void registerAnonymousRunner(RunnerRaceData data, Course course, String ecard)
			throws RunnerCreationException {
		Runner newRunner = runnerControl.buildAnonymousRunner(ecard, course);
		runnerControl.registerRunner(newRunner, data);
		geco().log("Creation " + data.infoString()); //$NON-NLS-1$
	}

	@Override
	public String handleUnregistered(RunnerRaceData data, String cardId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean foundInArchive() {
		return false;
	}

}
