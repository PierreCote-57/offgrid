package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;
import com.lc.offgrid.webapp.spring.tools.RestBaseAnswer;

import java.util.EnumMap;
import java.util.Map;

public class SkyRestAnswer extends RestBaseAnswer
{
	private Map<HorizonsBody, SkyBodyInfo>	bodyMap;

	public SkyRestAnswer()
	{
		this.bodyMap = makeBodyMap();
	}

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
