package com.lc.offgrid.pojo.page;

import com.lc.offgrid.pojo.part.MaintenanceEntry;

import java.util.List;

/**
 * What a vehicle needs and what it has had. The noteMap every page carries is the plan —
 * what is scheduled, what is being watched. actualList is the record, one entry per visit,
 * written on the way home.
 */
public class MaintenancePage extends PageData
{
	private List<MaintenanceEntry>	actualList;

	public List<MaintenanceEntry> getActualList()
	{
		return actualList;
	}
}
