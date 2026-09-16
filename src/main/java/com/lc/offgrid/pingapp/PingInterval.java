package com.lc.offgrid.pingapp;

/**
 * How long one monitor pass lasts. Monitor asks for one before it starts, so the list here is
 * the list offered and adding an interval is adding a constant.
 */
public enum PingInterval
{
	Sec_10(10_000),
	Sec_30(30_000),
	Min_1(60_000),
	Min_5(300_000);

	private final int		msPerPass;

	PingInterval(int msPerPass)
	{
		this.msPerPass = msPerPass;
	}

	public int getMsPerPass()
	{
		return msPerPass;
	}

	public int getSecondPerPass()
	{
		int		secondPerPass		= getMsPerPass() / 1000;
		return secondPerPass;
	}
}
