package com.lc.offgrid.pojo.part;

/**
 * A stretch of the approach road and what it is like. The type vocabulary seen so far is
 * potholes, sharp_rock, unpaved and dirt; it is a String until that list is settled.
 *
 * km is a Double so that a leg with no distance stated stays different from a leg measured at
 * zero: unpaved asserts a measured tail, and a null says nobody has measured it yet.
 */
public class Leg
{
	private String	type;
	private Double	km;

	public Leg()
	{
	}

	public Leg(String legType, Double legKm)
	{
		type = legType;
		km = legKm;
	}

	public String getType()
	{
		return type;
	}

	public Double getKm()
	{
		return km;
	}
}
