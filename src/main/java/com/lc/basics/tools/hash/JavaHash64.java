/*
 * Copyright (c) 2015 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.hash;

public class JavaHash64 extends HashProvider
{
	private long		m_hashCode;

	public JavaHash64()
	{
		super(HashSize.Bits64);
		reset();
	}

	@Override
	public void reset()
	{
		m_hashCode = 2147483659L; 	// == PrimeNumbers.nextPrime((long) Integer.MAX_VALUE);
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
		add((long) n);
	}

	@Override
	public void add(long n)
	{
		m_hashCode	= 31 * m_hashCode + n;
	}

	@Override
	public int getHashCode32()
	{
		return (int) m_hashCode;
	}

	@Override
	public long getHashCode64()
	{
		return m_hashCode;
	}

	@Override
	public long getHashCode128()
	{
		return getHashCode64();
	}
}
