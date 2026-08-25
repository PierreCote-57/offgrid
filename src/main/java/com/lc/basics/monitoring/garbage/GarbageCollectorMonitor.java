/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */
package com.lc.basics.monitoring.garbage;


import com.lc.basics.tools.logging.BasicLogger;
import org.springframework.beans.factory.DisposableBean;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

//@Component
public class GarbageCollectorMonitor extends TimerTask implements DisposableBean
{
	private static final BasicLogger LOGGER			= BasicLogger.getLogger(GarbageCollectorMonitor.class);
	private static final Timer						TIMER			= new Timer();
	private static GarbageCollectorMonitor 			INSTANCE;

	private final Collection<GarbageCollectorBeanMonitor>		m_beanMonitorList	= new TreeSet<>();

	public GarbageCollectorMonitor()
	{
		List<GarbageCollectorMXBean>	beanList			= ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean bean : beanList)
		{
			m_beanMonitorList.add(new GarbageCollectorBeanMonitor(bean));
		}
		INSTANCE = this;
		start(60_000);
	}

	private static BasicLogger getLogger()
	{
		return LOGGER;
	}

	public static void start(int periodMS)
	{
		long		now			= System.currentTimeMillis();
		long		nowTick		= (now / periodMS) * periodMS;
		long		next		= nowTick + periodMS;
		long		delay		= next - now;
		delay += 1000;

		TIMER.scheduleAtFixedRate(INSTANCE, delay, periodMS);				// 2
	}
	public static void stop()
	{
		TIMER.cancel();
	}

	@Override
	public void run()
	{
		for (GarbageCollectorBeanMonitor beanMonitor : m_beanMonitorList)
		{
			beanMonitor.run();
		}
	}

	@Override
	public void destroy() throws Exception
	{
		TIMER.cancel();
		for (GarbageCollectorBeanMonitor beanMonitor : m_beanMonitorList)
		{
			beanMonitor.close();
		}
	}
}
