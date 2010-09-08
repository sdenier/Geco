/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import valmo.geco.model.Course;


/**
 * @author  Simon Denier
 * @since  Sep 5, 2010
 */
public class LiveMapControl {

	public Map<String, ControlCircle> controls;
	public Map<String, LivePunch> courses;

	private float xFactor;
	private float yFactor;

	
	public LiveMapControl() {	
		controls = Collections.emptyMap();
		courses = Collections.emptyMap();
	}
	
	
	public Map<String, ControlCircle> createControlsFrom(Map<String, Float[]> someControls, int dx, int dy) {
		Point mapOrigin = new Point();
		controls = new HashMap<String, ControlCircle>();
		for (String controlId : someControls.keySet()) {
			Float[] origin = someControls.get(controlId);
			 Point position = createPointFrom(origin);
			if( controlId.equals("Map") ) {
				mapOrigin = position;
			} else {
				controls.put(controlId, new ControlCircle(controlId, position));
			}
		}
		translateControls(dx - mapOrigin.x, dy - mapOrigin.y);
		return controls;
	}

	public void translateControls(int dx, int dy) {
		for (ControlCircle mapControl : controls.values()) {
			mapControl.getPosition().translate( dx, dy);
		}
	}

	public float xFactor() {
		return xFactor;
	}
	public void setXFactor(float xFactor) {
		this.xFactor = xFactor;
	}
	public float yFactor() {
		return yFactor;
	}
	public void setYFactor(float yFactor) {
		this.yFactor = - yFactor; // inverse coordinate in screen space
	}

	private Point createPointFrom(Float[] origin) {
		// TODO: check, is is circle center or bounding box origin?
		float x = origin[0].floatValue() * xFactor();
		float y = origin[1].floatValue() * yFactor();
		return new Point((int) x, (int) y);
	}

	public Map<String, LivePunch> createCoursesFrom(Collection<Course> someCourses) {
		courses = new HashMap<String, LivePunch>();
		for (Course course : someCourses) {
			LivePunch previousPunch = createPunch("S1");
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
			previousPunch.setNextPunch(createPunch("F1"));
		}
		return courses;
	}

	private LivePunch createPunch(String code) {
		return new LivePunch(controls.get(code));
	}

	private LivePunch createPunch(String code, int order) {
		return new LivePunch(controls.get(code), order);
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
			if( code.startsWith("-") ) {
				LivePunch nextPunch = previousPunch.getNextPunch().getNextPunch();
				if( !previousPunch.isAdded() ) {
					previousPunch.nextPunchMissed();
				} else {
					previousPunch.getNextPunch().beMissed();
				}
				previousPunch.setNextPunch(nextPunch);
				if( code.contains("+") ) {
					LivePunch addedPunch = createPunch(code.substring(code.indexOf("+") + 1));
					addedPunch.beAdded();
					addedPunch.setNextPunch(previousPunch.getNextPunch());
					previousPunch.setNextPunch(addedPunch);
					previousPunch = previousPunch.getNextPunch();
				}
			} else {
				if( code.startsWith("+") ) {
					LivePunch addedPunch = createPunch(code.substring(1));
					addedPunch.beAdded();
					addedPunch.setNextPunch(previousPunch.getNextPunch());
					previousPunch.setNextPunch(addedPunch);
				} else {
					previousPunch.getNextPunch().beOk();
				}
				previousPunch = previousPunch.getNextPunch();
			}
		}
		return startPunch;
	}
	
}