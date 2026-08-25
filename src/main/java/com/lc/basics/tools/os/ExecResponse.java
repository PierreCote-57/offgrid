/**
 *	ExecResponse.java
 *
 *	Copyright (c) 2007 LogicielCote.COM All rights reserved
 */
package com.lc.basics.tools.os;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.misc.BasicException;

/**
 * @author Pierre
 *
 */
public class ExecResponse
{
	private static final BasicLogger LOGGER = BasicLogger.getLogger(ExecResponse.class);

	private String			m_command;
	private String			m_response;
	private String			m_error;
	private BasicException	m_exception;
	private	long			m_timeNS;

	protected static BasicLogger getLogger()
	{
		return LOGGER;
	}

	/**
     * @return the response
     */
    public String getResponse()
    {
    	return m_response;
    }

	/**
     * @param response the response to set
     */
    public void setResponse(String response)
    {
    	m_response = response;
    }

	public String getError()
	{
		return m_error;
	}

	public void setError(String error)
	{
		m_error = error;
	}

	/**
     * @return the exception
     */
    public BasicException getException()
    {
    	return m_exception;
    }

	/**
     * @param exception the exception to set
     */
    public void setException(BasicException exception)
    {
    	m_exception = exception;
    }

    /**
     * Shortcut to add an Exception when a new error is being generated.
     * @param operation
     * @param error
     */
	public void setError(String operation, String error)
	{
		m_exception = new BasicException("Failed to " + operation + " on command " + getCommand() + " with " + error);
	}

	/**
     * @return the timeNS
     */
    public long getTimeNS()
    {
    	return m_timeNS;
    }

	/**
     * @param timeNS the timeNS to set
     */
    public void setTimeNS(long timeNS)
    {
    	m_timeNS = timeNS;
    }

    @Override
    public String toString()
    {
    	if (null != getException())
    	{
    		return getException().getMessageChain();
    	}
    	else if (null != getResponse())
    	{
    		return getResponse();
    	}
    	else
    	{
    		return getCommand();
    	}
    }

	/**
     * @return the command
     */
    public String getCommand()
    {
    	return m_command;
    }

	/**
     * @param command the command to set
     */
    public void setCommand(String command)
    {
    	m_command = command;
    }
}
