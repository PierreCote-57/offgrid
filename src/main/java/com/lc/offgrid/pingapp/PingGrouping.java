package com.lc.offgrid.pingapp;

import com.lc.basics.tools.time.WallClock;

/**
 * What closes a stay, which is the whole of what makes one statistics report differ from the
 * next. The same rows, the same columns, cut in a different place.
 */
public enum PingGrouping
{
	Overview
			{
				/** The network and nothing else, so a network reached over three days is one row. */
				@Override
				public boolean isStayEnded(PingStay stay, PingLogRow row)
				{
					boolean		isEnded		= isNetworkChanged(stay, row);
					return isEnded;
				}
			},

	ByDay
			{
				/** Midnight and nothing else, so a day is one row whatever was reached during it. */
				@Override
				public boolean isStayEnded(PingStay stay, PingLogRow row)
				{
					boolean		isEnded		= isDayChanged(stay, row);
					return isEnded;
				}
			};

	/** True when this row starts a new stay rather than continuing the one being built. */
	public abstract boolean isStayEnded(PingStay stay, PingLogRow row);

	public static boolean isNetworkChanged(PingStay stay, PingLogRow row)
	{
		boolean		isChanged		= !stay.getWifiName().equals(row.getWifiName());
		return isChanged;
	}

	public static boolean isDayChanged(PingStay stay, PingLogRow row)
	{
		String		dayStay			= formatDay(stay.getMsEnd());
		String		dayRow			= formatDay(row.getMsTime());
		boolean		isChanged		= !dayStay.equals(dayRow);
		return isChanged;
	}

	// The local date the row carries, as text, which is the comparison without any calendar maths.
	private static String formatDay(long msTime)
	{
		String		text			= WallClock.formatTime(
				WallClock.FormatDate.INTL, WallClock.FormatTime.None, msTime);
		return text;
	}
}
