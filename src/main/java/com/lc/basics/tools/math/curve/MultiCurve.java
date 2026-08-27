/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.math.curve;


import com.lc.basics.tools.misc.BasicRuntimeException;

import java.util.Collection;

public class MultiCurve extends BasicCurve
{
	private final BasicCurve[] 		m_curveList;

	public MultiCurve(BasicCurve... curveList)
	{
		m_curveList = curveList;
	}
	public static MultiCurve fromPointList(Collection<CurvePoint> pointList)
	{
		return fromPointList(pointList.toArray(new CurvePoint[0]));
	}
	public static MultiCurve fromPointList(CurvePoint[] pointList)
	{
		BasicCurve[]		curveList	= new BasicCurve[pointList.length - 1];

		for (int i = 0; i < pointList.length - 1; i++)
		{
			CurvePoint point1		= pointList[i];
			CurvePoint point2		= pointList[i + 1];
			curveList[i] = new SimpleLine(point1, point2).setRange(point1.getX(), point2.getX());
		}
		return new MultiCurve(curveList);
	}

	@Override
	public Double getMinX()
	{
		return getCurveList()[0].getMinX();
	}

	@Override
	public Double getMaxX()
	{
		return getCurveList()[getCurveList().length - 1].getMaxX();
	}

	public BasicCurve[] getCurveList()
	{
		return m_curveList;
	}

	@Override
	public Double apply(Double x)
	{
		if (x < getMinX() || x > getMaxX())
		{
			throw new BasicRuntimeException("Condition required: %.2f < x < %.2f", getMinX(), getMaxX());
		}

		int index = findIndex(x);
		BasicCurve curve = m_curveList[index];
		return curve.apply(x);
/*
		for (BasicCurve line : m_curveList)
		{
			if ((line.getMinX() <= x) && (line.getMaxX() >= x))
			{
				//  Found it
				return line.apply(x);
			}
		}
		throw new BasicRuntimeException("No line covers x = %.3f", x);
*/
	}
	private int findIndex(double x)
	{
		int iMin = 0;
		int iMax = m_curveList.length;
		while (true)
		{
			int iMid = (iMin + iMax) / 2;
			double xMin = m_curveList[iMid].getMinX();
			double xMax = m_curveList[iMid].getMaxX();
			if (xMin > x)
			{
				iMax = iMid;
			}
			else if (xMax < x)
			{
				iMin = iMid;
			}
			else
			{
				return iMid;
			}
		}
	}
}
