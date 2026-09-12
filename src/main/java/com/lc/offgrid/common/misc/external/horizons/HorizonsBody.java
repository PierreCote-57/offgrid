package com.lc.offgrid.common.misc.external.horizons;

/**
 * The nine bodies the sky table describes, each carrying the identifier JPL Horizons knows it
 * by. That identifier is what COMMAND takes when an ephemeris is fetched:
 * https://ssd-api.jpl.nasa.gov/doc/horizons.html
 *
 * The name and the period are ours rather than Horizons': an ephemeris file states neither.
 *
 * Earth is not here. The ephemeris is geocentric, so Earth has none of its own.
 */
public enum HorizonsBody
{
	SUN("Sun", "10", null),
	MOON("Moon", "301", 27.32),
	MERCURY("Mercury", "199", 87.97),
	VENUS("Venus", "299", 224.70),
	MARS("Mars", "499", 686.98),
	JUPITER("Jupiter", "599", 4332.59),
	SATURN("Saturn", "699", 10759.22),
	URANUS("Uranus", "799", 30688.5),
	NEPTUNE("Neptune", "899", 60182.0);

	private final String	displayName;
	private final String	commandId;

	/** Once around, in days. The Moon's is around the Earth; the Sun has none and carries null. */
	private final Double	periodDay;

	HorizonsBody(String displayName, String commandId, Double periodDay)
	{
		this.displayName = displayName;
		this.commandId = commandId;
		this.periodDay = periodDay;
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

	/**
	 * What Horizons calls this body: the value of COMMAND on a query for it.
	 */
	public String getCommandId()
	{
		return commandId;
	}

	/**
	 * The stem of this body's file, which is its name in lower case.
	 */
	public String getFileStem()
	{
		String fileStem = getDisplayName().toLowerCase();
		return fileStem;
	}
}
