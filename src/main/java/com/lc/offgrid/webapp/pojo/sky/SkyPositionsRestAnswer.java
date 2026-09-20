package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;

import java.util.Map;

/**
 * Each body's angle at a moment, as seen from above: a plan view, not one observer's sky.
 */
public class SkyPositionsRestAnswer extends SkyRestAnswer
{
	private Map<HorizonsBody, Double>	angleDegMap;

	public void setAngleDegMap(Map<HorizonsBody, Double> angleDegMap)
	{
		this.angleDegMap = angleDegMap;
	}
	public Map<HorizonsBody, Double> getAngleDegMap()
	{
		return angleDegMap;
	}
}
