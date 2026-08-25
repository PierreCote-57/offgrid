/*
 * Copyright (c) 2020 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.hash;

public class FnvHash64 extends HashProvider
{
	long		m_hash;

	protected FnvHash64()
	{
		super(HashSize.Bits64);
	}

	@Override
	public void reset()
	{
		// Nothing to do
	}

	@Override
	public void add(byte[] byteList)
	{
		m_hash = FnvHash.hash64(byteList);
	}

	@Override
	public void add(byte[] byteList, int indexMin, int length)
	{
		m_hash = FnvHash.hash64(byteList, indexMin, length);
	}

	@Override
	public void add(int n)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(long n)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getHashCode32()
	{
		return (int) m_hash;
	}

	@Override
	public long getHashCode64()
	{
		return m_hash;
	}

	@Override
	public long getHashCode128()
	{
		return getHashCode64();
	}
}
