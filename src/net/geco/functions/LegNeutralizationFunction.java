/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Sep 18, 2011
 *
 */
public class LegNeutralizationFunction extends GecoFunction {

	private int legStart;
	private int legEnd;

	public LegNeutralizationFunction(GecoControl gecoControl) {
		super(gecoControl);
	}

	@Override
	public String toString() {
		return "Leg Neutralization";
	}

	@Override
	public String executeTooltip() {
		return "Neutralize leg for all runners";
	}

	@Override
	public JComponent getParametersConfig() {
		JFormattedTextField startCodeF = new JFormattedTextField();
		startCodeF.setValue(Integer.valueOf(legStart));
		
		Box config = Box.createVerticalBox();
		config.add(startCodeF);
		return config;
	}

	@Override
	public void updateUI() {}

	@Override
	public void execute() {
		Collection<Course> courses = selectCoursesWithNeutralizedLeg();
		for (Course course : courses) {
			List<Runner> runners = registry().getRunnersFromCourse(course);
			for (Runner runner : runners) {
				neutralizeLeg(registry().findRunnerData(runner));
			}
		}
	}

	public void setNeutralizedLeg(int legStart, int legEnd) {
		this.legStart = legStart;
		this.legEnd = legEnd;
	}

	public Collection<Course> selectCoursesWithNeutralizedLeg() {
		ArrayList<Course> courses = new ArrayList<Course>();
		for (Course c : registry().getCourses()) {
			if( c.hasLeg(legStart, legEnd) ){
				courses.add(c);
			}
		}
		return courses;
	}

	public Collection<Runner> selectRunnersWithLegToNeutralize(Course course) {
		return registry().getRunnersFromCourse(course);
	}

	public void neutralizeLeg(RunnerRaceData raceData) {
		Trace[] leg = raceData.retrieveLeg(legStart, legEnd);
		if( leg!=null && ! leg[1].isNeutralized() ){
			long splitTime = TimeManager.computeSplit(leg[0].getTime().getTime(), leg[1].getTime().getTime());
			if( splitTime!=TimeManager.NO_TIME_l ){
				RunnerResult result = raceData.getResult();
				if( result.getRacetime()!=TimeManager.NO_TIME_l ){
					result.setRacetime(result.getRacetime() - splitTime);
				}
				leg[1].setNeutralized(true);				
			}
		}
	}

}
