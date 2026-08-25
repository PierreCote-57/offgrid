/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.monitoring.statistics;

public class LinearHistogram extends AbstractHistogram
{
	private LinearHistogram(LinearHistogram histogram)
	{
		super(histogram);
	}

	public LinearHistogram(double min, double max, double step)
	{
		super(calculateBinGroupList(min, max, step));
	}

	private static BinGroup[] calculateBinGroupList(double min, double max, double step)
	{
		BinGroup binGroup		= new BinGroup(min, max, step);
		BinGroup[]	binGroupList	= new BinGroup[1];
		binGroupList[0] = binGroup;

		return binGroupList;
	}

	@Override
	public Histogram getBlankClone()
	{
		return new LinearHistogram(this);
	}
}
