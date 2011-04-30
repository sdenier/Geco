/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.util.Collections;
import java.util.Vector;

import valmo.geco.basics.GecoRequestHandler;
import valmo.geco.model.Course;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Sep 26, 2010
 *
 */
public class AutoMergeHandler extends Control implements GecoRequestHandler {

	public AutoMergeHandler(GecoControl gecoControl) {
		super(AutoMergeHandler.class, gecoControl);
	}
	
	private RunnerControl runnerControl() {
		return geco().getService(RunnerControl.class);
	}

	@Override
	public String requestMergeExistingRunner(RunnerRaceData data, Runner target) {
		return processData(data, target.getEcard(), detectCourse(data), Status.DUP);
	}

	@Override
	public String requestMergeUnknownRunner(RunnerRaceData data, String ecard) {
		Course course = detectCourse(data);
		Runner r = detectArchiveRunner(data, ecard, course);
		if( r!=null ){
			// TODO: rethink this part with SIReaderHandler. We return null because detectArchiveRunner
			// already handled the case and announced CardRead (autoprinting).
			// Returning the e-card would led SIReaderHandler to announce an unknown card.
			return null;
		} else {
			return processData(data, ecard, course, Status.UNK);
		}
	}
	
	private static class CourseResult implements Comparable<CourseResult> {
		CourseResult(int dist, Course course) {
			this.course = course;
			this.dist = dist;
		}
		private Course course;
		private int dist;
		private RunnerResult result;
		@Override
		public int compareTo(CourseResult o) {
			return dist - o.dist;
		}
	}

	private Course detectCourse(RunnerRaceData data) {
		Vector<CourseResult> distances = new Vector<CourseResult>();
		int nbPunches = data.getPunches().length;
		for (Course course : registry().getCourses()) {
			distances.add(new CourseResult(Math.abs(nbPunches - course.nbControls()), course));
		}
		Collections.sort(distances);
		
		int minMps = Integer.MAX_VALUE;
		CourseResult bestResult = null;
		data.setRunner(runnerControl().buildMockRunner());
		for (CourseResult cResult : distances) {
			data.getRunner().setCourse(cResult.course);
			geco().checker().check(data);
			if( data.getStatus()==Status.OK ){
				// in the case of an orient'show, we may be ok with mps. However unlikely to have 2 courses
				// ok with the same trace
				return cResult.course;
			}
			int nbMPs = data.getResult().getNbMPs();
			if( nbMPs<minMps ){
				minMps = nbMPs;
				bestResult = cResult;
				bestResult.result = data.getResult(); // memoize result so we don't have to compute it again
			}
		}
		data.setResult(bestResult.result);
		return bestResult.course;
	}

	private Runner detectArchiveRunner(RunnerRaceData data, String ecard, Course course) {
		ArchiveManager archive = geco().getService(ArchiveManager.class);
		Runner newRunner = archive.findAndCreateRunner(ecard, course);
		if( newRunner==null ){
			return null;
		} else {
			runnerControl().registerRunner(newRunner, data);
			geco().announcer().announceCardRead(newRunner.getEcard());
			geco().log("Insertion " + data.infoString()); //$NON-NLS-1$
			return newRunner;
		}
	}
	
	private String processData(RunnerRaceData runnerData, String ecard, Course course, Status status) {
		String uniqueEcard = runnerControl().deriveUniqueChipnumber(ecard);
		try {
			// Create from scratch a brand new runner
			Runner newRunner = runnerControl().buildAnonymousRunner(uniqueEcard, course);
//			newRunner.setFirstname("Loisir");
//			newRunner.setLastname(uniqueEcard);
			runnerData.getResult().setStatus(status); // set custom (unresolved) status
			runnerControl().registerRunner(newRunner, runnerData);
			geco().log("Creation " + runnerData.infoString()); //$NON-NLS-1$
		} catch (RunnerCreationException e1) {
			e1.printStackTrace();
		}
		return uniqueEcard;
	}

}
