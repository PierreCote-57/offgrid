package com.lc.offgrid.spring.web;

import com.lc.offgrid.spring.tools.BaseWebProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
@Scope("prototype")
public class OffgridProcessor extends BaseWebProcessor
{
	@Value("${folder.image}")
	private String				m_folderImage;

	@Value("${folder.data}")
	private String				m_folderData;

	public String processHome(Model model)
	{
		processDefault(model, "Home");

		model.addAttribute("headline", "Offgrid");
		var viewName = "index";
		return viewName;
	}

	public String processPi(Model model)
	{
		processDefault(model, "The net");

		return "admin/pi";
	}
}
