/*
 * Copyright (c) 2017 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */
package com.lc.basics.tools.thread;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.misc.JmxTools;

import java.io.Closeable;

/**
 * Implements a lock that allows N threads to own the lock at the same time.
 */
public class MySemaphore implements MySemaphoreMBean, Closeable
{
	private static final BasicLogger LOGGER		= BasicLogger.getLogger(MySemaphore.class);

	private final String	m_name;
	private int				m_countMax;
	private int				m_countActive		= 0;
	private int				m_countWaiting		= 0;

	public MySemaphore(String name, int countMax)
	{
		m_name = name;
		setCountMax(countMax);

		JmxTools.registerMBean(this, null, new String[]{MySemaphore.class.getSimpleName(), name});
	}

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	@Override
	public int getCountMax()
	{
		return m_countMax;
	}
	@Override
	public void setCountMax(final int countMax)
	{
		if (countMax != m_countMax)
		{
			getLogger().debug("%s changed CountMax %,d -> %,d", toString(), m_countMax, countMax);

			m_countMax = countMax;
			synchronized (this)
			{
				notifyAll();
			}
		}
	}

	public String getName()
	{
		return m_name;
	}

	@Override
	public int getCountActive()
	{
		return m_countActive;
	}
	@Override
	public int getCountWaiting()
	{
		return m_countWaiting;
	}

	/**
	 * Called to acquire the lock.
	 * This method blocks, if necessary, until it is legal (as per maximum) to enter the lock.
	 */
	public void enter()
	{
		synchronized (this)
		{
			// Do we have to wait
			boolean		isWaiting		= false;
			while (m_countActive >= m_countMax)
			{
				if (!isWaiting)
				{
					isWaiting = true;
					m_countWaiting++;
				}

				try
				{
					wait();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			if (isWaiting)
			{
				m_countWaiting--;
			}

			// Allowed to enter
			m_countActive++;
		}
	}

	/**
	 * Called to release the lock
	 */
	public void leave()
	{
		synchronized (this)
		{
			notify();
			m_countActive--;
		}
	}

	@Override
	public void close()
	{
		JmxTools.unregisterMBean(null, new String[]{MySemaphore.class.getSimpleName(), m_name});
	}

	@Override
	public String toString()
	{
		return String.format("%s(%s)", getClass().getSimpleName(), getName());
	}
}
