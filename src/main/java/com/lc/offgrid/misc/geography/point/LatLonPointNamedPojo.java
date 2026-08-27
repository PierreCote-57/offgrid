/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.misc.geography.point;

public class LatLonPointNamedPojo extends LatLonPointPojo implements LatLonPointNamed
{
	private Object			m_reference;

	public LatLonPointNamedPojo(double latitudeDeg, double longitudeDeg)
	{
		this(null, latitudeDeg, longitudeDeg);
	}
	public LatLonPointNamedPojo(Object reference, double latitudeDeg, double longitudeDeg)
	{
		super(latitudeDeg, longitudeDeg);
		m_reference = reference;
	}
	@SuppressWarnings("unchecked")
	public <T> T getReference()
	{
		return (T) m_reference;
	}

	@Override
	public String toString()
	{
		return String.format("%s at %s", getReference(), super.toString());
	}
}
