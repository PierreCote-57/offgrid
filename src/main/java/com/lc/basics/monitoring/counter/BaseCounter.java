/*
 * Copyright (c) 2020 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.monitoring.counter;

import com.lc.basics.tools.misc.JmxTools;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseCounter implements Closeable
{
	private static final List<BaseCounter>			ALL_COUNTERS		= new LinkedList<>();
	private static final int						UPDATE_PERIOD_MS	= 15000;

	static
	{
		long		now			= System.currentTimeMillis();
		long		nowTick		= (now / UPDATE_PERIOD_MS) * UPDATE_PERIOD_MS;
		long		next		= nowTick + UPDATE_PERIOD_MS;
		long		delay		= next - now;
		delay += 2000;

		new Timer(BaseCounter.class.getSimpleName(), true)
				.scheduleAtFixedRate(new CounterLatchTask(), delay, UPDATE_PERIOD_MS);				// 2
	}

	private final String		m_mbeanName;

	public BaseCounter(String[] nameList)
	{
		m_mbeanName		= JmxTools.registerMBean(this, null, nameList);

		synchronized (ALL_COUNTERS)
		{
			ALL_COUNTERS.add(this);
		}
	}

	abstract protected void update(int updatePeriodMS);

	/**
	 * Closes this stream and releases any system resources associated
	 * with it. If the stream is already closed then invoking this
	 * method has no effect.
	 *
	 * <p> As noted in {@link AutoCloseable#close()}, cases where the
	 * close may fail require careful attention. It is strongly advised
	 * to relinquish the underlying resources and to internally
	 * <em>mark</em> the {@code Closeable} as closed, prior to throwing
	 * the {@code IOException}.
	 */
	@Override
	public void close()
	{
		synchronized (ALL_COUNTERS)
		{
			ALL_COUNTERS.remove(this);
		}
		JmxTools.unregisterMBean(m_mbeanName);
	}

	public String getName()
	{
		return m_mbeanName;
	}






	private static class CounterLatchTask extends TimerTask
	{
		@Override
		public void run()
		{
			synchronized (ALL_COUNTERS)
			{
				for (BaseCounter counter : ALL_COUNTERS)
				{
					counter.update(UPDATE_PERIOD_MS);
				}
			}
		}
	}
}
