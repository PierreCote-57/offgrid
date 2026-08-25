/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.misc;

import java.io.IOException;
import java.util.Base64;


/**
 * @author Pierre
 *
 * Stringizes byte arrays using the BASE64 algorithm
 *
 */
public abstract class BasicBase64
{
	static private final Base64.Encoder		s_encoder	= Base64.getEncoder();
	static private final Base64.Decoder		s_decoder	= Base64.getDecoder();

	/**
	 * Decodes the string into the original bytes
	 *
	 * @param str BasicBase64 encoded string to decode in to bytes.
	 * @return		The original byte[]
	 * @throws IOException  If something goes wrong with the decode.
	 */
	public static byte[] decode(String str)
	{
		return s_decoder.decode(str);
	}

	/**
	 * Perform the "correct" Base 64 encoding
	 *
	 * @param byteList  Bytes to encode into a string.
	 * @return	a BasicBase64 string
	 */
	public static String encode(byte[] byteList)
	{
		return s_encoder.encodeToString(byteList).replaceAll("\r", "").replaceAll("\n", "");
	}
}
