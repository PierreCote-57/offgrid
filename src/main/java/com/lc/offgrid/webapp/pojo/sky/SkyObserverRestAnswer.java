package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;
import com.lc.offgrid.common.misc.sky.SkyBodyDay;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * What the sky does for one observer on one day: when each body rises, transits and sets, and
 * what it looks like while it is up. It opens with the place it was read as — a parameter that
 * does not parse falls back, so the answer states the values it actually used.
 */
public class SkyObserverRestAnswer extends SkyRestAnswer
{
	private double		latitude;
	private double		longitude;
	private Map<HorizonsBody, SkyBodyDay>	skyBodyDayMap;

	public SkyObserverRestAnswer(ZonedDateTime dateTime, double latitude, double longitude)
	{
		super(dateTime);
		this.latitude = latitude;
		this.longitude = longitude;
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
