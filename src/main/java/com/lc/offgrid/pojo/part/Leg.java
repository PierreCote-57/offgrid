package com.lc.offgrid.pojo.part;

/**
 * A stretch of the approach road and what it is like. The type vocabulary seen so far is
 * potholes, sharp_rock, unpaved and dirt; it is a String until that list is settled.
 */
public class Leg
{
	private String	type;
	private double km;

	public String getType()
	{
		return type;
	}

	public double getKm()
	{
		return km;
	}
}
