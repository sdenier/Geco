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

	private RunnerControl runnerControl;
	private CourseDetector courseDetector;

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
			Runner newRunner = runnerControl.buildAnonymousRunner(uniqueEcard, course);
			runnerControl.registerRunner(newRunner, data);
			geco().log("Creation " + data.infoString()); //$NON-NLS-1$
		} catch (RunnerCreationException e1) {
			geco().log(e1.getLocalizedMessage());
			return null;
		}
		return uniqueEcard;
	}

	@Override
	public String handleUnregistered(RunnerRaceData data, String cardId) {
		// TODO Auto-generated method stub
		return null;
	}

}
