/*
 * Copyright (c) 2020 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.spring.tools;

import com.lc.basics.tools.logging.BasicLogger;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public class BaseController
{
	private static final BasicLogger LOGGER		= BasicLogger.getLogger(BaseController.class);

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static String buildFailureMessage(HttpServletRequest request, String failure)
	{
		StringBuilder		message			= new StringBuilder(100);
		message.append(failure);
		message.append(String.format(" for\n%s %s",
			request.getMethod(), request.getRequestURL()));
		String				prefix			= "?";
		for (Object object : request.getParameterMap().entrySet())
		{
			Map.Entry<String, String[]> entry = (Map.Entry) object;
			message.append("\n");
			message.append(prefix);
			message.append(entry.getKey());
			message.append("=");
			for(String value : entry.getValue())
			{
				message.append(value);
				message.append(" ");
			}
			prefix = "&";
		}
		return message.toString();
	}
}
