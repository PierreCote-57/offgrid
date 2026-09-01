package com.lc.offgrid.pojo.part;

/**
 * A stretch of the approach road and what it is like.
 *
 * km is a Double so that a leg with no distance stated stays different from a leg measured at
 * zero: unpaved asserts a measured tail, and a null says nobody has measured it yet.
 */
public class Leg
{
	private RoadType	type;
	private Double		km;

	public Leg()
	{
	}

	public Leg(RoadType legType, Double legKm)
	{
		type = legType;
		km = legKm;
	}

	public RoadType getType()
	{
		return type;
	}

	public Double getKm()
	{
		return km;
	}
}
