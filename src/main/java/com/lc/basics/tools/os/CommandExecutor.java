/*
 *	CommandExecutor.java
 *
 *	Copyright (c) 2007 LogicielCote All rights reserved
 */
package com.lc.basics.tools.os;

import com.lc.basics.tools.file.BasicFileReader;
import com.lc.basics.tools.function.SupplierWithException;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.misc.BasicException;

import java.util.Arrays;

/**
 * Access point for a number of simple network commands
 * 
 * @author Pierre
 *
 */
@SuppressWarnings("PMD.UseSingleton")
public class CommandExecutor
{
	private static BasicLogger		LOGGER		= BasicLogger.getLogger(CommandExecutor.class);

	protected static BasicLogger getLogger()
	{
		return LOGGER;
	}

	public static ExecResponse exec(String command) throws BasicException
	{
		return exec(new String[] {(command)});
	}
	public static ExecResponse exec(String[] command) throws BasicException
	{
		ExecResponse		response		= new ExecResponse();
		if (!exec(command, response))
		{
			throw new BasicException(response.getException(), "Failed to execute %s", Arrays.toString(command));
		}
		return response;
	}

	/**
	 * Executes an OS command and captures the output (stdout).
	 * 
	 * @param command		e.g. "ls"
	 * @param response		stdout and stderr
	 * @return				True if the command succeeded.
	 * 						If false, look at response.getException()
	 */
	public static boolean exec(String[] command, ExecResponse response)
	{
		return exec(() -> Runtime.getRuntime().exec(command), response);
	}

	public static boolean exec(String command, ExecResponse response)
	{
		return exec(() -> Runtime.getRuntime().exec(command), response);
	}
	private static boolean exec(SupplierWithException<Process> supplier, ExecResponse response)
	{
		long	t1	= System.nanoTime();

		String			strInput		= null;
		String			strError		= null;
		try
		{
			getLogger().debug("Launching process");
			Process			process		= supplier.get();
			getLogger().debug("Reading the command output");
			strInput = BasicFileReader.readTextFile(process.getInputStream());
			strError = BasicFileReader.readTextFile(process.getErrorStream());
			getLogger().debug("Done reading ");
		}
		catch (Throwable e)
		{
			response.setException(new BasicException(
					"Failed to execute command ", e));
		}

		long	t2	= System.nanoTime();
		response.setTimeNS(t2 - t1);
		getLogger().debug("exec() in " + response.getTimeNS() + " ms");

		// setResponse OUTSIDE the try/catch, to save what has been seen so far.
		response.setResponse(strInput);
		response.setError(strError);

		return (null == response.getException());
	}
}
