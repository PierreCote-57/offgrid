/*
 * Copyright (c) 2021 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.webapp.spring.tools;

import com.google.gson.Gson;
import com.lc.basics.tools.file.BaseFileHandler;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.time.BasicTimer;
import com.lc.basics.tools.time.WallClock;
import com.lc.offgrid.webapp.spring.tools.BaseWebController.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseWebProcessor
{
	private static final BasicLogger LOGGER					= BasicLogger.getLogger(BaseWebProcessor.class);
	// The pom's maven.build.timestamp.format
	private static final DateTimeFormatter BUILD_TIME_FORMATTER	= DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	@Value("${BaseWebProcessor.siteName}")
	private String				siteName;

	@Value("${BaseWebProcessor.welcomeMessage}")
	private String				welcomeMessage;

	@Value("${BaseWebProcessor.administratorEmail}")
	private String				administratorEmail;

	@Value("${BaseWebProcessor.siteVersion}")
	private String				siteVersion;

	@Autowired
	private Environment environment;

	// UTC
	@Value("${BaseWebProcessor.siteBuildTime}")
	private String				siteBuildTime;

	private Model				model;
	private BasicTimer			timer		= new BasicTimer("WebPage");

	private final List<String>		userMessageList			= new LinkedList<>();
	private final List<String>		warningMessageList		= new LinkedList<>();
	private final List<String>		errorMessageList			= new LinkedList<>();

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
		return BaseFileHandler.getGson();
	}
	public BasicTimer getTimer()
	{
		return timer;
	}

	public String getSiteName()
	{
		return siteName;
	}
	public String getWelcomeMessage()
	{
		return welcomeMessage;
	}
	public String getAdministratorEmail()
	{
		return administratorEmail;
	}
	public String getSiteVersion()
	{
		return siteVersion;
	}
	public ZonedDateTime getSiteBuildTime()
	{
		LocalDateTime buildTimeUTC = LocalDateTime.parse(siteBuildTime, BUILD_TIME_FORMATTER);
		ZonedDateTime zonedDateTimeUTC = buildTimeUTC.atZone(ZoneId.of("UTC"));
		ZonedDateTime buildTimeLocal = zonedDateTimeUTC.withZoneSameInstant(ZoneOffset.systemDefault());
		return buildTimeLocal;
	}
	public Environment getEnvironment()
	{
		return environment;
	}
	public Model getModel()
	{
		return model;
	}
	public void setModel(Model model)
	{
		this.model = model;
	}

	public String getActiveProfile()
	{
		return getEnvironment().getActiveProfiles()[0];
	}

	public String getSiteBuildTimeText()
	{
		String buildTimeText = WallClock.formatTime(WallClock.FormatDate.INTLD, WallClock.FormatTime.HMS, getSiteBuildTime().toEpochSecond() * 1000);
		return buildTimeText;
	}

	public String getSiteVersionText()
	{
		String buildTimeText = getSiteBuildTimeText();
		String siteVersionText = String.format("%1$s (%2$s)",
				getSiteVersion(), buildTimeText, getActiveProfile());
		return siteVersionText;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void processDefault(Model model, String pageName)
	{
		setModel(model);

		model.addAttribute("Timer", getTimer());

		model.addAttribute("SiteName", getSiteName());
		model.addAttribute("WelcomeMessage", getWelcomeMessage());
		model.addAttribute("SiteVersion", getSiteVersionText());
		model.addAttribute("BuildVersion", getSiteVersion());
		model.addAttribute("BuildTime", getSiteBuildTimeText());
		model.addAttribute("BuildProfile", getActiveProfile());
		model.addAttribute("AdministratorEmail", getAdministratorEmail());
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
		return userMessageList;
	}
	public List<String> getWarningMessageList()
	{
		return warningMessageList;
	}
	public List<String> getErrorMessageList()
	{
		return errorMessageList;
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
		getModel().addAttribute("Line1", text);
		getModel().addAttribute("Title1", title);
	}
	public void addLine2(String text)
	{
		addLine2(text, "");
	}
	public void addLine2(String text, String title)
	{
		getModel().addAttribute("Line2", text);
		getModel().addAttribute("Title2", title);
	}

}
