package com.lc.offgrid.common.misc.astronomy.planet;

/**
 * The moments of a body's day an observer is told about, each carrying the name as a reader
 * sees it. A moment taken while a crossing is looked for carries none of these.
 */
public enum HorizonsMomentName
{
	/** The body comes up over the horizon. */
	RISE("Rise"),

	/** The body is as high as it gets that day, which is where its bearing reads 180. */
	TRANSIT("Transit"),

	/** The body goes back down under the horizon. */
	SET("Set"),

	/** The moment the caller asked about, which is the one the observer chose rather than the body. */
	NOW("Now");

	private final String displayName;

	HorizonsMomentName(String displayName)
	{
		this.displayName = displayName;
	}

	/**
	 * The name as it is written for a reader.
	 */
	public String getDisplayName()
	{
		return displayName;
	}
}
