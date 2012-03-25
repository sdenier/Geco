/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.control.StageControl;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class AutoCheckerHandler extends Control implements ECardHandler {

	private StageControl stageControl;

	private RunnerControl runnerControl;

	private CourseDetector courseDetector;

	public AutoCheckerHandler(GecoControl gecoControl, CourseDetector detector) {
		super(gecoControl);
		this.stageControl = getService(StageControl.class);
		this.runnerControl = getService(RunnerControl.class);
		this.courseDetector = detector;
	}

	@Override
	public String handleFinish(RunnerRaceData data) {
		Runner runner = data.getRunner();
		Course autoCourse = stageControl.getAutoCourse();
		if( runner.getCourse() == autoCourse ) {
			runnerControl.updateCourse(runner, autoCourse, courseDetector.detectCourse(data));
		} else {
			geco().checker().check(data);
		}
		return runner.getEcard();
	}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {
		// TODO Auto-generated method stub
		return null;
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
