/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */
package com.lc.basics.monitoring.thread;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.misc.JmxTools;
import com.lc.basics.tools.units.TimeUnits;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.stereotype.Component;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.Closeable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

@Component
public class ThreadCpuMonitor extends TimerTask implements ThreadCpuMonitorMBean, SelfNaming
{
//	public static final String					MBEAN_FORMAT		= "LC:Name=CpuUtilization,Thread=%s";
	private static final BasicLogger LOGGER				= BasicLogger.getLogger(ThreadCpuMonitor.class);
	private static final Timer					TIMER				= new Timer();
//	private static final MBeanServer 			MBEAN_SERVER		= ManagementFactory.getPlatformMBeanServer();
	private static final ThreadMXBean			THREAD_BEAN			= ManagementFactory.getThreadMXBean();
	private static ThreadCpuMonitor				INSTANCE			= null;

	private final Map<String, ThreadData>		m_threadDataMap		= new TreeMap<>();
	private double								m_thresholdPct		= 1.0;
	private long								m_timeNS			= System.nanoTime();
	private long								m_durationNS		= 0;
	private long								m_updateTimeMS		= System.currentTimeMillis();
	private Date								m_updateDate		= new Date(m_updateTimeMS);

	private ThreadCpuMonitor()
	{
		INSTANCE = this;
		start(15_000);

//		registerMBean(this, "Summary");
	}
	/**
	 * Return the {@code ObjectName} for the implementing object.
	 *
	 * @throws MalformedObjectNameException if thrown by the ObjectName constructor
	 * @see ObjectName#ObjectName(String)
	 * @see ObjectName#getInstance(String)
	 * @see ObjectNameManager#getInstance(String)
	 */
	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException
	{
		String[]		nameList		= new String[] {ThreadCpuMonitor.class.getSimpleName(), "Summary"};
		String			mbeanName		= JmxTools.createMBeanName(null, nameList);
		ObjectName		objectName		= new ObjectName(mbeanName);
		return objectName;
	}

