/*
 * Copyright (c) 2017 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.thread;

import com.lc.basics.monitoring.counter.OperationContext;
import com.lc.basics.monitoring.counter.OperationCounter;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.time.WallClock;
import com.lc.basics.tools.units.TimeUnits;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongConsumer;

public class BasicTimerTask extends TimerTask implements Comparable<BasicTimerTask>
{
	private static final BasicLogger LOGGER					= BasicLogger.getLogger(BasicTimerTask.class);

	private static final AtomicInteger				PERIOD_OFFSET_MS		= new AtomicInteger(0);
	private static final int						SPACING_OFFSET_MS		= 100;
	private static final Set<BasicTimerTask>		TASK_LIST				= new TreeSet<>();

	@Override
	public int compareTo(BasicTimerTask that)
	{
		return this.getName().compareTo(that.getName());
	}

	private OperationCounter		m_counter;

	private Timer					m_timer;
	private final String			m_name;
	private final long				m_periodMS;
	private final long				m_offsetMS;
	private final LongConsumer		m_function;

	private long					m_lastTimeMS		= 0;
	private long					m_lastDurationUS	= 0;

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	public BasicTimerTask(String name, long periodMS, LongConsumer function)
	{
		m_name = name;
		m_periodMS = periodMS;
		m_offsetMS = PERIOD_OFFSET_MS.addAndGet(SPACING_OFFSET_MS);
		m_function = function;

		start();
	}
	private void start()
	{
		long		now			= System.currentTimeMillis();
		long		nowTick		= (now / getPeriodMS()) * getPeriodMS();
		long		next		= nowTick + getPeriodMS();
		long		delayMS		= next - now + m_offsetMS;

		getLogger().info("Timer %s schedule to start at %s and run every %s, offset %s",
				getName(),
				WallClock.formatTime(WallClock.FormatType.DateTime, WallClock.FormatSize.Medium, now + delayMS),
				TimeUnits.MS.format(getPeriodMS()),
				TimeUnits.MS.format(getOffsetMS()));

		m_timer = new Timer(getName(), true);
		m_timer.scheduleAtFixedRate(this, delayMS, getPeriodMS());

		m_counter = new OperationCounter(new String[]{getClass().getSimpleName(), getName()});

		TASK_LIST.add(this);
	}


	public static Set<BasicTimerTask> getTaskList()
	{
		return TASK_LIST;
	}

	@Override
	public void run()
	{
		long		timeMS		= System.currentTimeMillis();
		long		sinceMS		= timeMS - m_lastTimeMS;
		if (sinceMS < (getPeriodMS() / 2))
		{
			// Trying to run too close to last time
			getLogger().debug("Skipping timer after only %s (Period is %s)",
					TimeUnits.MS.format(sinceMS), TimeUnits.MS.format(getPeriodMS())
					);
			return;
		}

		m_lastTimeMS = timeMS;

		long				t1			= System.nanoTime();
		boolean				isSuccess	= false;
		OperationContext	context		= m_counter.begin();
		try
		{
			getLogger().debug("Running %s", getName());
			m_function.accept(m_lastTimeMS);
			isSuccess = true;
		}
		catch (Exception exception)
		{
			getLogger().error(exception, "Exception from TimerTask %s", getName());
		}
		finally
		{
			context.end(isSuccess);
		}
		long		t2		= System.nanoTime();
		m_lastDurationUS = (t2 - t1) / 1_000;
	}

	public void stop()
	{
		if (null != m_timer)
		{
			getLogger().info("Stopping Timer %s", getName());
			m_counter.close();
			m_timer.cancel();
			m_timer = null;
		}
	}

	public String getName()
	{
		return m_name;
	}
	public long getPeriodMS()
	{
		return m_periodMS;
	}
	public long getOffsetMS()
	{
		return m_offsetMS;
	}
	public long getLastTimeMS()
	{
		return m_lastTimeMS;
	}
	public long getLastDate()
	{
		return m_lastTimeMS;
	}
	public long getLastDurationUS()
	{
		return m_lastDurationUS;
	}

	@Override
	public String toString()
	{
		return String.format("%s.%s(%s + %s)",
			getClass().getSimpleName(), getName(),
			TimeUnits.MS.format(m_periodMS), TimeUnits.MS.format(m_offsetMS));
	}
}
