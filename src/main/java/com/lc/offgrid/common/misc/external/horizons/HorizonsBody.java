package com.lc.offgrid.common.misc.external.horizons;

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
 */
public enum HorizonsBody
{
	SUN("Sun", "10", null, null),
	EARTH("Earth", null, 365.26, SUN),
	MOON("", "301", 27.32, EARTH),
	MERCURY("Mercury", "199", 87.97, SUN),
	VENUS("Venus", "299", 224.70, SUN),
	MARS("Mars", "499", 686.98, SUN),
	JUPITER("Jupiter", "599", 4332.59, SUN),
	SATURN("Saturn", "699", 10759.22, SUN),
	URANUS("Uranus", "799", 30688.5, SUN),
	NEPTUNE("Neptune", "899", 60182.0, SUN);

	private final String	displayName;
	private final String	commandId;

	/** Once around, in days. The Moon's is around the Earth; the Sun has none and carries null. */
	private final Double	periodDay;

	/** What this body goes around, and what its orbit is drawn centred on. The Sun carries null. */
	private final HorizonsBody	parent;

	HorizonsBody(String displayName, String commandId, Double periodDay, HorizonsBody parent)
	{
		this.displayName = displayName;
		this.commandId = commandId;
		this.periodDay = periodDay;
		this.parent = parent;
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

	/**
	 * The stem of this body's file, which is its name in lower case.
	 */
	public String getFileStem()
	{
		String fileStem = name().toLowerCase();
		return fileStem;
	}
}
