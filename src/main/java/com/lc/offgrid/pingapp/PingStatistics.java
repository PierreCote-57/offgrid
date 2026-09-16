package com.lc.offgrid.pingapp;

import com.lc.basics.container.AbstractContainer;
import com.lc.basics.tools.file.BasicFileReader;
import com.lc.basics.tools.time.WallClock;
import com.lc.basics.tools.units.TimeUnits;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the ping log back and reports one line per stay, oldest first so the newest stay is
 * the one still on screen when the printing stops. What ends a stay is the `PingGrouping` it
 * is handed, and that is the only difference between the reports.
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

	// One row, one line, one number per column so a column reads down the page. The end
	// carries no date: a stay that runs past midnight says so in its duration, and a fixed
	// width is what keeps the columns lined up.
	private static final String		FORMAT_LINE			=
			"%-20s %-19s %-8s %-14s %8s %8s %8s%n";

	private static void reportHeader()
	{
		System.out.println();
		System.out.printf(FORMAT_LINE,
				"Network", "Start", "End", "Duration", "Answered", "Pings", "Success");
	}

	private static void reportStay(PingStay stay)
	{
		int			countRow		= stay.getRowList().size();
		int			countReplied	= stay.getCountReplied();
		double		percentReplied	= 100.0 * countReplied / countRow;
		String		textStart		= formatDateTime(stay.getMsStart());
		String		textEnd			= formatTimeOfDay(stay.getMsEnd());
		String		textDuration	= TimeUnits.MS.format(stay.getMsEnd() - stay.getMsStart());
		String		textAnswered	= String.format("%,d", countReplied);
		String		textRow			= String.format("%,d", countRow);
		String		textPercent		= String.format("%.1f%%", percentReplied);

		System.out.printf(FORMAT_LINE, stay.getWifiName(),
				textStart, textEnd, textDuration, textAnswered, textRow, textPercent);
	}

	public void report(PingGrouping pingGrouping)
	{
		List<PingStay>		stayList		= readStayList(pingGrouping);
		if (stayList.isEmpty())
		{
			AbstractContainer.timeStamp("Nothing logged yet in %s",
					getPingFileManager().getPingLogFilename());
		}
		else
		{
			reportHeader();
			for (PingStay stay : stayList)
			{
				reportStay(stay);
			}
		}
	}

	// The stays in the order they were logged, which is the order they are printed.
	private List<PingStay> readStayList(PingGrouping pingGrouping)
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
					if (null == stay || pingGrouping.isStayEnded(stay, row))
					{
						stay = new PingStay(row.getWifiName());
						stayList.add(stay);
					}
					stay.add(row);
				}
			}
		}
		return stayList;
	}

	private static String formatDateTime(long msTime)
	{
		String		text		= WallClock.formatTime(
				WallClock.FormatDate.INTL, WallClock.FormatTime.HMS, msTime);
		return text;
	}

	private static String formatTimeOfDay(long msTime)
	{
		String		text		= WallClock.formatTime(
				WallClock.FormatDate.None, WallClock.FormatTime.HMS, msTime);
		return text;
	}
}
