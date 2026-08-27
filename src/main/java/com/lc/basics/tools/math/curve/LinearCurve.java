/**
 * Copyright (c) 2024 LogicielCote.COM All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.math.curve;

// y = mx + b
public class LinearCurve implements Curve
{
	final double m_slope;		// aka m
	final double m_offset;		// aka b

	public LinearCurve(double slope, double offset)
	{
		m_slope = slope;
		m_offset = offset;
	}
	public static LinearCurve fromPoints(CurvePoint point1, CurvePoint point2)
	{
		double slope = (point2.getY() - point1.getY()) / (point2.getX() - point1.getX());
		double offset = point1.getY() - (slope * point1.getX());
		return new LinearCurve(slope, offset);
	}

	public double getSlope()
	{
		return m_slope;
	}
	public double getOffset()
	{
		return m_offset;
	}

	@Override
	public Double apply(Double x)
	{
		return (x * getSlope()) + getOffset();
	}

	@Override
	public String toString()
	{
		return String.format("y = %.3f x + %.3f", getSlope(), getOffset());
	}
}
