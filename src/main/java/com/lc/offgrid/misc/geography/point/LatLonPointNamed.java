/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.misc.geography.point;


import com.lc.offgrid.misc.geography.misc.KeyMaker;

public interface LatLonPointNamed extends LatLonPoint
{
	@SuppressWarnings("unchecked")
	default <T> T getReference()
	{
		return (T) this;
	}
	default String getKey()
	{
		String		key;
		if (getReference() instanceof KeyMaker)
		{
			key = ((KeyMaker) getReference()).getKey();
		}
		else if (getReference() instanceof String)
		{
			key = getReference();
		}
		else
		{
			throw new UnsupportedOperationException();
		}
		return key;
	}
}
