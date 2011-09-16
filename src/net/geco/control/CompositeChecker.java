/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.util.HashMap;
import java.util.Map;

import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Sep 4, 2011
 *
 */
public class CompositeChecker extends PenaltyChecker {

	private Map<Course,MultiCourse> multis = new HashMap<Course, MultiCourse>();

	public CompositeChecker(Factory factory, CompositeTracer tracer) {
		super(factory, tracer);
	}
	
	public CompositeChecker(Factory factory) {
		super(factory, new CompositeTracer(factory));
	}

	public CompositeChecker(GecoControl gecoControl) {
		super(gecoControl, new CompositeTracer(gecoControl.factory()));
	}

	protected CompositeTracer tracer() {
		return (CompositeTracer) tracer;
	}
	
	@Override
	public Status computeStatus(RunnerRaceData data) {
		// TODO: what if no matching multicourse?
		tracer().setMultiCourse(multis.get(data.getCourse()));
		return super.computeStatus(data);
	}

	public void registerMultiCourse(MultiCourse multiCourse) {
		multis.put(multiCourse.getCourse(), multiCourse);
	}

}
