package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;

import java.util.Map;

/**
 * Where the bodies stand at a moment, seen from above. Nothing in it depends on where the
 * caller is, which is why it is its own answer: the request goes out as the page loads, rather
 * than after the browser has said where the visitor stands.
 */
public class SkyPositionsRestAnswer extends SkyRestAnswer
{
	private Map<HorizonsBody, Double>	sunAngleMap;

	public void setSunAngleMap(Map<HorizonsBody, Double> sunAngleMap)
	{
		this.sunAngleMap = sunAngleMap;
	}
	public Map<HorizonsBody, Double> getSunAngleMap()
	{
		return sunAngleMap;
	}
}
