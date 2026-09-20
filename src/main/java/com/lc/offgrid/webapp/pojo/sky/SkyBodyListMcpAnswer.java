package com.lc.offgrid.webapp.pojo.sky;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Every body the ephemeris covers, for one observer on one day. The Earth is not in it: the
 * ephemeris is geocentric, so there is no file to read for it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkyBodyListMcpAnswer extends SkyMcpAnswer
{
	/** The time of day asked about, written HH:mm, and left out when the caller named none. */
	@JsonProperty(required = false)
	private final String	timeText;

	private final List<SkyBodyMcpAnswer>	bodyList;

	public SkyBodyListMcpAnswer(LocalDate localDate, LocalTime localTime, ZoneId timeZone,
			double latitude, double longitude, List<SkyBodyMcpAnswer> bodyList)
	{
		super(localDate, timeZone, latitude, longitude);
		this.timeText = null == localTime ? null : localTime.toString();
		this.bodyList = bodyList;
	}

	public String getTimeText()
	{
		return timeText;
	}

	public List<SkyBodyMcpAnswer> getBodyList()
	{
		return bodyList;
	}
}
