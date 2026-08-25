/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */
package com.lc.basics.monitoring.garbage;

import com.lc.basics.tools.misc.JmxTools;

import java.io.Closeable;
import java.util.Date;

public class GarbageCollectorItemMonitor implements Comparable<GarbageCollectorItemMonitor>, GarbageCollectorItemMonitorMBean, Closeable
{
	private String			m_name;
	private long			m_measureTimeMS;
	private Date			m_measureDate		= new Date();
	private long			m_timeMS			= 0;
	private long			m_count				= 0;
	private long			m_durationMS		= 0;
	private String			m_mbeanName			= null;

	public GarbageCollectorItemMonitor(String heapName, String typeName, String collectorName)
	{
		m_name = collectorName;

		String[]		nameList	= new String[3];
		nameList[0] = "GarbageCollector";
		nameList[1] = heapName;
		nameList[2] = typeName;

		try
		{
			m_mbeanName = JmxTools.registerMBean(this, null, nameList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void set(long timeMS, long count, long durationMS)
	{
		m_timeMS		= timeMS;
		m_count			= count;
		m_durationMS	= durationMS;

		m_measureTimeMS = System.currentTimeMillis();
		m_measureDate.setTime(m_measureTimeMS);
	}

	@Override
	public long getMeasureTimeMS()
	{
		return m_measureTimeMS;
	}
	@Override
	public Date getMeasureDate()
	{
		return m_measureDate;
	}

	public long getTimeMS()
	{
		return m_timeMS;
	}

	public long getCount()
	{
		return m_count;
	}

	public long getDurationMS()
	{
		return m_durationMS;
	}

	@Override
	public void setName(String name)
	{
		m_name = name;
	}

	@Override
	public String getName()
	{
		return m_name;
	}
	@Override
	public double getPeriodSec()
	{
		return 0 == m_count ? 0.0 : (m_timeMS / 1000.0) / m_count;
	}
	@Override
	public double getDurationAvgMS()
	{
		return 0 == m_count ? 0.0 : m_durationMS * 1.0 / m_count;
	}
	@Override
	public double getDutyCyclePct()
	{
		return 0 == m_timeMS ? 0.0 : (m_durationMS) * 100.0 / m_timeMS;
	}
	@Override
	public void reset(String reason)
	{
		System.out.println("Reset because of " + reason);
	}

	@Override
	public void close()
	{
		if (null != m_mbeanName)
		{
			JmxTools.unregisterMBean(m_mbeanName);
		}
	}

	@Override
	public String toString()
	{
		return String.format("%,d * (%.1f ms / %.3f sec) = %.1f %%",
				getCount(), getDurationAvgMS(), getPeriodSec(), getDutyCyclePct());
	}

	@Override
	public int compareTo(GarbageCollectorItemMonitor o)
	{
		return m_name.compareTo(o.m_name);
	}
}
