/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.misc.geography.point;

public class LatLonPointPojo implements LatLonPoint
{
	private double		m_latitudeDeg;
	private double		m_longitudeDeg;

	public LatLonPointPojo(double latitudeDeg, double longitudeDeg)
	{
		m_latitudeDeg = latitudeDeg;
		m_longitudeDeg = longitudeDeg;
	}
	public double getLatitudeDeg()
	{
		return m_latitudeDeg;
	}
	public double getLongitudeDeg()
	{
		return m_longitudeDeg;
	}

	@Override
	public String toString()
	{
		return String.format("(%.6f, %.6f)", getLatitudeDeg(), getLongitudeDeg());
	}
}
