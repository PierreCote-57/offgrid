package com.lc.offgrid.misc;

import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.OffgridTestApplication;
import com.lc.offgrid.common.misc.astronomy.constellation.Constellation;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsMoment;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsMomentName;
import com.lc.offgrid.common.misc.sky.SkyBodyAnalyser;
import com.lc.offgrid.common.misc.sky.SkyBodyDay;
import com.lc.offgrid.webapp.spring.site.OffgridUtil;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The day is read from the ephemeris files under {@code folder.local}, and those cover the
 * years that have been fetched.
 */
@SpringBootTest(classes = OffgridTestApplication.class)
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SkyBodyAnalyserTests extends AbstractTests
{
	private static final ZoneId ZONE_ROOT = ZoneId.of(OffgridUtil.getDefaultTimeZone());

	/**
	 * How far a crossing may sit from the value it crosses. The answer is the sample nearer to
	 * the crossing, so the gap is the body's own movement over half a sample step: a body near
	 * the zenith swings through its bearing faster than it climbs.
	 */
	private static final double ELEVATION_TOLERANCE = 1.0;
	private static final double BEARING_TOLERANCE = 5.0;

	private static final double DEGREES_AROUND = 360.0;

	private static final DateTimeFormatter TEXT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private static final String NO_MOMENT = "does not happen on the day";

	@Value("${folder.local}")
	private String dataRootFolder;

	public String getDataRootFolder()
	{
		return dataRootFolder;
	}

	private static ZonedDateTime makeZDT(String text)
	{
		LocalDateTime localDateTime = LocalDateTime.parse(text, TEXT_FORMAT);
		ZonedDateTime dateTime = localDateTime.atZone(ZONE_ROOT);
		return dateTime;
	}

	/** Fixed moments, on a day the files on disk cover, so every run asks the same thing. */
	public static Object[][] AngleDegSource()
	{
		return new Object[][] {
				new Object[] {makeZDT("2026-09-14 12:00")},
		};
	}

	public static Object[][] BodyDaySource()
	{
		return new Object[][] {
				new Object[] {makeZDT("2026-09-14 12:00")},
		};
	}

	@ParameterizedTest
	@MethodSource("AngleDegSource")
	public void testAngleDegMap(ZonedDateTime dateTime) throws Exception
	{
		SkyBodyAnalyser analyser = makeAnalyser(dateTime);
		Map<HorizonsBody, Double> angleMap = analyser.getAngleDegMap();

		int bodyCount = ephemerisBodyCount();
		assertEquals(bodyCount, angleMap.size(), "getAngleDegMap() answered a body short");

		for (HorizonsBody body : HorizonsBody.values())
		{
			if (!body.hasEphemeris())
			{
				continue;
			}

			Double angle = angleMap.get(body);
			String missing = String.format("getAngleDegMap() has no angle for %s", body);
			assertNotNull(angle, missing);

			getLogger().info("%-8s is %6.2f° around the ecliptic from its parent", body, angle);

			boolean inCircle = angle >= 0.0 && angle < DEGREES_AROUND;
			String outside = String.format("getAngleDeg(%s) answered %s, which is outside 0 to 360",
					body, angle);
			assertTrue(inCircle, outside);
		}
	}

	@ParameterizedTest
	@MethodSource("BodyDaySource")
	public void testSkyBodyDayMap(ZonedDateTime dateTime) throws Exception
	{
		SkyBodyAnalyser analyser = makeAnalyser(dateTime);
		Map<HorizonsBody, SkyBodyDay> bodyDayMap = analyser.getSkyBodyDayMap(ZONE_ROOT);

		int bodyCount = ephemerisBodyCount();
		assertEquals(bodyCount, bodyDayMap.size(), "getSkyBodyDayMap() answered a body short");

		for (HorizonsBody body : HorizonsBody.values())
		{
			if (!body.hasEphemeris())
			{
				continue;
			}

			SkyBodyDay bodyDay = bodyDayMap.get(body);
			String missing = String.format("getSkyBodyDayMap() has no day for %s", body);
			assertNotNull(bodyDay, missing);

			logBodyDay(body, bodyDay);
			checkBodyDay(body, bodyDay);
		}
	}

	private static int ephemerisBodyCount()
	{
		int bodyCount = 0;

		for (HorizonsBody body : HorizonsBody.values())
		{
			if (body.hasEphemeris())
			{
				bodyCount++;
			}
		}

		return bodyCount;
	}

	private void logBodyDay(HorizonsBody body, SkyBodyDay bodyDay)
	{
		Constellation constellation = bodyDay.getConstellation();
		String constellationName = (null == constellation) ? null : constellation.getLatinName();

		getLogger().info("%-8s %.3f AU, %s, %.0f%% lit", body, bodyDay.getDistanceAu(),
				constellationName, bodyDay.getLitFraction() * 100.0);

		for (Map.Entry<HorizonsMomentName, HorizonsMoment> entry :bodyDay.getMomentMap().entrySet())
		{
			HorizonsMomentName name = entry.getKey();
			HorizonsMoment moment = entry.getValue();
			String momentText = null == moment ? NO_MOMENT : moment.toString();
			getLogger().info("\t%-8s %s", name.getDisplayName(), momentText);
		}
	}

	private void checkBodyDay(HorizonsBody body, SkyBodyDay bodyDay)
	{
		Map<HorizonsMomentName, HorizonsMoment> momentMap = bodyDay.getMomentMap();

		HorizonsMoment now = momentMap.get(HorizonsMomentName.NOW);
		String noNow = String.format("%s has no NOW moment, which every body has", body);
		assertNotNull(now, noNow);

		HorizonsMoment rise = momentMap.get(HorizonsMomentName.RISE);
		checkCrossing(body, HorizonsMomentName.RISE, rise, 0.0, ELEVATION_TOLERANCE, true);

		HorizonsMoment set = momentMap.get(HorizonsMomentName.SET);
		checkCrossing(body, HorizonsMomentName.SET, set, 0.0, ELEVATION_TOLERANCE, true);

		HorizonsMoment transit = momentMap.get(HorizonsMomentName.TRANSIT);
		checkCrossing(body, HorizonsMomentName.TRANSIT, transit, 180.0, BEARING_TOLERANCE, false);

		double distanceAu = bodyDay.getDistanceAu();
		String noDistance = String.format("%s answered a distance of %s AU", body, distanceAu);
		assertTrue(distanceAu > 0.0, noDistance);

		double litFraction = bodyDay.getLitFraction();
		boolean litInRange = litFraction >= 0.0 && litFraction <= 1.0;
		String badLit = String.format("%s answered a lit fraction of %s", body, litFraction);
		assertTrue(litInRange, badLit);
	}

	private void checkCrossing(HorizonsBody body, HorizonsMomentName name, HorizonsMoment moment,
			double targetValue, double tolerance, boolean onElevation)
	{
		if (null == moment)
		{
			return;
		}

		double value = onElevation ? moment.getElevation() : moment.getBearing();
		double gap = Math.abs(targetValue - value);
		String tooFar = String.format("%s %s is at %s, which is %s from %s",
				body, name, value, gap, targetValue);
		assertTrue(gap <= tolerance, tooFar);
	}

	private SkyBodyAnalyser makeAnalyser(ZonedDateTime dateTime)
	{
		SkyBodyAnalyser analyser = new SkyBodyAnalyser(dateTime.toInstant(),
				OffgridUtil.getDefaultLatitude(), OffgridUtil.getDefaultLongitude());
		return analyser;
	}
}
