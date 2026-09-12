package com.lc.offgrid.webapp.pojo.sky;

/**
 * What the site states about one body, whatever the date and wherever the observer stands:
 * the name a reader knows it by, and how long it takes to go once around.
 *
 * It rides on the sky answer so the browser names a row and states its period without a list
 * of its own. A body with no period — the Sun — carries a null.
 */
public class SkyBodyInfo
{
	private final String	name;
	private final Double	periodDay;

	public SkyBodyInfo(String name, Double periodDay)
	{
		this.name = name;
		this.periodDay = periodDay;
	}

	public String getName()
	{
		return name;
	}

	public Double getPeriodDay()
	{
		return periodDay;
	}
}
