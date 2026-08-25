/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.monitoring.statistics;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractHistogram implements Histogram
{
	private static final int	ZERO_HASH_CODE		= "Zero".hashCode();

	private double				m_minStep			= 0.0;
	private double				m_maxValue			= 0.0;
	private final BinGroup[]	m_binGroupList;
	private int[]				m_binList;
	private int					m_count				= 0;

	// Normal constructor, extending class will have calculated/constructed an appropriate BinGroup[]
	protected AbstractHistogram(BinGroup[] binGroupList)
	{
		m_binGroupList = binGroupList;

		int			binCount		= 0;
		for (BinGroup binGroup : m_binGroupList)
		{
			binCount += binGroup.getCount();
		}
		m_binList = new int[binCount];

		m_minStep = m_binGroupList[0].getStep();
		m_maxValue = m_binGroupList[m_binGroupList.length - 1].getMax();
	}

	// Constructor for getBlankClone(), inherits the exact m_binGroupList of the parent.
	protected AbstractHistogram(AbstractHistogram histogram)
	{
		this(histogram.m_binGroupList);
	}

	@Override
	public int getBinCount()
	{
		return m_binList.length;
	}
	@Override
	public int getBinValue(int bin)
	{
		return m_binList[bin];
	}

	@Override
	public int getCount()
	{
		return m_count;
	}

	@Override
	public double getMinStep()
	{
		return m_minStep;
	}

	@Override
	public double getMaxValue()
	{
		return m_maxValue;
	}

	@Override
	public void record(double value)
	{
		m_binList[getBinIndex(value)]++;
		m_count++;
	}
	protected int getBinIndex(double value)
	{
		int		binIndex		= 0;
		for (BinGroup binGroup : m_binGroupList)
		{
			if (binGroup.getMin() > value)
			{
				break;
			}
			else if (binGroup.getMax() < value)
			{
				binIndex += binGroup.getCount();
			}
			else
			{
				binIndex += ((value - binGroup.getMin()) / binGroup.getStep());
				break;
			}
		}
		binIndex = Math.min(binIndex, getBinCount() - 1);

		return binIndex;
	}

	public BinData getWaterMarkValue(double wmLevelFraction)
	{
		wmLevelFraction = Math.max(0.0, wmLevelFraction);
		wmLevelFraction = Math.min(1.0, wmLevelFraction);

		int		targetCount		= Double.valueOf((wmLevelFraction * m_count) + 0.5).intValue();

		// Find the bin with the measurement making the water mark...
		int		runningCount	= 0;
		int		targetBin		= -1;
		for (int i = 0; i < m_binList.length; i++)
		{
			runningCount += m_binList[i];
			if (runningCount >= targetCount)
			{
				targetBin = i;
				break;
			}
		}
		if (-1 == targetBin)
		{
			targetBin = m_binList.length - 1;
		}

		// Determine the max value of that bin
		int			binCount		= 0;
		BinData wmData			= null;
		for (BinGroup binGroup : m_binGroupList)
		{
			if (binCount + binGroup.getCount() > targetBin)
			{
				// Found it: The bin is in this group.
				double		wmMin		= binGroup.getMin() + ((targetBin - binCount) * binGroup.getStep());
				double		wmMax		= wmMin + binGroup.getStep();		// To be at the top of the bin!
				wmData = new BinData(wmMin, wmMax, m_binList[targetBin]);
				break;
			}
			else
			{
				// The bin is not in this group...
				binCount += binGroup.getCount();
			}
		}

		return wmData;
	}

	@Override
	public void record(Histogram histogram)
	{
		if (m_binList.length != histogram.getBinCount() || !(histogram instanceof AbstractHistogram))
		{
			throw new IllegalArgumentException("BinCount must be the same && histogram must be an AbstractHistogram");
		}

		AbstractHistogram abstractHistogram		= (AbstractHistogram) histogram;
		for (int i = 0; i < m_binList.length; i++)
		{
			// write only the non-zero values
			int		binValue		= abstractHistogram.m_binList[i];
			if (0 != binValue)
			{
				m_binList[i] += binValue;
				m_count += binValue;
			}
		}
	}

	@Override
	public void reset()
	{
		Arrays.fill(m_binList, 0);
		m_count = 0;
	}

	@Override
	public Histogram cloneAndReset()
	{
		AbstractHistogram histogram		= (AbstractHistogram) getBlankClone();
		cloneAndReset(histogram);
		return histogram;
	}

	@Override
	public void cloneAndReset(Histogram histogramIn)
	{
		AbstractHistogram histogram		= (AbstractHistogram) histogramIn;

		histogram.m_count = m_count;

		// Swap the m_binList to give the current array to the clone support.
		int[]		temp		= histogram.m_binList;
		histogram.m_binList = m_binList;
		m_binList = temp;
		reset();
	}

	@Override
	public List<BinData> getBinData()
	{
		List<BinData> list		= new LinkedList<>();
		int					iBin		= 0;

		for (BinGroup binGroup : m_binGroupList)
		{
			double		min		= binGroup.getMin();
			double		max		= min + binGroup.getStep();
			for (int i = 0; i < binGroup.getCount(); i++)
			{
				int			count		= m_binList[iBin++];
				if (0 != count)
				{
					BinData data		= new BinData(min, max, count);
					list.add(data);
				}

				min = max;
				max += binGroup.getStep();
			}
		}

		// Ensure the last bin is always present, so analysing code knows where the maximum is...
		int				count			= m_binList[m_binList.length - 1];
		if (0 == count)
		{
			// The last bin was NOT included in the loop above, include it manually at the end...
			BinGroup binGroup		= m_binGroupList[m_binGroupList.length - 1];
			double			max				= binGroup.getMax();
			double			min				= max - binGroup.getStep();
			BinData data			= new BinData(min, max, 0);
			list.add(data);
		}

		return list;
	}

	@Override
	public String toString()
	{
		return String.format("%s(Step = %6.1e; Max = %6.1e) = %,d points over %,d bins (%,d groups)",
				getClass().getSimpleName(), getMinStep(), getMaxValue(),
				getCount(), getBinCount(), m_binGroupList.length);
	}
}
