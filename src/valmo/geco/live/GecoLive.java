/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import valmo.geco.model.Course;
import valmo.geco.model.impl.POFactory;
import valmo.geco.model.xml.CourseSaxImporter;

/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class GecoLive {

//	150dpi
	private static final int yTrans = -25;
	private static final int xTrans = -18;
	private static final float factor = 150 / 25.4f; // dpi / mm/inch 
	private static final float xFactor = factor;
	private static final float yFactor = factor;
	private static Map<String, MapControl> controls;

//	300 dpi
//	private static final int yTrans = -25;
//	private static final int xTrans = -18;
//	private static final float xFactor = 11.8f;
//	private static final float yFactor = xFactor;

	public static void main(String[] args) {
		JFrame jFrame = new JFrame();
		GecoMapComponent map = new GecoMapComponent();
//		map.loadMapImage("hellemmes_all150.jpg");
		map.loadMapImage("hellemmes.jpg");
		
		try {
			CourseSaxImporter importer = new CourseSaxImporter(new POFactory());
			importer.importFromXml("hellemmes.xml");
			controls = createMapControlsFrom(importer.controls());
			Map<String, Punch> courses = createCoursesFrom(importer.courses());
//			map.setControls(controls.values());
//			map.showTrace(courses.get("A"));
//			map.showTrace(createPunchTrace(controls));
			map.showTrace(createPunchTraceA(courses.get("A")));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		jFrame.add(new JScrollPane(map));
		jFrame.pack();
		jFrame.setVisible(true);
	}

	public static Map<String, MapControl> createMapControlsFrom(Map<String, Float[]> controls) {
		Point mapOrigin = new Point();
		Map<String, MapControl> mCtr = new HashMap<String, MapControl>();
		for (String controlId : controls.keySet()) {
			Float[] origin = controls.get(controlId);
			 Point position = createPointFrom(origin);
			if( controlId.equals("Map") ) {
				mapOrigin = position;
			} else {
				mCtr.put(controlId, new MapControl(controlId, position));
			}
		}
		for (MapControl mapControl : mCtr.values()) {
			mapControl.getPosition().translate( xTrans - mapOrigin.x, yTrans - mapOrigin.y);
		}
		return mCtr;
	}

	public static Map<String, Punch> createCoursesFrom(Vector<Course> courses) {
		HashMap<String,Punch> startPunches = new HashMap<String, Punch>();
		for (Course course : courses) {
			Punch previousPunch = createPunch("S1");
			startPunches.put(course.getName(), previousPunch);
			for (int code : course.getCodes()) {
				Punch punch = createPunch(Integer.toString(code));
				if( previousPunch!=null ) {
					previousPunch.setNextPunch(punch);
				}
				previousPunch = punch;
			}
			previousPunch.setNextPunch(createPunch("F1"));
		}
		return startPunches;
	}

	private static Punch createPunch(String code) {
		return new Punch(controls.get(code));
	}


	private static Point createPointFrom(Float[] origin) {
		float x = origin[0].floatValue() * xFactor;
		float y = origin[1].floatValue() * -1 * yFactor;
		return new Point((int) x, (int) y);
	}
	
	private static Punch createPunchTrace(Map<String, MapControl> controls) {
		Punch start = createPunch("31");
		Punch punch2 = createPunch("32");
		Punch punch3 = createPunch("33");
		Punch punch4 = createPunch("41");
		Punch punch5 = createPunch("36");
		start.setNextPunch(punch2);
		punch2.setNextPunch(punch3);
		punch2.nextPunchMissed();
		punch2.setNextPunch(punch4);
		punch3.setNextPunch(punch5);
		punch4.setNextPunch(punch5);
		start.beOk();
		punch2.beOk();
		punch4.beAdded();
		punch5.beOk();
		return start;
	}

	/**
	 * @param punch
	 * @return
	 */
	private static Punch createPunchTraceA(Punch punch) {
		Punch startPunch = punch.clone(); // start control
		Punch previousPunch = startPunch;
		String[] traceString = new String[] {
				"31", "+40", "32", "-33+41", "-34", "35", "36", "+38", "37", "38", "39", "-31"
		};
		for (String code : traceString) {
			if( code.startsWith("-") ) {
				Punch nextPunch = previousPunch.getNextPunch().getNextPunch();
				if( !previousPunch.isAdded() ) {
					previousPunch.nextPunchMissed();
				} else {
					previousPunch.getNextPunch().beMissed();
				}
				previousPunch.setNextPunch(nextPunch);
				if( code.contains("+") ) {
					Punch addedPunch = createPunch(code.substring(code.indexOf("+") + 1));
					addedPunch.beAdded();
					addedPunch.setNextPunch(previousPunch.getNextPunch());
					previousPunch.setNextPunch(addedPunch);
					previousPunch = previousPunch.getNextPunch();
				}
			} else {
				if( code.startsWith("+") ) {
					Punch addedPunch = createPunch(code.substring(1));
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
