package com.lc.offgrid.common.misc.sky;

import java.util.List;

/**
 * One body's day, as an observer at one place sees it: where to look and when, and what it
 * looks like while it is up.
 *
 * The moments are in time order and stated in the observer's own zone. One that does not
 * happen on the day is not in the list.
 */
public class SkyBodyDay
{
	/** The asked-for moment, the rise, the transit and the set, in time order. */
	private final List<SkyMoment>	momentList;

	/** Distance from the observer, in astronomical units. */
	private final double	distance;

	/** Apparent visual magnitude, or null where Horizons does not state one. */
	private final Double	apparentMagnitude;

	/** The three-letter IAU constellation the body sits in. */
	private final String	constellation;

	/** How much of the disc is lit, from 0 to 1. */
	private final double	litFraction;

	public SkyBodyDay(List<SkyMoment> momentList, double distance, Double apparentMagnitude,
			String constellation, double litFraction)
	{
		this.momentList = momentList;
		this.distance = distance;
		this.apparentMagnitude = apparentMagnitude;
		this.constellation = constellation;
		this.litFraction = litFraction;
	}

	public List<SkyMoment> getMomentList()
	{
		return momentList;
	}

	public double getDistance()
	{
		return distance;
	}

	public Double getApparentMagnitude()
	{
		return apparentMagnitude;
	}

	public String getConstellation()
	{
		return constellation;
	}

	public double getLitFraction()
	{
		return litFraction;
	}
}
