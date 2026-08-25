/**
 * Copyright (c) 2024 LogicielCote.COM All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.file;

import com.lc.basics.tools.logging.BasicLogger;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassFinder
{
	private static final BasicLogger LOGGER = BasicLogger.getLogger(ClassFinder.class);
	private static final Set<String> FILENAME_SET = new TreeSet<>();
	private static final Comparator<Class<?>> CLASS_COMPARATOR = Comparator.comparing(Class::getName);
	private static final Set<Class<?>> CLASS_SET = new TreeSet<>(CLASS_COMPARATOR);

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Set<Class> findClassSetBelow(Class clazz) throws Exception
	{
		Package pkg = clazz.getPackage();
		String resourceName = clazz.getName().replace('.', '/') + ".class";
		URL url = ClassLoader.getSystemResource(resourceName);
		String rootFolder = url.getFile().replace("/" + clazz.getSimpleName() + ".class", "");
		Set<File> fileSet = FileFinder
				.getFilenameSet(rootFolder, new SimpleFilenameFilter(null, null, null));
		Set<Class> classSet = new TreeSet(CLASS_COMPARATOR);
		for (File file : fileSet)
		{
			String nameSuffix = file.getAbsolutePath()
					.substring(rootFolder.length() + 1)
					.replaceAll("/", ".")
					.replace(".class", "");
			String className = pkg.getName() + "." + nameSuffix;
			classSet.add(ClassLoader.getSystemClassLoader().loadClass(className));
		}
		return classSet;
	}
	@SuppressWarnings("unchecked")
	public static <T> Set<Class<? extends T>> findClassSet(Class<T> extendedClazz) throws Exception
	{
		Set<Class<? extends T>> set = new TreeSet<>(CLASS_COMPARATOR);
		for (Class<?> clazz : findClassSet())
		{
			if (extendedClazz.isAssignableFrom(clazz))
			{
				set.add((Class<? extends T>) clazz);
			}
		}
		return set;
	}
	public static Set<Class<?>> findClassSet() throws Exception
	{
		if (CLASS_SET.isEmpty())
		{
			CLASS_SET.addAll(findClassSetInternal());
		}
		return CLASS_SET;
	}
	public static Set<Class<?>> findClassSetInternal() throws Exception
	{
		getLogger().info("Looking for all classes in the classpath");
		Set<String> filenameSet = findFileSet(new SimpleFilter(null, null, ".class"));
		Set<Class<?>> classSet = new TreeSet<>(Comparator.comparing(Class::getName));
		for (String filename : filenameSet)
		{
			try
			{
				String className = toClassName(filename);
				if (null != className)
				{
					Class<?> clazz = ClassFinder.class.getClassLoader().loadClass(className);
					classSet.add(clazz);
				}
			}
			catch (Throwable exception)
			{
				// Not a class
			}
		}
		getLogger().info("Found %,d classes in the classpath", classSet.size());
		return classSet;
	}
	private static String toClassName(String filename)
	{
		int beginIndex;
		if (filename.contains("!"))
		{
			beginIndex = filename.lastIndexOf('!') + 2;
		}
		else if (filename.contains("/classes/"))
		{
			beginIndex = filename.lastIndexOf("/classes/") + 9;
		}
		else
		{
			beginIndex = 0;
		}
		int endIndex = filename.length() - ".class".length();
		if (-1 == beginIndex || -1 == endIndex)
		{
			return null;
		}
		String rawClassName = filename.substring(beginIndex, endIndex);
		String className = rawClassName.replaceAll("/", ".");
		return className;
	}

	public static Set<String> findFileSet() throws Exception
	{
		if (FILENAME_SET.isEmpty())
		{
			FILENAME_SET.addAll(findFileSetInternal());
		}
		return FILENAME_SET;
	}
	public static Set<String> findFileSet(SimpleFilter filter) throws Exception
	{
		Set<String> rawSet = findFileSet();
		Set<String> filtered = rawSet.stream().filter(filter::accept).collect(Collectors.toSet());
		return filtered;
	}
	private static Set<String> findFileSetInternal() throws Exception
	{
		getLogger().info("Looking for all files in the classpath");
		String fullPath = System.getProperty("java.class.path");
		String[] pathList = fullPath.split(":");

		Set<String> filenameSet = new TreeSet<>();
		for  (String path : pathList)
		{
			Set<String> set = findFileSet(path);
			filenameSet.addAll(set);
		}
		getLogger().info("Found %,d files in the classpath", filenameSet.size());

		return filenameSet;
	}
	public static Set<String> findFileSet(String rootFilename) throws Exception
	{
		Set<String> set = new TreeSet<>();
		File file = new File(rootFilename);
		if (rootFilename.endsWith(".jar"))
		{
			Collection<String> collection = findFilesInJar(file.getAbsolutePath());
			set.addAll(collection);
		}
		else if (file.isFile())
		{
			set.add(file.getAbsolutePath());
		}
		else if (file.isDirectory())
		{
			File[] fileList = file.listFiles(pathname -> true);
			if (null != fileList)
			{
				for (File subFile : fileList)
				{
					Collection<String> childrenList = findFileSet(subFile.getAbsolutePath());
					set.addAll(childrenList);
				}
			}
		}
		return set;
	}
	private static Set<String> findFilesInJar(String jarFilename) throws Exception
	{
		Set<String>			filenameSet		= new TreeSet<>();
		URL					jarUrl			= new URL("file://" + jarFilename);
		JarEntry jarEntry		= null;
		try (ZipFile zipFile = new ZipFile(jarFilename))
		{
			Set<String> set = findFilesInZip(zipFile);
			filenameSet.addAll(set);
		}
		return filenameSet;
	}
	private static Set<String> findFilesInZip(ZipFile zipFile) throws Exception
	{
		Set<String> filenameSet = new TreeSet<>();

		Enumeration<? extends ZipEntry> entryList = zipFile.entries();
		while (entryList.hasMoreElements())
		{
			ZipEntry entry = entryList.nextElement();
			String entryName = entry.getName();
			if (entryName.endsWith(".jar"))
			{
				Collection<String> collection = findFilesInZip(zipFile, entry);
				filenameSet.addAll(collection);
			}
			else
			{
				String filename = zipFile.getName() + "!/" + entryName;
				filenameSet.add(filename);
			}
		}
		return filenameSet;
	}
	private static Set<String> findFilesInZip(ZipFile zipFile, ZipEntry zipEntry) throws Exception
	{
		Set<String> filenameSet = new TreeSet<>();
		try (InputStream inputStream = zipFile.getInputStream(zipEntry))
		{
			try (JarInputStream jarInputStream = new JarInputStream(inputStream))
			{
				ZipEntry entry;
				while (null != (entry = jarInputStream.getNextJarEntry()))
				{
					String entryName = entry.getName();
					if (entryName.endsWith(".jar"))
					{
						System.out.println("InnerInner");
					}
					else
					{
						String filename = zipFile.getName() + "!/" + entryName;
						filenameSet.add(filename);
					}
				}
			}
		}
		return filenameSet;
	}





	public static class SimpleFilter
	{
		private String m_prefix;
		private String m_contains;
		private String m_suffix;

		public SimpleFilter(String prefix, String contains, String suffix)
		{
			m_prefix = prefix;
			m_contains = contains;
			m_suffix = suffix;
		}

		public boolean accept(String text)
		{
			boolean		isAccept		= true;

			if (null != m_prefix)
			{
				isAccept = text.startsWith(m_prefix);
			}
			if (null != m_contains)
			{
				isAccept &= text.contains(m_contains);
			}
			if (null != m_suffix)
			{
				isAccept &= text.endsWith(m_suffix);
			}
			return isAccept;
		}

		@Override
		public String toString()
		{
			return String.format("(%s,%s,%s)", m_prefix, m_contains, m_suffix);
		}
	}
}
