package com.lc.offgrid.common.misc.sky;

import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.offgrid.common.misc.astronomy.constellation.Constellation;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsEphemeris;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsMoment;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsMomentName;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsPosition;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class SkyBodyAnalyser
{
	private static final Duration SAMPLE_STEP = Duration.ofMinutes(2);

	private final Double			latitude;
	private final Double			longitude;
	private final HorizonsEphemeris	ephemeris;
	private final Instant			instant;
	private final HorizonsPosition	sunPosition;
	private final HorizonsPosition	earthPosition;

	public SkyBodyAnalyser(Instant instant)
	{
		this(instant, null, null);
	}
	public SkyBodyAnalyser(Instant instant, Double latitude, Double longitude)
	{
		this.instant = instant;
		this.latitude = latitude;
		this.longitude = longitude;

		try
		{
			this.ephemeris = new HorizonsEphemeris(instant);
		}
		catch (Exception exception)
		{
			throw new BasicRuntimeException(exception,
					"SkyBodyAnalyser(%1$s, %2$s, %3$s) could not read the ephemeris",
					instant, latitude, longitude);
		}

		this.sunPosition = ephemeris.getPosition(HorizonsBody.SUN, getInstant());
		this.earthPosition = new HorizonsPosition(getInstant(), 0.0, 0.0, 0.0, null, null);
	}

	public static int[] getYearRange()
	{
		int[] yearRange = HorizonsEphemeris.getYearRange();
		return yearRange;
	}

	public Double getLatitude()
	{
		return latitude;
	}

	public Double getLongitude()
	{
		return longitude;
	}

	public HorizonsEphemeris getEphemeris()
	{
		return ephemeris;
	}

	public Instant getInstant()
	{
		return instant;
	}

	public HorizonsPosition getSunPosition()
	{
		return sunPosition;
	}
	public HorizonsPosition getEarthPosition()
	{
		return earthPosition;
	}

	/**
	 * The angle from a body's parent to the body, in degrees from 0 to 360, 0 being 3 o'clock.
	 */
	public double getSunAngle(HorizonsBody body)
	{
		// The only body without ephemeris is Earth
		HorizonsPosition bodyPosition = body.hasEphemeris()
				? getEphemeris().getPosition(body, getInstant())
				: getEarthPosition();
		HorizonsPosition parentPosition = HorizonsBody.EARTH.equals(body.getParent())
				? getEarthPosition()
				: getSunPosition();
		double angle = bodyPosition.getSunAngle(parentPosition);
		return angle;
	}

	public Map<HorizonsBody, Double> getSunAngleMap()
	{
		Map<HorizonsBody, Double> angleMap = new EnumMap<>(HorizonsBody.class);

		for (HorizonsBody body : HorizonsBody.values())
		{
			double angle = getSunAngle(body);
			angleMap.put(body, angle);
		}

		return angleMap;
	}

	public SkyBodyDay getSkyBodyDay(HorizonsBody body, ZoneId timeZone)
	{
		HorizonsPosition position = getEphemeris().getPosition(body, getInstant());
		HorizonsPosition parentPosition = HorizonsBody.EARTH.equals(body.getParent())
				? getEarthPosition()
				: getSunPosition();

		Map<HorizonsMomentName, HorizonsMoment> momentMap = makeMomentMap(body, timeZone);
		double distanceAu = position.getRange();
		double orbitRadiusAu = position.getOrbitRadius(parentPosition);
		Double apparentMagnitude = position.getApparentMagnitude();
		Constellation constellation = position.getConstellation();
		double litFraction = position.getLitFraction(getSunPosition());

		SkyBodyDay bodyDay = new SkyBodyDay(momentMap, distanceAu, orbitRadiusAu, apparentMagnitude,
				constellation, litFraction);
		return bodyDay;
	}

	public Map<HorizonsBody, SkyBodyDay> getSkyBodyDayMap(ZoneId timeZone)
	{
		Map<HorizonsBody, SkyBodyDay> bodyDayMap = new EnumMap<>(HorizonsBody.class);

		for (HorizonsBody body : HorizonsBody.values())
		{
			if (!body.hasEphemeris())
			{
				continue;
			}

			SkyBodyDay bodyDay = getSkyBodyDay(body, timeZone);
			bodyDayMap.put(body, bodyDay);
		}

		return bodyDayMap;
	}

	private Map<HorizonsMomentName, HorizonsMoment> makeMomentMap(HorizonsBody body, ZoneId timeZone)
	{
		List<HorizonsMoment> sampleList = makeSampleList(body, timeZone);
		Map<HorizonsMomentName, HorizonsMoment> momentMap = new EnumMap<>(HorizonsMomentName.class);

		momentMap.put(HorizonsMomentName.RISE, findRise(body, sampleList));
		momentMap.put(HorizonsMomentName.TRANSIT, findTransit(body, sampleList));
		momentMap.put(HorizonsMomentName.SET, findSet(body, sampleList));

		momentMap.put(HorizonsMomentName.NOW, makeMoment(body, getInstant()));

		return momentMap;
	}

	/** The step is the precision every crossing found in the list carries. */
	private List<HorizonsMoment> makeSampleList(HorizonsBody body, ZoneId timeZone)
	{
		LocalDate localDate = getInstant().atZone(timeZone).toLocalDate();
		ZonedDateTime dayStart = localDate.atStartOfDay(timeZone);
		ZonedDateTime dayEnd = dayStart.plusDays(1);

		Instant endInstant = dayEnd.toInstant();
		List<HorizonsMoment> sampleList = new ArrayList<>();
		Instant sampleInstant = dayStart.toInstant();

		while (!sampleInstant.isAfter(endInstant))
		{
			HorizonsMoment sample = makeMoment(body, sampleInstant);
			sampleList.add(sample);
			sampleInstant = sampleInstant.plus(SAMPLE_STEP);
		}

		return sampleList;
	}

	private static HorizonsMoment findRise(HorizonsBody body, List<HorizonsMoment> sampleList)
	{
		HorizonsMoment moment = findCrossing(sampleList, 0.0, HorizonsMoment::getElevation, true);
		return moment;
	}
	private static HorizonsMoment findSet(HorizonsBody body, List<HorizonsMoment> sampleList)
	{
		HorizonsMoment moment = findCrossing(sampleList, 0.0, HorizonsMoment::getElevation, false);
		return moment;
	}
	private static HorizonsMoment findTransit(HorizonsBody body, List<HorizonsMoment> sampleList)
	{
		HorizonsMoment moment = findCrossing(sampleList, 180.0, HorizonsMoment::getBearing, true);
		return moment;
	}
	private static HorizonsMoment findCrossing(
			List<HorizonsMoment> sampleList, double targetValue,
			ToDoubleFunction<HorizonsMoment> valueFunction,
			boolean rising)
	{
		HorizonsMoment moment = null;
		for (int index = 1; index < sampleList.size(); index++)
		{
			HorizonsMoment momentBefore = sampleList.get(index - 1);
			HorizonsMoment momentAfter = sampleList.get(index);
			double valueBefore = valueFunction.applyAsDouble(momentBefore);
			double valueAfter = valueFunction.applyAsDouble(momentAfter);
			boolean crosses = rising
					? valueBefore < targetValue && valueAfter >= targetValue
					: valueBefore > targetValue && valueAfter <= targetValue;
			if (crosses)
			{
				double beforeGap = Math.abs(targetValue - valueBefore);
				double afterGap = Math.abs(targetValue - valueAfter);
				moment = beforeGap < afterGap ? momentBefore : momentAfter;
				break;
			}
		}
		return moment;
	}

	private HorizonsMoment makeMoment(HorizonsBody body, Instant momentInstant)
	{
		HorizonsMoment moment = getEphemeris().getMoment(body, momentInstant, getLatitude(), getLongitude());
		return moment;
	}

}
