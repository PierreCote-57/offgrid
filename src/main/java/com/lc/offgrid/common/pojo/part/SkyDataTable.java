package com.lc.offgrid.common.pojo.part;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Everything the table draws and nothing else: where the observer stands and the clock they
 * read.
 */
public class SkyDataTable extends SkyData
{
	private double			latitude;
	private double			longitude;
	private ZoneId			timeZone;

	public double getLatitude()
	{
		return latitude;
	}
	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}
	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public ZoneId getTimeZone()
	{
		return timeZone;
	}
	public void setTimeZone(ZoneId timeZone)
	{
		this.timeZone = timeZone;
	}

	/**
	 * The date, pinned to the observer's zone. It is what carries the offset: an offset only
	 * exists for a moment, since a zone changes its own twice a year.
	 */
	public ZonedDateTime getZonedDate()
	{
		ZonedDateTime zonedDate = getDate().atStartOfDay(getTimeZone());
		return zonedDate;
	}
}
