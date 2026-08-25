/*
 * Copyright (c) 2020 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.file;

import com.lc.basics.tools.misc.BasicException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CsvFile
{
	private final String[]				m_nameList;
	private final List<String[]>		m_valueList		= new ArrayList<>();

	public CsvFile(BasicFileReader reader) throws Exception
	{
		this(reader, null);
	}
	public CsvFile(BasicFileReader reader, String[] nameList) throws Exception
	{
		String				line		= reader.readRawLine();
		if (null == line)
		{
			throw new RuntimeException("File is empty");
		}

//		getRecordFromLine(line);
		m_nameList = null == nameList ? getRecordFromLine(line) : nameList;
		if (1 == m_nameList.length && "{".equals(m_nameList[0]))
		{
			throw new RuntimeException("Throttling exceeded");
		}

		while (null != (line = reader.readLine()))
		{
			if (line.contains("API call frequency"))
			{
				throw new RuntimeException("Throttling exceeded");
			}
			String[]		value		= getRecordFromLine(line);
			m_valueList.add(value);
			if (Integer.MAX_VALUE < m_valueList.size())
			{
				break;
			}
		}
	}
	public static String[] getRecordFromLine(String line)
	{
		boolean				isInsideQuote		= false;
		StringBuilder		sb					= new StringBuilder(line.length());
		for (char ch : line.toCharArray())
		{
			switch (ch)
			{
			case '"':
				isInsideQuote = !isInsideQuote;
				break;
			case ',':
				sb.append(isInsideQuote ? ';' : ',');
				break;
			default:
				sb.append(ch);
			}
		}

		String[]		parts		= sb.toString().split(",");
		return parts;
	}

	public int size()
	{
		return m_valueList.size();
	}
	public int getColumnCount()
	{
		return m_nameList.length;
	}

	public String get(int row, String name)
	{
		int		column		= getColumnIndex(name);
		if (-1 == column)
		{
			return null;
		}
		return get(row, column);
	}
	public String get(int row, int column)
	{
		String[]		valueList	= m_valueList.get(row);
		String			value		= column < valueList.length ? valueList[column] : null;
		if (null == value || value.isEmpty() || "null".equals(value))
		{
			value = null;
		}
		return value;
	}

	public Map<String, String> getRowAsMap(int iRow)
	{
		Map<String, String> map = new TreeMap<>();
		for (int iCol = 0; iCol < getColumnCount(); iCol++)
		{
			map.put(getColumnName(iCol), get(iRow, iCol));
		}
		return map;
	}

	public int getColumnIndex(String name)
	{
		for (int i = 0; i < m_nameList.length; i++)
		{
			if (name.equals(m_nameList[i]))
			{
				return i;
			}
		}
		return -1;
	}
	public String[] getColumnNameList()
	{
		return m_nameList;
	}

	public String getColumnName(int i)
	{
		return m_nameList[i];
	}

	public void cleanup()
	{
		for (int i = 0; i < m_nameList.length; i++)
		{
			m_nameList[i] = m_nameList[i].replaceAll("\"", "");
		}

		for (String[] values : m_valueList)
		{
			for (int i = 0; i < values.length; i++)
			{
				values[i] = values[i].replaceAll("\"", "");
			}
		}
	}

	public static void fixWrappedLines(String filenameIn, int colCount) throws Exception
	{
		String		filenameOut		= filenameIn.replace("Original", "Fixed");
		try (BasicFileReader reader = new BasicFileReader())
		{
			reader.openFile(filenameIn);
			try (BasicFileWriter writer = new BasicFileWriter(filenameOut))
			{
				String		line		= "";
				String		text;
				while (null != (text = reader.readLine()))
				{
					line += text;
					String[]		parts		= getRecordFromLine(line);
					if (parts.length == colCount)
					{
						writer.println(line);
						line = "";
					}
					else if (parts.length > colCount)
					{
						throw new BasicException("Line %,d is now too long at ", reader.getLineCount(), line);
					}
				}
			}
		}
	}
}
