package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;
import com.lc.offgrid.webapp.spring.tools.RestBaseAnswer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * What both sky endpoints answer: the moment they were asked for, and what every body is
 * called. The moment travels as the second and the zone it was read in, which the caller puts
 * back together. The timing comes from RestBaseAnswer.
 *
 * The body map is on both answers rather than one, so a caller drawing from either has
 * everything that answer's block needs and waits on no other call.
 */
public class SkyRestAnswer extends RestBaseAnswer
{
	private long		epochSecond;
	private ZoneId		timeZone;
	/** The moment as the caller's own zone reads it, so the answer can be read. Nothing computes from it. */
	private String		dateTimeText;
	private Map<HorizonsBody, SkyBodyInfo>	bodyMap;

	public SkyRestAnswer(ZonedDateTime dateTime)
	{
		this.epochSecond = dateTime.toEpochSecond();
		this.timeZone = dateTime.getZone();
		this.dateTimeText = dateTime.toString();
		this.bodyMap = makeBodyMap();
	}

	/**
	 * What every body is called, how long it takes to go around and what it goes around, keyed
	 * the way the other maps are keyed. Nothing in it depends on the request, so it is built
	 * here rather than handed in.
	 */
	private Map<HorizonsBody, SkyBodyInfo> makeBodyMap()
	{
		Map<HorizonsBody, SkyBodyInfo> infoMap = new EnumMap<>(HorizonsBody.class);

		for (HorizonsBody body : HorizonsBody.values())
		{
			SkyBodyInfo bodyInfo = new SkyBodyInfo(body.getDisplayName(), body.getPeriodDay(),
					body.getParent());
			infoMap.put(body, bodyInfo);
		}

		return infoMap;
	}

	public Map<HorizonsBody, SkyBodyInfo> getBodyMap()
	{
		return bodyMap;
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
}
