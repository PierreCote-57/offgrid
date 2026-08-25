/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */
package com.lc.basics.monitoring.garbage;

import com.lc.basics.tools.file.BaseFileHandler;

import java.io.Closeable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class GarbageCollectorBeanMonitor implements Runnable, Comparable<GarbageCollectorBeanMonitor>, Closeable
{
	private static final RuntimeMXBean		RUNTIME_BEAN		= ManagementFactory.getRuntimeMXBean();

	private GarbageCollectorItemMonitor			m_itemCurrent;
	private GarbageCollectorItemMonitor			m_itemTotal;

	private final GarbageCollectorMXBean		m_bean;
	private final String						m_heapName;

	public GarbageCollectorBeanMonitor(GarbageCollectorMXBean bean)
	{
		m_bean = bean;

		String		heapName;
		if (2 == m_bean.getMemoryPoolNames().length)
		{
			// This is the young collector
			heapName = "Young";
		}
		else
		{
			// This is the old collector
			heapName = "Old";
		}
		m_heapName = heapName;

		m_itemCurrent = new GarbageCollectorItemMonitor(heapName, "Current", m_bean.getName());
		m_itemTotal = new GarbageCollectorItemMonitor(heapName, "Total", m_bean.getName());
	}

	public long getCount()
	{
		return m_itemCurrent.getCount();
	}

	@Override
	public void run()
	{
		long		timeMS			= RUNTIME_BEAN.getUptime();
		long		count			= m_bean.getCollectionCount();
		long		durationMS		= m_bean.getCollectionTime();

		m_itemCurrent.set(
				timeMS - m_itemTotal.getTimeMS(),
				count - m_itemTotal.getCount(),
				durationMS - m_itemTotal.getDurationMS());

		m_itemTotal.set(timeMS, count, durationMS);
	}

	@Override
	public void close()
	{
		m_itemCurrent = BaseFileHandler.closeSafe(m_itemCurrent);
		m_itemCurrent = BaseFileHandler.closeSafe(m_itemTotal);
	}

	@Override
	public String toString()
	{
		return String.format("%-5s: Current = %s; Total = %s",
				m_heapName,
				m_itemCurrent.toString(), m_itemTotal.toString());
	}

	@Override
	public int compareTo(GarbageCollectorBeanMonitor o)
	{
		return m_heapName.compareTo(o.m_heapName);
	}
}
