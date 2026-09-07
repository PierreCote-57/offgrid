package com.lc.offgrid.common.pojo.part;

import java.time.LocalDate;
import java.util.List;

/**
 * What the chart and the table have in common: the date they are both about, and the bodies
 * they both describe. The chart is the sky on that date and the table is the times on it.
 */
public class SkyData
{
	private LocalDate		date;
	private List<SkyBody>	bodyList;

	public LocalDate getDate()
	{
		return date;
	}
	public void setDate(LocalDate date)
	{
		this.date = date;
	}

	public List<SkyBody> getBodyList()
	{
		return bodyList;
	}
	public void setBodyList(List<SkyBody> bodyList)
	{
		this.bodyList = bodyList;
	}
}
