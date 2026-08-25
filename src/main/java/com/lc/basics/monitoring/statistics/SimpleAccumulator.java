/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.monitoring.statistics;

/**
 * User is responsible for synchronization.
 */
public class SimpleAccumulator implements Accumulator
{
	private long		m_count;
	private double		m_min;
	private double		m_max;
	private double		m_sum;
	private double		m_sum2;		// Sum of square for Standard Deviation calculation

	public SimpleAccumulator()
	{
		reset();
	}

	@Override
	public void record(double value)
	{
		if (0 == m_count)
		{
			m_min = value;
			m_max = value;
			m_sum = value;
			m_sum2 = (value * value);
		}
		else
		{
			m_min = Math.min(m_min, value);
			m_max = Math.max(m_max, value);
			m_sum += value;
			m_sum2 += (value * value);
		}
		m_count++;
	}
	@Override
	public void record(Accumulator accumulatorIn)
	{
		if (!(accumulatorIn instanceof SimpleAccumulator accumulator))
		{
			throw new IllegalArgumentException("Argument must be a " + getClass().getSimpleName());
		}

		if (0 == m_count)
		{
			m_min = accumulator.m_min;
			m_max = accumulator.m_max;
			m_sum = accumulator.m_sum;
			m_sum2 = accumulator.m_sum2;
		}
		else if (0 != accumulator.m_count)
		{
			m_min = Math.min(m_min, accumulator.m_min);
			m_max = Math.max(m_max, accumulator.m_max);
			m_sum += accumulator.m_sum;
			m_sum2 += accumulator.m_sum2;
		}
		m_count += accumulator.m_count;
	}

	@Override
	public Accumulator getBlankClone()
	{
		return new SimpleAccumulator();
	}
	@Override
	public void reset()
	{
		m_count		= 0;
	}
	@Override
	public Accumulator cloneAndReset()
	{
		SimpleAccumulator accumulator		= (SimpleAccumulator) getBlankClone();
		cloneAndReset(accumulator);
		return accumulator;
	}

	@Override
	public void cloneAndReset(Accumulator accumulator)
	{
		assert(accumulator instanceof SimpleAccumulator); // cloneAndReset() supported only for like accumulators
		SimpleAccumulator simpleAccumulator		= (SimpleAccumulator) accumulator;

		simpleAccumulator.m_count = m_count;
		simpleAccumulator.m_min = m_min;
		simpleAccumulator.m_max = m_max;
		simpleAccumulator.m_sum = m_sum;
		simpleAccumulator.m_sum2 = m_sum2;

		reset();
	}

	@Override
	public long getCount()
	{
		return m_count;
	}

	@Override
	public double getMin()
	{
		return 0 == m_count ? 0.0 : m_min;
	}

	@Override
	public double getMax()
	{
		return 0 == m_count ? 0.0 : m_max;
	}

	@Override
	public double getAvg()
	{
		return 0 == m_count ? 0.0 : m_sum / m_count;
	}

	// Equation for STD from http://en.wikipedia.org/wiki/Standard_deviation
	@Override
	public double getStd()
	{
		if (0 == m_count)
		{
			return 0.0;
		}
		else
		{
			double		avg			= getAvg();
			double		sqrtSum		= avg * avg;
			double		sigma2		= (m_sum2 / m_count) - sqrtSum;

			return Math.sqrt(sigma2);
		}
	}

	@Override
	public double getSum()
	{
		return 0 == m_count ? 0.0 : m_sum;
	}

	@Override
	public String toString()
	{
		return String.format("Min=%7.2f; Avg=%7.2f; STD=%7.2f; Max=%7.2f out of %,10d samples",
				getMin(), getAvg(), getStd(), getMax(), getCount());
	}
}
