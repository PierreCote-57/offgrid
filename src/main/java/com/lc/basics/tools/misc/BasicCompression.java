/*
 * Copyright (c) 2013-2019 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.misc;

import com.lc.basics.monitoring.counter.OperationContext;
import com.lc.basics.monitoring.counter.OperationCounter;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class BasicCompression
{
	public enum CompressType
	{
		none,				// No compression
		compressDefault,
		deflate
	}
	private static final BasicCompression		INSTANCE						= new BasicCompression();
	public static final CompressType[]			COMPRESS_TYPE_LIST				= CompressType.values();
	private static final int					COMPRESS_MIN_LENGTH				= 1234;

	private OperationCounter		m_counterDeflate;
	private OperationCounter		m_counterInflate;

	private BasicCompression()
	{
		m_counterDeflate = new OperationCounter(new String[] {BasicCompression.class.getSimpleName(), "Deflate"});
		m_counterInflate = new OperationCounter(new String[] {BasicCompression.class.getSimpleName(), "Inflate"});
	}
	public static int getCompressMinLength()
	{
		return COMPRESS_MIN_LENGTH;
	}

	public static byte[] compress(byte[] bytesIn) throws Exception
	{
		return compress(bytesIn, CompressType.compressDefault);
	}
	public static byte[] compress(byte[] bytesIn, CompressType compressType) throws Exception
	{
		return INSTANCE.compressInternal(bytesIn, compressType);
	}
	@SuppressWarnings("fallthrough")
	private byte[] compressInternal(byte[] bytesIn, CompressType compressType) throws Exception
	{
		if (bytesIn.length < getCompressMinLength())
		{
			compressType = CompressType.none;
		}

		byte[]				bytesOut	= null;
		try (OperationContext context = m_counterDeflate.begin())
		{
			switch (compressType)
			{
			case none:
				bytesOut = compressNone(bytesIn);
				break;

			case compressDefault:
				compressType = CompressType.deflate;
			case deflate:
				bytesOut = compressDeflate(bytesIn);
				break;
			}
			bytesOut[1] = (byte) compressType.ordinal();
		}

		return bytesOut;
	}
	private byte[] compressNone(byte[] bytesIn)
	{
		int					cb				= bytesIn.length;
		byte[]				bytesOut		= new byte[cb + 2];

		// Even at 1K, this extra arrayCopy costs less than 0.05 us; less than 0.1 us at 2K
		// Anything more than 2K will be compressed.
		// No point in complicating things to avoid this arrayCopy.
		// (see BasicsTests.tools.MiscTests.testSystemArrayCopy)
		System.arraycopy(bytesIn, 0, bytesOut, 2, cb);

		return bytesOut;
	}
	private byte[] compressDeflate(byte[] bytesIn) throws BasicException
	{
		byte[]						bytesOut;
		Deflater					deflater			= new Deflater();
		ByteArrayOutputStream		byteStream			= new ByteArrayOutputStream();
		try
		{
			deflater.setLevel(Deflater.BEST_SPEED);
			deflater.setInput(bytesIn);
			deflater.finish();

			// Write the header bytes
			byte			zero		= '\0';
			byteStream.write(zero);
			byteStream.write(zero);

			byte[] buf = new byte[10_240];
			while ( !deflater.finished() )
			{
				int cb = deflater.deflate(buf);
				byteStream.write(buf, 0, cb);
			}
			bytesOut = byteStream.toByteArray();
		}
		catch (Throwable throwable)
		{
			throw new BasicException(
					String.format("Compression failed on object of length %,d", bytesIn.length), throwable);
		}

		return bytesOut;
	}



	public static byte[] decompress(byte[] bytesIn) throws Exception
	{
		return decompress(bytesIn, 0, bytesIn.length);
	}
	public static byte[] decompress(byte[] bytesIn, int indexMin, int length) throws Exception
	{
		return INSTANCE.decompressInternal(bytesIn, indexMin, length);
	}
	private byte[] decompressInternal(byte[] bytesIn, int indexMin, int length) throws Exception
	{
		int ordinal				= Byte.valueOf(bytesIn[indexMin + 1]).intValue();
		if (COMPRESS_TYPE_LIST.length <= ordinal)
		{
			throw new BasicException(
					String.format("Unknown compression type %d found.", ordinal));
		}
		CompressType		compressType		= COMPRESS_TYPE_LIST[ordinal];

		byte[]				bytesOut			= null;
		try (OperationContext context = m_counterInflate.begin())
		{
			switch (compressType)
			{
			default:
			case none:
				bytesOut = decompressNone(bytesIn, indexMin, length);
				break;
			case deflate:
				bytesOut = decompressInflate(bytesIn, indexMin, length);
				break;
			}
		}

		return bytesOut;
	}


	private byte[] decompressNone(byte[] bytesIn, int indexMin, int length) throws BasicException
	{
		int			actualStart		= indexMin + 2;
		byte[]		bytesOut		= new byte[length - 2];

		System.arraycopy(bytesIn, actualStart, bytesOut, 0, length - 2);

		return bytesOut;
	}
	private byte[] decompressInflate(byte[] bytesIn, int indexMin, int length) throws BasicException
	{
		int			actualStart		= indexMin + 2;
		byte[]		bytesOut;

		Inflater inflater			= new Inflater();
		try
		{
			inflater.setInput(bytesIn, actualStart, length - 2);
			byte[]					result			= new byte[10_240];
			ByteArrayOutputStream	byteStream 		= new ByteArrayOutputStream();
			while (!inflater.finished())
			{
				int			cb				= inflater.inflate(result);
				byteStream.write(result, 0, cb);
			}
			bytesOut = byteStream.toByteArray();
		}
		catch (Throwable t)
		{
			throw new BasicException(
					String.format("De-serialization error on  %,d bytes", bytesIn.length), t);
		}

		return bytesOut;
	}
}
