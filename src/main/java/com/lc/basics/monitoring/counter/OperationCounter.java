/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.monitoring.counter;

import com.lc.basics.monitoring.statistics.SimpleAccumulator;

import java.util.concurrent.atomic.AtomicLong;

public class OperationCounter extends BaseCounter implements OperationCounterMBean
{

	private AtomicLong			 m_countBegin			= new AtomicLong(0);
	private AtomicLong			m_countBeginSav			= new AtomicLong(0);
	private AtomicLong			m_countSuccess			= new AtomicLong(0);
	private AtomicLong			m_countFailure			= new AtomicLong(0);

	// Apply only to the m_beanClone instance...
	private double				m_rate					= 0;

	private SimpleAccumulator	m_accumulatorTimeMS			= new SimpleAccumulator();
	private SimpleAccumulator	m_accumulatorTimeMSLatched	= new SimpleAccumulator();


	public OperationCounter(String[] nameList)
	{
		super(nameList);
	}

	public OperationContext begin()
	{
		m_countBegin.incrementAndGet();

		OperationContext		context		= new OperationContext(this);
		return context;
	}

	void end(boolean isSuccess, double durationMS)
	{
		synchronized (this)
		{
			if (isSuccess)
			{
				m_countSuccess.incrementAndGet();
			}
			else
			{
				m_countFailure.incrementAndGet();
			}

			m_accumulatorTimeMS.record(durationMS);
		}
	}

	protected void update(int updatePeriodMS)
	{
		synchronized (this)
		{
			m_rate = (m_countBegin.get() - m_countBeginSav.get()) / (updatePeriodMS / 1000.0);
			m_countBeginSav.set(m_countBegin.get());
			m_accumulatorTimeMS.cloneAndReset(m_accumulatorTimeMSLatched);
		}
	}

	//	**************************************************
	//	MBean methods
	//	**************************************************
	public long getCountBegin()
	{
		return m_countBegin.get();
	}

	public long getCountSuccess()
	{
		return m_countSuccess.get();
	}

	public long getCountFailure()
	{
		return m_countFailure.get();
	}

	public long getCountOutstanding()
	{
		return m_countBegin.get() - (m_countSuccess.get() + m_countFailure.get());
	}

	public double getRate()
	{
		return m_rate;
	}

	public double getDurationCount()
	{
		return m_accumulatorTimeMSLatched.getCount();
	}
	public double getDurationMinMS()
	{
		return m_accumulatorTimeMSLatched.getMin();
	}
	public double getDurationAvgMS()
	{
		return m_accumulatorTimeMSLatched.getAvg();
	}
	public double getDurationMaxMS()
	{
		return m_accumulatorTimeMSLatched.getMax();
	}
	public double getDurationStdMS()
	{
		return m_accumulatorTimeMSLatched.getStd();
	}

	@Override
	public String toString()
	{
		return String.format("%s(%s)", getClass().getSimpleName(), getName());
	}
}
