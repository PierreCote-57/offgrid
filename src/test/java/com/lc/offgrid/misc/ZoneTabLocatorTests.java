package com.lc.offgrid.misc;

import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.common.misc.external.ZoneTabLocator;
import com.lc.offgrid.common.misc.geography.point.LatLonPoint;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * What {@code ZoneTabLocator} answers for a zone name the browser can state, whether the tzdb
 * release names it in zone1970.tab, names it only in backward, or does not name it at all.
 */
public class ZoneTabLocatorTests extends AbstractTests
{
	/** The tables state seconds, so a value is right to well inside one of them. */
	private static final double DEGREE_TOLERANCE = 0.0001;

	/**
	 * A zone name and the location it stands at. A null latitude says the release does not name
	 * the zone and the lookup answers null.
	 */
	public static Object[][] ZonePointSource()
	{
		return new Object[][] {
				new Object[] {"America/Montreal",    43.650000,  -79.383333},
				new Object[] {"Asia/Calcutta",       22.533333,   88.366667},
				new Object[] {"Europe/Kiev",         50.433333,   30.516667},
				new Object[] {"US/Pacific",          34.052222, -118.242778},
				new Object[] {"America/Vancouver",   49.266667, -123.116667},
				new Object[] {"America/Los_Angeles", 34.052222, -118.242778},
				new Object[] {"America/Anchorage",   61.218056, -149.900278},
				new Object[] {"America/Enchorage",   null, null},
		};
	}

	@ParameterizedTest
	@MethodSource("ZonePointSource")
	public void testGetPointOf(String zoneName, Double latitudeDeg, Double longitudeDeg) throws Exception
	{
		LatLonPoint point = ZoneTabLocator.getPointOf(zoneName);

		if (null == latitudeDeg)
		{
			assertNull(point, String.format("getPointOf(%s) named a place the release does not", zoneName));
		}
		else
		{
			assertNotNull(point, String.format("getPointOf(%s) answered nothing", zoneName));
			assertEquals(latitudeDeg, point.getLatitudeDeg(), DEGREE_TOLERANCE,
					String.format("getPointOf(%s) answered the wrong latitude", zoneName));
			assertEquals(longitudeDeg, point.getLongitudeDeg(), DEGREE_TOLERANCE,
					String.format("getPointOf(%s) answered the wrong longitude", zoneName));
		}
	}
}
