/*
 * Copyright (c) 2015 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"PMD", "rawtypes"})
public class BasicLogger
{
	private static final Map<String, BasicLogger>		LOGGER_MAP		= new HashMap<>();

	private final Logger m_logger;

	private BasicLogger(Class clazz)
	{
		m_logger = LogManager.getLogger(clazz);
	}
	private BasicLogger(String name)
	{
		m_logger = LogManager.getLogger(name);
	}

	public synchronized static BasicLogger getLogger(Class clazz)
	{
		return getLogger(clazz.getName());
	}
	public synchronized static BasicLogger getLogger(String name)
	{
		BasicLogger logger = LOGGER_MAP.get(name);
		if (logger == null)
		{
			logger = new BasicLogger(name);
			LOGGER_MAP.put(name, logger);
		}
		return logger;
	}

	public Logger getWrappedLogger()
	{
		return m_logger;
	}

	public boolean isDebugEnabled()
	{
		return isLevelEnabled(Level.DEBUG);
	}
	public boolean isLevelEnabled(Level level)
	{
		return getWrappedLogger().isEnabled(level);
	}

	public void log(Level level, String format, Object...args)
	{
		log(level, null, format, args);
	}

	public void log(Level level, Throwable throwable, String format, Object...args)
	{
		if (isLevelEnabled(level))
		{
			String					messageText		= getMessage(format, args);
			getWrappedLogger().log(level, messageText, throwable);
		}
	}

	public void trace(String format, Object...args)
	{
		trace(null, format, args);
	}
	public void trace(Throwable throwable, String format, Object...args)
	{
		log(Level.TRACE, throwable, format, args);
	}

	public void debug(String format, Object...args)
	{
		debug(null, format, args);
	}
	public void debug(Throwable throwable, String format, Object...args)
	{
		log(Level.DEBUG, throwable, format, args);
	}

	public void info(String format, Object...args)
	{
		info(null, format, args);
	}
	public void info(Throwable throwable, String format, Object...args)
	{
		log(Level.INFO, throwable, format, args);
	}

	public void warn(String format, Object...args)
	{
		warn(null, format, args);
	}
	public void warn(Throwable throwable, String format, Object...args)
	{
		log(Level.WARN, throwable, format, args);
	}

	public void error(String format, Object...args)
	{
		error(null, format, args);
	}
	public void error(Throwable throwable, String format, Object...args)
	{
		log(Level.ERROR, throwable, format, args);
	}

	private String getMessage(String format, Object...args)
	{
		// eventIndex and eventText are inserted by the logger formatter
		return String.format(format, args);
	}
}
