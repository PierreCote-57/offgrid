/*
 * Copyright (c) 2020 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.spring.tools;

import com.lc.basics.tools.logging.BasicLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Supplier;

public class BaseRestController extends BaseController
{
	private static final BasicLogger LOGGER		= BasicLogger.getLogger(BaseRestController.class);

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	public <T extends RestBaseAnswer> ResponseEntity<T> processRequest(
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse,
		Supplier<T> supplier)
	{
		try
		{
			long			timeBeginMS			= System.currentTimeMillis();
			long			timeBeginNS			= System.nanoTime();

			T		response		= supplier.get();

			response.begin(timeBeginMS, timeBeginNS);
			response.markDone();

			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch (Exception exception)
		{
			String		message		= buildFailureMessage(servletRequest, "Failed to process request");
			getLogger().error(exception, message);

			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
