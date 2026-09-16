/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.misc;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.os.CommandExecutor;
import com.lc.basics.tools.os.ExecResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BasicTools
{
	private static final BasicLogger LOGGER				= BasicLogger.getLogger(BasicTools.class);
	private static final boolean		IS_DEBUG			= false;
	static
	{
		getLogger().info("Operating mode: %s",
				isDebug() ? "Debug" : "Production"
				);
	}
	private static final String[]		SIZE_PREFIXES		= new String[] {"", "K", "M", "G", "T", "P", "E"};

	private static final long			TIME_BIG_BANG;
	private static final String			HOST_NAME;
	private static final String			HOST_ADDRESS;

	private static final String			USER_FOLDER;

	private static final Random			RANDOM					= new Random();
	private static final char[]			RANDOM_CHAR_LIST		=
								"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

	private static final MemoryMXBean	MEMORY_BEAN				= ManagementFactory.getMemoryMXBean();

	static		// TIME_BIG_BANG
	{
		TIME_BIG_BANG = getTimeMS(2016, Calendar.JANUARY, 1, 8, 59, 59);
	}

	static		// HOST_NAME
	{
		String			hostName = null;
		String			hostAddress;

		// Safest on mac
		try {
			// Executes the macOS command line utility to get the Computer Name
			Process process = Runtime.getRuntime().exec("scutil --get ComputerName");
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			hostName = reader.readLine();
		} catch (Exception e) {
			// Try something else
		}

		// generic
		try
		{
			InetAddress		host		= InetAddress.getLocalHost();
			hostName	= null == hostName ? host.getHostName() : hostName;
			hostAddress = host.getHostAddress();
		}
		catch (UnknownHostException e)
		{
			hostName = System.getenv("COMPUTERNAME");
			if (null == hostName)
			{
				hostName = "Unknown_host";
			}
			hostAddress = "127.0.0.1";
		}

		HOST_NAME = hostName;
		HOST_ADDRESS = hostAddress;
	}

	static		// USER_FOLDER
	{
		USER_FOLDER = System.getProperty("user.home");
	}

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}
	public static boolean isDebug()
	{
		return IS_DEBUG;
	}
	public static long getTimeBigBang()
	{
		return TIME_BIG_BANG;
	}
	public static String getComputerName()
	{
		return HOST_NAME;
	}
	public static String getComputerNameUC()
	{
		return HOST_NAME.toUpperCase(Locale.US);
	}
	public static String getUserFolder()
	{
		return USER_FOLDER;
	}

	public static String generateRandomString(int length)
	{
		return generateRandomString(length, RANDOM);
	}

	public static String generateReproducibleRandomString(int length)
	{
		Random			random		= new Random(TIME_BIG_BANG + length);
		return generateRandomString(length, random);
	}
	public static String generateRandomString(int length, Random random)
	{
		StringBuilder	sb			= new StringBuilder(Math.max(0, length));
		for (int i = 0; i < length; i++)
		{
			int		r		= random.nextInt(RANDOM_CHAR_LIST.length);
			sb.append(RANDOM_CHAR_LIST[r]);
		}
		return sb.toString();
	}

	public static long getTimeMS(int year, int month, int date, int hour, int minute, int second)
	{
		Calendar calendar		= Calendar.getInstance();
		calendar.setTimeInMillis(0);		// Ensures MS == 0;
		calendar.set(year, month, date, hour, minute, second);
		return calendar.getTimeInMillis();
	}

	/**
	 * Sleep as a wait within the thread, to be interruptable with a notify on the thread object.
	 *
	 * @param ms		Number of ms to wait; 0 = until notified.
	 */
	@SuppressWarnings("ALl")
	public static void wait(int ms)
	{
		Thread		thread		= Thread.currentThread();

		// Proceed this wait to be easily interruptible.
		synchronized (thread)
		{
			try
			{
				thread.wait(ms);
			}
			catch (InterruptedException e)
			{
				// Ignore and exit.
			}
		}
	}

	public static boolean await(CountDownLatch latch, int timeoutMS)
	{
		try
		{
			if (0 != timeoutMS)
			{
				latch.await(timeoutMS, TimeUnit.MILLISECONDS);
			}
			else
			{
				latch.await();
			}
		}
		catch (Exception exception)
		{
			return false;
		}
		return true;
	}

	/**
	 * Real Thread.sleep(), just without the Interrupted Exception.
	 *
	 * @param ms		Number of ms to wait; 0 = return immediately (possibly give up time slice).
	 */
	public static long sleepMS(long ms)
	{
		long		t1		= System.currentTimeMillis();
		try
		{
			Thread.sleep(ms);
		}
		catch (InterruptedException e)
		{
			// NOPMD Ignore and exit.
		}
		return System.currentTimeMillis() - t1;
	}

	public static int burnCpuMS(int ms)
	{
		return (int) burnCpuUS(ms * 1000L) / 1000;
	}
	public static long burnCpuUS(long us)
	{
		return burnCpuNS(us * 1000) / 1000;
	}
	@SuppressWarnings("ALl")
	public static long burnCpuNS(long requestedNS)
	{
		long	startNS			= System.nanoTime();
		long	endNS			= startNS + requestedNS;

		while (System.nanoTime() < endNS)
		{
			// Keep burning CPU
		}

		return System.nanoTime() - startNS;
	}


	public static int getNumberOfProcessors()
	{
		return Runtime.getRuntime().availableProcessors();
	}
	public static String formatByteCount(long value)
	{
		for (int i = 6; i > 0; i--)
		{
			double step = Math.pow(1024, i);
			if (value > step)
			{
				return String.format("%.1f %sB", value / step, SIZE_PREFIXES[i]);
			}
		}
		return Long.toString(value);
	}

	public static long getHeapSizeMax()
	{
		MemoryUsage u = MEMORY_BEAN.getHeapMemoryUsage();

		return u.getMax();
	}

	public static long getHeapSize()
	{
		MemoryUsage u = MEMORY_BEAN.getHeapMemoryUsage();

		return u.getUsed();
	}

	public static int getHeapPercent(boolean isLazy)
	{
		MemoryUsage u	= MEMORY_BEAN.getHeapMemoryUsage();

		return (int) ((u.getUsed() * 100) / u.getMax());
	}

	private static final String WIFI_NAME_COMMAND = "WIFI_INT=$(networksetup -listallhardwareports | awk '/Wi-Fi|AirPort/{getline; print $NF}')\n" +
			"networksetup -listpreferredwirelessnetworks \"$WIFI_INT\" | sed -n '2s/^\\t//p'\n";
	public static String getWifiName()
	{
		try
		{
			String[] cmd = {
					"/bin/zsh",
					"-c",
					WIFI_NAME_COMMAND
			};
			ExecResponse response = CommandExecutor.exec(cmd);
			return response.getResponse().trim();
		}
		catch (BasicException e)
		{
			return "Unknown";
		}
	}
}
