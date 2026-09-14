package com.lc.offgrid.common.misc.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsMoment;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsMomentName;

import java.util.Map;

/**
 * One body's day, as an observer at one place sees it: where to look and when, and what it
 * looks like while it is up.
 *
 * Each moment is held under its name, and one that does not happen on the day is a null. The
 * times are epoch seconds: the zone they are shown in is the reader's business.
 */
public class SkyBodyDay
{
	/** The rise, the transit, the set and the moment that was asked about. */
	private final Map<HorizonsMomentName, HorizonsMoment>	momentMap;

	/** Distance from the observer, in astronomical units. */
	private final double	distance;

	/** Distance from the Sun, in astronomical units. */
	private final double	orbitRadius;

	/** Apparent visual magnitude, or null where Horizons does not state one. */
	private final Double	apparentMagnitude;

	/** The three-letter IAU constellation the body sits in. */
	private final String	constellation;

	/** How much of the disc is lit, from 0 to 1. */
	private final double	litFraction;

	public SkyBodyDay(Map<HorizonsMomentName, HorizonsMoment> momentMap, double distance,
			double orbitRadius, Double apparentMagnitude, String constellation, double litFraction)
	{
		this.momentMap = momentMap;
		this.distance = distance;
		this.orbitRadius = orbitRadius;
		this.apparentMagnitude = apparentMagnitude;
		this.constellation = constellation;
		this.litFraction = litFraction;
	}

	public Map<HorizonsMomentName, HorizonsMoment> getMomentMap()
	{
		return momentMap;
	}

	public double getDistance()
	{
		return distance;
	}

	public double getOrbitRadius()
	{
		return orbitRadius;
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
