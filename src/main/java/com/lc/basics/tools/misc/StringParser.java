/*
 * Copyright (c) 2012 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */
/*
 *	StringParser.java
 *	Copyright (c) 2007 La Famille Cote All rights reserved
 */
package com.lc.basics.tools.misc;

/**
* Utility class to parse strings.
* @author Pierre
*
*/
public class StringParser
{
	private String m_oiriginal;
	private String  m_str;

	public StringParser(String str)
	{
		m_oiriginal = str;
		m_str = str;
	}
	public void reset()
	{
		m_str = m_oiriginal;
	}

	public String getString()
	{
		return m_str;
	}

	public String skipLine()
	{
		return skipTo("\n");
	}

	public String skip(int n)
	{
		int		len		= m_str.length();
		String	temp;
		if (n >= len)
		{
			temp	= m_str;
			m_str = "";
		}
		else
		{
			temp = m_str.substring(0, n);
			m_str = m_str.substring(n);
		}
		return temp;
	}

	public char spyNextChar()
	{
		return m_str.charAt(0);
	}
	public String spyNextLine()
	{
		int i = m_str.indexOf('\n');
		return i == -1
				? m_str
				: m_str.substring(0, i);
	}

	public String skipTo(String to)
	{
		String		  temp;

		int			 i	   = m_str.indexOf(to);
		if (-1 == i)
		{
			temp = m_str;
			m_str = "";
		}
		else
		{
			temp = m_str.substring(0, i);
			m_str = m_str.substring(i + to.length());
		}

		return temp;
	}

	public void skipWhite()
	{
		int		index		= 0;
		while (index < m_str.length() && Character.isWhitespace(m_str.charAt(index)))
		{
			index++;
		}
		m_str = m_str.substring(index);
	}

	public String extract(String prefix, String suffix, String ... replaceAll)
	{
		skipTo(prefix);
		if (isEmpty())
		{
			return null;
		}
		String text = skipTo(suffix);
		for (String replace : replaceAll)
		{
			text = text.replaceAll(replace, "");
		}
		return text.strip();
	}

	public void cleanup()
	{
		m_str = m_str.replaceAll("\r", "\n");
		m_str = m_str.replaceAll("\n\n", "\n");
		m_str = m_str.replaceAll("\n\n", "\n");
	}

	public boolean isEmpty()
	{
		return "".equals(m_str);
	}

	@Override
	public String toString()
	{
		return m_str;
	}
}
