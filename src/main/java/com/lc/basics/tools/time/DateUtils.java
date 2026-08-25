/**
 * Copyright (c) 2024 LogicielCote.COM All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.time;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class DateUtils
{
	public static final long ONE_SECOND_MS = 1_000;
	public static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;
	public static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;
	public static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;

	private static final String[] DAY_OF_CALENDAR_WEEK =
			{
					"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
			};
	private static final String[] MONTH_OF_CALENDAR =
			{
					"Jan", "Feb", "Mar", "Apr", "May", "Jun",
					"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
			};

	public static String toMonthName(Calendar calendar)
	{
		return MONTH_OF_CALENDAR[calendar.get(Calendar.MONTH)];
	}
	public static String toDayName(Calendar calendar)
	{
		return DAY_OF_CALENDAR_WEEK[calendar.get(Calendar.DAY_OF_WEEK) - 1];
	}

	public static Date toDate(int year, int month, int day)
	{
		return new Date(toLong(year, month, day));
	}
	public static long toLong(int year, int month, int day)
	{
		return toLong(year, month, day, 0, 0, 0);
	}
	public static Date toDate(int year, int month, int day, int hour, int minute, int sec)
	{
		return new Date(toLong(year, month, day, hour, minute, sec));
	}
	@SuppressWarnings("ALL")
	public static long toLong(int year, int month, int day, int hour, int minute, int sec)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);
		calendar.set(year, month - 1, day, hour, minute, sec);
		return calendar.getTimeInMillis();
	}
	public static Date toBeginningOfDay(Date date)
	{
		date = null == date ? new Date() : date;
		return new Date(toBeginningOfDay(date.getTime()));
	}
	public static long toBeginningOfDay(long time)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(year, month, day, 0, 0, 0);
		time = calendar.getTimeInMillis();
		return time;
	}
	public static Date toEndOfDay(Date date)
	{
		date = null == date ? new Date() : date;
		return new Date(toEndOfDay(date.getTime()));
	}
	public static long toEndOfDay(long time)
	{
		return toBeginningOfDay(time + ONE_DAY_MS) - 1;
	}
	public static long toNextDay(long time)
	{
		long nextTime = toBeginningOfDay(time) + ONE_DAY_MS + 6 * ONE_HOUR_MS;
		long beginNext = toBeginningOfDay(nextTime);
		return beginNext;
	}

	public static Date toBeginningOfWeek(Date date)
	{
		return new Date(toBeginningOfWeek(date.getTime()));
	}
	public static long toBeginningOfWeek(long time)
	{
		time = toBeginningOfDay(time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		while (Calendar.SUNDAY != calendar.get(Calendar.DAY_OF_WEEK))
		{
			calendar.setTimeInMillis(calendar.getTimeInMillis() - ONE_DAY_MS);
		}
		return calendar.getTimeInMillis();
	}
	public static Date toEndOfWeek(Date date)
	{
		return new Date(toEndOfWeek(date.getTime()));
	}
	public static long toEndOfWeek(long time)
	{
		return 7 * ONE_DAY_MS + toBeginningOfWeek(time) - 1;
	}

	public static Date toBeginningOfMonth(Date date)
	{
		return new Date(toBeginningOfMonth(date.getTime()));
	}
	public static long toBeginningOfMonth(long time)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		calendar.set(year, month, 1, 0, 0, 0);
		time = calendar.getTimeInMillis();
		return time;
	}
	public static Date toEndOfMonth(Date date)
	{
		return new Date(toEndOfMonth(date.getTime()));
	}
	public static long toEndOfMonth(Long time)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		calendar.set(year, month, 28, 0, 1, 0);
		long timeEnd;
		while (true)
		{
			long time1 = calendar.getTimeInMillis();
			calendar.setTimeInMillis(time1 + ONE_DAY_MS);
			long time2 = calendar.getTimeInMillis();
			calendar.setTimeInMillis(time2);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			if (1 == day)
			{
				timeEnd = toEndOfDay(time1);
				break;
			}
		}
		return timeEnd;
	}

	public static Date toBeginningOfYear(Date date)
	{
		return new Date(toBeginningOfYear(date.getTime()));
	}
	public static long toBeginningOfYear(long time)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int year = calendar.get(Calendar.YEAR);
		calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0);
		time = calendar.getTimeInMillis();
		return time;
	}
	public static Date toEndOfYear(Date date)
	{
		return new Date(toEndOfYear(date.getTime()));
	}
	public static long toEndOfYear(long time)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int year = calendar.get(Calendar.YEAR);
		calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
		time = calendar.getTimeInMillis();
		return time;
	}

	public static Date toDayOfWeek(Date date, int dayOfWeek)
	{
		long sunday = toBeginningOfDay(date.getTime());
		long day = sunday + (dayOfWeek - 1) * ONE_DAY_MS;
		return new Date(day);
	}
	public static Date toTimeOfDay(Date dateMiddle, int hour, int minute, int second)
	{
		long time = toTimeOfDay(dateMiddle.getTime(), hour, minute, second);
		Date dateOut = new Date(time);
		return dateOut;
	}
	public static long toTimeOfDay(long time, int hour, int minute, int second)
	{
		long timeBegin = toBeginningOfDay(time);
		return timeBegin + hour * ONE_HOUR_MS + minute * ONE_MINUTE_MS + second * ONE_SECOND_MS;
	}

	public static long getTimeOfDay(Date date)
	{
		return getTimeOfDay(date.getTime());
	}
	public static long getTimeOfDay(long time)
	{
		return time - toBeginningOfDay(time);
	}

	public static int getDayOfWeek(Date date)
	{
		return getDayOfWeek(date.getTime());
	}
	public static int getDayOfWeek(long time)
	{
		return getCalendarField(time, Calendar.DAY_OF_WEEK);
	}
	public static int getDayOfMonth(long time)
	{
		return getCalendarField(time, Calendar.DAY_OF_MONTH);
	}
	public static int getDayOfYear(long time)
	{
		return getCalendarField(time, Calendar.DAY_OF_YEAR);
	}
	public static int getWeekOfYear(Date date)
	{
		return getWeekOfYear(date.getTime());
	}
	public static int getWeekOfYear(long time)
	{
		return getCalendarField(time, Calendar.WEEK_OF_YEAR);
	}
	public static int getMonthOfYear(Date date)
	{
		return getMonthOfYear(date.getTime());
	}
	public static int getMonthOfYear(long time)
	{
		return getCalendarField(time, Calendar.MONTH);
	}
	public static int getCalendarField(Date date, int field)
	{
		return getCalendarField(date.getTime(), field);
	}
	public static int getCalendarField(long time, int field)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int value = calendar.get(field);
		return value;
	}

	public static String getHourGroup(Date date, int hoursPerGroup)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int index = hour / hoursPerGroup;
		String key = String.format("%02d:00 -> %02d:00", index * hoursPerGroup, (index + 1) * hoursPerGroup);
		return key;
	}

	public static int getAge(LocalDate dateBirth, LocalDate dateTo)
	{
		int deltaYear = dateTo.getYear() - dateBirth.getYear();
		int deltaMonth = dateTo.getMonthValue() - dateBirth.getMonthValue();
		int deltaDay = dateTo.getDayOfMonth() - dateBirth.getDayOfMonth();
		return getAge(deltaYear, deltaMonth, deltaDay);
	}
	public static int getAge(Date dateBirth, Date dateTo)
	{
		int deltaYear = getCalendarField(dateTo, Calendar.YEAR) - getCalendarField(dateBirth, Calendar.YEAR);
		int deltaMonth = getCalendarField(dateTo, Calendar.MONTH) - getCalendarField(dateBirth, Calendar.MONTH);
		int deltaDay = getCalendarField(dateTo, Calendar.DAY_OF_MONTH) - getCalendarField(dateBirth, Calendar.DAY_OF_MONTH);
		return getAge(deltaYear, deltaMonth, deltaDay);
	}

	public static int getAge(int deltaYear, int deltaMonth, int deltaDay)
	{
		int age = deltaYear;
		if (deltaMonth < 0)
		{
			age--;
		} else if (deltaMonth == 0 && deltaDay < 0)
		{
			age--;
		}

		return age;
	}


	public static LocalDate toLocalDate(Date date)
	{
		return toLocalDate(date.getTime());
	}

	public static LocalDate toLocalDate(long time)
	{
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTimeInMillis(time);
		int year = calendar1.get(Calendar.YEAR);
		int month = calendar1.get(Calendar.MONTH) + 1;
		int day = calendar1.get(Calendar.DAY_OF_MONTH);
		return LocalDate.of(year, month, day);
	}
}
