/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.model.Category;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class CourseDetector extends Control {

	private RunnerControl runnerControl;

	private Course autoCourse;

	private boolean useCategoryCourseSet;

	public CourseDetector(GecoControl gecoControl) {
		super(gecoControl);
		this.runnerControl = getService(RunnerControl.class);
		this.autoCourse = registry().autoCourse();
	}

	private static class CandidateCourse implements Comparable<CandidateCourse> {
		private Course course;
		private int distance;

		CandidateCourse(int distance, Course course) {
			this.course = course;
			this.distance = distance;
		}

		@Override
		public int compareTo(CandidateCourse o) {
			return distance - o.distance;
		}
	}
	
	private static class BestMatch {
		private int minMPs;
		private Course course;
		private TraceData trace;
		private RunnerResult result;
		
		public BestMatch(int nbMPs, Course candicate, RunnerRaceData data) {
			minMPs = nbMPs;
			course = candicate;
			// memoize results so we don't have to compute it again
			trace = data.getTraceData();
			result = data.getResult();
		}

		public Course setAndReturn(RunnerRaceData data) {
			data.setTraceData(trace);
			data.setResult(result);
			return course;
		}
	}

	public void toggleCategoryCourseSet(boolean flag) {
		useCategoryCourseSet = flag;
	}

	public boolean categoryCourseSetEnabled() {
		return useCategoryCourseSet;
	}

	public Course detectCourse(RunnerRaceData data) {
		return detect(data, registry().getCourses());
	}

	public Course detectCourse(RunnerRaceData data, Category cat) {
		Collection<Course> selectedCourses;
		if( useCategoryCourseSet && cat.getCourseSet() != null) {
			selectedCourses = registry().getCoursesFromCourseSet(cat.getCourseSet());
			if ( selectedCourses.isEmpty() ) {
				geco().info(Messages.getString("CourseDetector.EmptyCourseSetWarning") + cat.getName(), true); //$NON-NLS-1$
				selectedCourses = registry().getCourses();
			}
		} else {
			selectedCourses =  registry().getCourses();
		}
		return detect(data, selectedCourses);
	}

	private Course detect(RunnerRaceData data, Collection<Course> courses) {
		int nbPunches = data.getPunches().length;
		List<CandidateCourse> candidates = new ArrayList<CandidateCourse>(courses.size());
		for (Course course : courses) {
			if( course != autoCourse ){
				// don't include Auto course in candidates - will match all traces as OK
				candidates.add(new CandidateCourse(Math.abs(nbPunches - course.nbControls()), course));
			}
		}
		Collections.sort(candidates);
		
		RunnerRaceData testData = data.clone();
		testData.setRunner(runnerControl.buildMockRunner());
		if( data.getRunner() != null ){
			testData.getRunner().setRegisteredStarttime(data.getRunner().getRegisteredStarttime());
		}
		BestMatch bestMatch = new BestMatch(Integer.MAX_VALUE, autoCourse, data);

		for (CandidateCourse candicate : candidates) {
			testData.getRunner().setCourse(candicate.course);
			geco().checker().check(testData);
			int nbMPs = testData.getTraceData().getNbMPs();
			if( nbMPs < bestMatch.minMPs ){
				bestMatch = new BestMatch(nbMPs, candicate.course, testData);
				if( nbMPs == 0 ){
					// early stop only if no MP detected
					// in some race case with orient'show, one trace may be ok with multiple courses (as soon as MPs < MP limit)
					// so we should continue to look for a better match even if status == OK
					return bestMatch.setAndReturn(data); // candicate.course;
				}
			}
		}
		return bestMatch.setAndReturn(data);
	}

}
