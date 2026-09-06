package com.lc.offgrid.common.pojo.external.shared;

/**
 * Where a way or a relation sits. Overpass puts lat/lon on a node directly and hands back a
 * computed centre for everything else, which is what "out center" asked for.
 */
public class OverpassCenter
{
	private Double	lat;
	private Double	lon;

	public Double getLat()
	{
		return lat;
	}

	public Double getLon()
	{
		return lon;
	}
}
