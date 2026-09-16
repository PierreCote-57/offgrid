package com.lc.offgrid.pingapp;

import com.lc.basics.container.AbstractContainer;
import com.lc.basics.tools.file.BasicFileWriter;
import com.lc.basics.tools.misc.BasicException;
import com.lc.basics.tools.misc.BasicTools;
import com.lc.basics.tools.time.WallClock;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Pings one host a pass until Return is pressed, picking it at random from the list each
 * time. The list is read again every pass, so a host added while this runs joins the draw.
 * <p>
 * Sampling one host rather than all of them keeps a single silent minute ambiguous — that
 * host or the whole link — and reads the difference out of the run instead: the draw moves
 * on, so a dead link goes quiet across hosts while a dead host does not.
 */
public class PingMonitor
{
	private static final int		MS_TIMEOUT			= 5_000;
	private static final int		MS_SLICE			= 250;

	private static final String		HEADER_LOG			= "dateTime\twifi\thost\tms";

	// The console lines. The host is padded and the time right-aligned so the numbers read down
	// a column; a host longer than the width pushes its own line out and leaves the rest alone.
	private static final String		FORMAT_REPLIED		= "%-20s answered in %,8.1f ms";
	private static final String		FORMAT_SILENT		= "%-20s did not answer";

	private final PingFileManager	pingFileManager;
	private final PingInterval		pingInterval;

	public PingMonitor(PingFileManager pingFileManager, PingInterval pingInterval)
	{
		this.pingFileManager = pingFileManager;
		this.pingInterval = pingInterval;
	}

	public PingFileManager getPingFileManager()
	{
		return pingFileManager;
	}

	public PingInterval getPingInterval()
	{
		return pingInterval;
	}

	public void run() throws Exception
	{
		BasicFileWriter		writer		= openLog();
		try
		{
			boolean			isStopped	= false;
			while (!isStopped)
			{
				long			msPassStart		= System.currentTimeMillis();
				List<String>	hostList		= getPingFileManager().readHostList();
				String			host			= drawHost(hostList);

				if (null == host)
				{
					AbstractContainer.timeStamp("Nothing to ping: %s holds no hosts",
							getPingFileManager().getHostListFile().getName());
				}
				else
				{
					PingResult	result		= PingProbe.ping(host, MS_TIMEOUT);
					report(writer, result);
				}
				isStopped = waitForNextPass(msPassStart + getPingInterval().getMsPerPass());
			}
		}
		finally
		{
			writer.close();
		}
	}

	// The host this pass pings, or null when the list is empty.
	private static String drawHost(List<String> hostList)
	{
		String		host		= null;
		if (!hostList.isEmpty())
		{
			int		index		= AbstractContainer.getRandom().nextInt(hostList.size());
			host = hostList.get(index);
		}
		return host;
	}

	private BasicFileWriter openLog() throws BasicException
	{
		String				filename	= getPingFileManager().getPingLogFilename();
		boolean				isNew		= !new File(filename).exists();
		BasicFileWriter		writer		= new BasicFileWriter();

		writer.appendFile(filename);
		if (isNew)
		{
			writer.println("%s", HEADER_LOG);
		}
		return writer;
	}

	/**
	 * One row in the log and one line on the console. The log row leaves ms empty when nothing
	 * came back, so a statistics run counts blanks rather than reading a stand-in number. The
	 * wifi name is read every pass, so moving to another network shows up in the rows.
	 */
	private static void report(BasicFileWriter writer, PingResult result)
	{
		String		dateTime		= WallClock.formatTime(
				WallClock.FormatDate.INTL, WallClock.FormatTime.HMS, System.currentTimeMillis());
		String		wifiName		= BasicTools.getWifiName();
		String		msText			= result.isReplied()
				? String.format("%.3f", result.getMsElapsed())
				: "";

		writer.println("%s\t%s\t%s\t%s", dateTime, wifiName, result.getHost(), msText);
		writer.flush();

		if (result.isReplied())
		{
			AbstractContainer.timeStamp(FORMAT_REPLIED, result.getHost(), result.getMsElapsed());
		}
		else
		{
			AbstractContainer.timeStamp(FORMAT_SILENT, result.getHost());
		}
	}

	// Sleeps in slices so Return is noticed inside the pass rather than at the end of it.
	private static boolean waitForNextPass(long msPassEnd) throws IOException
	{
		boolean		isStopped		= isReturnPressed();
		while (!isStopped && System.currentTimeMillis() < msPassEnd)
		{
			BasicTools.sleepMS(MS_SLICE);
			isStopped = isReturnPressed();
		}
		return isStopped;
	}

	/**
	 * True once the console has something waiting, which is what Return produces on a
	 * line-buffered terminal. The bytes are read so the newline does not fall through into
	 * the next menu prompt.
	 */
	private static boolean isReturnPressed() throws IOException
	{
		boolean		isPressed		= 0 < System.in.available();
		while (0 < System.in.available())
		{
			System.in.read();
		}
		return isPressed;
	}
}
