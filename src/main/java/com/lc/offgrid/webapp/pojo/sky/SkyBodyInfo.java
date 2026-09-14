package com.lc.offgrid.webapp.pojo.sky;

import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;

/**
 * What the site states about one body, whatever the date and wherever the observer stands:
 * the name a reader knows it by, how long it takes to go once around, and what it goes around.
 *
 * It rides on the sky answer so the browser names a row and states its period without a list
 * of its own. A body with no period — the Sun — carries a null, and so does one with nothing
 * to go around. The parent travels as the key the answer's maps are keyed by, so the chart
 * looks up what it is drawn around.
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
