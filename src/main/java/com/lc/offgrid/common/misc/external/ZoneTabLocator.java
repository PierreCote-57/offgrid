package com.lc.offgrid.common.misc.external;

import com.lc.basics.tools.file.BasicFileReader;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.offgrid.common.misc.geography.point.LatLonPoint;
import com.lc.offgrid.common.misc.geography.point.LatLonPointPojo;

import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 * Where a timezone's principal location is, from the tzdb release under
 * resources/external/download. The tables are read once, when the class loads; a read that fails
 * leaves them empty and every zone then answers null.
 */
public class ZoneTabLocator
{
	private static final BasicLogger			LOGGER				= BasicLogger.getLogger(ZoneTabLocator.class);

	/** Zone name to its principal location, from zone1970.tab. */
	private static final Map<String, LatLonPoint>	POINT_MAP		= new TreeMap<>();

	/** Retired zone name to the one it became, from backward. */
	private static final Map<String, String>		CANONICAL_MAP	= new TreeMap<>();

	/*
	 * zone1970.tab states one tab between its columns; backward pads its own with runs of them,
	 * so a run is what separates a field in both files. Comment and blank lines never arrive —
	 * BasicFileReader.readLine() drops them.
	 */
	private static final String	FIELD_SEPARATOR			= "\t+";

	private static final int	COORDINATE_INDEX		= 1;
	private static final int	ZONE_NAME_INDEX			= 2;

	private static final String	LINK_KEYWORD			= "Link";
	private static final int	KEYWORD_INDEX			= 0;
	private static final int	CANONICAL_NAME_INDEX	= 1;
	private static final int	ALIAS_NAME_INDEX		= 2;

	/** ISO 6709 sign-degrees-minutes-seconds: ±DDMM±DDDMM, or ±DDMMSS±DDDMMSS when it is longer. */
	private static final int	LONG_COORDINATE_LENGTH	= 15;
	private static final int	SHORT_LATITUDE_LENGTH	= 5;
	private static final int	LONG_LATITUDE_LENGTH	= 7;
	private static final int	LATITUDE_DEGREE_DIGITS	= 2;
	private static final int	LONGITUDE_DEGREE_DIGITS	= 3;
	private static final int	MINUTE_DIGITS			= 2;
	private static final String	MINUS_SIGN				= "-";
	private static final double	MINUTES_PER_DEGREE		= 60.0;
	private static final double	SECONDS_PER_DEGREE		= 3600.0;

	static
	{
		String zoneTabPath = ExternalUpdater.getZoneTabPath();
		String backwardPath = ExternalUpdater.getBackwardPath();

		try
		{
			readZoneTab(zoneTabPath);
			readBackward(backwardPath);
		}
		catch (Exception exception)
		{
			getLogger().warn(exception, "ZoneTabLocator(%s, %s) read nothing; every zone answers null",
					zoneTabPath, backwardPath);
		}
	}

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	private static Map<String, LatLonPoint> getPointMap()
	{
		return POINT_MAP;
	}

	private static Map<String, String> getCanonicalMap()
	{
		return CANONICAL_MAP;
	}

	/**
	 * The principal location of the zone, or null when the release does not name it — a zone
	 * retired since 1970 is looked up again under the name it became.
	 */
	public static LatLonPoint getPointOf(String zoneName)
	{
		LatLonPoint point = getPointMap().get(zoneName);

		if (null == point)
		{
			String canonicalName = getCanonicalMap().get(zoneName);
			if (null != canonicalName)
			{
				point = getPointMap().get(canonicalName);
			}
		}

		return point;
	}

	/** The file as the classpath holds it, since resources/external/download ships in the jar. */
	private static URL getResourceUrl(String path)
	{
		URL url = ZoneTabLocator.class.getClassLoader().getResource(path);

		if (null == url)
		{
			throw new BasicRuntimeException("getResourceUrl(%s) found nothing on the classpath", path);
		}

		return url;
	}

	/** One signed group, its degrees written in the digit count the group states. */
	private static double parseDegree(String text, int degreeDigits)
	{
		int minuteStart = 1 + degreeDigits;
		int secondStart = minuteStart + MINUTE_DIGITS;

		double degree = Double.parseDouble(text.substring(1, minuteStart));
		double minute = Double.parseDouble(text.substring(minuteStart, secondStart));
		double second = 0.0;
		if (text.length() > secondStart)
		{
			second = Double.parseDouble(text.substring(secondStart));
		}

		double size = degree + minute / MINUTES_PER_DEGREE + second / SECONDS_PER_DEGREE;
		double value = text.startsWith(MINUS_SIGN) ? -size : size;
		return value;
	}

	/** Latitude then longitude, run together, each carrying its own sign. */
	private static LatLonPoint parsePoint(String coordinateText)
	{
		int latitudeLength = LONG_COORDINATE_LENGTH == coordinateText.length()
				? LONG_LATITUDE_LENGTH : SHORT_LATITUDE_LENGTH;

		String latitudeText = coordinateText.substring(0, latitudeLength);
		String longitudeText = coordinateText.substring(latitudeLength);

		double latitudeDeg = parseDegree(latitudeText, LATITUDE_DEGREE_DIGITS);
		double longitudeDeg = parseDegree(longitudeText, LONGITUDE_DEGREE_DIGITS);

		LatLonPointPojo point = new LatLonPointPojo(latitudeDeg, longitudeDeg);
		return point;
	}

	/** The countries in column 1 and the comment in column 4 are not read. */
	private static void readZoneTab(String path) throws Exception
	{
		URL url = getResourceUrl(path);

		try (BasicFileReader reader = new BasicFileReader())
		{
			reader.openFile(url);

			String line = reader.readLine();
			while (null != line)
			{
				String[] fieldList = line.split(FIELD_SEPARATOR);
				if (fieldList.length > ZONE_NAME_INDEX)
				{
					LatLonPoint point = parsePoint(fieldList[COORDINATE_INDEX]);
					getPointMap().put(fieldList[ZONE_NAME_INDEX], point);
				}

				line = reader.readLine();
			}
		}
	}

	/** The Link lines, which read "Link <canonical name> <alias name>"; anything after is comment. */
	private static void readBackward(String path) throws Exception
	{
		URL url = getResourceUrl(path);

		try (BasicFileReader reader = new BasicFileReader())
		{
			reader.openFile(url);

			String line = reader.readLine();
			while (null != line)
			{
				String[] fieldList = line.split(FIELD_SEPARATOR);
				if (fieldList.length > ALIAS_NAME_INDEX && LINK_KEYWORD.equals(fieldList[KEYWORD_INDEX]))
				{
					getCanonicalMap().put(fieldList[ALIAS_NAME_INDEX], fieldList[CANONICAL_NAME_INDEX]);
				}

				line = reader.readLine();
			}
		}
	}
}
