package com.lc.offgrid.pingapp;

import com.lc.basics.tools.os.CommandExecutor;
import com.lc.basics.tools.os.ExecResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs the OS {@code ping} command and reads the round trip out of its output.
 * <p>
 * The flags are macOS: {@code -c} is the packet count and {@code -W} is the milliseconds to
 * wait for the reply. Linux reads {@code -W} as seconds, so this needs a branch the day the
 * app runs anywhere but here.
 */
public class PingProbe
{
	private static final Pattern	PATTERN_TIME		= Pattern.compile("time=([0-9.]+)\\s*ms");
	private static final Pattern	PATTERN_GATEWAY		= Pattern.compile("gateway:\\s*(\\S+)");

	/**
	 * Sends one packet and waits up to msTimeout for the answer.
	 *
	 * @param host			what to ping, a name or an address
	 * @param msTimeout		how long to wait for the single reply
	 * @return				the round trip, or a failure when nothing came back. An unknown
	 * 						host and an unreachable one are the same answer here: no time.
	 */
	public static PingResult ping(String host, int msTimeout)
	{
		String			timeoutText		= String.format("%d", msTimeout);
		String[]		commandList		= {"ping", "-c", "1", "-W", timeoutText, host};
		ExecResponse	response		= new ExecResponse();
		boolean			isExecuted		= CommandExecutor.exec(commandList, response);

		PingResult		result			= isExecuted
				? readElapsed(host, response.getResponse())
				: PingResult.failed(host);
		return result;
	}

	/**
	 * The default gateway's address, or null when the route cannot be read. Written into the
	 * seed host list so the first pass already separates the LAN from everything past it.
	 */
	public static String getDefaultGateway()
	{
		String[]		commandList		= {"route", "-n", "get", "default"};
		ExecResponse	response		= new ExecResponse();
		boolean			isExecuted		= CommandExecutor.exec(commandList, response);

		String			gateway			= null;
		if (isExecuted && null != response.getResponse())
		{
			Matcher		matcher		= PATTERN_GATEWAY.matcher(response.getResponse());
			if (matcher.find())
			{
				gateway = matcher.group(1);
			}
		}
		return gateway;
	}

	// A reply prints "time=11.348 ms". Nothing else in the output says the packet came back:
	// a lost packet and a name that does not resolve both simply lack the line.
	private static PingResult readElapsed(String host, String text)
	{
		PingResult		result		= PingResult.failed(host);
		if (null != text)
		{
			Matcher		matcher		= PATTERN_TIME.matcher(text);
			if (matcher.find())
			{
				double	msElapsed	= Double.parseDouble(matcher.group(1));
				result = PingResult.replied(host, msElapsed);
			}
		}
		return result;
	}
}
