/*
 * Copyright 2013-2020 LogicielCote.COM All rights reserved.
 */

package com.lc.basics.tools.file;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;


public class FileFinder
{
	private static ClassLoader s_classLoader		= FileFinder.class.getClassLoader();

	public static ClassLoader getClassLoader()
	{
		return s_classLoader;
	}

	public static File getUniqueFilename(String rootFolderName, FilenameFilter filter)
	{
		Set<File> set		= getFilenameSet(rootFolderName, filter);
		int				size	= set.size();
		if (1 == size)
		{
			return set.iterator().next();
		}
		else if (0 == size)
		{
			throw new IllegalStateException(
					String.format("File not found: %s/%s", rootFolderName, filter));
		}
		else // if (1 < size)
		{
			StringBuilder sb			= new StringBuilder(1000);
			sb.append(String.format("Multiple files found for: %s/%s", rootFolderName, filter));
			for (File file : set)
			{
				sb.append(BaseFileHandler.LINE_SEPARATOR);
				sb.append(file.getAbsolutePath());
			}

			throw new IllegalStateException(sb.toString());
		}
	}

	public static Set<File> getFilenameSet(String rootFolderName, FilenameFilter filter)
	{
		Set<File> set		= new TreeSet<>();

		Path dir = FileSystems.getDefault().getPath(rootFolderName);
		getFilenameSet(dir.toFile(), filter, set);

		return set;
	}

	private static void getFilenameSet(File folder, FilenameFilter filter, Set<File> set)
	{
		if (!folder.isDirectory())
		{
			return;
		}
		File[]		childList		= folder.listFiles();
		if (null != childList)
		{
			for (File child : childList)
			{
				if (child.isFile())
				{
					if (null == filter || filter.accept(folder, child.getAbsolutePath()))
					{
						set.add(child);
					}
				}
				else	// if (child.isDirectory())
				{
					getFilenameSet(child, filter, set);
				}
			}
		}
	}
}
