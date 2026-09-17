package com.lc.offgrid.common.misc.astronomy.planet;

import com.lc.offgrid.common.misc.astronomy.constellation.Constellation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * One line of a JPL Horizons OBSERVER ephemeris, in the comma-separated form the API answers
 * when CSV_FORMAT is YES and ANG_FORMAT is DEG.
 *
 * The API and its parameters:
 * https://ssd-api.jpl.nasa.gov/doc/horizons.html
 *
 * The Horizons system the API fronts:
 * https://ssd.jpl.nasa.gov/horizons/
 *
 * The columns, in the order they arrive, are the ones QUANTITIES 1, 9, 20, 23, 24 and 29 ask
 * for. Their meanings are stated at the foot of every answer, and in the manual:
 * https://ssd.jpl.nasa.gov/horizons/manual.html
 *
 * The call that fetched a file, with the body's COMMAND and the year's dates:
 *
 * https://ssd.jpl.nasa.gov/api/horizons.api
 *     ?format=text
 *     &amp;COMMAND='499'
 *     &amp;OBJ_DATA='NO'
 *     &amp;MAKE_EPHEM='YES'
 *     &amp;EPHEM_TYPE='OBSERVER'
 *     &amp;CENTER='500@399'
 *     &amp;START_TIME='2026-09-01'
 *     &amp;STOP_TIME='2026-12-31'
 *     &amp;STEP_SIZE='1 d'
 *     &amp;QUANTITIES='1,9,20,23,24,29'
 *     &amp;ANG_FORMAT='DEG'
 *     &amp;CSV_FORMAT='YES'
 *
 * CENTER 500@399 is the geocentre, so the answers are Earth-centred and serve any observer.
 * The answer wraps the rows in $$SOE and $$EOE, above and below the header line; the file on
 * disk keeps the header and the rows and drops the rest.
 *
 * A magnitude is printed as n.a. outside the phase angles the model covers, so the two
 * brightness fields are Double and answer null there. Everything else is always printed.
 *
 * The constellation column is read as a Constellation, and a code that is not one of the 88
 * answers null rather than failing the row: the rest of the line is still good.
 */
public class HorizonsRow
{
	/** The header the API writes above the rows, naming the columns this class reads. */
	public static final String HEADER_PREFIX = " Date__(UT)__HR:MN";

	/** What Horizons prints where a quantity is outside the validity of its model. */
	private static final String NOT_AVAILABLE = "n.a.";

	/** The date column, e.g. 2026-Sep-01 00:00. Horizons writes the month in English. */
	private static final String DATE_PATTERN = "yyyy-MMM-dd HH:mm";

	private final Date		date;
	private final String	solarPresence;
	private final String	lunarPresence;
	private final double	rightAscension;
	private final double	declination;
	private final Double	apparentMagnitude;
	private final Double	surfaceBrightness;
	private final double	range;
	private final double	rangeRate;
	private final double	solarElongation;
	private final String	elongationFlag;
	private final double	phaseAngle;
	private final Constellation	constellation;

	/**
	 * Reads one row. The line is the whole comma-separated record between $$SOE and $$EOE.
	 */
	public HorizonsRow(String line) throws ParseException
	{
		String[] fieldList = line.split(",", -1);

		this.date = parseDate(fieldList[0]);
		this.solarPresence = fieldList[1].trim();
		this.lunarPresence = fieldList[2].trim();
		this.rightAscension = parseNumber(fieldList[3]);
		this.declination = parseNumber(fieldList[4]);
		this.apparentMagnitude = parseOptionalNumber(fieldList[5]);
		this.surfaceBrightness = parseOptionalNumber(fieldList[6]);
		this.range = parseNumber(fieldList[7]);
		this.rangeRate = parseNumber(fieldList[8]);
		this.solarElongation = parseNumber(fieldList[9]);
		this.elongationFlag = fieldList[10].trim();
		this.phaseAngle = parseNumber(fieldList[11]);
		this.constellation = parseConstellation(fieldList[12]);
	}

	/**
	 * The moment the row describes, in UT.
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * Whether the Sun stands between the observer and the target: a blank, or one of Horizons'
	 * codes for a transit or an eclipse.
	 */
	public String getSolarPresence()
	{
		return solarPresence;
	}

	/**
	 * The same for the Moon, or for whichever body was named as the interferer.
	 */
	public String getLunarPresence()
	{
		return lunarPresence;
	}

	/**
	 * Astrometric right ascension in degrees, ICRF.
	 */
	public double getRightAscension()
	{
		return rightAscension;
	}

	/**
	 * Astrometric declination in degrees, ICRF.
	 */
	public double getDeclination()
	{
		return declination;
	}

	/**
	 * Apparent visual magnitude, or null where the model does not cover the phase angle.
	 */
	public Double getApparentMagnitude()
	{
		return apparentMagnitude;
	}

	/**
	 * Average magnitude of a square arcsecond of the lit disc, or null where there is none.
	 */
	public Double getSurfaceBrightness()
	{
		return surfaceBrightness;
	}

	/**
	 * Distance from the observer, in astronomical units. It is what gives the Moon its
	 * parallax.
	 */
	public double getRange()
	{
		return range;
	}

	/**
	 * How fast the range changes, in kilometres a second. Positive is receding.
	 */
	public double getRangeRate()
	{
		return rangeRate;
	}

	/**
	 * The Sun-observer-target angle in degrees: how far from the Sun the target appears.
	 */
	public double getSolarElongation()
	{
		return solarElongation;
	}

	/**
	 * Which side of the Sun that elongation is on: T for the evening sky, L for the morning.
	 */
	public String getElongationFlag()
	{
		return elongationFlag;
	}

	/**
	 * The Sun-target-observer angle in degrees, which is what decides the lit fraction.
	 */
	public double getPhaseAngle()
	{
		return phaseAngle;
	}

	/**
	 * The constellation the target sits in, or null where the column held a code that is not
	 * one of the 88.
	 */
	public Constellation getConstellation()
	{
		return constellation;
	}

	private static Date parseDate(String field) throws ParseException
	{
		String text = field.trim();
		SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = format.parse(text);
		return date;
	}

	private static double parseNumber(String field)
	{
		String text = field.trim();
		double number = Double.parseDouble(text);
		return number;
	}

	private static Constellation parseConstellation(String field)
	{
		String text = field.trim();
		Constellation constellation;
		try
		{
			constellation = Constellation.valueOf(text);
		}
		catch (IllegalArgumentException exception)
		{
			constellation = null;
		}
		return constellation;
	}

	private static Double parseOptionalNumber(String field)
	{
		String text = field.trim();
		if (text.isEmpty() || NOT_AVAILABLE.equals(text))
		{
			return null;
		}
		Double number = Double.valueOf(text);
		return number;
	}
}
