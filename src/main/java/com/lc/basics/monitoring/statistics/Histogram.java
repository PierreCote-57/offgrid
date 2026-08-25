/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.monitoring.statistics;

import java.util.List;

public interface Histogram
{
	void			record(double value);
	void 			record(Histogram histogram);
	BinData			getWaterMarkValue(double wmLevelFraction);

	Histogram		getBlankClone();
	void			reset();
	Histogram		cloneAndReset();
	void			cloneAndReset(Histogram histogram);

	double			getMinStep();
	double			getMaxValue();
	int				getBinCount();
	int				getBinValue(int bin);
	int				getCount();
	List<BinData> getBinData();

	class BinGroup
	{
		double			m_min;		// inclusive, min bin includes all values less than ...
		double			m_max;		// exclusive; max bin includes all values higher than ...
		double			m_step;
		int				m_count;	// Number of bins

		public BinGroup(double min, double max, double step)
		{
			m_min = min;
			m_max = max;
			m_step = step;
			m_count = Double.valueOf(Math.ceil((max - min) / step)).intValue();
		}

		public double getMin()
		{
			return m_min;
		}

		public double getMax()
		{
			return m_max;
		}

		public double getStep()
		{
			return m_step;
		}

		public int getCount()
		{
			return m_count;
		}

		@Override
		public String toString()
		{
			return String.format("%6.1e - %6.1e by %6.1e (%2d steps)", getMin(), getMax(), getStep(), getCount());
		}
	}

	class BinData
	{
		double			m_min;		// exclusive, min bin includes all values less than ...
		double			m_max;		// Inclusive; max bin includes all values higher than ...
		int				m_count;	// Count of hits on this bin

		public BinData(double min, double max, int count)
		{
			m_min = min;
			m_max = max;
			m_count = count;
		}

		public double getMin()
		{
			return m_min;
		}

		public double getMax()
		{
			return m_max;
		}

		public int getCount()
		{
			return m_count;
		}

		public String toTextString()
		{
			if (getMin() < 100)
			{
				return String.format("[%6.3f, %6.3f[ = %,d", getMin(), getMax(), getCount());
			}
			else
			{
				return String.format("[%6.1e, %6.1e[ = %,d", getMin(), getMax(), getCount());
			}
		}

		@Override
		public String toString()
		{
			return String.format("%6.1e_%6.1e=%d", getMin(), getMax(), getCount());
		}
	}
}
