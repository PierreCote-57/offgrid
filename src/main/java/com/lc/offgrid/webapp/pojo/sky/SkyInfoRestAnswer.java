package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.external.horizons.HorizonsBody;
import com.lc.offgrid.common.misc.sky.SkyBodyDay;
import com.lc.offgrid.webapp.spring.tools.RestBaseAnswer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * What the sky endpoint answers. It opens with the observer it was read as: a parameter
 * that does not parse falls back, so the answer states the values it actually used. The
 * moment travels as the second and the zone it was read in, which the caller puts back
 * together. The timing comes from RestBaseAnswer.
 */
public class SkyInfoRestAnswer extends RestBaseAnswer
{
	private long		epochSecond;
	private ZoneId		timeZone;
	/** The moment as the caller's own zone reads it, so the answer can be read. Nothing computes from it. */
	private String		dateTimeText;
	private double		latitude;
	private double		longitude;
	private Map<HorizonsBody, Double>		sunAngleMap;
	private Map<HorizonsBody, SkyBodyDay>	skyBodyDayMap;

	public SkyInfoRestAnswer(ZonedDateTime dateTime, double latitude, double longitude)
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

	public void setSunAngleMap(Map<HorizonsBody, Double> sunAngleMap)
	{
		this.sunAngleMap = sunAngleMap;
	}
	public Map<HorizonsBody, Double> getSunAngleMap()
	{
		return sunAngleMap;
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
