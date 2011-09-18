/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;

import net.geco.control.GecoControl;
import net.geco.model.Course;
import net.geco.model.Runner;

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
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.functions.GecoFunction#execute()
	 */
	@Override
	public void execute() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.geco.functions.GecoFunction#executeTooltip()
	 */
	@Override
	public String executeTooltip() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.functions.GecoFunction#getParametersConfig()
	 */
	@Override
	public JComponent getParametersConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.functions.GecoFunction#updateUI()
	 */
	@Override
	public void updateUI() {
		// TODO Auto-generated method stub

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

}
