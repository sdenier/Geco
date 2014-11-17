/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.GecoControl;
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
public class AnonCreationHandler extends AbstractHandlerWithCourseDetector implements ECardHandler {

	public static class UnknownCreationHandler extends AnonCreationHandler {
		public UnknownCreationHandler(GecoControl gecoControl, CourseDetector detector) {
			super(gecoControl, detector);
		}

		@Override
		protected void setCustomStatus(RunnerRaceData data) {
			data.getResult().setStatus(Status.UNK);
		}
		
	}
	
	public static class DuplicateCreationHandler extends AnonCreationHandler {
		public DuplicateCreationHandler(GecoControl gecoControl, CourseDetector detector) {
			super(gecoControl, detector);
		}

		@Override
		public String handleDuplicate(RunnerRaceData data, Runner runner) {
			return handleData(data, runner.getEcard());
		}

		@Override
		protected void setCustomStatus(RunnerRaceData data) {
			data.getResult().setStatus(Status.DUP);
		}
	
	}
	
	public AnonCreationHandler(GecoControl gecoControl, CourseDetector detector) {
		super(gecoControl, detector);
	}
	
	@Override
	public String handleFinish(RunnerRaceData data) {return null;}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {return null;}

	@Override
	public String handleUnregistered(RunnerRaceData data, String cardId) {
		return handleData(data, cardId);
	}

	protected String handleData(RunnerRaceData data, String ecard) {
		Course course = courseDetector.detectCourse(data);
		try {
			setCustomStatus(data);
			Runner newRunner = runnerControl.buildAnonymousRunner(ecard, course); // derive unique ecard
			runnerControl.registerRunner(newRunner, data);
			geco().log("Creation " + data.infoString()); //$NON-NLS-1$
		} catch (RunnerCreationException e1) {
			geco().log(e1.getLocalizedMessage());
			return null;
		}
		return data.getRunner().getEcard();
	}

	protected void setCustomStatus(RunnerRaceData data) {	}

	@Override
	public boolean foundInArchive() {
		return false;
	}

}
