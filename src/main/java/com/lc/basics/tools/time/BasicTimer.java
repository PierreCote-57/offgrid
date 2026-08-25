/*
 * Copyright (c) 2015 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.time;

import com.lc.basics.tools.units.TimeUnits;

import java.io.PrintStream;

/**
 * BasicTimer: Timer using the highest resolution available on the system.
 *      This is based on java's System.nanoTime().
 *
 * Typical usage is in debugging environment, where the result is sent to the logger.
 * By returning the delay to the caller, additional capabilities can be added.
 *
 * The start time is reset when start() is called.
 * IF start() is not called, then the beginning of time is when the object was constructed...
 * meaning start() can be omitted in most cases.
 * This is both a re-usable object AND lap can be called multiple times.
 *
 * Timing, as of 2015
 * lap() does not cause output, it locally caches the result for better performance and accuracy.
 * lap() itself takes approximately 2 microSeconds to execute. That is the resolution limit.
 * (On my machine, the basic call System.nanoTime() takes approximately 500 nanoSeconds to execute!)
 * showResults() writes all recorded laps to the logger.
 *
 * Example:
 *              BasicTimer    timer   = new BasicTimer();
 *              ...
 *              timer.start();
 *              ...
 *              timer.lap("End of part 1");
 *              ...
 *              timer.lap("End of part 2");
 *              ...
 *              timer.lap("End of operation");
 *              timer.showResults();
 *
 * @author <a href="mailto:Pierre@LogicielCote.com"> </a>
 *
 */
@SuppressWarnings("unused")
public final class BasicTimer
{
	private static final String			LINE_SEPARATOR		= System.getProperty("line.separator");

	private String					m_name;
	private long					m_startTime				= System.nanoTime();
	private long					m_previousTime			= m_startTime;
	private SimpleCheckpoint[]		m_checkpointList;
	private int						m_checkpointCount		= 0;
	private int						m_checkpointCountMax	= 50;
	private String					m_format				= "%3$13s (%4$13s) %1$s (%2$s)";

	/**
	 * Creates a (initialized) timer object.
	 *
	 * @param name	Name of the timer, to differentiate it from potential other timers
	 *				operating a the same time.
	 */
	public BasicTimer(String name)
	{
			m_name = name;
			start();
	}

	/**
	 * Allow the caller to fine tune the format of the output.
	 *
	 * @param format        Caller specified format for the output string.
	 */
	public void setFormat(String format)
	{
			m_format = format;
	}

	/**
	 * Resets the time considered as 0 by the timer.
	 *      NOTE: The list of laps is also cleared.
	 */
	public void start()
	{
		start(m_checkpointCountMax);
	}

	/**
	 * Resets the time considered as 0 by the timer.
	 *      NOTE: The list of laps is also cleared.
	 *
	 * @param maxCheckpoint	Pre-allocate checkpoint objects for this many checkpoints.
	 */
	public void start(int maxCheckpoint)
	{
		m_checkpointCountMax = maxCheckpoint;
		m_checkpointList = new SimpleCheckpoint[m_checkpointCountMax];
		for (int i = 0; i < m_checkpointCountMax; i++)
		{
			m_checkpointList[i] = new SimpleCheckpoint();
		}

		m_checkpointCount = 0;
		m_startTime = System.nanoTime();
		m_previousTime = m_startTime;
	}

	/**
	 * Records the current time, for later output.
	 *
	 * @param message       Description of current lap event.
	 * @return                      Time since the start of time for the timer.
	 */
	public long checkpoint(String message)
	{
			long    stopTime        = System.nanoTime();

			m_checkpointList[m_checkpointCount].setMessage(message);
			m_checkpointList[m_checkpointCount].setStopTime(stopTime);
			m_checkpointCount++;

			long delay = stopTime - m_previousTime;
			m_previousTime = stopTime;

			return delay;
	}
	public long stop()
	{
		checkpoint("Stopping");

		return m_checkpointList[m_checkpointCount - 1].getStopTime() - m_startTime;
	}

	/**
	 * Obtain a string version of the list of laps for the StopWatch.
	 * @return	A multi-lines string showing the timing of the checkpoints
	 */
	@SuppressWarnings("PMD.UseStringBufferForStringAppends")
	public String getResults()
	{
		StringBuilder		result		= new StringBuilder(m_checkpointCount * 100);

		long    previousTime    = m_startTime;
		for (int i = 0; i < m_checkpointCount; i++)
		{
			SimpleCheckpoint lap = m_checkpointList[i];
			result.append(formatLap(lap, previousTime));
			result.append(LINE_SEPARATOR);
			previousTime = lap.getStopTime();
		}

		return result.toString();
	}
	/**
	 * Display the time for each lap since the beginning of time.
	 *
	 * @param printStream        Optional printStream to use for output.
	 *                           If no printStream is specified, System.out is used.
	 */
	public void showResults(PrintStream printStream)
	{
		if (null == printStream)
		{
			printStream = System.out;
		}
		long    previousTime    = m_startTime;
		for (int i = 0; i < m_checkpointCount; i++)
		{
			SimpleCheckpoint lap = m_checkpointList[i];
			printStream.println(formatLap(lap, previousTime));
			previousTime = lap.getStopTime();
		}
	}

	/**
	 * In case someone cares (e.g. PierreTest)
	 *
	 * @return	The current number of laps.
	 */
	public int getCheckpointCount()
	{
		return m_checkpointCount;
	}
	private String formatLap(SimpleCheckpoint lap, long previousTime)
	{
		String result;

		if (0 == previousTime)
		{
			previousTime = m_startTime;
		}

		result = String.format(m_format,
				m_name, lap.getMessage(),
				TimeUnits.NS.format(lap.getStopTime() - m_startTime),
				TimeUnits.NS.format(lap.getStopTime() - previousTime));

		return result;
    }

	public long getCheckpointTimeNS(String name)
	{
		for (int i = 0; i < m_checkpointCount; i++)
		{
			SimpleCheckpoint checkpoint = m_checkpointList[i];
			if (name.equals(checkpoint.getMessage()))
			{
				return checkpoint.getStopTime();
			}
		}
		return 0;
	}
	public long getTimeDeltaNS(String nameFrom, String nameTo)
	{
		long		timeFrom	= getCheckpointTimeNS(nameFrom);
		long		timeTo		= getCheckpointTimeNS(nameTo);

		return timeTo - timeFrom;
	}

	private static class SimpleCheckpoint
    {
		String m_message;
		long            m_stopTime;
		/**
		 * @return Returns the message.
		 */
		public String getMessage()
		{
				return m_message;
		}
		/**
		 * @param message The message to set.
		 */
		public void setMessage(String message)
		{
				m_message = message;
		}
		/**
		 * @return Returns the stopTime.
		 */
		public long getStopTime()
		{
				return m_stopTime;
		}
		/**
		 * @param stopTime The stopTime to set.
		 */
		public void setStopTime(long stopTime)
		{
				m_stopTime = stopTime;
		}
		@Override
		public String toString()
		{
			return String.format("%-30s: %,d", m_message, m_stopTime);
		}
    }

	/**
	 * 	Standard toString() method
	 */
	public String toString()
	{
		return m_name;
	}
}
