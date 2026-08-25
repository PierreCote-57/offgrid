/*
 * Copyright (c) 2017 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.hash;


public abstract class HashProvider
{
	public enum HashSize
	{
		Bits32(32)
		{
			@Override
			public long getHashMax()
			{
				return Short.MAX_VALUE;
			}
			public long getHashA(HashProvider provider)
			{
				return provider.getHashCode32();
			}
			public long getHashB(HashProvider provider)
			{
				return provider.getHashCode32();
			}
			public long getHashC(HashProvider provider)
			{
				return provider.getHashCode32();
			}
		},
		Bits64(64)
		{
			@Override
			public long getHashMax()
			{
				return Integer.MAX_VALUE;
			}
			public long getHashA(HashProvider provider)
			{
				return provider.getHashCode64() & 0xFFFFFFFFL;
			}
			public long getHashB(HashProvider provider)
			{
				return (provider.getHashCode64() >> 32) & 0xFFFFFFFFL;
			}
			public long getHashC(HashProvider provider)
			{
				return getHashA(provider) ^ getHashB(provider);
			}
		},
		Bits128(128)
		{
			@Override
			public long getHashMax()
			{
				return Long.MAX_VALUE;
			}
			public long getHashA(HashProvider provider)
			{
				return provider.getHashCode64();
			}
			public long getHashB(HashProvider provider)
			{
				return provider.getHashCode128();
			}
			public long getHashC(HashProvider provider)
			{
				return provider.getHashCode64() ^ provider.getHashCode128();
			}
		};

		private final int m_size;

		HashSize(int size)
		{
			m_size = size;
		}

		public int getSize()
		{
			return m_size;
		}

		// Generates a set of 3 hashes compatible with Bucket/Bin/HashMap
		abstract public long getHashA(HashProvider provider);		// Bucket
		abstract public long getHashB(HashProvider provider);		// Bin
		abstract public long getHashC(HashProvider provider);		// HashMap
		abstract public long getHashMax();
	}

	private HashSize		m_hashSize;
	protected HashProvider(HashSize hashSize)
	{
		m_hashSize = hashSize;
	}

	public abstract void		reset();
	public abstract void		add(byte[] byteList);
	public abstract void		add(byte[] byteList, int indexMin, int length);
	public abstract void		add(int n);
	public abstract void		add(long n);

	public abstract int 		getHashCode32();
	public abstract long		getHashCode64();
	public abstract long		getHashCode128();

	public HashSize getHashSize()
	{
		return m_hashSize;
	}

	public long getHashMax()
	{
		return m_hashSize.getHashMax();
	}
}
