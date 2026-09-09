package com.lc.offgrid.common.misc.sky;

/**
 * The moments in a body's day that have a name of their own, each carrying the name as a
 * reader sees it. A moment that is only a time somebody asked about carries none of these.
 */
public enum SkyMomentName
{
	/** The body comes up over the horizon. */
	RISE("Rise"),

	/** The body is as high as it gets that day, which is where its bearing reads 180. */
	TRANSIT("Transit"),

	/** The body goes back down under the horizon. */
	SET("Set");

	private final String displayName;

	SkyMomentName(String displayName)
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
