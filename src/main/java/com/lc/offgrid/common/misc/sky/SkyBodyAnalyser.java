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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * What the sky page needs, worked out from the positions JPL Horizons gives.
 *
 * The astronomy is Horizons' own: a position answers the angle from the Sun and the lit
 * fraction, and the ephemeris answers where a body stands for an observer. What is here is the
 * day — which moments are asked for, and what is made of them.
 *
 * One of these is built for one moment, stated in the zone of the place asking about it, and
 * answers about that moment only. It serves one request and then goes away, so it is not a bean
 * and the local folder arrives as a constructor parameter.
 */
public class SkyBodyAnalyser
{
	/** How often the day is sampled when a crossing is looked for. */
	private static final Duration SAMPLE_STEP = Duration.ofMinutes(2);

	private final ZonedDateTime		dateTime;
	private final Double			latitude;
	private final Double			longitude;
	private final HorizonsEphemeris	ephemeris;
	private final Instant			instant;
	private final HorizonsPosition	sunPosition;
	private final HorizonsPosition	earthPosition;

	/**
	 * Reads the ephemeris for the local date the moment falls on, which is the day the caller is
	 * asking about where the caller is. The latitude and longitude are that place, in degrees,
	 * north and east positive: they are the horizon a body rises over. The Sun's position is
	 * read here too, every angle and every lit fraction being measured against it.
	 *
	 * An ephemeris that cannot be read is a runtime failure: nothing a caller states can make
	 * a missing or malformed file readable, so there is nothing for it to catch.
	 */
	public SkyBodyAnalyser(ZonedDateTime dateTime)
	{
		this(dateTime, null, null);
	}
	public SkyBodyAnalyser(ZonedDateTime dateTime, Double latitude, Double longitude)
	{
		this.dateTime = dateTime;
		this.latitude = latitude;
		this.longitude = longitude;

		LocalDate localDate = dateTime.toLocalDate();
		try
		{
			this.ephemeris = new HorizonsEphemeris(localDate);
		}
		catch (Exception exception)
		{
			throw new BasicRuntimeException(exception,
					"SkyBodyAnalyser(%1$s, %2$s, %3$s) could not read the ephemeris",
					dateTime, latitude, longitude);
		}

		this.instant = dateTime.toInstant();
		this.sunPosition = ephemeris.getPosition(HorizonsBody.SUN, instant);
		this.earthPosition = new HorizonsPosition(getInstant(), 0.0, 0.0, 0.0, null, null);
	}

	/**
	 * The first and last year an analyser can be built for, which is what there are ephemeris
	 * files for. Null when there are none.
	 */
	public static int[] getYearRange()
	{
		int[] yearRange = HorizonsEphemeris.getYearRange();
		return yearRange;
	}

	public ZonedDateTime getDateTime()
	{
		return dateTime;
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

	/**
	 * Every body's angle from its parent.
	 */
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

	/**
	 * One body's day at the observer's place: when it rises, transits and sets, and what it
	 * looks like while it is up.
	 */
	public SkyBodyDay getSkyBodyDay(HorizonsBody body)
	{
		HorizonsPosition position = getEphemeris().getPosition(body, getInstant());
		HorizonsPosition parentPosition = HorizonsBody.EARTH.equals(body.getParent())
				? getEarthPosition()
				: getSunPosition();

		Map<HorizonsMomentName, HorizonsMoment> momentMap = makeMomentMap(body);
		double distance = position.getRange();
		double orbitRadius = position.getOrbitRadius(parentPosition);
		Double apparentMagnitude = position.getApparentMagnitude();
		Constellation constellation = position.getConstellation();
		double litFraction = position.getLitFraction(getSunPosition());

		SkyBodyDay bodyDay = new SkyBodyDay(momentMap, distance, orbitRadius, apparentMagnitude,
				constellation, litFraction);
		return bodyDay;
	}

	/**
	 * Every body's day, in the order the enum states them. Only the bodies there is an ephemeris
	 * to read are in it, which is what the table's rows are built from.
	 */
	public Map<HorizonsBody, SkyBodyDay> getSkyBodyDayMap()
	{
		Map<HorizonsBody, SkyBodyDay> bodyDayMap = new EnumMap<>(HorizonsBody.class);

		for (HorizonsBody body : HorizonsBody.values())
		{
			if (!body.hasEphemeris())
			{
				continue;
			}

			SkyBodyDay bodyDay = getSkyBodyDay(body);
			bodyDayMap.put(body, bodyDay);
		}

		return bodyDayMap;
	}

	/**
	 * The day's moments, each under its name. A crossing that does not happen on the day is a
	 * null, and only the first of each is taken.
	 */
	private Map<HorizonsMomentName, HorizonsMoment> makeMomentMap(HorizonsBody body)
	{
		List<HorizonsMoment> sampleList = makeSampleList(body);
		Map<HorizonsMomentName, HorizonsMoment> momentMap = new EnumMap<>(HorizonsMomentName.class);

		momentMap.put(HorizonsMomentName.RISE, findRise(body, sampleList));
		momentMap.put(HorizonsMomentName.TRANSIT, findTransit(body, sampleList));
		momentMap.put(HorizonsMomentName.SET, findSet(body, sampleList));

		momentMap.put(HorizonsMomentName.NOW, makeMoment(body, getInstant()));

		return momentMap;
	}

	/**
	 * The body's place in the sky at every step of the observer's day, which is what a crossing
	 * is looked for in. The step is the precision the answers carry.
	 */
	private List<HorizonsMoment> makeSampleList(HorizonsBody body)
	{
		LocalDate localDate = getDateTime().toLocalDate();
		ZonedDateTime dayStart = localDate.atStartOfDay(getDateTime().getZone());
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

	/**
	 * Where the body stands for this observer at one moment.
	 */
	private HorizonsMoment makeMoment(HorizonsBody body, Instant momentInstant)
	{
		HorizonsMoment moment = getEphemeris().getMoment(body, momentInstant, getLatitude(), getLongitude());
		return moment;
	}

}
