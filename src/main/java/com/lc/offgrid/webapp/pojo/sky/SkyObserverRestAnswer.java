package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;
import com.lc.offgrid.common.misc.sky.SkyBodyDay;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * What the sky does for one observer on one day: when each body rises, transits and sets, and
 * what it looks like while it is up. It opens with the moment and the place it was read as — a
 * parameter that does not parse falls back, so the answer states the values it actually used.
 *
 * The moment travels as the second and the zone it was read in, which the caller puts back
 * together. This is the only answer carrying a zone: the positions endpoint is never asked for
 * one.
 */
public class SkyObserverRestAnswer extends SkyRestAnswer
{
	private long		epochSecond;
	private ZoneId		timeZone;
	/** The moment as the caller's own zone reads it, so the answer can be read. Nothing computes from it. */
	private String		dateTimeText;
	private double		latitude;
	private double		longitude;
	private Map<HorizonsBody, SkyBodyDay>	skyBodyDayMap;

	public SkyObserverRestAnswer(ZonedDateTime dateTime, double latitude, double longitude)
	{
		this.epochSecond = dateTime.toEpochSecond();
		this.timeZone = dateTime.getZone();
		this.dateTimeText = dateTime.toString();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public long getEpochSecond()
	{
		return epochSecond;
	}
	public ZoneId getTimeZone()
	{
		return timeZone;
	}
	public String getDateTimeText()
	{
		return dateTimeText;
	}

	public double getLatitude()
	{
		return latitude;
	}
	public double getLongitude()
	{
		return longitude;
	}

	public void setSkyBodyDayMap(Map<HorizonsBody, SkyBodyDay> skyBodyDayMap)
	{
		this.skyBodyDayMap = skyBodyDayMap;
	}
	public Map<HorizonsBody, SkyBodyDay> getSkyBodyDayMap()
	{
		return skyBodyDayMap;
	}
}
