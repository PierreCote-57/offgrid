package com.lc.offgrid.common.misc.sky;

import java.time.ZonedDateTime;

/**
 * One moment in a body's day: when it is, and where to look at that time.
 *
 * The time is in the observer's own zone. The two angles are what a person standing outside
 * uses: which way to turn, and how far up from there.
 */
public class SkyMoment
{
	/** When this moment is, in the observer's zone. */
	private final ZonedDateTime	time;

	/** True bearing in degrees, clockwise from north: 90 is east, 180 is south. */
	private final double	bearing;

	/** How far above the horizon, in degrees. */
	private final double	elevation;

	/** Which moment this is, or null when it is only a time somebody asked about. */
	private final SkyMomentName	name;

	public SkyMoment(ZonedDateTime time, double bearing, double elevation, SkyMomentName name)
	{
		this.time = time;
		this.bearing = bearing;
		this.elevation = elevation;
		this.name = name;
	}

	public ZonedDateTime getTime()
	{
		return time;
	}

	public double getBearing()
	{
		return bearing;
	}

	public double getElevation()
	{
		return elevation;
	}

	public SkyMomentName getName()
	{
		return name;
	}
}
