package com.lc.offgrid.pingapp;

import com.lc.basics.tools.file.BasicFileReader;
import com.lc.basics.tools.file.BasicFileWriter;
import com.lc.basics.tools.misc.BasicException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The ping app's own folder under {@code folder.local}: the host list it reads and the log it
 * appends to. Nothing here is cached — the host list is read from disk on every call so the
 * file can be edited while the monitor runs.
 */
@Component
public class PingFileManager
{
	private static final String		FOLDER_NAME				= "/ping";
	private static final String		FILENAME_HOST_LIST		= "hostList.txt";
	private static final String		FILENAME_PING_LOG		= "pingLog.tsv";

	private static final String		GATEWAY_NOT_FOUND		= "# The gateway could not be read. Add it by hand.";

	/**
	 * What the host list is created with. Every entry answered a ping from this machine when
	 * the list was written; amazon.com, netflix.com and microsoft.com were dropped because
	 * they do not answer ICMP at all.
	 */
	private static final String		HOST_LIST_SEED			= """
			# Hosts pinged one per pass, one per line. The file is re-read at the top of every
			# pass, so it can be edited while Monitor runs. Blank lines and # lines are ignored.

			# The gateway. It answering while nothing else does puts the fault past the LAN.
			%s

			# Public resolvers by address. There is no name to look up, so a DNS outage does not
			# read as a network outage.
			8.8.8.8
			8.8.4.4
			1.1.1.1
			1.0.0.1
			9.9.9.9
			149.112.112.112
			208.67.222.222
			208.67.220.220
			4.2.2.2
			76.76.2.0

			# By name, which puts DNS in the path as well.
			google.com
			dns.google
			one.one.one.one
			cloudflare.com
			github.com
			wikipedia.org
			apple.com
			a.root-servers.net
			""";

	@Value("${folder.local}")
	// Initializer for tests. As a bean, it gets the value from config.
	private String folderLocal = "/Users/pierrecote/Working/offgrid";

	public String getFolderLocal()
	{
		return folderLocal;
	}

	// The app's folder, created on first use so a fresh machine needs no setup.
	public File getPingFolder()
	{
		File		folder		= new File(getFolderLocal() + FOLDER_NAME);
		folder.mkdirs();
		return folder;
	}

	public File getPingLogFile()
	{
		File		file		= new File(getPingFolder(), FILENAME_PING_LOG);
		return file;
	}

	public String getPingLogFilename()
	{
		String		filename	= getPingLogFile().getAbsolutePath();
		return filename;
	}

	// Writes the seed list the first time, so there is something to ping before it is edited.
	public File getHostListFile()
	{
		File		file		= new File(getPingFolder(), FILENAME_HOST_LIST);
		if (!file.exists())
		{
			writeHostListSeed(file);
		}
		return file;
	}

	/**
	 * The hosts to ping, read from disk every time. Blank lines and # comments are dropped.
	 */
	public List<String> readHostList()
	{
		File			file		= getHostListFile();
		String			text		= BasicFileReader.readTextFile(file.getAbsolutePath());
		String[]		lineList	= text.split("\n");

		List<String>	hostList	= new ArrayList<>();
		for (String line : lineList)
		{
			String		host		= line.trim();
			if (!host.isEmpty() && !host.startsWith("#"))
			{
				hostList.add(host);
			}
		}
		return hostList;
	}

	private static void writeHostListSeed(File file)
	{
		String				gateway			= PingProbe.getDefaultGateway();
		String				gatewayText		= null == gateway ? GATEWAY_NOT_FOUND : gateway;
		String				text			= String.format(HOST_LIST_SEED, gatewayText);
		String				filename		= file.getAbsolutePath();

		BasicFileWriter		writer			= new BasicFileWriter();
		try
		{
			writer.openStream(filename);
			writer.println("%s", text);
		}
		catch (BasicException exception)
		{
			throw new IllegalStateException(
					String.format("writeHostListSeed(%s)", filename), exception);
		}
		finally
		{
			writer.close();
		}
	}
}
