/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;

/**
 * @author Simon Denier
 * @since Mar 25, 2012
 *
 */
public abstract class AbstractHandlerWithCourseDetector extends Control {

	protected RunnerControl runnerControl;

	protected CourseDetector courseDetector;

	public AbstractHandlerWithCourseDetector(GecoControl gecoControl, CourseDetector detector) {
		super(gecoControl);
		runnerControl = getService(RunnerControl.class);
		courseDetector = detector;
	}

}