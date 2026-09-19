package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;

import java.util.Map;

/**
 * Each body's angle at a moment, as seen from above: a plan view, not one observer's sky.
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
