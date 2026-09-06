/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.common.misc.geography.object;


import com.lc.offgrid.common.misc.geography.point.LatLonPointNamed;

public class PolarPointComplete extends PolarPointSmart implements LatLonPointNamed
{
	private Object		m_reference;

	public PolarPointComplete(Object reference, Angle latitude, Angle longitude)
	{
		super(latitude, longitude);
		m_reference = reference;
	}
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getReference()
	{
		return (T) m_reference;
	}

	@Override
	public String toString()
	{
		return String.format("%s%s", getReference(), super.toString());
	}
}
