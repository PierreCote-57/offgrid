package com.lc.offgrid.spring.web;

import com.lc.offgrid.pojo.page.PageData;
import com.lc.offgrid.spring.tools.BaseWebProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Date;

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
		processDefault(model, "Offgrid's home page");

		addInfoMessage("Welcome to Offgrid " + new Date());
		addWarningMessage("Welcome to WARNING " + new Date());
		addErrorMessage("Welcome to ERROR " + new Date());

		String viewName = "index";
		return viewName;
	}

	public String processAbout(Model model)
	{
		processDefault(model, "About");

		String viewName = "about/about";
		return viewName;
	}

	/**
	 * One of the Useful pages. The name completes the page name, which is the view name and
	 * the data folder both, so a new one is a template plus its folder.
	 */
	public String processUseful(Model model, String name)
	{
		String pageName = "useful-" + name;

		PageData pageData = readFile("data/about/" + pageName + "/" + pageName + ".json", PageData.class);

		processDefault(model, pageData.getName());
		model.addAttribute("noteList", pageData.getNoteList());

		String viewName = "about/" + pageName;
		return viewName;
	}

	/**
	 * One of the hardware pages — the van, the Bronco. The name is the view name and the data
	 * folder both.
	 */
	public String processHardware(Model model, String name)
	{
		PageData pageData = readFile("data/hardware/" + name + "/" + name + ".json", PageData.class);

		processDefault(model, pageData.getName());
		model.addAttribute("noteList", pageData.getNoteList());
		model.addAttribute("photoGalleries", pageData.getPhotoGalleries());

		String viewName = "hardware/" + name;
		return viewName;
	}

	/**
	 * A checklist page. The name is both the template under templates/hardware/checklists
	 * and the folder holding its JSON, so one mapping serves every checklist.
	 */
	public String processChecklist(Model model, String name)
	{
		PageData pageData = readFile("data/hardware/checklists/" + name + "/" + name + ".json", PageData.class);

		processDefault(model, pageData.getName());
		model.addAttribute("photoGalleries", pageData.getPhotoGalleries());

		String viewName = "hardware/checklists/" + name;
		return viewName;
	}

	public String processPi(Model model)
	{
		processDefault(model, "The net");

		return "admin/pi";
	}
}
