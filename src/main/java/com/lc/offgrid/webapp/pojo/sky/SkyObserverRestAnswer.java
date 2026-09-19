package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;
import com.lc.offgrid.common.misc.sky.SkyBodyDay;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

public class SkyObserverRestAnswer extends SkyRestAnswer
{
	private long		epochSecond;
	private ZoneId		timeZone;
	/** There to be read: nothing computes from it. */
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
