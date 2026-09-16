package com.lc.offgrid.pingapp;

import java.util.ArrayList;
import java.util.List;

/**
 * One run of log rows, held as the rows themselves so nothing here is a second copy of what
 * they already carry. What ends a run is the `PingReport` being printed, so a stay is one
 * network for Overview and one day for ByDay.
 */
public class PingStay
{
	private final List<PingLogRow>	rowList		= new ArrayList<>();

	// The network the stay opened on, which is the one Overview measures a row against.
	public String getWifiName()
	{
		String		wifiName	= getRowList().get(0).getWifiName();
		return wifiName;
	}

	/** Every network the stay touched, in the order first seen, each named once. */
	public List<String> getWifiNameList()
	{
		List<String>	wifiNameList	= new ArrayList<>();
		for (PingLogRow row : getRowList())
		{
			String		wifiName		= row.getWifiName();
			if (!wifiNameList.contains(wifiName))
			{
				wifiNameList.add(wifiName);
			}
		}
		return wifiNameList;
	}
	public List<PingLogRow> getRowList()
	{
		return rowList;
	}

	public void add(PingLogRow row)
	{
		getRowList().add(row);
	}

	public long getMsStart()
	{
		long		msStart		= getRowList().get(0).getMsTime();
		return msStart;
	}
	public long getMsEnd()
	{
		int			indexLast	= getRowList().size() - 1;
		long		msEnd		= getRowList().get(indexLast).getMsTime();
		return msEnd;
	}
	/** The fastest round trip the stay saw, or 0.0 when nothing in it answered. */
	public double getMsElapsedMin()
	{
		double		msElapsedMin	= 0.0;
		for (PingLogRow row : getRowList())
		{
			if (row.isReplied() && (0.0 == msElapsedMin || row.getMsElapsed() < msElapsedMin))
			{
				msElapsedMin = row.getMsElapsed();
			}
		}
		return msElapsedMin;
	}

	/** The slowest round trip the stay saw, or 0.0 when nothing in it answered. */
	public double getMsElapsedMax()
	{
		double		msElapsedMax	= 0.0;
		for (PingLogRow row : getRowList())
		{
			if (row.isReplied() && msElapsedMax < row.getMsElapsed())
			{
				msElapsedMax = row.getMsElapsed();
			}
		}
		return msElapsedMax;
	}

	public int getCountReplied()
	{
		int			countReplied	= 0;
		for (PingLogRow row : getRowList())
		{
			if (row.isReplied())
			{
				countReplied++;
			}
		}
		return countReplied;
	}
}
