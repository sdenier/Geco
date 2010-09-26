/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import valmo.geco.core.GecoRequestHandler;
import valmo.geco.model.Course;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Sep 26, 2010
 *
 */
public class AutoMergeHandler extends Control implements GecoRequestHandler {

	private RunnerControl runnerControl;

	public AutoMergeHandler(GecoControl gecoControl, RunnerControl runnerControl) {
		super(gecoControl);
		this.runnerControl = runnerControl;
	}

	@Override
	public String requestMergeUnknownRunner(RunnerRaceData data, String chip) {
		return processData(data, chip);
	}

	@Override
	public String requestMergeExistingRunner(RunnerRaceData data, Runner target) {
		return processData(data, target.getChipnumber());
	}
	
	private Course detectCourse(RunnerRaceData data) {
		// Dummy heuristics based on number of punches on card, compared to expected punches for courses
		// take the nearest course, above or below
		// TODO: a bit more clever heuristics is to use set intersection/difference, matching the lowest diff
		// next one is to use the checker
		Course probableCourse = null;
		int dist = 1000;
		int nbPunches = data.getPunches().length;
		for (Course course : registry().getCourses()) {
			int dist2 = Math.abs(nbPunches - course.getCodes().length);
			if( dist2 < dist ) {
				dist = dist2;
				probableCourse = course;
			}
		}
		return probableCourse;
	}
	
	private String processData(RunnerRaceData runnerData, String ecard) {
		String uniqueEcard = runnerControl.deriveUniqueChipnumber(ecard);
		Course course = detectCourse(runnerData);
		try {
			// Create from scratch a brand new runner
			Runner newRunner = runnerControl.buildAnonymousRunner(uniqueEcard, course);
			newRunner.setFirstname("Loisir");
			newRunner.setLastname(uniqueEcard);
			runnerControl.registerRunner(newRunner, runnerData);
			geco().checker().check(runnerData);
			geco().log("Creation " + runnerData.infoString());
			geco().announcer().announceStatusChange(runnerData, Status.Unknown);
		} catch (RunnerCreationException e1) {
			e1.printStackTrace();
		}
		return uniqueEcard;
	}

}
