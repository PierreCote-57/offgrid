package com.lc.offgrid.pojo.part;

import java.util.List;

/**
 * How hard the place is to reach: how far it is from the towns a reader starts from, and what
 * the road does along the way.
 */
public class Access
{
	private List<TownDistance>	haversineList;
	private List<Leg>			legList;

	public List<TownDistance> getHaversineList()
	{
		return haversineList;
	}

	public List<Leg> getLegList()
	{
		return legList;
	}
}
