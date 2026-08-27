/*
 * Copyright (c) 2013-2014 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.file;

import com.lc.basics.tools.misc.BasicException;
import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.basics.tools.units.TimeUnits;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;


public class BasicFileReader extends BaseFileHandler implements Closeable
{
	private static final String FAILED_TO_OPEN_FORMAT	= "Failed to open file '%s'";

	private volatile InputStream m_inputStream			= null;
	private volatile GZIPInputStream m_gzipInputStream		= null;
	private volatile InputStreamReader m_inputReader			= null;
	private volatile BufferedReader m_bufferedReader		= null;

	public BasicFileReader()
	{
	}

	public BasicFileReader(Object source) throws BasicException
	{
		if (source instanceof URL)
		{
			openFile((URL) source);
		}
		else if (source instanceof String)
		{
			openFile((String) source);
		}
		else if (source instanceof File)
		{
			openFile((File) source);
		}
		else
		{
			throw new BasicException("Unknown source type %s", source.getClass().getName());
		}
	}

	public void openFile(URL url) throws BasicException
	{
		openStream(url);

		m_inputReader = new InputStreamReader(m_inputStream);
		m_bufferedReader = new BufferedReader(m_inputReader);
	}
	public void openStream(URL url) throws BasicException
	{
		// Make sure there is not a leaked opened file
		close();

		try
		{
			setFilename(url.getPath());
			m_inputStream = url.openStream();
		}
		catch (IOException exception)
		{
			throw new BasicException(exception,
					FAILED_TO_OPEN_FORMAT, url.toString());
		}
	}

	public void openGzipStream(URL url) throws BasicException
	{
		openStream(url);

		try
		{
			m_gzipInputStream = new GZIPInputStream(m_inputStream);
		}
		catch (IOException exception)
		{
			throw new BasicException(exception,
					FAILED_TO_OPEN_FORMAT, url.toString());
		}
	}

	public void openFile(String filename) throws BasicException
	{
		openStream(filename);
		m_inputReader = new InputStreamReader(m_inputStream);
		m_bufferedReader = new BufferedReader(m_inputReader);
	}

	public void openStream(String filename) throws BasicException
	{
		// Make sure there is not a leaked opened file
		close();

		try
		{
			setFilename(filename);
			m_inputStream = new FileInputStream(filename);
		}
		catch (IOException exception)
		{
			throw new BasicException(exception,
					FAILED_TO_OPEN_FORMAT, filename);
		}
	}

	public void openGzipStream(String filename) throws BasicException
	{
		openStream(filename);
		try
		{
			m_gzipInputStream = new GZIPInputStream(m_inputStream);
		}
		catch (IOException exception)
		{
			throw new BasicException(exception,
					FAILED_TO_OPEN_FORMAT, filename);
		}
	}

	public void openFile(File file) throws BasicException
	{
		openFile(file.getAbsolutePath());
	}
	public void openStream(File file) throws BasicException
	{
		openStream(file.getAbsolutePath());
	}

	public void attachStreamAsReader(InputStream inputStream)
	{
		// Note that the caller retains ownership (and responsibility to close) of the inputStream)
		m_inputReader = new InputStreamReader(inputStream);
		m_bufferedReader = new BufferedReader(m_inputReader);
	}

	public String readLine() throws IOException
	{
		String line;

		while (null != (line = readRawLine()))
		{
			if (line.startsWith("#") || line.isEmpty())
			{
				continue;
			}

			break;
		}

		return line;
	}

	public String readRawLine() throws IOException
	{
		String line;

		line = m_bufferedReader.readLine();
		incrementLineCount();

		return line;
	}

	public InputStream getInputStream()
	{
		return m_inputStream;
	}

	public InputStream getGzipInputStream()
	{
		return m_gzipInputStream;
	}

	@SuppressWarnings("UnusedDeclaration")
	public InputStreamReader getInputReader()
	{
		return m_inputReader;
	}

	@SuppressWarnings("UnusedDeclaration")
	public BufferedReader getBufferedReader()
	{
		return m_bufferedReader;
	}

	@SuppressWarnings("NonAtomicOperationOnVolatileField")
	public void close()
	{
		m_bufferedReader	= closeSafe(m_bufferedReader);
		m_inputReader		= closeSafe(m_inputReader);
		m_gzipInputStream	= closeSafe(m_gzipInputStream);
		m_inputStream		= closeSafe(m_inputStream);

		setFilename(null);
	}

	@SuppressWarnings("UnusedDeclaration")
	public static String readTextFile(String filename)
	{
		InputStream inputStream		= null;
		try
		{
			inputStream = new FileInputStream(filename);
			return readTextFile(inputStream);
		}
		catch (Exception exception)
		{
			throw new BasicRuntimeException(exception, "Reading file %s", filename);
		}
		finally
		{
			closeSafe(inputStream);
		}
	}
	public static String readTextFile(URL url)
	{
		long t1 = System.nanoTime();
		InputStream inputStream		= null;
		try
		{
			inputStream = url.openStream();
			return readTextFile(inputStream);
		}
		catch (Exception exception)
		{
			throw new BasicRuntimeException(exception, "Failed to read %s", url);
		}
		finally
		{
			closeSafe(inputStream);

			long t2 = System.nanoTime();
			getLogger().debug("Reading %s took %s", url, TimeUnits.NS.format(t2 - t1));
		}
	}
	public static String readTextFile(InputStream inputStream) throws IOException
	{
		StringBuilder sb			= new StringBuilder();

		byte[]					bytes		= new byte[4096];
		int 					cb;
		while (-1 != (cb = inputStream.read(bytes)))
		{
			sb.append(new String(bytes, 0, cb));

		}
		return sb.toString();
	}
	public static byte[] readFile(File file) throws IOException
	{
		try (InputStream inputStream = new FileInputStream(file))
		{
			return readInputStream(inputStream);
		}
	}
	public static byte[] readInputStream(InputStream inputStream) throws IOException
	{
		ByteArrayOutputStream outputStream		= new ByteArrayOutputStream();
		byte[]					byteArray			= new byte[10 * 1024];
//		int						cbGuess				= inputStream.available();
		int						cb;
		while (-1 != (cb = inputStream.read(byteArray)))
		{
			outputStream.write(byteArray, 0, cb);
		}
		outputStream.flush();
		byte[]		outputBytes		= outputStream.toByteArray();
		outputStream.close();

		return outputBytes;
	}

	public static Properties readPropertiesFile(URL url) throws IOException
	{
		InputStream inputStream		= null;

		try
		{
			inputStream		= url.openStream();
			return readPropertiesFile(inputStream);
		}
		finally
		{
			closeSafe(inputStream);
		}
	}

	public static Properties readPropertiesFile(File file) throws IOException
	{
		InputStream inputStream		= null;
		try
		{
			inputStream = new FileInputStream(file);
			return readPropertiesFile(inputStream);
		}
		finally
		{
			closeSafe(inputStream);
		}
	}

	public static Properties readPropertiesFile(String filename) throws IOException
	{
		InputStream inputStream		= null;
		try
		{
			inputStream = new FileInputStream(filename);
			return readPropertiesFile(inputStream);
		}
		finally
		{
			closeSafe(inputStream);
		}
	}
	public static Properties readPropertiesFile(InputStream inputStream) throws IOException
	{
		Properties properties				= new Properties();

		properties.load(inputStream);

		return properties;
	}

	public static <T> T readJsonFile(URL url, Class<T> clazz) throws IOException
	{
		InputStream inputStream		= null;

		try
		{
			inputStream		= url.openStream();
			return readJsonFile(inputStream, clazz);
		}
		finally
		{
			closeSafe(inputStream);
		}
	}

	public static <T> T readJsonFile(File file, Class<T> clazz) throws IOException
	{
		InputStream inputStream		= null;
		try
		{
			inputStream = new FileInputStream(file);
			return readJsonFile(inputStream, clazz);
		}
		finally
		{
			closeSafe(inputStream);
		}
	}

	public static <T> T readJsonFile(String filename, Class<T> clazz) throws IOException
	{
		InputStream inputStream		= null;
		try
		{
			inputStream = new FileInputStream(filename);
			return readJsonFile(inputStream, clazz);
		}
		finally
		{
			closeSafe(inputStream);
		}
	}
	public static <T> T readJsonFile(InputStream inputStream, Class<T> clazz) throws IOException
	{
		String	text		= readTextFile(inputStream);
		T		object		= getGson().fromJson(text, clazz);

		return object;
	}

	public static Map<String, String> convertToMap(Properties properties)
	{
		Map<String, String>		map		= new TreeMap<>();
		for (Map.Entry<Object, Object> entry : properties.entrySet())
		{
			map.put(entry.getKey().toString(), entry.getValue().toString());
		}
		return map;
	}

	@Override
	public String toString()
	{
		return String.format("%s(%s)", getClass().getSimpleName(), getFilename());
	}
}
