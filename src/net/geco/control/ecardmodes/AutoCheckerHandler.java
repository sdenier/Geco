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
 * @since Mar 21, 2012
 *
 */
public class AutoCheckerHandler extends AbstractHandlerWithCourseDetector implements ECardHandler {

	private Course autoCourse;

	public AutoCheckerHandler(GecoControl gecoControl, CourseDetector detector) {
		super(gecoControl, detector);
		autoCourse = registry().autoCourse();
	}

	@Override
	public String handleFinish(RunnerRaceData data) {
		Runner runner = data.getRunner();
		if( runner.getCourse() == autoCourse ) {
			runnerControl.updateCourse(runner, autoCourse, courseDetector.detectCourse(data, runner.getCategory()));
		} else {
			geco().checker().check(data);
		}
		return runner.getEcard();
	}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {return null;}

	@Override
	public String handleUnregistered(RunnerRaceData data, String cardId) {return null;}

	@Override
	public boolean foundInArchive() {
		return false;
	}

}
