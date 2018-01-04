package com.skyline.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {
	public enum Unit {
		millisecond, second, minute, hour, day, month, year
	}
	
	public static Calendar getCalendar() {
		return getCalendar(TimeZone.getTimeZone("GMT+8"));
	}
	
	public static Calendar getCalendar(TimeZone timeZone) {
		TimeZone.setDefault(timeZone);
		return Calendar.getInstance();
	}
	
	public static Date getDateNow(TimeZone timeZone) {
		return getCalendar(timeZone).getTime();
	}
	
	public static Date getDateNow() {
		return getCalendar().getTime();
	}
	
	public static Date getOffsetDate(Date date, int index, Unit unit) {
		if(date == null || unit == null) return null;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int temp = 0;
		switch (unit) {
		case millisecond:
			temp = Calendar.MILLISECOND;
			break;
		case second:
			temp = Calendar.SECOND;
			break;
		case minute:
			temp = Calendar.MINUTE;
			break;
		case hour:
			temp = Calendar.HOUR;
			break;
		case day:
			temp = Calendar.DATE;
			break;
		case month:
			temp = Calendar.MONTH;
			break;
		case year:
			temp = Calendar.YEAR;
			break;
		default:
			return null;
		}
		index += c.get(temp);
		c.set(temp, index);
		return c.getTime();		
	}
	
	public static Date getOffsetDate(int index, Unit unit, TimeZone timeZone) {
		return getOffsetDate(getDateNow(timeZone), index, unit);
	}
	
	public static Date getOffsetDate(int index, Unit unit) {
		return getOffsetDate(getDateNow(), index, unit);
	}
	
	public static Date getOffsetDate(int index, TimeZone timeZone) {
		return getOffsetDate(getDateNow(timeZone), index, Unit.day);
	}
	
	public static Date getOffsetDate(int index) {
		return getOffsetDate(getDateNow(), index, Unit.day);
	}
	
	public static void main(String[] args) {
		System.out.println(getDateNow());
	}
}
