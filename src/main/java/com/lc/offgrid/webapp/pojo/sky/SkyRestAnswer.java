package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;
import com.lc.offgrid.webapp.spring.tools.RestBaseAnswer;

import java.util.EnumMap;
import java.util.Map;

/**
 * What both sky endpoints answer: what every body is called. The timing comes from
 * RestBaseAnswer, and the moment belongs to the answer that was asked for a zone.
 *
 * The body map is on both answers rather than one, so a caller drawing from either has
 * everything that answer's block needs and waits on no other call.
 */
public class SkyRestAnswer extends RestBaseAnswer
{
	private Map<HorizonsBody, SkyBodyInfo>	bodyMap;

	public SkyRestAnswer()
	{
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

}
