/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.GecoControl;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Mar 23, 2012
 *
 */
public class CopyRunnerHandler extends AbstractHandlerWithCourseDetector implements ECardHandler {

	public CopyRunnerHandler(GecoControl gecoControl, CourseDetector detector) {
		super(gecoControl, detector);
	}

	@Override
	public String handleFinish(RunnerRaceData data) {return null;}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {
		Course course = courseDetector.detectCourse(data);
		String uniqueEcard = runnerControl.deriveUniqueEcard(runner.getEcard());
		Runner newRunner = runner.copyWith(registry().nextStartId(), uniqueEcard, course);
		runnerControl.registerRunner(newRunner, data);
		geco().log("Copy " + data.infoString()); //$NON-NLS-1$
		return uniqueEcard;
	}

	@Override
	public String handleUnregistered(RunnerRaceData data, String cardId) {return null;}

	@Override
	public boolean foundInArchive() {
		return false;
	}

}
