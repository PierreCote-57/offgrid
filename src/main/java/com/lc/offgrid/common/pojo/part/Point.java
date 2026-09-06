package com.lc.offgrid.common.pojo.part;

/**
 * A coordinate. The base of everything the site puts on a map.
 */
public class Point
{
	private Double	lat;
	private Double	lng;

	public void setLat(Double lat)
	{
		this.lat = lat;
	}

	public void setLng(Double lng)
	{
		this.lng = lng;
	}

	public Double getLat()
	{
		return lat;
	}

	public Double getLng()
	{
		return lng;
	}
}
