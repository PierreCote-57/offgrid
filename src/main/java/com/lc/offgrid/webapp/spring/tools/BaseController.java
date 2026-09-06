/*
 * Copyright (c) 2020 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.webapp.spring.tools;

import com.lc.basics.tools.logging.BasicLogger;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public class BaseController
{
	private static final BasicLogger LOGGER		= BasicLogger.getLogger(BaseController.class);

	/**
	 * One row per page view, kept apart from everything else so the file can be analysed on
	 * its own. The name is what routes it: log4j2-spring.xml gives offgrid.visit its own
	 * appender and does not let it reach the others.
	 */
	private static final BasicLogger VISIT_LOGGER	= BasicLogger.getLogger("offgrid.visit");

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}
	public static BasicLogger getVisitLogger()
	{
		return VISIT_LOGGER;
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
