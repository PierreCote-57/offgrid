package com.lc.offgrid.webapp.pojo.sky;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * What both sky tools state back: the day and the place the answer was worked out for. Every
 * parameter falls back silently, so without the echo a caller cannot tell a fallback from the
 * value it sent.
 *
 * Times are minutes on the observer's clock rather than epoch seconds: nothing here is finer
 * than a minute, and the client reading it is a language model that shows the text as it is.
 */
public class SkyMcpAnswer
{
	/** The day asked about, written YYYY-MM-DD. */
	private final String	dateText;

	/** The zone every time in the answer is on, named once. */
	private final String	timeZoneName;

	private final double	latitude;
	private final double	longitude;

	public SkyMcpAnswer(LocalDate localDate, ZoneId timeZone, double latitude, double longitude)
	{
		this.dateText = localDate.toString();
		this.timeZoneName = timeZone.getId();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getDateText()
	{
		return dateText;
	}

	public String getTimeZoneName()
	{
		return timeZoneName;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}
}
