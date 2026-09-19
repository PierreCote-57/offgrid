package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;

/**
 * A body with no period — the Sun — carries a null, and so does one with nothing to go around.
 */
public class SkyBodyInfo
{
	private final String		name;
	private final Double		periodDay;
	private final HorizonsBody	parent;

	public SkyBodyInfo(String name, Double periodDay, HorizonsBody parent)
	{
		this.name = name;
		this.periodDay = periodDay;
		this.parent = parent;
	}

	public String getName()
	{
		return name;
	}

	public Double getPeriodDay()
	{
		return periodDay;
	}

	public HorizonsBody getParent()
	{
		return parent;
	}
}
