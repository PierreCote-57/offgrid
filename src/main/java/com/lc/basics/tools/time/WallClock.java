/*
 * Copyright (c) 2015 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.lc.basics.tools.time.WallClock.FormatType.DateTime;

public class WallClock
{
	public static final long		ONE_MINUTE_SEC				= 60;
	public static final long		ONE_HOUR_SEC				= 60 * ONE_MINUTE_SEC;
	public static final long		ONE_HOUR_MS					= ONE_HOUR_SEC * 1000;
	public static final long		ONE_DAY_MS					= 24 * ONE_HOUR_MS;

	public enum FormatType
	{
		Date,
		Time,
		DateTime
	}
	public enum FormatSize
	{
		Small,
		Medium,
		Large,
		XLarge
	}

	public enum FormatDate
	{
		None(null),
		USSmall("%1$tm/%1$td/%1$tY"),
		USTLA("%1$ta %1$tb %1$td, %1$tY"),
		USLarge("%1$tA, %1$tB %1$td, %1$tY"),
		INTL("%1$tY-%1$tm-%1$td"),
		INTLD("%1$ta %1$tY-%1$tm-%1$td");

		private final String m_format;
		FormatDate(String format)
		{
			m_format = format;
		}
		public String getFormat()
		{
			return m_format;
		}
	}
	public enum FormatTime
	{
		None(null),
		HM("%1$tH:%1$tM"),
		HMS("%1$tH:%1$tM:%1$tS"),
		HMSm("%1$tH:%1$tM:%1$tS.%1$tL"),
		HMZ("%1$tH:%1$tM %1$tZ");

		private final String m_format;
		FormatTime(String format)
		{
			m_format = format;
		}
		public String getFormat()
		{
			return m_format;
		}
	}

	// [Type][Size]
	private static final String[][]		FORMATTER_LIST	=
			{
					// Date
					{
							"%1$tY-%1$tm-%1$td",
							"%1$tY-%1$tm-%1$td (%1$ta)",
							"%1$ta %1$tb %1$td, %1$tY",
							"%1$tA, %1$tB %1$td, %1$tY"
					},
					// Time
					{
							"%1$tH:%1$tM",
							"%1$tH:%1$tM:%1$tS",
							"%1$tH:%1$tM:%1$tS.%1$tL",
							"%1$tI:%1$tM:%1$tS.%1$tL %1$tp %1$tZ"
					},
					// DateTime
					{
							"%1$tY-%1$tm-%1$td @ %1$tH:%1$tM:%1$tS",
							"%1$ta %1$tY/%1$tm/%1$td @ %1$tH:%1$tM:%1$tS",
							"%1$ta %1$tb %1$td, %1$tY @ %1$tH:%1$tM:%1$tS.%1$tL",
							"%1$tA, %1$tB %1$td, %1$tY @ %1$tI:%1$tM:%1$tS.%1$tL %1$tp %1$tZ"
					}
			};

	private static final TimeZone		TIME_ZONE		= TimeZone.getTimeZone("America/Los_Angeles");

	static
	{
		// Make sure the default timezone is US/Pacific
		TimeZone.setDefault(TIME_ZONE);
	}

	public static String formatTime(long timeMS)
	{
		return formatTime(DateTime, FormatSize.Large, timeMS);
	}
	public static long parseTime(String text)
	{
		String formatText = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat		formatter		= new SimpleDateFormat(formatText);
		long					time			= 0;
		try
		{
			Date date = formatter.parse(text);
			time = date.getTime();
		}
		catch (ParseException exception)
		{
			// Do nothing
			exception.printStackTrace();
		}
		return time;
	}
	public static String formatTime(FormatType formatType, FormatSize formatSize, long timeMS)
	{
		String format = FORMATTER_LIST[formatType.ordinal()][formatSize.ordinal()];
		Date date = new Date(timeMS);
		String text = String.format(format, date);
		return text;
	}
	public static String formatTime(FormatDate formatDate, FormatTime formatTime, long timeMS)
	{
		String format		= null;
		if (null != formatDate && null != formatDate.getFormat())
		{
			format = formatDate.getFormat();
		}
		if (null != formatTime && null != formatTime.getFormat())
		{
			format = null == format ? "" : format + " ";
			format += formatTime.getFormat();
		}
		if (null == format)
		{
			throw new IllegalArgumentException("Please specify at least one of formatDate or formatTime.");
		}

		Date date = new Date(timeMS);
		String text = String.format(format, date);
		return text;
	}

	public static TimeZone getTimeZone()
	{
		return TIME_ZONE;
	}

	public long getLocalTime()
	{
		return System.currentTimeMillis();
	}
	public String getLocalDate()
	{
		return WallClock.formatTime(FormatDate.INTLD, FormatTime.HMS, getLocalTime());
	}

	public static long getTimezoneOffset(long time)
	{
		return getTimezoneOffset(TIME_ZONE, time);
	}
	public static long getTimezoneOffset(TimeZone timeZone, long time)
	{
		return timeZone.getOffset(time);
	}

	public static long getBeginningOfDay(long time)
	{
		return getBeginningOfDay(null, time);
	}
	public static long getBeginningOfDay(TimeZone timeZone, Long time)
	{
		if (null == timeZone)
		{
			timeZone = TIME_ZONE;
		}
		Calendar		calendar		= Calendar.getInstance(timeZone);
		if (null != time)
		{
			calendar.setTimeInMillis(time);
		}
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis();
	}

	public static long getTimeOfDayMS(long time)
	{
		long timeMidnight = getBeginningOfDay(null, time);
		long timeOfDay = time - timeMidnight;
		return timeOfDay;
	}
}
