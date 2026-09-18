package com.lc.offgrid.common.misc.astronomy.planet;

import com.lc.offgrid.common.misc.astronomy.constellation.Constellation;

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
	/** The tilt of the Earth's axis, in degrees: what turns an equatorial direction into an ecliptic one. */
	private static final double OBLIQUITY = 23.4392911;

	/** What is lit of a body that shines by itself. */
	private static final double FULLY_LIT = 1.0;

	private final Instant	instant;
	private final double	rightAscension;
	private final double	declination;
	private final double	range;
	private final Constellation	constellation;
	private final Double	apparentMagnitude;

	public HorizonsPosition(Instant instant, double rightAscension, double declination, double range,
			Constellation constellation, Double apparentMagnitude)
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
	 * The constellation the body sits in, taken from the closer of the two rows: a name does
	 * not average with another name. Null where that row held a code that is not one of the 88.
	 */
	public Constellation getConstellation()
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

	/**
	 * The direction from the Sun to this body, in degrees from 0 to 360, taken in the ecliptic
	 * plane. Both positions are seen from the Earth's centre, so one taken from the other is the
	 * vector from the Sun to the body.
	 */
	public double getSunAngle(HorizonsPosition sunPosition)
	{
		double[] bodyVector = toVector();
		double[] sunVector = sunPosition.toVector();
		double[] sunToBody = subtract(bodyVector, sunVector);
		double[] eclipticVector = toEcliptic(sunToBody);

		double radians = Math.atan2(eclipticVector[1], eclipticVector[0]);
		double degrees = Math.toDegrees(radians);
		double angle = HorizonsEphemeris.normalise(degrees);
		return angle;
	}

	/**
	 * How far this body stands from the Sun, in astronomical units: the length of the same
	 * vector getSunAngle takes its direction from. For a body that orbits the Earth it is the
	 * distance from the Sun all the same, and not the size of its own orbit.
	 */
	public double getOrbitRadius(HorizonsPosition sunPosition)
	{
		double[] bodyVector = toVector();
		double[] sunVector = sunPosition.toVector();
		double[] sunToBody = subtract(bodyVector, sunVector);

		double orbitRadius = length(sunToBody);
		return orbitRadius;
	}

	/**
	 * How much of this body's disc an observer on Earth sees lit, from the angle the Sun and the
	 * Earth stand apart at the body. A body sitting on either of them is taken as fully lit.
	 */
	public double getLitFraction(HorizonsPosition sunPosition)
	{
		double[] bodyVector = toVector();
		double[] sunVector = sunPosition.toVector();
		double[] bodyToEarth = subtract(new double[] { 0.0, 0.0, 0.0 }, bodyVector);
		double[] bodyToSun = subtract(sunVector, bodyVector);

		double earthLength = length(bodyToEarth);
		double sunLength = length(bodyToSun);

		double litFraction;
		if (0.0 == earthLength || 0.0 == sunLength)
		{
			litFraction = FULLY_LIT;
		}
		else
		{
			double dotProduct = dot(bodyToEarth, bodyToSun);
			double cosine = HorizonsEphemeris.clamp(dotProduct / (earthLength * sunLength));
			litFraction = (1.0 + cosine) / 2.0;
		}

		return litFraction;
	}

	/**
	 * This position as x, y and z from the Earth's centre, against the celestial equator.
	 */
	private double[] toVector()
	{
		double rightAscension = Math.toRadians(getRightAscension());
		double declination = Math.toRadians(getDeclination());
		double range = getRange();

		double x = range * Math.cos(declination) * Math.cos(rightAscension);
		double y = range * Math.cos(declination) * Math.sin(rightAscension);
		double z = range * Math.sin(declination);

		double[] vector = { x, y, z };
		return vector;
	}

	/**
	 * The same vector turned onto the ecliptic, which is the plane the chart draws.
	 */
	private static double[] toEcliptic(double[] vector)
	{
		double obliquity = Math.toRadians(OBLIQUITY);
		double x = vector[0];
		double y = vector[1] * Math.cos(obliquity) + vector[2] * Math.sin(obliquity);
		double z = -vector[1] * Math.sin(obliquity) + vector[2] * Math.cos(obliquity);

		double[] eclipticVector = { x, y, z };
		return eclipticVector;
	}

	private static double[] subtract(double[] first, double[] second)
	{
		double[] difference = { first[0] - second[0], first[1] - second[1], first[2] - second[2] };
		return difference;
	}

	private static double dot(double[] first, double[] second)
	{
		double dotProduct = first[0] * second[0] + first[1] * second[1] + first[2] * second[2];
		return dotProduct;
	}

	private static double length(double[] vector)
	{
		double dotProduct = dot(vector, vector);
		double length = Math.sqrt(dotProduct);
		return length;
	}
}
