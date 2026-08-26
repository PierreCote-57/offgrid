package com.lc.offgrid.pojo.part;

/**
 * One visit to a shop: what was done, where, what it cost, and what it sets up next. A field
 * with nothing behind it stays null and its cell is left empty.
 */
public class MaintenanceEntry
{
	private String		date;
	private Integer		odometerKm;
	private String		shopName;
	private String		shopUrl;
	private String		workName;
	private String		workUrl;
	private Double		costCad;
	private Integer		nextDueKm;
	private String		nextDueDate;

	public String getDate()
	{
		return date;
	}

	public Integer getOdometerKm()
	{
		return odometerKm;
	}

	public String getShopName()
	{
		return shopName;
	}

	public String getShopUrl()
	{
		return shopUrl;
	}

	public String getWorkName()
	{
		return workName;
	}

	public String getWorkUrl()
	{
		return workUrl;
	}

	public Double getCostCad()
	{
		return costCad;
	}

	public Integer getNextDueKm()
	{
		return nextDueKm;
	}

	public String getNextDueDate()
	{
		return nextDueDate;
	}
}