	private static void registerMBean(Object object, String name)
	{
		JmxTools.registerMBean(object, null, createNameList(name));
	}
	private static void unregisterMBean(String name)
	{
		try
		{
			JmxTools.unregisterMBean(null, createNameList(name));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static String[] createNameList(String threadName)
	{
		String[]		nameList		= new String[] {ThreadCpuMonitor.class.getSimpleName(), threadName};
		return nameList;
	}

	public static void start(int periodMS)
	{
		long		now			= System.currentTimeMillis();
		long		nowTick		= (now / periodMS) * periodMS;
		long		next		= nowTick + periodMS;
		long		delay		= next - now;
		delay += 200;

		TIMER.scheduleAtFixedRate(INSTANCE, delay, periodMS);				// 2
	}

	public static void stop()
	{
		TIMER.cancel();
	}

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());

		long			timeNS		= System.nanoTime();
		m_durationNS = timeNS - m_timeNS;
		m_timeNS = timeNS;
		m_updateTimeMS = System.currentTimeMillis();
		m_updateDate.setTime(m_updateTimeMS);

		// Accumulate the CPU utilization...
		long[]			idList		= THREAD_BEAN.getAllThreadIds();
		Set<String>		activeSet	= new HashSet<>();
		for (long id : idList)
		{
			ThreadInfo			info			= THREAD_BEAN.getThreadInfo(id);
			if (null == info)
			{
				continue;
			}
			String				threadName		= info.getThreadName();
			String				cleanName		= cleanThreadName(threadName);
			long				ns				= THREAD_BEAN.getThreadCpuTime(id);
			ThreadData			threadData		= m_threadDataMap.get(cleanName);
			if (null == threadData)
			{
				threadData = new ThreadData(cleanName);
				m_threadDataMap.put(cleanName, threadData);
			}
			threadData.addNS(ns);
			activeSet.add(cleanName);
		}

		// Latch the CPU for the active threads, clean the map for no longer active threads.
		Iterator<Map.Entry<String, ThreadData>>		iterator		= m_threadDataMap.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<String, ThreadData>		entry		= iterator.next();
			String								cleanName	= entry.getKey();
			ThreadData		threadData		= entry.getValue();
			if (activeSet.contains(cleanName))
			{
				threadData.latch(m_durationNS);
			}
			else
			{
				threadData.close();
				iterator.remove();
			}
		}
	}

	public static String cleanThreadName(String threadName)
	{
		char[]		charList		= threadName.toCharArray();
		int			lastChar		= 0;
		for (int i = 0; i < charList.length; i++)
		{
			char		ch		= charList[lastChar];
			if (Character.isAlphabetic(ch) || Character.isWhitespace(ch))
			{
				lastChar++;
			}
			else
			{
				continue;
			}
		}
		String		tempName		= new String(charList, 0, lastChar);

		int			index			= tempName.lastIndexOf('(');
		if (-1 == index)
		{
			index = tempName.lastIndexOf('-');
		}
		String		cleanName		= -1 == index ? tempName : tempName.substring(0, index);

		return cleanName;
	}

	@Override
	public double getThresholdPct()
	{
		return m_thresholdPct;
	}

	@Override
	public void setThresholdPct(double thresholdPct)
	{
		m_thresholdPct = thresholdPct;
	}

	@Override
	public Map<String, String> getCpuMap()
	{
		Map<String, String>		map		= new TreeMap<>();

		for (Map.Entry<String, ThreadData> entry : m_threadDataMap.entrySet())
		{
			String			cleanName		= entry.getKey();
			ThreadData		threadData		= entry.getValue();
			double			cpuPct			= threadData.getUtilizationPct();
			if (cpuPct >= m_thresholdPct)
			{
				String			cpuText			= String.format("%.3f %%", cpuPct);
				map.put(cleanName, cpuText);
			}
		}

		return map;
	}

	@Override
	public long getLastUpdateTime()
	{
		return m_updateTimeMS;
	}

	@Override
	public Date getLastUpdateDate()
	{
		return m_updateDate;
	}

	public interface ThreadDataMBean
	{
		long getLastUpdateTime();
		Date getLastUpdateDate();

		double getUtilizationMSPerSec();
		double getUtilizationPct();
	}
	public class ThreadData implements ThreadDataMBean, Closeable
	{
		private final String	m_cleanName;
		private long			m_cpuNSSav		= 0;
		private long			m_cpuNS			= 0;
		private long			m_deltaNS		= 0;
		private long			m_durationNS	= 0;

		public ThreadData(String cleanName)
		{
			m_cleanName = cleanName;
			registerMBean(this, cleanName);
		}

		public void addNS(long cpuNS)
		{
			m_cpuNS += cpuNS;
		}

		public void latch(long durationNS)
		{
			m_durationNS	= durationNS;
			m_deltaNS		= m_cpuNS - m_cpuNSSav;
			m_cpuNSSav		= m_cpuNS;
			m_cpuNS			= 0;
		}

		@Override
		public void close()
		{
			unregisterMBean(m_cleanName);
		}

		public long getNS()
		{
			return m_cpuNSSav;
		}

		@Override
		public long getLastUpdateTime()
		{
			return m_updateTimeMS;
		}

		@Override
		public Date getLastUpdateDate()
		{
			return m_updateDate;
		}

		@Override
		public double getUtilizationMSPerSec()
		{
			return 0 == m_durationNS ? 0.0 : (m_deltaNS * 1000.0 / m_durationNS);
		}
		@Override
		public double getUtilizationPct()
		{
			return 0 == m_durationNS ? 0.0 : (m_deltaNS * 100.0 / m_durationNS);
		}

		@Override
		public String toString()
		{
			return String.format("%s: Was (%s), accumulating to (%s)",
					m_cleanName, TimeUnits.NS.format(m_cpuNSSav), TimeUnits.NS.format(m_cpuNS));
		}
	}
}
