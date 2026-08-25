/*
 * Copyright (c) 2013 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.misc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class BasicRuntimeException extends RuntimeException
{
	private static final long serialVersionUID		= 20130331235959L;

	public BasicRuntimeException(String format, Object...args)
	{
		super(String.format(format, args));
	}

	public BasicRuntimeException(Throwable throwable, String format, Object...args)
	{
		super(String.format(format, args), throwable);
	}

	public String getRawMessage()
	{
		int			index		= getMessage().indexOf(':');
		if (-1 == index || (index + 2) > getMessage().length())
		{
			return getMessage();
		}
		else
		{
			return getMessage().substring(index + 2);
		}
	}

	public String getMessageChain()
	{
		StringBuilder sb 			= new StringBuilder(1000);

		Throwable throwable	= this;
		while (null != throwable)
		{
			sb.append(throwable.getClass().getSimpleName());
			sb.append('=');
			sb.append(throwable.getMessage());
			sb.append('\n');
			throwable = throwable.getCause();
		}

		return sb.toString();
	}

	public String getCallStackChain()
	{
		ByteArrayOutputStream outputStream	= new ByteArrayOutputStream();
		PrintStream printStream		= new PrintStream(outputStream);

		printStackTrace(printStream);
		printStream.flush();
		byte[]						bytes			= outputStream.toByteArray();
		String stackTrace		= new String(bytes);
		return stackTrace;
	}
}
