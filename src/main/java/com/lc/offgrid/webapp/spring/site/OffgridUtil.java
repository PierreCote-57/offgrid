package com.lc.offgrid.webapp.spring.site;

import com.lc.basics.tools.logging.BasicLogger;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * What the site needs in more than one place and no one class owns: the values it is built
 * around, and the small operations on them.
 */
public class OffgridUtil
{
	/** The 50th parallel marker in Campbell River, until the caller says otherwise. */
	public static final double DEFAULT_LATITUDE = 50.0;
	public static final double DEFAULT_LONGITUDE = -125.230450;
	public static final String DEFAULT_TIME_ZONE = "America/Vancouver";

	public static final double MAXIMUM_LATITUDE = 90.0;
	public static final double MAXIMUM_LONGITUDE = 180.0;

	/**
	 * What the ephemeris covers. Ephemeris draws on the JPL approximate elements, which are
	 * stated as valid 1800-2050; outside that its positions are wrong rather than rough, so a
	 * date past either end is answered with that end.
	 */
	public static final LocalDate SKY_FIRST_DATE = LocalDate.of(1800, 1, 1);
	public static final LocalDate SKY_LAST_DATE = LocalDate.of(2050, 12, 31);

	/** The chart's width when the caller states none, and the range it will draw at. */
	public static final int DEFAULT_CHART_WIDTH = 430;
	public static final int MINIMUM_CHART_WIDTH = 200;
	public static final int MAXIMUM_CHART_WIDTH = 2000;

	private static final BasicLogger LOGGER		= BasicLogger.getLogger(OffgridUtil.class);

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	/**
	 * A number off the query string, or the default when it is missing, unparseable or further
	 * from zero than the limit. A null limit is no limit.
	 */
	public static double parseDouble(String text, double defaultValue, Double limit)
	{
		double value;
		try
		{
			value = Double.parseDouble(text.trim());
		}
		catch (Exception failure)
		{
			if (null != text && !text.isBlank())
			{
				getLogger().info("parseDouble('%s', %s, %s) could not be parsed", text, defaultValue, limit);
			}
			value = defaultValue;
		}

		if (null != limit && (value < -limit || value > limit))
		{
			getLogger().info("parseDouble('%s', %s, %s) is outside the limit", text, defaultValue, limit);
			return defaultValue;
		}
		return value;
	}

	/**
	 * The observer's latitude off the query string, or the marker's own.
	 */
	public static double parseLatitude(String text)
	{
		double latitude = parseDouble(text, DEFAULT_LATITUDE, MAXIMUM_LATITUDE);
		return latitude;
	}

	/**
	 * The observer's longitude off the query string, or the marker's own.
	 */
	public static double parseLongitude(String text)
	{
		double longitude = parseDouble(text, DEFAULT_LONGITUDE, MAXIMUM_LONGITUDE);
		return longitude;
	}

	/**
	 * The zone off the query string, or the marker's own when it is missing or names no zone
	 * the machine knows.
	 */
	public static ZoneId parseTimeZone(String text)
	{
		ZoneId timeZone;
		try
		{
			timeZone = ZoneId.of(text.trim());
		}
		catch (Exception failure)
		{
			if (null != text && !text.isBlank())
			{
				getLogger().info("parseTimeZone('%s') is not a zone, using '%s'", text, DEFAULT_TIME_ZONE);
			}
			timeZone = ZoneId.of(DEFAULT_TIME_ZONE);
		}
		return timeZone;
	}

	/**
	 * A width off the query string, or the default when it is missing, unparseable or outside
	 * what the drawing is legible at.
	 */
	public static int parseWidth(String text)
	{
		int width;
		try
		{
			width = Integer.parseInt(text.trim());
		}
		catch (Exception failure)
		{
			if (null != text && !text.isBlank())
			{
				getLogger().info("parseWidth('%s') could not be parsed, using '%s'", text, DEFAULT_CHART_WIDTH);
			}
			width = DEFAULT_CHART_WIDTH;
		}

		if (width < MINIMUM_CHART_WIDTH || width > MAXIMUM_CHART_WIDTH)
		{
			getLogger().info("parseWidth('%s') is outside %s to %s, using '%s'",
					text, MINIMUM_CHART_WIDTH, MAXIMUM_CHART_WIDTH, DEFAULT_CHART_WIDTH);
			return DEFAULT_CHART_WIDTH;
		}
		return width;
	}
}
