/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.monitoring.counter;


import java.io.Closeable;

public class OperationContext implements Closeable
{
	private long				m_timeBeginNS;
	private OperationCounter	m_operation;

	public OperationContext(OperationCounter operation)
	{
		m_operation = operation;
		m_timeBeginNS = System.nanoTime();
	}

	public double end(boolean isSuccess)
	{
		long		timeEndNS		= System.nanoTime();
		double		durationMS		= (timeEndNS - m_timeBeginNS) / (1000.0 * 1000);
		m_operation.end(isSuccess, durationMS);

		return durationMS;
	}

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
	 *
	 */
	@Override
	public void close()
	{
		end(true);
	}
}
