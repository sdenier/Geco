/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.geco.model.Course;



/**
 * @author  Simon Denier
 * @since  Sep 5, 2010
 */
public class LiveMapControl {

	private Map<String, ControlCircle> controls;

	private Map<String, LivePunch> courses;
	
	public LiveMapControl() {	
		controls = Collections.emptyMap();
		courses = Collections.emptyMap();
	}
	
	public void createControls(Map<String, Float[]> someControls, float dpmmFactor) {
		Point origin = createPointFrom(someControls.get("Map"), dpmmFactor); //$NON-NLS-1$
		controls = new HashMap<String, ControlCircle>();
		for (String controlId : someControls.keySet()) {
			if( !controlId.equals("Map") ) { //$NON-NLS-1$
				Point position = createPointFrom(someControls.get(controlId), dpmmFactor);
				ControlCircle mapControl = new ControlCircle(controlId, position);
				controls.put(controlId, mapControl);
				mapControl.translate(-origin.x, -origin.y);
			}
		}
	}

	private Point createPointFrom(Float[] position, float dpmmFactor) {
		float x = position[0].floatValue() * dpmmFactor;
		float y = position[1].floatValue() * -dpmmFactor; // inverse y coordinate
		return new Point((int) x, (int) y);
	}
	
	public void translateControls(int dx, int dy) {
		for (ControlCircle control : controls.values()) {
			control.translate(dx, dy);
		}
	}

	public void adjustControls(int ox, int oy, float fx, float fy) {
		for (ControlCircle control : controls.values()) {
			Point point = control.getPosition();
			float ddx = (point.x - ox) * (fx - 1);
			float ddy = (point.y - oy) * (fy - 1);
			control.translate((int) ddx, (int) ddy);
		}
	}

	public Map<String, LivePunch> createCoursesFrom(Collection<Course> someCourses) {
		courses = new HashMap<String, LivePunch>();
		for (Course course : someCourses) {
			try {
				LivePunch previousPunch = createPunch("S1"); //$NON-NLS-1$
				courses.put(course.getName(), previousPunch);
				int i = 1;
				for (int code : course.getCodes()) {
					LivePunch punch = createPunch(Integer.toString(code), i);
					if( previousPunch!=null ) {
						previousPunch.setNextPunch(punch);
					}
					previousPunch = punch;
					i++;
				}
				previousPunch.setNextPunch(createPunch("F1")); //$NON-NLS-1$
			} catch (Exception e) {
				System.err.println("ill-formed controls"); //$NON-NLS-1$
				System.err.println(e);
			}
		}
		return courses;
	}

	private LivePunch createPunch(String code) throws Exception {
		ControlCircle control = controls.get(code);
		if( control==null ) {
			throw new Exception("Unknown Control " + code); //$NON-NLS-1$
		}
		return new LivePunch(control);
	}

	private LivePunch createPunch(String code, int order) throws Exception {
		ControlCircle control = controls.get(code);
		if( control==null ) {
			throw new Exception("Unknown Control " + code); //$NON-NLS-1$
		}
		return new LivePunch(control, order);
	}
	
	public Collection<ControlCircle> allControls() {
		return this.controls.values();
	}

	public void resetControls() {
		for (ControlCircle control : allControls()) {
			control.resetStatus();
		}
	}
	
	public LivePunch startPunchForCourse(String coursename) {
		return courses.get(coursename);
	}

	public LivePunch createPunchTraceFor(LivePunch punch, String[] traceString) {
		LivePunch startPunch = punch.clone(); // start control
		LivePunch previousPunch = startPunch;
		for (String code : traceString) {
			if( code.startsWith("-") ) { //$NON-NLS-1$
				LivePunch nextPunch = previousPunch.getNextPunch().getNextPunch();
				if( !previousPunch.isAdded() ) {
					previousPunch.nextPunchMissed();
				} else {
					previousPunch.getNextPunch().beMissed();
				}
				previousPunch.setNextPunch(nextPunch);
				if( code.contains("+") ) { //$NON-NLS-1$
					try {
						LivePunch addedPunch = createPunch(code.substring(code.indexOf("+") + 1)); //$NON-NLS-1$
						addedPunch.beAdded();
						addedPunch.setNextPunch(previousPunch.getNextPunch());
						previousPunch.setNextPunch(addedPunch);
						previousPunch = previousPunch.getNextPunch();
					} catch (Exception e) {
						System.err.println(e);
					}
				}
			} else {
				if( code.startsWith("+") ) { //$NON-NLS-1$
					try {
						LivePunch addedPunch = createPunch(code.substring(1));
						addedPunch.beAdded();
						addedPunch.setNextPunch(previousPunch.getNextPunch());
						previousPunch.setNextPunch(addedPunch);
					} catch (Exception e) {
						System.err.println(e);
					}
				} else {
					previousPunch.getNextPunch().beOk();
				}
				previousPunch = previousPunch.getNextPunch();
			}
		}
		return startPunch;
	}
	
}