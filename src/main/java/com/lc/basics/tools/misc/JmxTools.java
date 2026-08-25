/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.misc;


import com.lc.basics.tools.logging.BasicLogger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Objects;

public class JmxTools
{
	public static final BasicLogger LOGGER					= BasicLogger.getLogger(JmxTools.class);

	public static final String			MBEAN_DOMAIN			= "LogicielCote";
	private static final MBeanServer	MBEAN_SERVER			= ManagementFactory.getPlatformMBeanServer();
	private static final MemoryMXBean	MEMORY_BEAN				= ManagementFactory.getMemoryMXBean();

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	public static String registerMBean(Object object, String domain, String[] nameList)
	{
		String		mbeanName		= createMBeanName(domain, nameList);
		registerMBean(object, mbeanName);
		return mbeanName;
	}
	public static void registerMBean(Object object, String mbeanName)
	{
		try
		{
			ObjectName objectName		= new ObjectName(mbeanName);
			unregisterMBean(mbeanName, false);
			MBEAN_SERVER.registerMBean(object, objectName);
		}
		catch (Exception exception)
		{
			getLogger().info(exception,"Failed to register the MBean %s", mbeanName);
		}
	}
	public static void unregisterMBean(String domain, String[] nameList)
	{
		String mbeanName		= createMBeanName(domain, nameList);
		unregisterMBean(mbeanName);
	}
	public static void unregisterMBean(String mbeanName)
	{
		unregisterMBean(mbeanName, true);
	}
	private static void unregisterMBean(String mbeanName, boolean inform)
	{
		try
		{
			ObjectName		objectName		= new ObjectName(mbeanName);
			MBEAN_SERVER.unregisterMBean(objectName);
		}
		catch (Exception exception)
		{
			if (inform)
			{
				getLogger().warn(exception, "Failed to unregister the MBean %s", mbeanName);
			}
		}
	}
	public static String createMBeanName(String domain, String[] nameList)
	{
		StringBuilder sb			= new StringBuilder(256);
		sb.append(Objects.requireNonNullElse(domain, MBEAN_DOMAIN));
		sb.append(':');

		for (int index = 0; index < nameList.length; index++)
		{
			String namePart		= nameList[index];
			if (0 != index)
			{
				sb.append(",");
			}
			sb.append(String.format("Name-%,d=%s", index, namePart));
		}
		return sb.toString();
	}

}

