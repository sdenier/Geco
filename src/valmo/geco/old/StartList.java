/**
 * Copyright (c) 2008 Simon Denier
 */
package valmo.geco.model;

/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class StartList {
	
	private Course course;
	
	private TimeSlot[] slots;
	
	
	public static class TimeSlot {
		
		private int starttime;
		
		private Runner runner;
	}

}
