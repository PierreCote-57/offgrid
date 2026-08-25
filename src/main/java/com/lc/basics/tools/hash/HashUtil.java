/*
 * Copyright (c) 2017 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.hash;

import com.lc.basics.tools.misc.BasicTools;

import java.nio.charset.Charset;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * public face of various hash algorithm and other hash related tools.
 */
public abstract class HashUtil
{
	public enum ProviderName
	{
		Java32(JavaHash32::new),			// Primary for public use
		Java64(JavaHash64::new),
//		Fvn32(FnvHash32::new),
		Fvn64(FnvHash64::new),
		Jenkins32(JenkinsHash32::new),
		Jenkins64(JenkinsHash64::new),
//		Murmur32(MurmurHash32::new),
//		Murmur128(MurmurHash128::new);		// Primary for internal use
		;
		private final Supplier<HashProvider>		m_supplier;
		ProviderName(Supplier<HashProvider> supplier)
		{
			m_supplier = supplier;
		}

		public HashProvider getProvider()
		{
			return m_supplier.get();
		}
	}

	/**
	 * Official method to obtain a pooled hash provider of this type.
	 * The HashProvider is in a reset state when initially given.
	 * After use, release it back to the pool.
	 *
	 * @return		Requested hash provider, do not forget to release it
	 */
	public static HashProvider getHashProvider()
	{
		return ProviderName.Java64.getProvider();
	}

	public static int hashJava32(byte[] bytes)
	{
		HashProvider			hashProvider		= ProviderName.Java32.getProvider();
		hashProvider.add(bytes);
		int						hashCode			= hashProvider.getHashCode32();
		return hashCode;
	}



	///////////////////////////////////////////////////////////////////////////
	//
	//	Everything beyond this point is used only for testing.
	//
	///////////////////////////////////////////////////////////////////////////
	/* Typical performance test results...
Evaluating over 100,000 keys of 20 chars
                          Java32    Java64     Fvn32     Fvn64 Jenkins32 Jenkins64  Murmur32  Murmur128
Pass  0 (Key =     20)     133.6      32.1      66.5      64.0   1,838.0   1,478.8      46.0      88.9  ns/key
Pass  1 (Key =     20)      65.4      33.7      33.2      30.4   1,151.8   1,459.2      37.4      52.2  ns/key
Pass  2 (Key =     20)      79.6      32.7      32.0      35.5   1,121.0   1,282.7      34.6      40.3  ns/key
Pass  3 (Key =     20)      49.8      44.2      56.8      31.8   1,272.0   1,590.5      30.0      50.0  ns/key
Pass  4 (Key =     20)      30.5      33.7      28.8      46.7   1,164.1   1,290.6      37.7      47.0  ns/key
	 */

	public static final ProviderName[]		PROVIDER_NAME_LIST		= ProviderName.values();
	public static final int 				PROVIDER_COUNT			= PROVIDER_NAME_LIST.length;

	public enum KeyType
	{
		GuidText
				{
					public byte[] generateKey(int index, int keySize)
					{
						byte[]			key;
						UUID			uuid	= UUID.randomUUID();
						key = uuid.toString().getBytes(Charset.defaultCharset());
						return key;
					}
				},
		GuidBytes
				{
					public byte[] generateKey(int index, int keySize)
					{
						byte[]			key		= new byte[16];
						UUID			uuid	= UUID.randomUUID();
						extractBytes(uuid.getLeastSignificantBits(), key,  0, 8);
						extractBytes(uuid.getMostSignificantBits(), key,  8, 8);
						return key;
					}
				},
		NumberText
				{
					public byte[] generateKey(int index, int keySize)
					{
						byte[]			key;
						key = String.format("%d", index).getBytes(Charset.defaultCharset());
						return key;
					}
				},
		NumberBytes
				{
					public byte[] generateKey(int index, int keySize)
					{
						byte[]			key		= new byte[4];
						extractBytes(index, key, 0, 4);
						return key;
					}
				},
		KeyDash
				{
					public byte[] generateKey(int index, int keySize)
					{
						byte[]			key;
						key = String.format("Key-%,12d", index).getBytes(Charset.defaultCharset());
						return key;
					}
				},
		ValueDash
				{
					public byte[] generateKey(int index, int keySize)
					{
						byte[]			key;
						key = String.format("Value-%,12d", index).getBytes(Charset.defaultCharset());
						return key;
					}
				},
		RANDOM
				{
					public byte[] generateKey(int index, int keySize)
					{
						byte[]			key;
						key = BasicTools.generateRandomString(keySize).getBytes(Charset.defaultCharset());
						return key;
					}
				};

		public abstract byte[] generateKey(int index, int keySize);
		private static void extractBytes(long value, byte[] bytes, int index, int count)
		{
			while (0 < count--)
			{
				byte		b		= (byte) value;
				bytes[index++] = b;
				value = value >> 8;
			}
		}

	}
	public static final KeyType[]			KEY_TYPE_LIST			= KeyType.values();

/*
	public static void main(String[] args)
	{
		String hello			= "Hello world.";
		byte[]		helloBytes		= hello.getBytes();

		for (ProviderName providerName : PROVIDER_NAME_LIST)
		{
			AbstractHashProvider provider	= providerName.getProvider();
			provider.add(helloBytes);
			long		hash		= provider.getHashCode32();
			log(String.format("The %1$-15s of '%2$s' is %3$,25d == 0x%3$16X", providerName.name(), hello, hash));
		}


		int[]		keySizeList		= { 100, 10, 100, 1000 };
		for (int keySize : keySizeList)
		{
			measure(keySize);
		}
	}
*/
	@SuppressWarnings("PMD.SystemPrintln")
	private static void log(String text)
	{
		System.out.println(text);
	}

	public static void measure(int keySize)
	{
		int				keyCount	= 100 * 1000;
		log("");
		log(String.format("Evaluating over %,d keys of %,d chars", keyCount, keySize));
		byte[][]		keyList		= new byte[keyCount][];
		for (int i = 0; i < keyList.length; i++)
		{
			keyList[i] = BasicTools.generateRandomString(keySize).getBytes();
		}

		{
			StringBuilder sb			= new StringBuilder(String.format("%23s", ""));
			for (ProviderName providerName : PROVIDER_NAME_LIST)
			{
				sb.append(String.format("%9s ", providerName));
			}
			log(sb.toString());
		}

		for (int iPass = 0; iPass < 5; iPass++)
		{
			long[]			durationList		= new long[PROVIDER_COUNT];
			for (ProviderName providerName : PROVIDER_NAME_LIST)
			{
				HashProvider provider		= providerName.getProvider();
				durationList[providerName.ordinal()] = measureAlgorithm(provider, keyList);
			}

			StringBuilder sb		= new StringBuilder(1024);
			sb.append(String.format("Pass %2d (Key = %,6d) ", iPass, keySize));
			for (int algo = 0; algo < PROVIDER_COUNT; algo++)
			{
				sb.append(String.format("%,9.1f ", durationList[algo] * 1.0 / keyCount));
			}
			sb.append(" ns/key");
			log(sb.toString());
			BasicTools.sleepMS(25);
		}
	}

	private static long measureAlgorithm(HashProvider provider, byte[][] keyList)
	{
		long		t1		= System.nanoTime();
		for (byte[] key : keyList)
		{
			provider.reset();
			provider.add(key);
//			provider.getHashCode();
		}
		long		t2		= System.nanoTime();

		return (t2 - t1);
	}
}

