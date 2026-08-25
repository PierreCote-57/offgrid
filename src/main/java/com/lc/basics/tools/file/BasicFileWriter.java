/*
 * Copyright (c) 2013-2020 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.file;

import com.lc.basics.tools.misc.BasicException;
import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.basics.tools.time.WallClock;
import com.lc.basics.tools.time.WallClock.FormatSize;
import com.lc.basics.tools.time.WallClock.FormatType;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class BasicFileWriter extends BaseFileHandler implements Closeable
{
	private static final String				DATE_FORMAT					= "%1$tY.%1$tm.%1$td %1$tH.%1$tM.%1$tS.%1$tL";
	private static	final String			FAILED_TO_WRITE_TEMP_FILE	= "Failed to create temporary file with "
			+ "PREFIX '%s' SUFFIX '%s'" ;
	private	static	final String FAILED_TO_WRITE_FILE		= "Failed to write file '%s' ";

	private OutputStream m_outputStream				= null;
	private PrintWriter m_printWriter				= null;

	public BasicFileWriter()
	{

	}
	public BasicFileWriter(String filename)
	{
		openFile(filename);
	}

	public static void write(byte[] bytes, String filename) throws Exception
	{
		try (BasicFileWriter writer = new BasicFileWriter())
		{
			writer.openStream(filename);
			writer.m_outputStream.write(bytes);
		}
	}
	public static void write(String text, String filename)
	{
		try (BasicFileWriter writer = new BasicFileWriter())
		{
			writer.openFile(filename);
			writer.println("%s", text);
		}
	}

	public void openTempFile(String prefix, String suffix) throws BasicException
	{
		try
		{
			File file = File.createTempFile(prefix, suffix);
			String filename = file.getAbsolutePath();
			openFile(filename);
		}
		catch (IOException exception)
		{
			throw new BasicException(exception,
					FAILED_TO_WRITE_TEMP_FILE, prefix, suffix);
		}
	}

	public void openLogFile(String prefix)
	{
		String		dateText		= String.format(DATE_FORMAT, new Date());
		String		shortName		= String.format("%s-%s.log", prefix, dateText);
		String		filename		= getWorkingFilename("log", shortName);

		openFile(filename);
	}
	public final void openFile(String filename)
	{
		try
		{
			ensureFile(filename, false);
			m_printWriter = new PrintWriter(filename);
		}
		catch (IOException exception)
		{
			throw new BasicRuntimeException(exception,
					FAILED_TO_WRITE_FILE, filename);
		}
	}

	public final void setStream(OutputStream outputStream)
	{
		m_outputStream = outputStream;
		m_printWriter = new PrintWriter(m_outputStream);
	}

	public final void openStream(String filename) throws BasicException
	{
		openStream(filename, false);
	}
	public final void openStream(String filename, boolean fAppend) throws BasicException
	{
		try
		{
			ensureFile(filename, fAppend);
			setStream(new FileOutputStream(filename, fAppend));
		}
		catch (IOException exception)
		{
			throw new BasicException(exception,
				FAILED_TO_WRITE_FILE, filename);
		}
	}

	public final void openStream(URL url) throws BasicException
	{
		setFilename(url.getPath());

		try
		{
			URLConnection connection	= url.openConnection();
			setStream(connection.getOutputStream());
		}
		catch (IOException exception)
		{
			throw new BasicException(exception,
					FAILED_TO_WRITE_FILE, url.toString());
		}
	}

	/**
	* This method ensures if the file can be created.
	 * It does not actually create the file as that would be done as part of the FileOutputStream or PrintWriter class
	 * */
	private void ensureFile(String filename, boolean fAppend)
	{
		// Make sure there is not a leaked opened file
		close();
		File file		= new File(filename);
		if (file.exists() && !fAppend)
		{
			file.delete();
		}

		setFilename(filename);
		ensureFolder(filename);
	}

	public final void appendFile(String filename) throws BasicException
	{
		openStream(filename, true);
		m_printWriter = new PrintWriter(m_outputStream);
	}
	public void println(String format, Object ... args)
	{
		incrementLineCount();

		String		text		= String.format(format, args);
		m_printWriter.println(text);
	}
	public void printStackTrace(Throwable throwable)
	{
		throwable.printStackTrace(m_printWriter);
	}

	public void flush()
	{
		m_printWriter.flush();
	}

	@Override
	public void close()
	{
		closeSafe(m_printWriter);
		m_printWriter = null;
		closeSafe(m_outputStream);
		m_outputStream = null;

		setFilename(null);
	}

	public OutputStream getOutputStream()
	{
		return m_outputStream;
	}

	public PrintWriter getPrintWriter()
	{
		return m_printWriter;
	}

	public static void writePropertiesFile(String filename, Object object) throws Exception
	{
		Map<String, String>		map		= toPropertiesMap(object);
		writePropertiesFile(filename, map);
	}
	public static void writePropertiesFile(String filename, Properties properties) throws IOException
	{
		Map<String, String>		map		= new TreeMap<>();
		for (Map.Entry<Object, Object> entry : properties.entrySet())
		{
			map.put(entry.getKey().toString(), entry.getValue().toString());
		}
		writePropertiesFile(filename, map);
	}
	public static void writePropertiesFile(String filename, Map<String, String> map) throws IOException
	{
		long					now			= System.currentTimeMillis();
		String					nowText		= WallClock.formatTime(FormatType.DateTime, FormatSize.Medium, now);
		String					comment		= String.format("# %s on %s", filename, nowText);
		try(BasicFileWriter		writer		= new BasicFileWriter(filename))
		{
			writer.println(comment);
			for (Map.Entry<String, String> entry : map.entrySet())
			{
				writer.println("%s=%s", entry.getKey(), entry.getValue());
			}
		}
		catch (Exception exception)
		{
			throw new IOException(String.format(FAILED_TO_WRITE_FILE, filename), exception);
		}
	}
	public static Map<String, String> toPropertiesMap(Object object) throws IllegalAccessException
	{
		Map<String, String>		map		= new TreeMap<>();

		Field[]			fieldList		= object.getClass().getDeclaredFields();
		for (Field field : fieldList)
		{
			int			modifiers	= field.getModifiers();
			if (Modifier.isStatic(modifiers))
			{
				continue;
			}
			String		name		= field.getName();		// Remove the leading m_
			if (name.startsWith("m_"))
			{
				name = name.substring(2);
			}
			field.setAccessible(true);
			Object		value		= field.get(object);
			if (null != value)
			{
				map.put(name, value.toString());
			}
		}

		return map;
	}
}
