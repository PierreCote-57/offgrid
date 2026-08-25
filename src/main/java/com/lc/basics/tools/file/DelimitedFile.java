/*
 * Copyright (c) 2020 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class DelimitedFile<T extends DelimitedFile.DelimitedRow> implements Iterable<T>
{
	private final String					m_filename;
	private final String					m_delimiter;
	private DelimitedRow					m_headerRow;
	private final Map<String, Integer>		m_columnNameMap		= new TreeMap<>();
	private final List<T>					m_rowList			= new ArrayList<>();

	public DelimitedFile(String filename, String delimiter, BiFunction<DelimitedFile<T>, String, T> rowSupplier) throws Exception
	{
		m_filename = filename;
		m_delimiter = delimiter;

		read(rowSupplier);
	}

	public void read(BiFunction<DelimitedFile<T>, String, T> rowSupplier) throws Exception
	{
		try(BasicFileReader		reader		= new BasicFileReader())
		{
			reader.openFile(getFilename());
			String line = reader.readLine();
			m_headerRow = new DelimitedRow(this, line);
			for (int i = 0; i < m_headerRow.size(); i++)
			{
				m_columnNameMap.put(m_headerRow.getColumn(i), i);
			}

			while (null != (line = reader.readLine()))
			{
				T row = rowSupplier.apply(this, line);
				m_rowList.add(row);
			}
		}
	}

	public String getFilename()
	{
		return m_filename;
	}
	public String getDelimiter()
	{
		return m_delimiter;
	}
	public DelimitedRow getHeaderRow()
	{
		return m_headerRow;
	}
	public Integer getColumnIndex(String name)
	{
		return m_columnNameMap.get(name);
	}
	public int size()
	{
		return m_rowList.size();
	}

	/**
	 * Returns an iterator over elements of type {@code T}.
	 *
	 * @return an Iterator.
	 */
	@Override
	public Iterator<T> iterator()
	{
		return m_rowList.iterator();
	}

	/**
	 * Performs the given action for each element of the {@code Iterable}
	 * until all elements have been processed or the action throws an
	 * exception.  Unless otherwise specified by the implementing class,
	 * actions are performed in the order of iteration (if an iteration order
	 * is specified).  Exceptions thrown by the action are relayed to the
	 * caller.
	 *
	 * @param action The action to be performed for each element
	 * @throws NullPointerException if the specified action is null
	 * @implSpec <p>The default implementation behaves as if:
	 * <pre>{@code
	 *     for (T t : this)
	 *         action.accept(t);
	 * }</pre>
	 * @since 1.8
	 */
	@Override
	public void forEach(Consumer<? super T> action)
	{
		m_rowList.forEach(action);
	}

	public static class DelimitedRow
	{
		private final DelimitedFile<? extends DelimitedRow>		m_file;
		private String[]				m_columnList;

		public DelimitedRow(DelimitedFile<? extends DelimitedRow> file, String line)
		{
			m_file = file;

			if (null == line || line.isEmpty())
			{
				System.out.println("Boom");
			}

			while (!Character.isLetterOrDigit(line.charAt(0))
				&& line.charAt(0) != ',')
			{
				line = line.substring(1);
			}

			m_columnList = line.split(m_file.m_delimiter);
			int		iTo		= 0;
			for (int iFrom = 0; iFrom < m_columnList.length; iFrom++)
			{
				m_columnList[iTo] = m_columnList[iFrom].trim();
				if (m_columnList[iTo].startsWith("\""))
				{
					m_columnList[iTo] = m_columnList[iTo].substring(1);
					do
					{
						m_columnList[iTo] += "," + m_columnList[++iFrom];
					}
					while (!m_columnList[iFrom].endsWith("\""));
					m_columnList[iTo] = m_columnList[iTo].substring(0, m_columnList[iTo].length() - 1);
				}
				else if (m_columnList[iTo].endsWith("\""))
				{
					iTo--;
					m_columnList[iTo] += "," + m_columnList[iTo + 1];
					m_columnList[iTo] = m_columnList[iTo].substring(0, m_columnList[iTo].length() - 1);
				}
				iTo++;
			}
			m_columnList = Arrays.copyOf(m_columnList, iTo);
		}

		public int size()
		{
			return m_columnList.length;
		}
		public int getColumnIndex(String ... nameList)
		{
			for (String name : nameList)
			{
				Integer		index		= m_file.getColumnIndex(name);
				if (null != index)
				{
					return index;
				}
			}

			return -1;
		}
		public void setColumn(int index, String value)
		{
			m_columnList[index] = value;
		}
		public String getColumnName(int index)
		{
			return m_file.m_headerRow.getColumn(index);
		}
		public String getColumn(int index)
		{
			if (index < 0 || index >= m_columnList.length)
			{
				return null;
			}
			String		value		= m_columnList[index];
			value = value.replaceAll("\"", "");

			return value;
		}
		public String getColumn(String name)
		{
			return getColumn(m_file.getColumnIndex(name));
		}

		public int getColumnInt(int index)
		{
			String		column		= getColumn(index);
			if (null == column || column.isEmpty())
			{
				return 0;
			}
			else
			{
				return Integer.parseInt(column);
			}
		}
		public long getColumnLong(int index)
		{
			String		column		= getColumn(index);
			if (null == column || column.isEmpty())
			{
				return 0;
			}
			else
			{
				try
				{
					return Long.parseLong(column.replace(".0", ""));
				}
				catch (Exception exception)
				{
					return 0;
				}
			}
		}
		public double getColumnDouble(int index)
		{
			String		column		= getColumn(index);
			if (null == column || column.isEmpty())
			{
				return 0;
			}
			else
			{
				return Double.parseDouble(column);
			}
		}
	}

	public static class PlainRow extends DelimitedRow
	{
		public PlainRow(DelimitedFile<? extends DelimitedRow> file, String line)
		{
			super(file, line);
		}
	}
}
