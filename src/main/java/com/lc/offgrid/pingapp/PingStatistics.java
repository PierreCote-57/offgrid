package com.lc.offgrid.pingapp;

import com.lc.basics.container.AbstractContainer;
import com.lc.basics.tools.file.BasicFileReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the ping log back and reports one line per stay, oldest first so the newest stay is
 * the one still on screen when the printing stops. The `PingReport` it is handed owns both
 * halves of what varies: what ends a stay, and the columns it is printed in.
 */
@SuppressWarnings("PMD.SystemPrintln")		// This IS a command line application!
public class PingStatistics
{
	private final PingFileManager	pingFileManager;

	public PingStatistics(PingFileManager pingFileManager)
	{
		this.pingFileManager = pingFileManager;
	}

	public PingFileManager getPingFileManager()
	{
		return pingFileManager;
	}

	public void report(PingReport pingReport)
	{
		List<PingStay>		stayList		= readStayList(pingReport);
		if (stayList.isEmpty())
		{
			AbstractContainer.timeStamp("Nothing logged yet in %s",
					getPingFileManager().getPingLogFilename());
		}
		else
		{
			System.out.println();
			pingReport.reportHeader();
			for (PingStay stay : stayList)
			{
				pingReport.reportStay(stay);
			}
		}
	}

	// The stays in the order they were logged, which is the order they are printed.
	private List<PingStay> readStayList(PingReport pingReport)
	{
		String				filename		= getPingFileManager().getPingLogFilename();
		File				file			= new File(filename);
		List<PingStay>		stayList		= new ArrayList<>();

		if (file.exists())
		{
			String			text			= BasicFileReader.readTextFile(filename);
			String[]		lineList		= text.split("\n");
			PingStay		stay			= null;

			for (String line : lineList)
			{
				PingLogRow	row			= PingLogRow.parse(line);
				if (null != row)
				{
					if (null == stay || pingReport.isStayEnded(stay, row))
					{
						stay = new PingStay();
						stayList.add(stay);
					}
					stay.add(row);
				}
			}
		}
		return stayList;
	}
}
