package com.lc.offgrid.pingapp;

import com.lc.basics.tools.time.WallClock;

/**
 * One row of the ping log, read back: when it was written, the network it was written on, the
 * host that was drawn, and the round trip when one came back.
 */
public class PingLogRow
{
	private static final int		COUNT_FIELD		= 4;

	private final long				msTime;
	private final String			wifiName;
	private final String			host;
	private final boolean			replied;
	private final double			msElapsed;

	private PingLogRow(long msTime, String wifiName, String host, boolean replied, double msElapsed)
	{
		this.msTime = msTime;
		this.wifiName = wifiName;
		this.host = host;
		this.replied = replied;
		this.msElapsed = msElapsed;
	}

	/**
	 * Reads one line of the log, or null when the line carries no row. The header and a blank
	 * line both fail the leading digit, which is what keeps them out without a special case.
	 */
	public static PingLogRow parse(String line)
	{
		PingLogRow		row				= null;
		String[]		fieldList		= line.split("\t", -1);
		boolean			isRow			= COUNT_FIELD == fieldList.length
				&& !fieldList[0].isEmpty()
				&& Character.isDigit(fieldList[0].charAt(0));

		if (isRow)
		{
			long		msTime			= WallClock.parseTime(fieldList[0]);
			String		msText			= fieldList[3].trim();
			boolean		replied			= !msText.isEmpty();
			double		msElapsed		= replied ? Double.parseDouble(msText) : 0.0;

			row = new PingLogRow(msTime, fieldList[1], fieldList[2], replied, msElapsed);
		}
		return row;
	}

	public long getMsTime()
	{
		return msTime;
	}
	public String getWifiName()
	{
		return wifiName;
	}
	public String getHost()
	{
		return host;
	}
	public boolean isReplied()
	{
		return replied;
	}
	public double getMsElapsed()
	{
		return msElapsed;
	}
}
