package com.lc.offgrid.pingapp;

/**
 * One host's answer to one ping. A ping either comes back with a round trip or it does not,
 * so there is no partial result and no status code: {@code replied} decides, and
 * {@code msElapsed} means nothing when it is false.
 */
public class PingResult
{
	private final String		host;
	private final boolean		replied;
	private final double		msElapsed;

	private PingResult(String host, boolean replied, double msElapsed)
	{
		this.host = host;
		this.replied = replied;
		this.msElapsed = msElapsed;
	}

	public static PingResult replied(String host, double msElapsed)
	{
		PingResult result = new PingResult(host, true, msElapsed);
		return result;
	}
	public static PingResult failed(String host)
	{
		PingResult result = new PingResult(host, false, 0.0);
		return result;
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
