package com.lc.offgrid.common.pojo.page;

import com.lc.offgrid.common.pojo.part.MaintenanceEntry;

import java.util.List;

/**
 * What a vehicle needs and what it has had. The noteMap every page carries is the plan —
 * what is scheduled, what is being watched. actualList is the record, one entry per visit,
 * written on the way home.
 */
public class MaintenancePage extends PageData
{
	private List<MaintenanceEntry>	actualList;

	public MaintenancePage(List<MaintenanceEntry> actualList)
	{
		this.actualList = actualList;
	}

	public List<MaintenanceEntry> getActualList()
	{
		return actualList;
	}

	public MaintenanceEntry getLastMaintenance()
	{
		List<MaintenanceEntry> list = getActualList();
		if (null != list)
		{
			for (int i = list.size() - 1; i >= 0; i--)
			{
				MaintenanceEntry entry = list.get(i);
				if (null != entry.getNextDue()
						&& (null != entry.getNextDue().getNextDueDate()
						|| null != entry.getNextDue().getNextDueKm()))
				{
					return entry;
				}
			}
		}
		return null;
	}

	public MaintenanceEntry.NextDue getNextMaintenance()
	{
		MaintenanceEntry entry = getLastMaintenance();
		return null == entry ? null : entry.getNextDue();
	}
}
