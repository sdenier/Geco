/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.geco.basics.TimeManager;

import org.junit.Assert;
import org.junit.Test;

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

	public static void main(String[] args) {
		FORMATTER.applyPattern("HHH:mm:ss Z");
		FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
		time(0);
		time(3600);
		time(3600000);
		time(-31240000);
		int s = 86400000;
		time(s);
		s = s /1000;
		System.out.println(String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60)));
	}
	
	public static void time(long time) {
		Date date = new Date(time);
		System.out.println(FORMATTER.format(date));
		System.out.println(FORMATTER60.format(date));
		System.out.println(FORMATTERMin.format(date));
		System.out.println(FORMATTERp.format(date));
		System.out.println();
	}
	
	@Test
	public void testComputeSplitTime(){
		Assert.assertEquals(0, TimeManager.computeSplit(10000, 10000));
		Assert.assertEquals(1000, TimeManager.computeSplit(10000, 11000));
		Assert.assertEquals(TimeManager.NO_TIME_l, TimeManager.computeSplit(15000, 11000));
		Assert.assertEquals(TimeManager.NO_TIME_l, TimeManager.computeSplit(TimeManager.NO_TIME_l, 11000));
		Assert.assertEquals(TimeManager.NO_TIME_l, TimeManager.computeSplit(15000, TimeManager.NO_TIME_l));
	}

}
