package com.lc.offgrid.common.misc.sky;

import com.lc.offgrid.common.misc.external.horizons.HorizonsBody;
import com.lc.offgrid.common.misc.external.horizons.HorizonsEphemeris;
import com.lc.offgrid.common.misc.external.horizons.HorizonsPosition;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * What the sky page needs, worked out from the positions JPL Horizons gives.
 *
 * The angles are taken in the ecliptic plane, which is the plane the chart is a plan view of.
 * The positions are stated against the celestial equator, so the rotation between the two is
 * this class's own business.
 *
 * One of these is built for one moment, stated in the zone of the place asking about it, and
 * answers about that moment only. It serves one request and then goes away, so it is not a bean
 * and the local folder arrives as a constructor parameter.
 */
public class SkyAnalyser
{
	/** The tilt of the Earth's axis, in degrees: what turns an equatorial direction into an ecliptic one. */
	private static final double OBLIQUITY = 23.4392911;

	/** Greenwich sidereal time at J2000 in degrees, and how much of it a day adds. */
	private static final double SIDEREAL_AT_J2000 = 280.46061837;
	private static final double SIDEREAL_PER_DAY = 360.98564736629;

	/** The Julian Date of J2000, and of the moment a millisecond count starts from. */
	private static final double JULIAN_AT_J2000 = 2451545.0;
	private static final double JULIAN_AT_EPOCH = 2440587.5;
	private static final double MILLIS_PER_DAY = 86400000.0;

	/** How often the day is sampled when a crossing is looked for. */
	private static final Duration SAMPLE_STEP = Duration.ofMinutes(2);

	/** The elevation a body rises over, and the bearing it transits at. */
	private static final double HORIZON_ELEVATION = 0.0;
	private static final double TRANSIT_BEARING = 180.0;

	/** What is lit of a body that shines by itself. */
	private static final double FULLY_LIT = 1.0;

	private static final double DEGREES_AROUND = 360.0;

	private final ZonedDateTime		dateTime;
	private final double			latitude;
	private final double			longitude;
	private final HorizonsEphemeris	ephemeris;
	private final Instant			instant;
	private final HorizonsPosition	sunPosition;

	/**
	 * Reads the ephemeris for the local date the moment falls on, which is the day the caller is
	 * asking about where the caller is. The latitude and longitude are that place, in degrees,
	 * north and east positive: they are the horizon a body rises over. The Sun's position is
	 * read here too, every angle and every lit fraction being measured against it.
	 */
	public SkyAnalyser(String dataRootFolder, ZonedDateTime dateTime, double latitude, double longitude)
			throws IOException, ParseException
	{
		this.dateTime = dateTime;
		this.latitude = latitude;
		this.longitude = longitude;

		LocalDate localDate = dateTime.toLocalDate();
		this.ephemeris = new HorizonsEphemeris(dataRootFolder, localDate);

		this.instant = dateTime.toInstant();
		this.sunPosition = ephemeris.getPosition(HorizonsBody.SUN, instant);
	}

