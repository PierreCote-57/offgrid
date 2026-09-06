/*
 * Copyright (c) 2020 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.common.misc.geography;

import com.lc.offgrid.common.misc.geography.object.PolarPointSmart;

import java.io.Serializable;

public class GeographyConstants implements Serializable
{
	private static final long serialVersionUID = 20210101165959L;

	public static final double		EARTH_RADIUS_MILE		= 3958.8;
	// https://en.wikipedia.org/wiki/Earth
	public static final double		EARTH_RADIUS_KM			= 6371.0;
	// https://en.wikipedia.org/wiki/Moon
	public static final double		MOON_RADIUS_KM			= 1737.4;

	public static final double		MILE_PER_KM				= (1000.0 * 1000 / 25.4) / (12 * 5280);
	// https://en.wikipedia.org/wiki/Nautical_mile
	public static final double		KM_PER_NM				= 1.852;
	public static final double		NM_PER_KM				= 1.0 / KM_PER_NM;

	public enum Units
	{
		Metric(1.0),
		English(MILE_PER_KM),
		Nautical(NM_PER_KM);

		private double m_perKM;

		Units(double perKM)
		{
			m_perKM = perKM;
		}

		public double getPerKM()
		{
			return m_perKM;
		}
		public double toKM(double distance)
		{
			double distanceKM = distance / m_perKM;
			return distanceKM;
		}
		public double fromKM(double distanceKM)
		{
			double distance = distanceKM * m_perKM;
			return distance;
		}
	}

	public enum Planet
	{
		Earth(EARTH_RADIUS_KM),
		Moon(MOON_RADIUS_KM);

		private double m_radiusKM;
		Planet(double radiusKM)
		{
			m_radiusKM = radiusKM;
		}

		public double getRadiusKM()
		{
			return m_radiusKM;
		}

		public double convertDistanceToLatRad(double distanceKM)
		{
			return distanceKM / getRadiusKM();
		}
		public double convertDistanceToLonAtLatRad(PolarPointSmart point, double distanceKM)
		{
			return convertDistanceToLonAtLatRadRaw(point.getCosLat(), distanceKM);
		}
		public double convertDistanceToLonAtLatRad(double latitudeRad, double distanceKM)
		{
			return Math.abs(convertDistanceToLonAtLatRadRaw(Math.cos(latitudeRad), distanceKM));
		}
		public double convertDistanceToLonAtLatRadRaw(double coslat, double distanceKM)
		{
			double		lonAtEquator		= convertDistanceToLatRad(distanceKM);
			double		lonAtLat			= lonAtEquator / coslat;
			return lonAtLat;
		}

		@Override
		public String toString()
		{
			return String.format("%s (R = %,.0f KM)", name(), getRadiusKM());
		}
	}
}
