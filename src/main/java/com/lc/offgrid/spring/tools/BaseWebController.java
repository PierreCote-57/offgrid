/*
 * Copyright (c) 2020 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.spring.tools;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BaseWebController extends BaseController
{
	public static final String			REDIRECT_PREFIX		= "redirect:";
	public static final String			REDIRECT_REFERRER	= "referrer:";

	@Autowired
	private BeanFactory m_beanFactory;

	public BeanFactory getBeanFactory()
	{
		return m_beanFactory;
	}

	public String processRequest(
		HttpServletRequest servletRequest,
		HttpServletResponse servletResponse,
		Model model,
		Supplier<String> supplier)
	{
		long startTime = System.nanoTime();

		PageContext pageContext = PageContext.getPageContext();
		if (null == pageContext)
		{
			pageContext = PageContext.create(servletRequest, servletRequest.getParameterMap(), servletResponse, model);
		}

		try
		{
			String				answer			= supplier.get();
			BaseWebProcessor	processor		= pageContext.getProcessor();
			if (null != processor)
			{
				processor.getTimer().checkpoint("Done processing");
			}

			if (REDIRECT_REFERRER.equals(answer))
			{
				answer = servletRequest.getHeader("referer");
				if (null != answer)
				{
					URL url = new URL(answer);
					answer = REDIRECT_PREFIX + url.getFile();
				}
			}
			if (null == answer)
			{
				answer = getRedirectPath(servletRequest);
			}
			if (!answer.startsWith(REDIRECT_PREFIX))
			{
				servletRequest.getSession().removeAttribute("UserMessages");
				servletRequest.getSession().removeAttribute("WarningMessages");
				servletRequest.getSession().removeAttribute("ErrorMessages");
			}
			logVisit(servletRequest, answer, startTime);

			return	answer;
		}
		catch (ResponseStatusException exception)
		{
			getLogger().info("Failed to serve %s", pageContext.getRequest().getServletPath());
			logVisit(servletRequest, "404" , startTime);
			throw exception;
		}
		catch (Exception exception)
		{
			String		message		= buildFailureMessage(servletRequest, "Failed to process request");
			getLogger().error(exception, "%s", message);

			model.addAttribute("PageName", "Something went wrong");
			model.addAttribute("errorMessage", message);

			logVisit(servletRequest, "exception", startTime);

			return "exception";
		}
		finally
		{
			PageContext.clearContext();
		}
	}

	/**
	 * Writes the one row this request leaves in the visit log: who asked, what they asked
	 * for, what was served them, and how long it took. Tab separated, and the appender puts
	 * the time in front of it.
	 */
	protected void logVisit(HttpServletRequest servletRequest, String viewName, long startTime)
	{
		long		elapsedNs		= System.nanoTime() - startTime;
		double		elapsedMs		= elapsedNs / 1000000.;
		String		method			= servletRequest.getMethod();
		String		path			= servletRequest.getRequestURI();
		String		queryString		= servletRequest.getQueryString();
		if (null != queryString)
		{
			path = String.format("%1$s?%2$s", path, queryString);
		}
		String		remoteAddress	= servletRequest.getRemoteAddr();

		getVisitLogger().info("%1$s\t%2$s\t%3$s\t%4$s\t%5$.1f",
				remoteAddress, method, path, viewName, elapsedMs);
	}

	protected String getRedirectPath(HttpServletRequest request)
	{
		return REDIRECT_PREFIX + request.getServletPath();
	}

	protected boolean getRequestParamBoolean(
			HttpServletRequest request,
			Map<String, String[]> parameterMap,
			HttpServletResponse response,
			String name, boolean defaultValue)
	{
		String		text		= getRequestParam(request, parameterMap, response, name, Boolean.toString(defaultValue));
		return "on".equals(text) || Boolean.parseBoolean(text);
	}
	protected double getRequestParamDouble(
			HttpServletRequest request,
			Map<String, String[]> parameterMap,
			HttpServletResponse response,
			String name, double defaultValue)
	{
		return Double.parseDouble(getRequestParam(request, parameterMap, response, name, Double.toString(defaultValue)));
	}

	protected String getRequestParam(
			HttpServletRequest request,
			Map<String, String[]> parameterMap,
			HttpServletResponse response,
			String name, String defaultValue)
	{
		String			submitText		= request.getParameter("submit");
		String			paramValue		= getParam(parameterMap, name);
		if (null == paramValue || paramValue.isBlank())
		{
			paramValue = defaultValue;
		}
		String			cookieValue		= getCookie(request, name);
		if (null != cookieValue)
		{
			cookieValue = URLDecoder.decode(cookieValue, Charset.defaultCharset());
		}

		if (null != submitText && !submitText.isEmpty())
		{
			// Submit, use param value
			response.addCookie(new Cookie(name, URLEncoder.encode(paramValue, Charset.defaultCharset())));
			response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
		}
		// Not a Submit, use cookie or default
		else if (null == paramValue || paramValue.isEmpty())
		{
			paramValue = cookieValue;
		}
		if (null == paramValue)
		{
			paramValue = defaultValue;
		}

		return paramValue;
	}
	protected String getParam(Map<String, String[]> parameterMap, String name)
	{
		String[]					param		= parameterMap.get(name);
		return null == param ? null : param[0];
	}
	protected String getCookie(HttpServletRequest request, String name)
	{
		Cookie[]		cookies		= request.getCookies();
		if (null != cookies)
		{
			for (Cookie cookie : cookies)
			{
				if (cookie.getName().equals(name))
				{
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	protected void addCookie(HttpServletResponse response, String name, String value)
	{
		Cookie cookie = new Cookie(name, value);
		response.addCookie(cookie);
	}
	protected void deleteAllCookies(HttpServletRequest request, HttpServletResponse response)
	{
		for (Cookie cookie : request.getCookies())
		{
			deleteCookie(response, cookie.getName());
		}
	}
	protected void deleteCookie(HttpServletResponse response, String name)
	{
		Cookie cookie = new Cookie(name, null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}





	public static class PageContext
	{
		private static final Map<Long, PageContext>		MAP		= new HashMap<>();

		private final HttpServletRequest			m_request;
		private final Map<String, String[]>			m_parameterMap;
		private final HttpServletResponse			m_response;
		private final Model							m_model;
		private BaseWebProcessor					m_processor;

		public PageContext(HttpServletRequest request, Map<String, String[]> parameterMap, HttpServletResponse response, Model model)
		{
			m_request = request;
			m_parameterMap = parameterMap;
			m_response = response;
			m_model = model;
		}

		public static PageContext create(HttpServletRequest request, Map<String, String[]> parameterMap, HttpServletResponse response, Model model)
		{
			PageContext pageContext = new PageContext(request, parameterMap, response, model);
			MAP.put(Thread.currentThread().getId(), pageContext);
			return pageContext;
		}
		public static PageContext getPageContext()
		{
			return MAP.get(Thread.currentThread().getId());
		}
		public static void clearContext()
		{
			MAP.remove(Thread.currentThread().getId());
		}

		public void setProcessor(BaseWebProcessor processor)
		{
			m_processor = processor;
		}

		public HttpServletRequest getRequest()
		{
			return m_request;
		}
		public Map<String, String[]> getParameterMap()
		{
			return m_parameterMap;
		}
		public HttpServletResponse getResponse()
		{
			return m_response;
		}
		public Model getSpringModel()
		{
			return m_model;
		}
		public BaseWebProcessor getProcessor()
		{
			return m_processor;
		}

		public String getCookieValue(String name, Object defaultValue)
		{
			for (Cookie cookie : getRequest().getCookies())
			{
				if (cookie.getName().equals(name))
				{
					return cookie.getValue();
				}
			}
			return defaultValue.toString();
		}
	}
}
