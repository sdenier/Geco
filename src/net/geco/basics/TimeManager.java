/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.basics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public class TimeManager {
	
	public static final Date NO_TIME = new Date(90000000000L);
	public static final long NO_TIME_l = NO_TIME.getTime();
	public static final String NO_TIME_STRING = "--:--"; //$NON-NLS-1$

	private static SimpleDateFormat FORMATTER;
	private static SimpleDateFormat FORMATTER60;

	static { // have to set GMT time zone to avoid TZ offset in race time computation
		FORMATTER = new SimpleDateFormat("H:mm:ss"); //$NON-NLS-1$
		FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		FORMATTER60 = new SimpleDateFormat("m:ss"); //$NON-NLS-1$
		FORMATTER60.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
	}
	

	public static Date parse(String time, SimpleDateFormat formatter) throws ParseException {
		return formatter.parse(time);
	}

	public static Date safeParse(String time) {
		try {
			return parse(time, FORMATTER);
		} catch (ParseException e) {}
		return NO_TIME;
	}
	
	public static Date userParse(String time) throws ParseException {
		try {
			return parse(time, FORMATTER);
		} catch (ParseException e) {
			return parse(time, FORMATTER60);
		}
	}

	public static String fullTime(Date date) {
		if( date.equals(NO_TIME) )
			return NO_TIME_STRING;
		return FORMATTER.format(date);
	}

	public static String fullTime(long timestamp) {
		return fullTime(new Date(timestamp));
	}
	
	public static String time(Date date) {
		if( date.equals(NO_TIME) )
			return NO_TIME_STRING;
		if( date.getTime()<3600000 ) {
			return FORMATTER60.format(date);
		} else {
			return FORMATTER.format(date);
		}
	}
	
	public static String time(long timestamp) {
		return time(new Date(timestamp));
	}
	
	public static Date absoluteTime(Date relativeTime, long zeroTime) {
		if( relativeTime.equals(NO_TIME) ){
			return relativeTime;
		} else {
			return new Date(zeroTime + relativeTime.getTime());
		}
	}
	
	public static Date relativeTime(Date absoluteTime, long zeroTime) {
		if( absoluteTime.equals(NO_TIME) ){
			return NO_TIME;
		} else {
			return new Date(absoluteTime.getTime() - zeroTime);
		}
	}


//	Calendar day = Calendar.getInstance();
//	// reset time
//	day.set(Calendar.HOUR_OF_DAY, 0);
//	day.set(Calendar.MINUTE, 0);
//	day.set(Calendar.SECOND, 0);
//	day.set(Calendar.MILLISECOND, 0);
	
//	private Date addTime(Calendar currentDay, Date time) {
//		Calendar correctTime = Calendar.getInstance();
//		correctTime.setTime(time);
//		setField(currentDay, correctTime, Calendar.DAY_OF_MONTH);
//		setField(currentDay, correctTime, Calendar.MONTH);
//		setField(currentDay, correctTime, Calendar.YEAR);
//		return correctTime.getTime();
//	}
//	
//	private void setField(Calendar currentDay, Calendar time, int field) {
//		time.set(field, currentDay.get(field));
//	}
	
}
