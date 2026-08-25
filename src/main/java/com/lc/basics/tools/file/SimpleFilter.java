/*
 * Copyright (c) 2013 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.function.Function;

public class SimpleFilter implements FilenameFilter
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

	public SimpleFilter(SimpleFilter filter, Function<String, String> modifier)
	{
		m_prefix = null == filter.m_prefix ? null : modifier.apply(filter.m_prefix);
		m_contains = null == filter.m_contains ? null : modifier.apply(filter.m_contains);
		m_suffix = null == filter.m_suffix ? null : modifier.apply(filter.m_suffix);
	}

	public boolean accept(File dir, String name)
	{
		boolean		isAccept		= true;

		if (null != m_prefix)
		{
			isAccept = name.startsWith(m_prefix);
		}
		if (null != m_contains)
		{
			isAccept &= name.contains(m_contains);
		}
		if (null != m_suffix)
		{
			isAccept &= name.endsWith(m_suffix);
		}
		return isAccept;
	}

	@Override
	public String toString()
	{
		return String.format("(%s,%s,%s)", m_prefix, m_contains, m_suffix);
	}
}
