/**
 * Copyright (c) 2023 LogicielCote.COM All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.math.curve;

public class CurvePoint
{
	private double		m_x;
	private double		m_y;

	public static CurvePoint fromAngle(double length, double deg)
	{
		double		rad		= toRad(deg);
		return new CurvePoint(length * Math.cos(rad), length * Math.sin(rad));
	}
	public CurvePoint(double x, double y)
	{
		set(x, y);
	}
	public void set(double x, double y)
	{
		m_x = x;
		m_y = y;
	}
	public void setX(double x)
	{
		m_x = x;
	}
	public void setY(double y)
	{
		m_y = y;
	}

	public static double toDeg(double rad)
	{
		return rad * 180 / Math.PI;
	}
	public static double toRad(double deg)
	{
		return deg * Math.PI / 180;
	}
	public static double sanitizeDeg(double deg)
	{
		while (deg <= -180)
		{
			deg += 360;
		}
		while (deg > 180)
		{
			deg -= 360;
		}
		return deg;
	}

	public double getX()
	{
		return m_x;
	}
	public double getY()
	{
		return m_y;
	}

	public double getDeltaX(CurvePoint that)
	{
		return this.getX() - that.getX();
	}
	public double getDeltaY(CurvePoint that)
	{
		return this.getY() - that.getY();
	}
	public double getLength()
	{
		return Math.sqrt(getX() * getX() + getY() * getY());
	}
	public double getAngleRad()
	{
		return Math.atan2(getY(), getX());
	}
	public double getAngleDeg()
	{
		return getAngleRad() * 180 / Math.PI;
	}

	public boolean isZero()
	{
		boolean		isZero		= (0 == getX());
		isZero &= (0 == getY());
		return isZero;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		CurvePoint curvePoint = (CurvePoint) o;

		if (Double.compare(curvePoint.getX(), getX()) != 0)
		{
			return false;
		}
		return Double.compare(curvePoint.getY(), getY()) == 0;
	}

	@Override
	public int hashCode()
	{
		int result;
		long temp;
		temp = Double.doubleToLongBits(getX());
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getY());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Returns a string representation of the object. In general, the
	 * {@code toString} method returns a string that
	 * "textually represents" this object. The result should
	 * be a concise but informative representation that is easy for a
	 * person to read.
	 * It is recommended that all subclasses override this method.
	 * <p>
	 * The {@code toString} method for class {@code Object}
	 * returns a string consisting of the name of the class of which the
	 * object is an instance, the at-sign character `{@code @}', and
	 * the unsigned hexadecimal representation of the hash code of the
	 * object. In other words, this method returns a string equal to the
	 * value of:
	 * <blockquote>
	 * <pre>
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 * </pre></blockquote>
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return String.format("(%6.2f, %6.2f)", getX(), getY());
	}
}
