/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.function;


import com.lc.basics.tools.logging.BasicLogger;

public class RunnableWithException implements Runnable
{
	public interface RunnableEx
	{
		void run() throws Exception;
	}
	BasicLogger LOGGER								= BasicLogger.getLogger(RunnableWithException.class);
	BasicLogger getLogger()
	{
		return LOGGER;
	}

	private String						m_name;
	private RunnableEx		m_runnable;

	public RunnableWithException(RunnableEx runnable)
	{
		this("NoName", runnable);
	}
	public RunnableWithException(String name, RunnableEx runnable)
	{
		m_name = name;
		m_runnable = runnable;
	}

	@Override
	public void run()
	{
		try
		{
			m_runnable.run();
		}
		catch (Exception exception)
		{
			getLogger().error(exception, "Unable to complete %s", m_name);
		}
	}

	@Override
	public String toString()
	{
		return m_name;
	}
}
