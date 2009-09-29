/**
 * Copyright (c) 2009 Simon Denier
 */
package test.valmo.geco.control;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Simon Denier
 * @since Feb 14, 2009
 *
 */
public class TestTimePattern {

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("H:mm:ss Z");
	private static final SimpleDateFormat FORMATTER60 = new SimpleDateFormat("m:ss Z");
	private static final SimpleDateFormat FORMATTERMin = new SimpleDateFormat("k:mm:ss Z");
	private static final SimpleDateFormat FORMATTERp = (SimpleDateFormat) DateFormat.getTimeInstance();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FORMATTER.applyPattern("H:mm:ss Z");
		FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
		time(0);
		time(3600);
		time(3600000);
		time(-31240000);
	}
	
	public static void time(long time) {
		Date date = new Date(time);
		System.out.println(FORMATTER.format(date));
		System.out.println(FORMATTER60.format(date));
		System.out.println(FORMATTERMin.format(date));
		System.out.println(FORMATTERp.format(date));
		System.out.println();
	}

}
