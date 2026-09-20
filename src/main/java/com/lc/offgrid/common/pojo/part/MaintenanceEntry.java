package com.lc.offgrid.common.pojo.part;

import java.time.LocalDate;

/**
 * One visit to a shop: what was done, where, what it cost, and what it sets up next. A field
 * with nothing behind it stays null and its cell is left empty.
 */
public class MaintenanceEntry
{
	private LocalDate date;
	private Integer odometerKm;
	private String shopName;
	private String shopUrl;
	private String workName;
	private String workUrl;
	private Double costCad;
	private NextDue nextDue;

	public MaintenanceEntry()
	{

	}
	public MaintenanceEntry(NextDue nextDue)
	{
		this.nextDue = nextDue;
	}

	public LocalDate getDate()
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

	public NextDue getNextDue()
	{
		return nextDue;
	}

	public static class NextDue
	{
		private Integer nextDueKm;
		private LocalDate nextDueDate;

		public NextDue()
		{

		}
		public NextDue(Integer nextDueKm, LocalDate nextDueDate)
		{
			this.nextDueKm = nextDueKm;
			this.nextDueDate = nextDueDate;
		}

		public Integer getNextDueKm()
		{
			return nextDueKm;
		}

		public LocalDate getNextDueDate()
		{
			return nextDueDate;
		}

		@Override
		public String toString()
		{
			return String.format("%d or %s", getNextDueKm(), getNextDueDate());
		}
	}
}
