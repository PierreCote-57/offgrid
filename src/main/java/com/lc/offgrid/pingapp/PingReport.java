package com.lc.offgrid.pingapp;

import com.lc.basics.tools.time.WallClock;
import com.lc.basics.tools.units.TimeUnits;

/**
 * One statistics report: what ends a stay, and the columns its stays are printed in. Those two
 * are the whole of what the reports do not share, so a third report is a third constant.
 * <p>
 * Answered, Pings, Success, Min and Max are the same in every report and carried by
 * FORMAT_TAIL; a constant supplies what comes before it, and what comes after.
 */
@SuppressWarnings("PMD.SystemPrintln")		// This IS a command line application!
public enum PingReport
{
	Overview("%-20s %-16s %-5s %-14s", "")
			{
				/** The wifi name and nothing else, so a network reached over three days is one row. */
				@Override
				public boolean isStayEnded(PingStay stay, PingLogRow row)
				{
					boolean		isEnded		= !stay.getWifiName().equals(row.getWifiName());
					return isEnded;
				}

				@Override
				public void reportHeader()
				{
					System.out.printf(getFormatLine(), "Network", "Start", "End", "Duration",
							"Answered", "Pings", "Success", "Min", "Max");
				}

				@Override
				public void reportStay(PingStay stay)
				{
					System.out.printf(getFormatLine(), stay.getWifiName(),
							formatDateTime(stay.getMsStart()), formatTimeOfDay(stay.getMsEnd()),
							formatDuration(stay), formatAnswered(stay), formatPings(stay),
							formatSuccess(stay), formatMin(stay), formatMax(stay));
				}
			},

	ByDay("%-10s", " %s")
			{
				/** Midnight and nothing else, so a day is one row whatever was reached during it. */
				@Override
				public boolean isStayEnded(PingStay stay, PingLogRow row)
				{
					String		dayStay		= formatDay(stay.getMsEnd());
					String		dayRow		= formatDay(row.getMsTime());
					boolean		isEnded		= !dayStay.equals(dayRow);
					return isEnded;
				}

				@Override
				public void reportHeader()
				{
					System.out.printf(getFormatLine(), "Date",
							"Answered", "Pings", "Success", "Min", "Max", "Networks");
				}

				/** The date, and last the networks the day held, however many that is. */
				@Override
				public void reportStay(PingStay stay)
				{
					System.out.printf(getFormatLine(), formatDay(stay.getMsStart()),
							formatAnswered(stay), formatPings(stay), formatSuccess(stay),
							formatMin(stay), formatMax(stay), formatNetworkList(stay));
				}
			};

	// Answered, Pings, Success, Min, Max — what every report carries, in the same place in each.
	private static final String		FORMAT_TAIL		= " %8s %8s %8s %9s %9s";

	private final String			formatHead;
	private final String			formatSuffix;

	PingReport(String formatHead, String formatSuffix)
	{
		this.formatHead = formatHead;
		this.formatSuffix = formatSuffix;
	}

	public String getFormatHead()
	{
		return formatHead;
	}

	public String getFormatSuffix()
	{
		return formatSuffix;
	}

	public String getFormatLine()
	{
		String		formatLine		= String.format("%s%s%s%n",
				getFormatHead(), FORMAT_TAIL, getFormatSuffix());
		return formatLine;
	}

	/** True when this row starts a new stay rather than continuing the one being built. */
	public abstract boolean isStayEnded(PingStay stay, PingLogRow row);

	public abstract void reportHeader();

	public abstract void reportStay(PingStay stay);

	public static String formatDuration(PingStay stay)
	{
		String		text		= TimeUnits.MS.format(stay.getMsEnd() - stay.getMsStart());
		return text;
	}

	public static String formatAnswered(PingStay stay)
	{
		String		text		= String.format("%,d", stay.getCountReplied());
		return text;
	}

	public static String formatPings(PingStay stay)
	{
		String		text		= String.format("%,d", stay.getRowList().size());
		return text;
	}

	public static String formatMin(PingStay stay)
	{
		String		text		= formatMsElapsed(stay, stay.getMsElapsedMin());
		return text;
	}

	public static String formatMax(PingStay stay)
	{
		String		text		= formatMsElapsed(stay, stay.getMsElapsedMax());
		return text;
	}

	// A stay nothing answered has no round trip to show, and a zero would read as one.
	private static String formatMsElapsed(PingStay stay, double msElapsed)
	{
		String		text		= 0 == stay.getCountReplied()
				? ""
				: String.format("%,.0f ms", msElapsed);
		return text;
	}

	public static String formatNetworkList(PingStay stay)
	{
		String		text		= String.join(", ", stay.getWifiNameList());
		return text;
	}

	public static String formatSuccess(PingStay stay)
	{
		int			countRow		= stay.getRowList().size();
		double		percentReplied	= 100.0 * stay.getCountReplied() / countRow;
		String		text			= String.format("%.1f%%", percentReplied);
		return text;
	}

	// The local date the row carries, as text, which is the comparison without any calendar maths.
	public static String formatDay(long msTime)
	{
		String		text		= WallClock.formatTime(
				WallClock.FormatDate.INTL, WallClock.FormatTime.None, msTime);
		return text;
	}

	// To the minute in both: a stay is hours long, so the seconds are noise in the column.
	public static String formatDateTime(long msTime)
	{
		String		text		= WallClock.formatTime(
				WallClock.FormatDate.INTL, WallClock.FormatTime.HM, msTime);
		return text;
	}

	public static String formatTimeOfDay(long msTime)
	{
		String		text		= WallClock.formatTime(
				WallClock.FormatDate.None, WallClock.FormatTime.HM, msTime);
		return text;
	}
}
