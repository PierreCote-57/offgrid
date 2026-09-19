package com.lc.offgrid.webapp.spring.site;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.common.misc.external.ZoneTabLocator;
import com.lc.offgrid.common.misc.geography.point.LatLonPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * What the site needs in more than one place and no one class owns: the values it is built
 * around, and the small operations on them.
 *
 * It is a component only so that Spring has an instance to inject {@code folder.local} into.
 * Everything here is static, including that folder: a class that is not a bean — an ephemeris
 * built to serve one request, say — needs the path and has nowhere to be given it.
 */
@Component
public class OffgridUtil
{
	/** The 50th parallel marker in Campbell River, where a caller whose zone names no place lands. */
	private static final double DEFAULT_LATITUDE = 50.0;
	private static final double DEFAULT_LONGITUDE = -125.230450;
	private static final String DEFAULT_TIME_ZONE = "America/Vancouver";

	public static final double MAXIMUM_LATITUDE = 90.0;
	public static final double MAXIMUM_LONGITUDE = 180.0;

	/** The ends parseEpochSecond answers with: a moment past either one is given that end. */
	public static final LocalDate SKY_FIRST_DATE = LocalDate.of(1800, 1, 1);
	public static final LocalDate SKY_LAST_DATE = LocalDate.of(2050, 12, 31);

	private static final BasicLogger LOGGER		= BasicLogger.getLogger(OffgridUtil.class);

	private static String dataRootFolder;

	/**
	 * Spring calls this once at startup, which is what puts {@code folder.local} where a static
	 * reader can get at it. An instance method because Spring does not inject a static field.
	 */
	@Value("${folder.local}")
	public void setDataRootFolder(String dataRootFolder)
	{
		OffgridUtil.dataRootFolder = dataRootFolder;
	}

	/** The local folder, which holds images, documents, logs and the ephemeris. */
	public static String getDataRootFolder()
	{
		return dataRootFolder;
	}

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

	/** The zone a caller naming none is answered for. */
	public static String getDefaultTimeZone()
	{
		return DEFAULT_TIME_ZONE;
	}

	/** The marker's own latitude, which is where a caller stating no place at all lands. */
	public static double getDefaultLatitude()
	{
		return DEFAULT_LATITUDE;
	}

	/** The marker's own longitude, the same way. */
	public static double getDefaultLongitude()
	{
		return DEFAULT_LONGITUDE;
	}

	/**
	 * The latitude of the zone's own principal location, or the marker's when the tzdb release
	 * names no place for it.
	 */
	public static double getDefaultLatitude(ZoneId timeZone)
	{
		LatLonPoint point = ZoneTabLocator.getPointOf(timeZone.getId());
		double latitude = (null == point) ? getDefaultLatitude() : point.getLatitudeDeg();
		return latitude;
	}

	/**
	 * The longitude of the zone's own principal location, the same way.
	 */
	public static double getDefaultLongitude(ZoneId timeZone)
	{
		LatLonPoint point = ZoneTabLocator.getPointOf(timeZone.getId());
		double longitude = (null == point) ? getDefaultLongitude() : point.getLongitudeDeg();
		return longitude;
	}

	/**
	 * The observer's latitude off the query string, or the zone's own.
	 */
	public static double parseLatitude(String text, ZoneId timeZone)
	{
		double defaultLatitude = getDefaultLatitude(timeZone);
		double latitude = parseDouble(text, defaultLatitude, MAXIMUM_LATITUDE);
		return latitude;
	}

	/**
	 * The observer's longitude off the query string, or the zone's own.
	 */
	public static double parseLongitude(String text, ZoneId timeZone)
	{
		double defaultLongitude = getDefaultLongitude(timeZone);
		double longitude = parseDouble(text, defaultLongitude, MAXIMUM_LONGITUDE);
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
				getLogger().info("parseTimeZone('%s') is not a zone, using '%s'", text, getDefaultTimeZone());
			}
			timeZone = ZoneId.of(getDefaultTimeZone());
		}
		return timeZone;
	}

	/**
	 * The moment the sky is asked for: the instant off the query string, read in the zone off
	 * the query string. Each part falls back on its own.
	 */
	public static ZonedDateTime parseZonedDateTime(String epochSecondText, String timeZoneText)
	{
		long epochSecond = parseEpochSecond(epochSecondText);
		ZoneId timeZone = parseTimeZone(timeZoneText);
		ZonedDateTime dateTime = Instant.ofEpochSecond(epochSecond).atZone(timeZone);
		return dateTime;
	}

	/**
	 * The instant the sky is asked for, in seconds since the epoch, or now when the caller
	 * states none. A value outside what the ephemeris covers is answered with the end it
	 * passed.
	 */
	public static long parseEpochSecond(String text)
	{
		long epochSecond;
		try
		{
			epochSecond = Long.parseLong(text.trim());
		}
		catch (Exception failure)
		{
			if (null != text && !text.isBlank())
			{
				getLogger().info("parseEpochSecond('%s') could not be parsed, using now", text);
			}
			epochSecond = System.currentTimeMillis() / 1000;
		}

		long firstEpochSecond = SKY_FIRST_DATE.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		long lastEpochSecond = SKY_LAST_DATE.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		if (epochSecond < firstEpochSecond)
		{
			getLogger().info("parseEpochSecond('%s') is before %s, using it", text, SKY_FIRST_DATE);
			return firstEpochSecond;
		}
		if (epochSecond > lastEpochSecond)
		{
			getLogger().info("parseEpochSecond('%s') is after %s, using it", text, SKY_LAST_DATE);
			return lastEpochSecond;
		}
		return epochSecond;
	}
}
