/*
 * Copyright (c) 2015 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.hash;

public class JavaHash32 extends HashProvider
{
	private int		m_hashCode;

	public JavaHash32()
	{
		super(HashSize.Bits32);
		reset();
	}

	@Override
	public void reset()
	{
		m_hashCode = 1;
	}

	@Override
	public void add(byte[] byteList)
	{
		add(byteList, 0, byteList.length);
	}
	@Override
	public void add(byte[] byteList, int indexMin, int length)
	{
		// Iterate manually to avoid allocation of iterator
		for (int i = indexMin; i < indexMin + length; i++)
		{
			byte element = byteList[i];
			m_hashCode	= 31 * m_hashCode + element;
		}
	}

	@Override
	public void add(int n)
	{
		m_hashCode	= 31 * m_hashCode + n;
	}

	@Override
	public void add(long n)
	{
		add((int) n);
		add((int) (n >> 32));
	}

	@Override
	public int getHashCode32()
	{
		return m_hashCode;
	}

	@Override
	public long getHashCode64()
	{
		return m_hashCode;
	}

	@Override
	public long getHashCode128()
	{
		return getHashCode32();
	}
}
