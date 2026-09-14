package com.lc.offgrid.common.misc.astronomy.planet;

import java.time.LocalDate;

/**
 * The bodies the sky describes, each carrying the identifier JPL Horizons knows it by. That
 * identifier is what COMMAND takes when an ephemeris is fetched:
 * <a href="https://ssd-api.jpl.nasa.gov/doc/horizons.html">...</a>
 * The name and the period are ours rather than Horizons': an ephemeris file states neither.
 * Earth carries no identifier: the ephemeris is geocentric, so there is no file to fetch for
 * it. It is here because the chart draws it, and hasEphemeris() is what keeps a body like that
 * out of everything that is read from a file.
 * Each body states what it goes around, which is why they are declared in this order: a
 * constant can only be named by one declared after it, so the Sun comes before the Earth and
 * the Earth before its Moon.
 * Each body also states the dates Horizons has an ephemeris for it. Horizons names a bound by
 * refusing a request beyond it, and the bound it names carries a time of day; the dates here
 * are the whole days inside it, so every one of them has a row at 00:00 UT. They were read on
 * 2026-09-13 and are the narrowest thing about a body: Neptune's 1800 to 2199 is what can be
 * fetched for all nine at once.
 */
public enum HorizonsBody
{
	SUN("Sun", "10", null, null, LocalDate.of(-9998, 3, 16), LocalDate.of(9999, 12, 30)),
	EARTH("Earth", null, 365.26, SUN, null, null),
	MOON("Moon", "301", 27.32, EARTH, LocalDate.of(-9998, 3, 16), LocalDate.of(9999, 12, 30)),
	MERCURY("Mercury", "199", 87.97, SUN, LocalDate.of(-9998, 3, 16), LocalDate.of(9999, 12, 30)),
	VENUS("Venus", "299", 224.70, SUN, LocalDate.of(-9998, 3, 16), LocalDate.of(9999, 12, 30)),
	MARS("Mars", "499", 686.98, SUN, LocalDate.of(1600, 1, 2), LocalDate.of(2599, 12, 31)),
	JUPITER("Jupiter", "599", 4332.59, SUN, LocalDate.of(1600, 1, 11), LocalDate.of(2200, 1, 8)),
	SATURN("Saturn", "699", 10759.22, SUN, LocalDate.of(1749, 12, 31), LocalDate.of(2250, 1, 4)),
	URANUS("Uranus", "799", 30688.5, SUN, LocalDate.of(1600, 1, 5), LocalDate.of(2399, 12, 15)),
	NEPTUNE("Neptune", "899", 60182.0, SUN, LocalDate.of(1800, 1, 2), LocalDate.of(2199, 12, 29));

	private final String	displayName;
	private final String	commandId;

	/** Once around, in days. The Moon's is around the Earth; the Sun has none and carries null. */
	private final Double	periodDay;

	/** What this body goes around, and what its orbit is drawn centred on. The Sun carries null. */
	private final HorizonsBody	parent;

	/** The first day Horizons has this body at. A body with no ephemeris carries null. */
	private final LocalDate		fromDate;

	/** The last day Horizons has this body at. A body with no ephemeris carries null. */
	private final LocalDate		toDate;

	HorizonsBody(String displayName, String commandId, Double periodDay, HorizonsBody parent,
			LocalDate fromDate, LocalDate toDate)
	{
		this.displayName = displayName;
		this.commandId = commandId;
		this.periodDay = periodDay;
		this.parent = parent;
		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	/**
	 * The body's name as it is written for a reader.
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	public Double getPeriodDay()
	{
		return periodDay;
	}

	public HorizonsBody getParent()
	{
		return parent;
	}

	/**
	 * What Horizons calls this body: the value of COMMAND on a query for it. A body Horizons
	 * has no ephemeris for carries null, and hasEphemeris() is how that is asked.
	 */
	public String getCommandId()
	{
		return commandId;
	}

	/**
	 * Whether there is an ephemeris to read for this body, which is whether there is a COMMAND
	 * to ask Horizons with. A body without one is placed from what the others state.
	 */
	public boolean hasEphemeris()
	{
		boolean hasEphemeris = null != getCommandId();
		return hasEphemeris;
	}

	public LocalDate getFromDate()
	{
		return fromDate;
	}

	public LocalDate getToDate()
	{
		return toDate;
	}

	/**
	 * Whether Horizons has this body on the given day, which is whether an ephemeris can be
	 * fetched for it. A body without an ephemeris is on no day at all.
	 */
	public boolean hasDate(LocalDate date)
	{
		boolean hasDate = hasEphemeris()
				&& !date.isBefore(getFromDate())
				&& !date.isAfter(getToDate());
		return hasDate;
	}

	/**
	 * The stem of this body's file, which is its name in lower case.
	 */
	public String getFileStem()
	{
		String fileStem = name().toLowerCase();
		return fileStem;
	}
}
