package com.lc.offgrid.common.misc.external.horizons;

/**
 * The nine bodies the sky table describes, each carrying the identifier JPL Horizons knows it
 * by. That identifier is what COMMAND takes when an ephemeris is fetched:
 * https://ssd-api.jpl.nasa.gov/doc/horizons.html
 *
 * Earth is not here. The ephemeris is geocentric, so Earth has none of its own.
 */
public enum HorizonsBody
{
	SUN("Sun", "10"),
	MOON("Moon", "301"),
	MERCURY("Mercury", "199"),
	VENUS("Venus", "299"),
	MARS("Mars", "499"),
	JUPITER("Jupiter", "599"),
	SATURN("Saturn", "699"),
	URANUS("Uranus", "799"),
	NEPTUNE("Neptune", "899");

	private final String	displayName;
	private final String	commandId;

	HorizonsBody(String displayName, String commandId)
	{
		this.displayName = displayName;
		this.commandId = commandId;
	}

	/**
	 * The body's name as it is written for a reader.
	 */
	public String getDisplayName()
	{
		return displayName;
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
