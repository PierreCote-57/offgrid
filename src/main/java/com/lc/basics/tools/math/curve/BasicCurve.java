/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.math.curve;

import java.util.function.Function;

public abstract class BasicCurve implements Curve
{
	private Double		m_xMin		= -Double.MAX_VALUE;
	private Double		m_xMax		= Double.MAX_VALUE;

	public BasicCurve setRange(Double xMin, Double xMax)
	{
		m_xMin = xMin;
		m_xMax = xMax;

		return this;
	}
	public Double getMinX()
	{
		return m_xMin;
	}
	public Double getMaxX()
	{
		return m_xMax;
	}

	public boolean isSame(BasicCurve that, double deltaX, double deltaYMax)
	{
		return true;
	}

	@Override
	public String toString()
	{
		return String.format("[%.3f, %.3f]",
				-Double.MAX_VALUE == getMinX() ? Double.NaN : getMinX(),
				 Double.MAX_VALUE == getMaxX() ? Double.NaN : getMaxX());
	}



	/*
			y = mx + b
			b = y - mx
	 */
	public static class SimpleLine extends BasicCurve
	{
		private CurvePoint m_point1		= null;
		private CurvePoint m_point2		= null;

		private double			m_slope;
		private double			m_offset;

		public SimpleLine(SimpleLine that)
		{
			m_point1 = that.m_point1;
			m_point2 = that.m_point2;
			m_slope = that.m_slope;
			m_offset = that.m_offset;
		}

		public SimpleLine(CurvePoint point, double angle)
		{
			m_point1 = point;

			m_slope = Math.tan(angle * Math.PI / 180);
			m_offset = point.getY() - m_slope * point.getX();
		}
		public SimpleLine(CurvePoint point1, CurvePoint point2)
		{
			m_point1 = point1;
			m_point2 = point2;

			if (0.0 == point2.getX() - point1.getX())
			{
				m_offset = point1.getY();
				m_slope = 0;
			}
			else
			{
				m_slope = (point2.getY() - point1.getY()) / (point2.getX() - point1.getX());
				m_offset = point1.getY() - (m_slope * point1.getX());
			}
		}
		public SimpleLine(double slope, double offset)
		{
			m_slope = slope;
			m_offset = offset;
		}

		public CurvePoint getPoint1()
		{
			return m_point1;
		}
		public CurvePoint getPoint2()
		{
			return m_point2;
		}

		@Override
		public Double apply(Double x)
		{
			return (m_slope * x) + m_offset;
		}

		@Override
		public String toString()
		{
			return String.format("%.3f * x + %.3f %s", m_slope, m_offset, super.toString());
		}
	}

	public static class FunctionCurve extends BasicCurve
	{
		private Function<Double, Double>		m_function;

		public FunctionCurve(Function<Double, Double> function)
		{
			m_function = function;
		}

		public Function<Double, Double> getFunction()
		{
			return m_function;
		}

		/**
		 * Applies this function to the given argument.
		 *
		 * @param x the function argument
		 * @return the function result
		 */
		@Override
		public Double apply(Double x)
		{
			return getFunction().apply(x);
		}
	}

	public static class CircleLineNegative extends CircleLine
	{
		public CircleLineNegative(CurvePoint center, double radius)
		{
			super(center, radius);
		}

		@Override
		public Double apply(Double x)
		{
			double		xCentered	= x - getCenter().getX();
			double		offset		= Math.sqrt(getRadius2() - (xCentered * xCentered));
			double		y			= getCenter().getY() - offset;
			return  y;
		}
	}
	public static class CircleLine extends BasicCurve
	{
		private final CurvePoint m_center;
		private final double		m_radius;
		private final double		m_radius2;

		public CircleLine(CurvePoint center, double radius)
		{
			m_center = center;
			m_radius = radius;
			m_radius2 = radius * radius;

			setRange(center.getX() - radius, center.getX() + radius);
		}

		public CurvePoint getCenter()
		{
			return m_center;
		}
		public double getRadius()
		{
			return m_radius;
		}
		public double getRadius2()
		{
			return m_radius2;
		}

		@Override
		public Double apply(Double x)
		{
			double		xCentered	= x - getCenter().getX();
			double		offset		= Math.sqrt(getRadius2() - (xCentered * xCentered));
			double		y			= getCenter().getY() + offset;
			return  y;
		}

		@Override
		public String toString()
		{
			return String.format("%s(R=%.3f; C=%s) %s", getClass().getSimpleName(), getRadius(), getCenter(), super.toString());
		}
	}
}
