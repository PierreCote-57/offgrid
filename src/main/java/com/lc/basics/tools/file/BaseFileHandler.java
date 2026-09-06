/*
 * Copyright (c) 2013 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.basics.tools.time.WallClock;
import com.lc.basics.tools.time.WallClock.FormatDate;
import com.lc.basics.tools.time.WallClock.FormatTime;
import com.lc.offgrid.webapp.spring.tools.BaseWebProcessor;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

public class BaseFileHandler
{
	private static final BasicLogger LOGGER = BasicLogger.getLogger(BaseFileHandler.class);
	public static final String	LINE_SEPARATOR					= System.lineSeparator();
	public static final String	FOLDER_SEPARATOR				= FileSystems.getDefault().getSeparator();

	private static final Gson	GSON							=
			new GsonBuilder().setPrettyPrinting()
					.disableHtmlEscaping()
					.registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
					.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
					.create();

	public static final String	SETTING_PREFIX					= BaseFileHandler.class.getName();
	public static final String	SETTING_NAME_WORKING_FOLDER		= SETTING_PREFIX + ".WorkingFolder";

	private static String s_currentFolderName				= System.getProperty("user.home") + "/Working";

	private String		m_filename				= null;
	private int			m_lineCount				= 0;

	public static String getCurrentFolderName()
	{
		return s_currentFolderName;
	}

	public static void setCurrentFolderName(String currentFolderName)
	{
		s_currentFolderName = currentFolderName;
	}

	public static Gson getGson()
	{
		return GSON;
	}
	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	public static <T> T readFile(String path, Class<T> clazz)
	{
		try
		{
			T obj = BasicFileReader.readJsonFile(path, clazz);
			return obj;
		}
		catch (Exception e)
		{
			// Fall through, try something else
		}

		URL url = BaseWebProcessor.class.getClassLoader().getResource(path);
		try
		{
			T obj = BasicFileReader.readJsonFile(url, clazz);
			return obj;
		}
		catch (Exception e)
		{
			getLogger().error("Error reading file: " + path, e);
			throw new BasicRuntimeException("Error reading file: " + path, e);
		}
	}

	public String getFilename()
	{
		return m_filename;
	}

	public void setFilename(String filename)
	{
		m_filename = filename;
	}

	public boolean isClosed()
	{
		return null == m_filename;
	}

	public void incrementLineCount()
	{
		++m_lineCount;
	}

	public int getLineCount()
	{
		return m_lineCount;
	}

	public static String getWorkingFolderName(String subFolderName)
	{
		String			rootWorkingFolder		= s_currentFolderName;
		StringBuilder	sb						= new StringBuilder(100);

		sb.append(rootWorkingFolder);
		sb.append(FOLDER_SEPARATOR);
		sb.append(subFolderName);

		return sb.toString();
	}

	public static String getWorkingFilename(String subFolderName, String filename)
	{
		String			rootWorkingFolder		= s_currentFolderName;
		StringBuilder	sb						= new StringBuilder(100);

		sb.append(rootWorkingFolder);
		sb.append(FOLDER_SEPARATOR);
		if (null != subFolderName)
		{
			sb.append(subFolderName);
			sb.append(FOLDER_SEPARATOR);
		}
		sb.append(filename);

		return sb.toString();
	}

	public static boolean ensureFolder(String filename)
	{
		File		file		= new File(filename);
		String		parentName	= file.getParent();
		if (null != parentName)
		{
			File parentFile	= new File(parentName);
			return parentFile.mkdirs();
		}
		else
		{
			return true;
		}
	}
	@SuppressWarnings("unchecked")
	public static boolean ensureCleanFolder(String filename)
	{
		return ensureCleanFolder(filename, Collections.EMPTY_SET);
	}
	public static boolean ensureCleanFolder(String filename, Set<String> excludeSet)
	{
		boolean		isSuccess	= true;
		File		file		= new File(filename);
		String		parentName	= file.isDirectory() ? filename : file.getParent();
		if (null != parentName)
		{
			File		parentFile	= new File(parentName);
			if (parentFile.exists())
			{
				File[]		files		= parentFile.listFiles();
				if (null != files)
				{
					for (File child : files)
					{
						if (child.isFile())
						{
							if (!excludeSet.contains(child.getAbsolutePath()))
							{
								isSuccess &= child.delete();
							}
						}
						else	// isDirectory
						{
							isSuccess &= ensureCleanFolder(child.getAbsolutePath(), excludeSet);
							child.delete();
						}
					}
				}
			}
		}
		return isSuccess;
	}

	/**
	 * Utility method to safely close any Closeable, null or not, catching and ignoring any exception.
	 *
	 * @param closeable		Whatever needs to be closed
	 * @return				null that can be assigned to the now closed closeable variable, in case you need it.
	 */
	public static <T extends Closeable> T closeSafe(T closeable)
	{
		if (null != closeable)
		{
			try
			{
				closeable.close();
			}
			catch (IOException e)
			{
				// Ignore errors on close
			}
		}

		return null;
	}

	public static long getFileSize(String filename)
	{
		File		file		= new File(filename);
		return getFileSize(file);
	}
	public static long getFileSize(File file)
	{
		Path		path		= file.toPath();
		return getFileSize(path);
	}
	public static long getFileSize(Path path)
	{
		try
		{
			return Files.size(path);
		}
		catch (IOException exception)
		{
			return 0;
		}
	}

	public static long getLastModified(String filename)
	{
		File		file		= new File(filename);
		return getLastModified(file);
	}
	public static long getLastModified(File file)
	{
		Path		path		= file.toPath();
		return getLastModified(path);
	}
	public static long getLastModified(Path path)
	{
		try
		{
			FileTime		fileTime	= Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS);
			return fileTime.toMillis();
		}
		catch (IOException exception)
		{
			return 0;
		}
	}

	public static void backup(File file) throws Exception
	{
		String		filename	= file.getAbsolutePath();
		int			index		= filename.lastIndexOf('/');
		String		prefix		= filename.substring(0, index);
		String		suffix		= filename.substring(index + 1);

		String		dateText	= WallClock.formatTime(FormatDate.INTL, FormatTime.HMS, System.currentTimeMillis());
		dateText = dateText.replaceAll("-", ".").replaceAll(":", ".").replaceAll(" ", "-");
		String		nameSav		= String.format("%s/sav/%s-%s", prefix, dateText, suffix);

		byte[] bytes = BasicFileReader.readFile(file);
		BasicFileWriter.write(bytes, nameSav);
	}

	public static String extractExtension(String filename)
	{
		int		index		= filename.lastIndexOf('.');
		if (-1 == index)
		{
			return null;
		}
		else
		{
			return filename.substring(index + 1);
		}
	}
	public static String extractName(String filename)
	{
		int		index		= filename.lastIndexOf('.');
		if (-1 == index)
		{
			return filename;
		}
		else
		{
			return filename.substring(0, index);
		}
	}
}