	public ZonedDateTime getDateTime()
	{
		return dateTime;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
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

	/**
	 * Where the Sun is at that moment, which every angle and every lit fraction is measured
	 * against.
	 */
	public HorizonsPosition getSunPosition()
	{
		return sunPosition;
	}

	/**
	 * The direction from the Sun to a body, in degrees from 0 to 360. It is the vector from the
	 * Sun's position to the body's, both of them seen from the Earth's centre.
	 */
	public double getSunAngle(HorizonsBody body)
	{
		HorizonsPosition bodyPosition = getEphemeris().getPosition(body, getInstant());

		double[] bodyVector = toEquatorialVector(bodyPosition);
		double[] sunVector = toEquatorialVector(getSunPosition());
		double[] sunToBody = subtract(bodyVector, sunVector);
		double[] eclipticVector = toEcliptic(sunToBody);

		double radians = Math.atan2(eclipticVector[1], eclipticVector[0]);
		double degrees = Math.toDegrees(radians);
		double angle = normalise(degrees);
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

	/**
	 * One body's day at the observer's place: when it rises, transits and sets, and what it
	 * looks like while it is up.
	 */
	public SkyBodyDay getSkyBodyDay(HorizonsBody body)
	{
		HorizonsPosition position = getEphemeris().getPosition(body, getInstant());

		List<SkyMoment> momentList = makeMomentList(body);
		double distance = position.getRange();
		Double apparentMagnitude = position.getApparentMagnitude();
		String constellation = position.getConstellation();
		double litFraction = getLitFraction(body);

		SkyBodyDay bodyDay = new SkyBodyDay(momentList, distance, apparentMagnitude, constellation, litFraction);
		return bodyDay;
	}

	public Map<HorizonsBody, SkyBodyDay> getSkyBodyDayMap()
	{
		Map<HorizonsBody, SkyBodyDay> bodyDayMap = new EnumMap<>(HorizonsBody.class);

		for (HorizonsBody body : HorizonsBody.values())
		{
			SkyBodyDay bodyDay = getSkyBodyDay(body);
			bodyDayMap.put(body, bodyDay);
		}

		return bodyDayMap;
	}

	/**
	 * The day's named moments and the moment that was asked about, in time order. A crossing
	 * that does not happen on the day is not in the list, and only the first of each is taken.
	 */
	private List<SkyMoment> makeMomentList(HorizonsBody body)
	{
		List<SkyMoment> sampleList = makeSampleList(body);
		List<SkyMoment> momentList = new ArrayList<>();

		SkyMoment askedFor = makeMoment(body, getDateTime(), null);
		momentList.add(askedFor);

		addWhenFound(momentList, findElevationCrossing(body, sampleList, true));
		addWhenFound(momentList, findTransit(body, sampleList));
		addWhenFound(momentList, findElevationCrossing(body, sampleList, false));

		Comparator<SkyMoment> byTime = Comparator.comparing(SkyMoment::getTime);
		momentList.sort(byTime);
		return momentList;
	}

	/**
	 * Keeps a moment that was found, a crossing that does not happen answering null.
	 */
	private void addWhenFound(List<SkyMoment> momentList, SkyMoment moment)
	{
		if (null != moment)
		{
			momentList.add(moment);
		}
	}

	/**
	 * The body's place in the sky every two minutes of the observer's day, which is what a
	 * crossing is looked for in.
	 */
	private List<SkyMoment> makeSampleList(HorizonsBody body)
	{
		LocalDate localDate = getDateTime().toLocalDate();
		ZonedDateTime dayStart = localDate.atStartOfDay(getDateTime().getZone());
		ZonedDateTime dayEnd = dayStart.plusDays(1);

		List<SkyMoment> sampleList = new ArrayList<>();
		ZonedDateTime time = dayStart;

		while (!time.isAfter(dayEnd))
		{
			SkyMoment sample = makeMoment(body, time, null);
			sampleList.add(sample);
			time = time.plus(SAMPLE_STEP);
		}

		return sampleList;
	}

	/**
	 * Where the body's elevation passes the horizon, going up or coming down.
	 */
	private SkyMoment findElevationCrossing(HorizonsBody body, List<SkyMoment> sampleList, boolean rising)
	{
		SkyMomentName name = rising ? SkyMomentName.RISE : SkyMomentName.SET;

		for (int index = 1; index < sampleList.size(); index++)
		{
			SkyMoment before = sampleList.get(index - 1);
			SkyMoment after = sampleList.get(index);
			double beforeElevation = before.getElevation();
			double afterElevation = after.getElevation();

			boolean crosses;
			if (rising)
			{
				crosses = beforeElevation < HORIZON_ELEVATION && afterElevation >= HORIZON_ELEVATION;
			}
			else
			{
				crosses = beforeElevation >= HORIZON_ELEVATION && afterElevation < HORIZON_ELEVATION;
			}

			if (crosses)
			{
				SkyMoment moment = refine(body, before, after, beforeElevation, afterElevation,
						HORIZON_ELEVATION, name);
				return moment;
			}
		}

		return null;
	}

	/**
	 * Where the body's bearing passes due south, which is the transit. A body that goes around
	 * the north instead never crosses it, and has none.
	 */
	private SkyMoment findTransit(HorizonsBody body, List<SkyMoment> sampleList)
	{
		for (int index = 1; index < sampleList.size(); index++)
		{
			SkyMoment before = sampleList.get(index - 1);
			SkyMoment after = sampleList.get(index);
			double beforeBearing = before.getBearing();
			double afterBearing = after.getBearing();

			boolean crosses = beforeBearing < TRANSIT_BEARING && afterBearing >= TRANSIT_BEARING;
			if (crosses)
			{
				SkyMoment moment = refine(body, before, after, beforeBearing, afterBearing,
						TRANSIT_BEARING, SkyMomentName.TRANSIT);
				return moment;
			}
		}

		return null;
	}

	/**
	 * The moment between two samples where the value they bracket reaches the target, taken as
	 * a straight line between them and then worked out properly at that time.
	 */
	private SkyMoment refine(HorizonsBody body, SkyMoment before, SkyMoment after, double beforeValue,
			double afterValue, double targetValue, SkyMomentName name)
	{
		ZonedDateTime beforeTime = before.getTime();
		double span = afterValue - beforeValue;

		if (0.0 == span)
		{
			SkyMoment moment = makeMoment(body, beforeTime, name);
			return moment;
		}

		double fraction = (targetValue - beforeValue) / span;
		Duration step = Duration.between(beforeTime, after.getTime());
		long nanoCount = (long) (fraction * step.toNanos());
		ZonedDateTime time = beforeTime.plusNanos(nanoCount);

		SkyMoment moment = makeMoment(body, time, name);
		return moment;
	}

	/**
	 * Where the body stands for this observer at one time: which way to turn, and how far up.
	 */
	private SkyMoment makeMoment(HorizonsBody body, ZonedDateTime time, SkyMomentName name)
	{
		Instant momentInstant = time.toInstant();
		HorizonsPosition position = getEphemeris().getPosition(body, momentInstant);

		double siderealTime = getLocalSiderealTime(momentInstant);
		double hourAngle = normalise(siderealTime - position.getRightAscension());
		double hourAngleRadians = Math.toRadians(hourAngle);
		double declinationRadians = Math.toRadians(position.getDeclination());
		double latitudeRadians = Math.toRadians(getLatitude());

		double north = Math.sin(declinationRadians) * Math.cos(latitudeRadians)
				- Math.cos(declinationRadians) * Math.cos(hourAngleRadians) * Math.sin(latitudeRadians);
		double east = -Math.cos(declinationRadians) * Math.sin(hourAngleRadians);
		double up = Math.sin(declinationRadians) * Math.sin(latitudeRadians)
				+ Math.cos(declinationRadians) * Math.cos(hourAngleRadians) * Math.cos(latitudeRadians);

		double bearingDegrees = Math.toDegrees(Math.atan2(east, north));
		double bearing = normalise(bearingDegrees);
		double elevation = Math.toDegrees(Math.asin(clamp(up)));

		SkyMoment moment = new SkyMoment(time, bearing, elevation, name);
		return moment;
	}

	/**
	 * How much of the body's disc the observer sees lit, from the angle the Sun and the Earth
	 * stand apart at the body.
	 */
	private double getLitFraction(HorizonsBody body)
	{
		HorizonsPosition bodyPosition = getEphemeris().getPosition(body, getInstant());

		double[] bodyVector = toEquatorialVector(bodyPosition);
		double[] sunVector = toEquatorialVector(getSunPosition());
		double[] bodyToEarth = subtract(new double[] { 0.0, 0.0, 0.0 }, bodyVector);
		double[] bodyToSun = subtract(sunVector, bodyVector);

		double earthLength = length(bodyToEarth);
		double sunLength = length(bodyToSun);
		if (0.0 == earthLength || 0.0 == sunLength)
		{
			return FULLY_LIT;
		}

		double dotProduct = dot(bodyToEarth, bodyToSun);
		double cosine = clamp(dotProduct / (earthLength * sunLength));
		double litFraction = (1.0 + cosine) / 2.0;
		return litFraction;
	}

	/**
	 * Sidereal time at the observer's longitude, in degrees: the right ascension that stands
	 * due south there at that moment.
	 */
	private double getLocalSiderealTime(Instant instant)
	{
		double julianDate = getJulianDate(instant);
		double dayCount = julianDate - JULIAN_AT_J2000;
		double greenwichDegrees = SIDEREAL_AT_J2000 + SIDEREAL_PER_DAY * dayCount;
		double siderealTime = normalise(greenwichDegrees + getLongitude());
		return siderealTime;
	}

	/**
	 * The Julian Date at a moment, which carries the time of day as its fraction.
	 */
	private double getJulianDate(Instant instant)
	{
		double milliCount = instant.toEpochMilli();
		double dayCount = milliCount / MILLIS_PER_DAY;
		double julianDate = dayCount + JULIAN_AT_EPOCH;
		return julianDate;
	}

	/**
	 * A position as x, y and z from the Earth's centre, against the celestial equator.
	 */
	private double[] toEquatorialVector(HorizonsPosition position)
	{
		double rightAscension = Math.toRadians(position.getRightAscension());
		double declination = Math.toRadians(position.getDeclination());
		double range = position.getRange();

		double x = range * Math.cos(declination) * Math.cos(rightAscension);
		double y = range * Math.cos(declination) * Math.sin(rightAscension);
		double z = range * Math.sin(declination);

		double[] vector = { x, y, z };
		return vector;
	}

	/**
	 * The same vector turned onto the ecliptic, which is the plane the chart draws.
	 */
	private double[] toEcliptic(double[] vector)
	{
		double obliquity = Math.toRadians(OBLIQUITY);
		double x = vector[0];
		double y = vector[1] * Math.cos(obliquity) + vector[2] * Math.sin(obliquity);
		double z = -vector[1] * Math.sin(obliquity) + vector[2] * Math.cos(obliquity);

		double[] eclipticVector = { x, y, z };
		return eclipticVector;
	}

	private double[] subtract(double[] first, double[] second)
	{
		double[] difference = { first[0] - second[0], first[1] - second[1], first[2] - second[2] };
		return difference;
	}

	private double dot(double[] first, double[] second)
	{
		double dotProduct = first[0] * second[0] + first[1] * second[1] + first[2] * second[2];
		return dotProduct;
	}

	private double length(double[] vector)
	{
		double dotProduct = dot(vector, vector);
		double length = Math.sqrt(dotProduct);
		return length;
	}

	/**
	 * An angle brought back into 0 to 360.
	 */
	private double normalise(double degrees)
	{
		double remainder = degrees % DEGREES_AROUND;
		if (remainder < 0.0)
		{
			remainder = remainder + DEGREES_AROUND;
		}
		return remainder;
	}

	/**
	 * A cosine or a sine held inside -1 to 1, where rounding can put it just outside.
	 */
	private double clamp(double value)
	{
		double clamped = Math.max(-1.0, Math.min(1.0, value));
		return clamped;
	}
}
