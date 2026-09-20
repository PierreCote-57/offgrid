package com.lc.basics.tools.file;

public class SimpleFilterString
{
	private String m_prefix;
	private String m_contains;
	private String m_suffix;

	public SimpleFilterString(String prefix, String contains, String suffix)
	{
		m_prefix = prefix;
		m_contains = contains;
		m_suffix = suffix;
	}

	public boolean accept(String text)
	{
		boolean isAccept = true;

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
