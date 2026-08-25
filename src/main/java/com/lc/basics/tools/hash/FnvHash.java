/*
 * Copyright (c) 2014 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.hash;

import java.util.Arrays;

/**
 * FNV Hash, as per http://www.isthe.com/chongo/tech/comp/fnv/#FNV-1a
 *
 * http://wiki.answers.com/Q/What_are_the_chances_of_being_hit_by_a_meteorite
 * 		What are the chances of being hit by a meteorite?
 * 			1 in 7,000,000,000 and you would not survive it (lifetime)
 * 			In any given second (at 2,365,200,000 seconds/75 years): 16.6 x 10^19
 *
 */
public abstract class FnvHash
{
//	private static final long 		OFFSET_32X		= 2166136261L;
	private static final int 		OFFSET_32		= 0x7FFFFFFF + 18652614;
	private static final int		PRIME_32		= 16777619;

	// Requires a manual subtraction
//	 2166136261		  Target
//	-2147483647		- 0x7FFF FFFF
//	   18652614		Number to add from 0x7FFF FFFF to obtain the offset

	// Requires a manual subtraction
//	 1469598103 9346656037	Target
//	- 922337203 6854775807L	0x7FFF...
//	  547260900 2491880230	Number to add to 0xFFF... to obtain the actual 64 bits offset

	private static final long		OFFSET_64		= 0x7FFFFFFFFFFFFFFFL + 5472609002491880230L;
	private static final long 		PRIME_64		= 1099511628211L;

	public static int hash32(final byte[] byteList)
	{
		// This function is much faster when doing the math using long (on 64 bits machines),
		// even if returning a int. The returned value is identical.
		long		hash		= OFFSET_32;

		for (byte b : byteList)
		{
			hash = hash ^ b;
			hash = hash * PRIME_32;
		}

		return (int) hash;
	}

	public static long hash64(final byte[] byteList)
	{
		return hash64(byteList, 0, byteList.length);
	}
	public static long hash64(final byte[] byteList, final int iMin, final int length)
	{
		long		hash		= OFFSET_64;

		for (int i = iMin; i < iMin + length; i++)
		{
			byte	b		= byteList[i];
			hash = hash ^ b;
			hash = hash * PRIME_64;
		}

		return hash;
	}

	/**
	 *
	 * @param args	Standard main arguments, ignored
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	public static void main(String[] args)
	{
		String hello		= "Hello world.";
		byte[]		helloBytes	= hello.getBytes();
		int			hash		= Arrays.hashCode(helloBytes);
		int			hash32		= hash32(helloBytes);
		long		hash64		= hash64(helloBytes);

		System.out.printf("The hash of '%s' is %d%n", hello, hash);
		System.out.printf("The hash32 of '%s' is %d%n", hello, hash32);
		System.out.printf("The hash64 of '%s' is %d%n", hello, hash64);
	}
}
