package com.lc.offgrid.pingapp;

import java.util.ArrayList;
import java.util.List;

/**
 * One stretch on one network: the rows logged without the wifi name changing and without the
 * monitor stopping long enough to leave a hole. Statistics are reported per stay because that
 * is the unit the question is asked in — how was the network where I was, while I was there.
 */
public class PingStay
{
	private final String			wifiName;
	private final List<PingLogRow>	rowList		= new ArrayList<>();

	public PingStay(String wifiName)
	{
		this.wifiName = wifiName;
	}

	public String getWifiName()
	{
		return wifiName;
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
