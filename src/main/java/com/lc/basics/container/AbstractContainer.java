/*
 * Copyright (c) 2014 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.container;

import com.lc.basics.monitoring.statistics.Accumulator;
import com.lc.basics.monitoring.statistics.SimpleAccumulator;
import com.lc.basics.tools.file.BaseFileHandler;
import com.lc.basics.tools.file.BasicFileWriter;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.logging.EventFormatter;
import com.lc.basics.tools.misc.BasicTools;
import com.lc.basics.tools.time.WallClock;
import com.lc.basics.tools.units.TimeUnits;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbstractContainer
{
	private static final BasicLogger			LOGGER		= BasicLogger.getLogger(AbstractContainer.class);
	private static final Random					RANDOM		= new Random();

	static
	{
		Runtime.getRuntime().addShutdownHook(new Thread(AbstractContainer::shutdown));
	}
	private static void shutdown()
	{
		getLogger().info("Shutting down");
	}

	private static BasicFileWriter s_writer		= null;

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}
	public static Random getRandom()
	{
		return RANDOM;
	}

	public static void createLogFile(String name)
	{
		String filename		= String.format("%1$s-%2$tY.%2$tm.%2$td_%2$tH.%2$tM.%2$tS.txt", name, new Date());
		filename = BaseFileHandler.getWorkingFilename("logs", filename);
		try
		{
			s_writer = new BasicFileWriter(filename);
		}
		catch (Exception e)
		{
			errorStamp("Failed to create the log file", e);
		}
	}

	public static String getArg(String[] args, int index, String notSet)
	{
		String		value;
		if (args.length > index)
		{
			value = args[index];
		}
		else
		{
			value = notSet;
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractContainer, A extends AbstractAction<T>> void runActionList(A[] list, Supplier<String> headerSupplier)
	{
		while (true)
		{
			AbstractAction<T> action;
			try
			{
				System.out.println();
				if (null != headerSupplier)
				{
					System.out.println(headerSupplier.get());
				}
				action = queryObject("Action?", "", list, 0);
			}
			catch (Exception exception)
			{
				System.out.println("Please specify a valid action");
				continue;
			}
			try
			{
				EventFormatter.resetTimer();
				timeStamp("Starting %s", action);
				action.execute((T) this);
			}
			catch (Exception exception)
			{
				getLogger().error(exception, "Something went wrong");
//				BasicTools.sleepMS(1000);
			}

			if (action.getClass().getName().contains("Exit")
					|| action.toString().contains("Exit"))
			{
				return;
			}
		}
	}

	@SuppressWarnings("PMD.SystemPrintln")		// This IS a command line application!
	public static void timeStamp(String format, Object... args)
	{
		timeStamp(null, format, args);
	}
	public static void timeStamp(Throwable throwable, String format, Object... args)
	{
		String message		= EventFormatter.formatMessage(throwable, format, args);

		System.out.println(message);
		if (null != s_writer)
		{
			s_writer.println(message);
			s_writer.flush();
		}
	}
	public static void timeStampSimple(String format, Object... args)
	{
		String		timeText		= WallClock.formatTime(WallClock.FormatDate.INTL, WallClock.FormatTime.HMSm, System.currentTimeMillis());
		String		objectText		= String.format(format, args);
		System.out.println(timeText + " " + objectText);
	}

	public static void errorStamp(String format, Object... args)
	{
		errorStamp(null, format, args);
	}
	public static void errorStamp(Throwable throwable, String format, Object... args)
	{
		String message		= EventFormatter.formatMessage(throwable, format, args);

		System.err.println(message);
		if (null != s_writer)
		{
			s_writer.println(message);
		}
	}

	public static boolean queryBoolean(String prompt, String prefix, boolean value) throws IOException
	{
		String		text		= queryText(prompt, prefix, value ? "Yes" : "No");
		boolean		answer		= text.toUpperCase(Locale.US).startsWith("Y");
		return answer;
	}
	public static int queryInt(String prompt, String prefix, int value) throws IOException
	{
		String		text		= queryText(prompt, prefix, String.format("%,d", value));
		int			answer		= Integer.parseInt(text.replaceAll(",", "").replaceAll("_", ""));
		return answer;
	}
	public static double queryDouble(String prompt, String prefix, double value) throws IOException
	{
		String		text		= queryText(prompt, prefix, String.format("%.2f", value));
		double		answer		= Double.parseDouble(text.replaceAll(",", "").replaceAll("_", ""));
		return answer;
	}
	public static String queryText(String prompt, String prefix, String value) throws IOException
	{
		System.out.printf("%s%-20s [%15s] > ", prefix, prompt, value);

		byte[]		bytes		= new byte[128];
		int			cbRead		= System.in.read(bytes);
		String		text		= 1 >= cbRead ? value : new String(bytes, 0, cbRead);
		text = text.replace("\n","");

		return text;
	}
	public static <T> T queryObject(String prompt, String prefix, T[] list) throws IOException
	{
		return queryObject(prompt, prefix, list, 0);
	}
	public static <T> T queryObject(String prompt, String prefix, T[] list, int value) throws IOException
	{
		return queryObject(prompt, prefix, Arrays.asList(list), value);
	}
	public static <T> T queryObject(String prompt, String prefix, List<T> list) throws IOException
	{
		return queryObject(prompt, prefix, list, 0);
	}
	public static <T> int queryInt(String prompt, String prefix, List<T> list, int value) throws IOException
	{
		for (int i = 0; i < list.size(); i++)
		{
			System.out.printf("%s%,d. %s%n", prefix, i, list.get(i));
		}
		int selection = queryInt(prompt, prefix, value);
		return selection;
	}
	public static <T> T queryObject(String prompt, String prefix, List<T> list, int value) throws IOException
	{
		for (int i = 0; i < list.size(); i++)
		{
			System.out.printf("%s%,d. %s%n", prefix, i, list.get(i).toString());
		}
		int		selection		= queryInt(prompt, prefix, value);
		return list.get(selection);
	}
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T queryEnum(String prompt, String prefix, Class<T> enumType, T value) throws Exception
	{
		Method		method		= enumType.getMethod("values");
		method.setAccessible(true);
		Object		object		= method.invoke(null);
		T[]			list		=(T[]) object;
		String[]	nameList	= new String[list.length];
		for (int i = 0; i < list.length; i++)
		{
			nameList[i] = list[i].name();
		}
		String		name		= queryObject(prompt, prefix, nameList, value.ordinal());
		T			answer		= Enum.valueOf(enumType, name);
		return answer;
	}

	public static long queryDurationSec(String prompt, String prefix, Long defaultValue) throws IOException
	{
		String text = queryText(prompt, prefix, defaultValue.toString());
		String[] parts = text.split(" ");
		long count = Long.parseLong(parts[0]);
		if (parts.length > 1)
		{
			String units = parts[1];
			double value = TimeUnits.SEC.parse(parts[0], parts[1]);
			count = (long) value;
		}
		return count;
	}

	@SafeVarargs
	public static void evaluateFunction(String[] nameList, int informPeriodSec, int iMax, Consumer<Integer>... functionList)
	{

	}
	@SafeVarargs
	public static void evaluateFunction(String[] nameList, int informPeriodSec, int iMax, Function<Integer, Double> ... functionList)
	{
		long					beginTimeMS			= System.currentTimeMillis();
		long					informTimeMS		= beginTimeMS + (1000L * informPeriodSec);
		int						count				= 0;

		SimpleAccumulator[]		durationListNS		= new SimpleAccumulator[functionList.length];
		SimpleAccumulator[]		accumulatorList		= new SimpleAccumulator[functionList.length];
		int[]					errorCountList		= new int[functionList.length];
		for (int i = 0; i < accumulatorList.length; i++)
		{
			accumulatorList[i] = new SimpleAccumulator();
			durationListNS[i] = new SimpleAccumulator();
		}

		while (System.currentTimeMillis() < informTimeMS)
		{
			for (int i = 0; i < accumulatorList.length; i++)
			{
				Function<Integer, Double> function = functionList[i];
				int			index		= getRandom().nextInt(iMax);
				long		t1			= System.nanoTime();
				double		result		= function.apply(index);
				long		t2			= System.nanoTime();
				durationListNS[i].record(t2 - t1);
				accumulatorList[i].record(result);
				if (result < 0)
				{
					errorCountList[i]++;
				}
				count++;
			}
		}

		reportTiming(nameList, count, errorCountList, durationListNS, accumulatorList, true);
	}

	private static final AtomicInteger	s_lineNo		= new AtomicInteger(0);
	public static void reportTiming(String[] nameList, int count, int[] errorCountList, SimpleAccumulator[] durationListNS, Accumulator[] accumulatorList, boolean showResult)
	{
		if (0 == (s_lineNo.getAndIncrement() % 5))
		{
			printHeader(nameList);
		}

		if (showResult)
		{
			printData(nameList, count, errorCountList, durationListNS, accumulatorList);
		}
	}
	private static void printHeader(String[] nameList)
	{
		String		header		= String.format("%12s %9s ",
				"Count ", "QPS ");

		StringBuilder		sb		= new StringBuilder(200);
		sb.append(header);
		for (String name : nameList)
		{
			sb.append(formatHeaderItem());
		}
		sb.append(formatHeaderItem());
		timeStampSimple("%s", sb.toString());
	}
	private static String formatHeaderItem()
	{
//		String		item		= String.format("%-6s %8s %8s %8s %6s ",
		String		item		= String.format("|%-6s %5s %8s %8s %6s",
				"(Name", "Error", "Avg ", "Max ", "  Hit%)");
		return item;
	}
	private static void printData(String[] nameList, int count, int[] errorCountList, SimpleAccumulator[] durationListNS, Accumulator[] accumulatorList)
	{
		double		totalNS		= 0;
		double		totalCount	= 0;
		for (SimpleAccumulator durationListN : durationListNS)
		{
			totalNS += durationListN.getSum();
			totalCount += durationListN.getCount();
		}
		double		qps		= totalCount / (totalNS / (1_000_000_000));

		String		header		= String.format("%,12d %,9.0f ",
				count, qps);

		StringBuilder		sb		= new StringBuilder(200);
		sb.append(header);
		double				min			= Double.MAX_VALUE;
		long				sumCount	= 0;
		double				sum			= 0;
		double				max			= 0.0;
		double				hitSum		= 0.0;
		int					errorTotal	= 0;
		for (int i = 0; i < durationListNS.length; i++)
		{
			String		name		= nameList[i];
			double		minNS		= durationListNS[i].getMin();
			double		avgNS		= durationListNS[i].getAvg();
			double		maxNS		= durationListNS[i].getMax();
			double		hitRate		= accumulatorList[i].getAvg();

			errorTotal += errorCountList[i];
			sumCount += durationListNS[i].getCount();
			sum += durationListNS[i].getSum();
			min = Math.min(min, minNS);
			max = Math.max(max, maxNS);
			max = Math.max(max, maxNS);
			hitSum += (hitRate * durationListNS[i].getCount());

			String		item		= formatItem(name, errorCountList[i], minNS, avgNS, maxNS, hitRate);
			sb.append(item);
		}
		String		total		= formatItem("Total", errorTotal,
				min, sum / sumCount, max, hitSum / sumCount);
		sb.append(total);
		timeStampSimple("%s", sb.toString());
	}
	private static String formatItem(String name, int errorCount, double minNS, double avgNS, double maxNS, double hitRate)
	{
//		String		item		= String.format(" %-5s %8s %8s %8s %5.1f%%  ",
		String		item		= String.format("| %-5s %,5d %8s %8s %5.1f%% ",
				name,
				errorCount,
				formatNS(avgNS),
				formatNS(maxNS),
				hitRate
		);
		return item;
	}
	private static String formatNS(double ns)
	{
		return TimeUnits.NS.format(ns);
	}
}
