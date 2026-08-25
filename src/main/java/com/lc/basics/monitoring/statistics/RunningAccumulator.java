/**
 * Copyright (c) 2023 LogicielCote.COM All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.monitoring.statistics;

import com.lc.basics.tools.misc.BasicRuntimeException;

public class RunningAccumulator implements Accumulator
{
	private int m_count;
	private int m_next;
	private double[] m_list;

	public RunningAccumulator(int count)
	{
		m_list = new double[count];
	}

	@Override
	public void record(double value)
	{
		// list is not full
		if (m_count != m_list.length)
		{
			m_count++;
		}
		m_list[m_next++] = value;
		if (m_next >= m_list.length)
		{
			m_next = 0;
		}
	}

	@Override
	public void record(Accumulator accumulator)
	{
		throw new BasicRuntimeException("NYI");
	}

	@Override
	public Accumulator getBlankClone()
	{
		throw new BasicRuntimeException("NYI");
	}

	@Override
	public void reset()
	{
		m_count = 0;
		m_next = 0;
	}

	@Override
	public Accumulator cloneAndReset()
	{
		throw new BasicRuntimeException("NYI");
	}

	@Override
	public void cloneAndReset(Accumulator accumulator)
	{
		throw new BasicRuntimeException("NYI");
	}

	@Override
	public long getCount()
	{
		return m_count;
	}

	@Override
	public double getMin()
	{
		double value = Double.MAX_VALUE;
		for (int i = 0; i < getCount(); i++)
		{
			value = Math.min(value, m_list[i]);
		}
		return value;
	}

	@Override
	public double getMax()
	{
		double value = 0;
		for (int i = 0; i < getCount(); i++)
		{
			value = Math.max(value, m_list[i]);
		}
		return value;
	}

	@Override
	public double getAvg()
	{
		return getSum() / getCount();
	}

	@Override
	public double getStd()
	{
		return 0.0;
	}

	@Override
	public double getSum()
	{
		double value = 0.0;
		for (int i = 0; i < getCount(); i++)
		{
			value += m_list[i];
		}
		return value;
	}
}
