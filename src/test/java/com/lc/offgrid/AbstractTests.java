/*
 * Copyright (c) 2016-2020 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid;

import com.google.gson.Gson;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.logging.EventFormatter;
import com.lc.basics.tools.units.TimeUnits;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Random;

public class AbstractTests
{
	private static final BasicLogger LOGGER						= BasicLogger.getLogger(AbstractTests.class);
	private static final Random					RANDOM						= new Random();
	private static final Gson					GSON						= new Gson();

	public static final String USER = "Test";

	public static final int		KILO	= 1024;
	public static final int		MEGA	= KILO * KILO;
	public static final int		GIGA	= KILO * KILO * KILO;

	@Autowired
	private ApplicationContext m_context;

	@Autowired
	private BeanFactory m_beanFactory;

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}
	public static Random getRandom()
	{
		return RANDOM;
	}
	public ApplicationContext getContext()
	{
		return m_context;
	}
	public BeanFactory getBeanFactory()
	{
		return m_beanFactory;
	}
	public Gson getGson()
	{
		return GSON;
	}

	protected String logTestName()
	{
		// Find the name of the caller, to specify the source of the message.
		String		testName		= getCallerName(3);

		timestamp("Running %s", testName);

		return testName;
	}
	@SuppressWarnings("rawtypes")
	public static String getCallerName(int n)
	{
		// Find the name of the caller, to specify the source of the message.
		StackTraceElement[]		stack		= Thread.currentThread().getStackTrace();
		StackTraceElement		frame		= stack[n];
		String					className	= frame.getClassName();
		try
		{
			Class					clazz		= Class.forName(className);
			className = clazz.getSimpleName();
		}
		catch (ClassNotFoundException e)
		{
			// Continue using the long class name
		}

		return String.format("%s.%s", className, frame.getMethodName());
	}

	public void timestamp(String format, Object ... args)
	{
		timestamp(null, format, args);
	}
	public void timestamp(Throwable throwable, String format, Object ... args)
	{
		log(Level.INFO, throwable, format, args);
	}
	public void log(Level level, Throwable throwable, String format, Object ... args)
	{
		if (getLogger().isLevelEnabled(level))
		{
			String			message		= EventFormatter.formatMessage(throwable, format, args);
			message = message.substring(24);
			getLogger().log(level, throwable, message);
		}
	}

	public interface MeasureFunction
	{
		long measure() throws Exception;
	}
	public void measurePerformance(String message, MeasureFunction function) throws Exception
	{
		long		t1		= System.nanoTime();
		long		count	= function.measure();
		long		t2		= System.nanoTime();
		reportPerformance(message, (t2 - t1), count, true);
	}
	public static long reportPerformance(String message, long ns, long count, boolean showResult)
	{
		long		nsPer	= ns / (0 == count ? 1 : count);
		double		rate	= 1000. * 1000 * 1000 / nsPer;

		if (showResult)
		{
			getLogger().info("%14s / %,7d items = %12s / item == %,10.1f item / sec for %s",
					TimeUnits.NS.format(ns), count,
					TimeUnits.NS.format(nsPer), rate, message);
//			BasicTools.wait(50);
		}

		return nsPer;
	}
}
