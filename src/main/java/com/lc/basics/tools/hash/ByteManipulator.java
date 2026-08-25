/*
 * Copyright (c) 2015 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.hash;

public class ByteManipulator
{
	private static final int		LONG_SIZE_IN_BYTE		= Long.SIZE / Byte.SIZE;

	public static long getLong(byte[] byteList, int offset)
	{
		long		value		= 0;
		for (int i = 0; i < LONG_SIZE_IN_BYTE; i++)
		{
			long		byteValue		= 0xff & ((long) byteList[i + offset]);
			byteValue = byteValue << (i * 8);
			value += byteValue;
		}
		return value;
	}
}
