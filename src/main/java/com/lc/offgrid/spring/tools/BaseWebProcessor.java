/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.spring.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lc.basics.tools.file.BasicFileReader;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.misc.BasicException;
import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.basics.tools.time.BasicTimer;
import com.lc.offgrid.spring.tools.BaseWebController.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseWebProcessor
{
	private static final BasicLogger LOGGER					= BasicLogger.getLogger(BaseWebProcessor.class);
	private static final Gson GSON							=
			new GsonBuilder().setPrettyPrinting()
					.disableHtmlEscaping()
					.create();

	@Value("${BaseWebProcessor.siteName}")
	private String				m_siteName;

	@Value("${BaseWebProcessor.welcomeMessage}")
	private String				m_welcomeMessage;

	@Value("${BaseWebProcessor.administratorEmail}")
	private String				m_administratorEmail;

	@Value("${BaseWebProcessor.siteVersion}")
	private String				m_siteVersion;

	private Model				m_model;
	private BasicTimer			m_timer		= new BasicTimer("WebPage");

	private final List<String>		m_userMessageList			= new LinkedList<>();
	private final List<String>		m_warningMessageList		= new LinkedList<>();
	private final List<String>		m_errorMessageList			= new LinkedList<>();

	public BaseWebProcessor()
	{
		BaseWebController.PageContext context = BaseWebController.PageContext.getPageContext();
		if (null != context)
		{
			context.setProcessor(this);
		}
	}

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}
	public static Gson getGson()
	{
		return GSON;
	}
	public BasicTimer getTimer()
	{
		return m_timer;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void processDefault(Model model, String pageName)
	{
		m_model = model;

		model.addAttribute("Timer", getTimer());

		model.addAttribute("SiteName", m_siteName);
		model.addAttribute("WelcomeMessage", m_welcomeMessage);
		model.addAttribute("SiteVersion", m_siteVersion);
		model.addAttribute("AdministratorEmail", m_administratorEmail);
		model.addAttribute("PageName", pageName);

		model.addAttribute("AwesomeUrl", "https://kit.fontawesome.com/23ac3050e4.js");

		model.addAttribute("Line1", "");
		model.addAttribute("Line2", "");

		model.addAttribute("UserMessageList", getUserMessageList());
		model.addAttribute("WarningMessageList", getWarningMessageList());
		model.addAttribute("ErrorMessageList", getErrorMessageList());

		PageContext context		= PageContext.getPageContext();
		if (null != context)
		{
			Object msg = context.getRequest().getSession().getAttribute("UserMessages");
			if (msg instanceof List)
			{
				getUserMessageList().addAll((List) msg);
			}
			context.getRequest().getSession().setAttribute("UserMessages", getUserMessageList());

			Object warn = context.getRequest().getSession().getAttribute("WarningMessages");
			if (msg instanceof List)
			{
				getWarningMessageList().addAll((List) warn);
			}
			context.getRequest().getSession().setAttribute("WarningMessages", getWarningMessageList());

			Object err = context.getRequest().getSession().getAttribute("ErrorMessages");
			if (err instanceof List)
			{
				getErrorMessageList().addAll((List) err);
			}
			context.getRequest().getSession().setAttribute("ErrorMessages", getErrorMessageList());
		}

		getTimer().checkpoint("Done processDefault()");
	}

	public List<String> getUserMessageList()
	{
		return m_userMessageList;
	}
	public List<String> getWarningMessageList()
	{
		return m_warningMessageList;
	}
	public List<String> getErrorMessageList()
	{
		return m_errorMessageList;
	}

	public void addInfoMessage(String format, Object ... args)
	{
		addMessage(getUserMessageList(), format, args);
	}
	public void addWarningMessage(String format, Object ... args)
	{
		addMessage(getWarningMessageList(), format, args);
	}
	public void addErrorMessage(String format, Object ... args)
	{
		addMessage(getErrorMessageList(), format, args);
	}
	private void addMessage(List<String> list, String format, Object ... args)
	{
		String		message		= String.format(format, args);
		list.add(message);
	}

	public void addLine1(String text)
	{
		addLine1(text, "");
	}
	public void addLine1(String text, String title)
	{
		m_model.addAttribute("Line1", text);
		m_model.addAttribute("Title1", title);
	}
	public void addLine2(String text)
	{
		addLine2(text, "");
	}
	public void addLine2(String text, String title)
	{
		m_model.addAttribute("Line2", text);
		m_model.addAttribute("Title2", title);
	}

	public static <T> T readFile(String path, Class<T> clazz)
	{
		URL url = BaseWebProcessor.class.getClassLoader().getResource(path);
		try
		{
			String text = BasicFileReader.readTextFile(url);
			T obj = GSON.fromJson(text, clazz);
			return obj;
		}
		catch (Exception e)
		{
			LOGGER.error("Error reading file: " + path, e);
			throw new BasicRuntimeException("Error reading file: " + path, e);
		}
	}
}
