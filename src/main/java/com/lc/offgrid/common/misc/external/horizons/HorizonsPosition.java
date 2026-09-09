package com.lc.offgrid.common.misc.external.horizons;

import java.time.Instant;

/**
 * Where one body is at one moment: the value interpolated between the two daily rows that
 * bracket that moment.
 *
 * The position is geocentric, as the files are — the query centres on 500@399, the Earth's
 * centre. The direction and the distance together are the vector from there to the body, which
 * is what lets one position be taken from another.
 */
public class HorizonsPosition
{
	private final Instant	instant;
	private final double	rightAscension;
	private final double	declination;
	private final double	range;
	private final String	constellation;
	private final Double	apparentMagnitude;

	public HorizonsPosition(Instant instant, double rightAscension, double declination, double range,
			String constellation, Double apparentMagnitude)
	{
		this.instant = instant;
		this.rightAscension = rightAscension;
		this.declination = declination;
		this.range = range;
		this.constellation = constellation;
		this.apparentMagnitude = apparentMagnitude;
	}

	/**
	 * The moment this position is for.
	 */
	public Instant getInstant()
	{
		return instant;
	}

	/**
	 * Right ascension in degrees, ICRF: where the body is around the celestial equator.
	 */
	public double getRightAscension()
	{
		return rightAscension;
	}

	/**
	 * Declination in degrees, ICRF: how far the body is from the celestial equator, north
	 * positive.
	 */
	public double getDeclination()
	{
		return declination;
	}

	/**
	 * Distance from the Earth's centre, in astronomical units.
	 */
	public double getRange()
	{
		return range;
	}

	/**
	 * The three-letter IAU constellation the body sits in, taken from the closer of the two
	 * rows: a name does not average with another name.
	 */
	public String getConstellation()
	{
		return constellation;
	}

	/**
	 * Apparent visual magnitude, taken from the closer of the two rows, or null where Horizons
	 * states none there.
	 */
	public Double getApparentMagnitude()
	{
		return apparentMagnitude;
	}
}
