/*
 * Copyright (c) 2013-2020 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.file;

import com.lc.basics.tools.logging.BasicLogger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


public class UrlFinder
{
	private static final BasicLogger LOGGER			= BasicLogger.getLogger(UrlFinder.class);
	private static final ClassLoader		CLASS_LOADER	= UrlFinder.class.getClassLoader();
	private static final Comparator<URL>	URL_COMPARATOR	= new UrlComparator();

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	public static ClassLoader getClassLoader()
	{
		return CLASS_LOADER;
	}

	public static URL findUrlOnClasspath(String shortName)
	{
		URL url				= getClassLoader().getResource(shortName);

		return url;
	}

	@SuppressWarnings("UrlHashCode")
	public static Set<URL> getFileUriList(String name) throws Exception
	{
		Enumeration<URL>	enumeration		= getClassLoader().getResources(name);
		Set<URL>			list			= new TreeSet<>(URL_COMPARATOR);
		while (enumeration.hasMoreElements())
		{
			list.add(enumeration.nextElement());
		}
		return list;
	}

	@SuppressWarnings("UrlHashCode")
	public static Set<URL> getFileUriListRecursive(String name, FilenameFilter filter) throws Exception
	{
		getLogger().debug("Looking for files in folder: " + name);

		Set<URL>		set		= new TreeSet<>(URL_COMPARATOR);
		Set<URL>		list	= getFileUriList(name);
		for (URL url : list)
		{
			String		urlText		= url.toString();
			if (urlText.startsWith("file:"))
			{
				// A file on a file system
				getChildrenOfFile(name, url, set);
			}
			else if (urlText.startsWith("jar:"))
			{
				getChildrenOfJar(name, url, set);
			}
		}

		Iterator<URL>		iterator		= set.iterator();
		while (iterator.hasNext())
		{
			URL			url			= iterator.next();
			String		urlName		= url.getFile();
			int			index		= urlName.lastIndexOf(BaseFileHandler.FOLDER_SEPARATOR);
			if (-1 == index)
			{
				index = urlName.lastIndexOf('/');	// e.g. for http URLs on Windows
			}
			if (-1 == index)
			{
				index = 0;
			}
			String		dir			= urlName.substring(0, index);
			String		filename	= urlName.substring(index + 1);
			if (null != filter && !filter.accept(new File(dir), filename))
			{
				iterator.remove();
			}
		}

		return set;
	}


	// Recursive, to get all the subfolders of the root folder.
	private static void getChildrenOfFile(String name, URL url, Collection<URL> masterList) throws IOException
	{
		getLogger().debug("Looking into folder: " + url.toString());
		String filename		= url.getPath();
		File file			= new File(filename);
		File[]		childrenList	= file.listFiles();
		if (null != childrenList)
		{
			for (File childFile : childrenList)
			{
				if (childFile.isFile())
				{
					getLogger().debug("Found filesystem file: " + childFile.getPath());
					masterList.add(childFile.toURI().toURL());
				}
				else if (childFile.isDirectory())
				{
					getLogger().debug("Found folder " + childFile.getPath());
					String filenameSuffix		= childFile.getPath().replace(filename, "");
					String nameChild			= name + filenameSuffix;
					getChildrenOfFile(nameChild, childFile.toURI().toURL(), masterList);
				}
			}
		}
	}

	// This method is not recursive, as the method jarInputStream.getNextJarEntry() goes through ALL entries in the jar
	// At least, it only goes through the jars that have the proper starting name.
	private static void getChildrenOfJar(String name, URL url, Collection<URL> masterList) throws IOException
	{
		int						sizeBefore		= masterList.size();

		String					jarText			= url.toString();
		String					jarFilename		= jarText;
		String					prefixToRemove	= null;
		while (!jarFilename.endsWith(".jar"))
		{
			String		target		= ".jar!/";
			int			index		= jarFilename.lastIndexOf(target);
			String		suffix		= jarFilename.substring(index + target.length());
			if (suffix.equals(name))
			{
				prefixToRemove = null;
			}
			else if (suffix.endsWith(name))
			{
				suffix = suffix.substring(0, suffix.length() - name.length() - 2);
				prefixToRemove = suffix + "/";
			}
			jarFilename = jarFilename.substring(0, index + 4);
		}
		if (!jarFilename.contains("!/"))
		{
			jarFilename = jarFilename.substring(4);
		}
		getLogger().debug("Looking into\n%s\n%s ", url.toString(), jarFilename);
		URL						jarUrl			= new URL(jarFilename);
		JarEntry				jarEntry		= null;
		try (InputStream inputStream = jarUrl.openStream(); JarInputStream jarInputStream = new JarInputStream(inputStream))
		{
			while (null != (jarEntry = jarInputStream.getNextJarEntry()))
			{
				// This lists ALL the files in the jar!
				String urlText = String.format("%s:%s!/%s",
						url.getProtocol(), jarFilename, jarEntry.getName());
				urlText = urlText.replace("jar:jar:", "jar:");

				String entryName = jarEntry.getName();
				if (null != prefixToRemove && entryName.startsWith(prefixToRemove))
				{
					entryName = entryName.substring(prefixToRemove.length());
				}
				if (entryName.startsWith("BOOT-INF/classes/"))
				{
					entryName = entryName.substring("BOOT-INF/classes/".length());
				}
				if (entryName.startsWith(name))
				{
					getLogger().debug("Found file in jar: " + jarEntry.getName());
					masterList.add(new URL(urlText));
				}
				else if (entryName.endsWith(".jar"))
				{
					getChildrenOfJar(name, new URL(urlText), masterList);
				}
			}
		} catch (Exception exception)
		{
			String jarEntryName = (null == jarEntry) ? "null" : jarEntry.getName();
			getLogger().error(exception,
					"Problem reading jar %s entry %s", url.toString(), jarEntryName);
		}
		getLogger().debug("Added %,+5d entries", masterList.size() - sizeBefore);
	}





	private static final class UrlComparator implements Comparator<URL>
	{

		/**
		 * Compares its two arguments for order.  Returns a negative integer,
		 * zero, or a positive integer as the first argument is less than, equal
		 * to, or greater than the second.<p>
		 * <p>
		 * In the foregoing description, the notation
		 * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
		 * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
		 * <tt>0</tt>, or <tt>1</tt> according to whether the value of
		 * <i>expression</i> is negative, zero or positive.<p>
		 * <p>
		 * The implementor must ensure that <tt>sgn(compare(x, y)) ==
		 * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
		 * implies that <tt>compare(x, y)</tt> must throw an exception if and only
		 * if <tt>compare(y, x)</tt> throws an exception.)<p>
		 * <p>
		 * The implementor must also ensure that the relation is transitive:
		 * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
		 * <tt>compare(x, z)&gt;0</tt>.<p>
		 * <p>
		 * Finally, the implementor must ensure that <tt>compare(x, y)==0</tt>
		 * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
		 * <tt>z</tt>.<p>
		 * <p>
		 * It is generally the case, but <i>not</i> strictly required that
		 * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
		 * any comparator that violates this condition should clearly indicate
		 * this fact.  The recommended language is "Note: this comparator
		 * imposes orderings that are inconsistent with equals."
		 *
		 * @param o1 the first object to be compared.
		 * @param o2 the second object to be compared.
		 * @return a negative integer, zero, or a positive integer as the
		 * first argument is less than, equal to, or greater than the
		 * second.
		 * @throws NullPointerException if an argument is null and this
		 *                              comparator does not permit null arguments
		 * @throws ClassCastException   if the arguments' types prevent them from
		 *                              being compared by this comparator.
		 */
		@Override
		public int compare(URL o1, URL o2)
		{
			return o1.getFile().compareTo(o2.getFile());
		}
	}
}
