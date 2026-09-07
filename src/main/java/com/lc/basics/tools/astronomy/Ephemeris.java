package com.lc.basics.tools.astronomy;

import java.time.LocalDate;

/**
 * Where the planets and the Moon are on a given date, from the JPL approximate elements and a
 * truncated lunar series. Positions are computed, never estimated, and the table behind them
 * is valid 1800-2050.
 */
public class Ephemeris
{
	/** The Julian Date of J2000.0, the epoch the elements and their rates are stated against. */
	private static final double J2000 = 2451545.0;

	/** Days in a Julian century, the unit every rate in Body is stated per. */
	private static final double DAYS_PER_CENTURY = 36525.0;

	/** Turns a proleptic Gregorian ordinal into a Julian Date at 00:00 UT. */
	private static final double ORDINAL_TO_JULIAN = 1721424.5;

	/**
	 * The Julian Date at 00:00 UT on a Gregorian date.
	 */
	public static double getJulianDate(LocalDate date)
	{
		long ordinal = date.toEpochDay() + 719163;
		double julianDate = ordinal + ORDINAL_TO_JULIAN;
		return julianDate;
	}

	/**
	 * Julian centuries from J2000 to a Julian Date.
	 */
	public static double getCenturyCount(double julianDate)
	{
		double centuryCount = (julianDate - J2000) / DAYS_PER_CENTURY;
		return centuryCount;
	}

	/**
	 * A planet's heliocentric position, by solving Kepler's equation for the eccentric anomaly
	 * and rotating the orbital plane onto the ecliptic.
	 */
	public static EclipticPosition getHeliocentric(Body body, double julianDate)
	{
		double centuryCount = getCenturyCount(julianDate);

		double semiMajorAxis = body.getSemiMajorAxis(centuryCount);
		double eccentricity = body.getEccentricity(centuryCount);
		double inclination = body.getInclination(centuryCount);
		double meanLongitude = body.getMeanLongitude(centuryCount);
		double perihelionLongitude = body.getPerihelionLongitude(centuryCount);
		double ascendingNodeLongitude = body.getAscendingNodeLongitude(centuryCount);

		double perihelionArgument = perihelionLongitude - ascendingNodeLongitude;
		double meanAnomaly = normalizeToHalfTurn(meanLongitude - perihelionLongitude);
		double eccentricAnomaly = solveKepler(meanAnomaly, eccentricity);

		// In the orbital plane, with the x axis pointing at perihelion.
		double planeX = semiMajorAxis * (Math.cos(eccentricAnomaly) - eccentricity);
		double planeY = semiMajorAxis * Math.sqrt(1 - eccentricity * eccentricity) * Math.sin(eccentricAnomaly);

		double argumentRadians = Math.toRadians(perihelionArgument);
		double nodeRadians = Math.toRadians(ascendingNodeLongitude);
		double inclinationRadians = Math.toRadians(inclination);

		double cosArgument = Math.cos(argumentRadians);
		double sinArgument = Math.sin(argumentRadians);
		double cosNode = Math.cos(nodeRadians);
		double sinNode = Math.sin(nodeRadians);
		double cosInclination = Math.cos(inclinationRadians);
		double sinInclination = Math.sin(inclinationRadians);

		double x = (cosArgument * cosNode - sinArgument * sinNode * cosInclination) * planeX
				+ (-sinArgument * cosNode - cosArgument * sinNode * cosInclination) * planeY;
		double y = (cosArgument * sinNode + sinArgument * cosNode * cosInclination) * planeX
				+ (-sinArgument * sinNode + cosArgument * cosNode * cosInclination) * planeY;
		double z = (sinArgument * sinInclination) * planeX
				+ (cosArgument * sinInclination) * planeY;

		EclipticPosition answer = new EclipticPosition(x, y, z);
		return answer;
	}

	/**
	 * The Moon's geocentric ecliptic longitude, from a truncated lunar series good to about
	 * 0.3 degrees. The JPL planetary elements do not cover the Moon.
	 */
	public static double getMoonLongitude(double julianDate)
	{
		double centuryCount = getCenturyCount(julianDate);

		double meanLongitude = 218.3164477 + 481267.88123421 * centuryCount;
		double meanElongation = 297.8501921 + 445267.1114034 * centuryCount;
		double sunMeanAnomaly = 357.5291092 + 35999.0502909 * centuryCount;
		double moonMeanAnomaly = 134.9633964 + 477198.8675055 * centuryCount;
		double latitudeArgument = 93.2720950 + 483202.0175233 * centuryCount;

		double longitude = meanLongitude
				+ 6.289 * sinDegrees(moonMeanAnomaly)
				+ 1.274 * sinDegrees(2 * meanElongation - moonMeanAnomaly)
				+ 0.658 * sinDegrees(2 * meanElongation)
				+ 0.214 * sinDegrees(2 * moonMeanAnomaly)
				- 0.186 * sinDegrees(sunMeanAnomaly)
				- 0.114 * sinDegrees(2 * latitudeArgument);

		double answer = (longitude % 360 + 360) % 360;
		return answer;
	}

	/**
	 * The eccentric anomaly for a mean anomaly, by Newton iteration. Fifty passes is far more
	 * than the handful any planetary eccentricity needs, and costs nothing once a day.
	 */
	private static double solveKepler(double meanAnomalyDegrees, double eccentricity)
	{
		double meanAnomaly = Math.toRadians(meanAnomalyDegrees);
		double eccentricAnomaly = meanAnomaly + eccentricity * Math.sin(meanAnomaly);

		for (int pass = 0; pass < 50; pass++)
		{
			double error = eccentricAnomaly - eccentricity * Math.sin(eccentricAnomaly) - meanAnomaly;
			double slope = 1 - eccentricity * Math.cos(eccentricAnomaly);
			eccentricAnomaly -= error / slope;
		}

		return eccentricAnomaly;
	}

	/**
	 * An angle in degrees brought into -180 to 180, which is where Newton starts closest.
	 */
	private static double normalizeToHalfTurn(double degrees)
	{
		double turned = (degrees % 360 + 360) % 360;
		double answer = 180 < turned ? turned - 360 : turned;
		return answer;
	}

	private static double sinDegrees(double degrees)
	{
		double answer = Math.sin(Math.toRadians(degrees));
		return answer;
	}
}
