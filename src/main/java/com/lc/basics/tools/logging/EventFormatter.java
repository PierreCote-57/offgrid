/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.logging;

import com.lc.basics.tools.time.WallClock;
import com.lc.basics.tools.units.TimeUnits;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EventFormatter
{
	private static long			s_firstReport;
	private static long			s_lastReport;
	static
	{
		resetTimer();
	}

	public static void resetTimer()
	{
		s_firstReport = System.currentTimeMillis();
		s_lastReport = s_firstReport;
	}

	public static String formatMessage(Throwable throwable, String format, Object... args)
	{
		String		timestamp		= formatTimestamp();
		String		message			= formatRawMessage(throwable, format, args);
		String		fullMessage		= String.format("%s %-15s: %s",
			timestamp, Thread.currentThread().getName(), message);
		return fullMessage;
	}

	public static String formatRawMessage(Throwable throwable, String format, Object... args)
	{
		String		message		= String.format(format, args);
		if (null != throwable)
		{
			message = String.format("Exception in '%s' -> %s: %s\n",
				message, throwable.getClass().getSimpleName(), throwable.getMessage());
			ByteArrayOutputStream	outputStream		= new ByteArrayOutputStream();
			PrintStream				printStream			= new PrintStream(outputStream);
			throwable.printStackTrace(printStream);
			printStream.flush();
			byte[]					stackBytes			= outputStream.toByteArray();
			String					stackText			= new String(stackBytes);
			message = message + stackText;
		}
		return message;
	}
	public static String formatTimestamp()
	{
		long		now			= System.currentTimeMillis();
		String		timestamp	= String.format("%1$s (%2$15s; +%3$15s)",
			WallClock.formatTime(WallClock.FormatDate.INTL, WallClock.FormatTime.HMSm, now),
			TimeUnits.MS.format(now - s_firstReport),
			TimeUnits.MS.format(now - s_lastReport));
		s_lastReport = now;
		return timestamp;
	}
}
