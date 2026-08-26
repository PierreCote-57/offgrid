package com.lc.offgrid.spring.web;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.misc.imaging.ImageMetadata;
import com.lc.offgrid.misc.imaging.OffgridImageManager;
import com.lc.offgrid.pojo.page.PageData;
import com.lc.offgrid.spring.tools.BaseWebProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.io.File;
import java.util.Date;

@Component
@Scope("prototype")
public class OffgridProcessor extends BaseWebProcessor
{
	private static final BasicLogger	LOGGER			= BasicLogger.getLogger(OffgridProcessor.class);

	/** Temporary: every image request answers with this one file, whatever name was asked for. */
	private static final String			FIXED_IMAGE		= "IMG_0627.JPG";

	@Autowired
	private OffgridImageManager imageManager;

	public OffgridImageManager getImageManager()
	{
		return imageManager;
	}

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

		String viewName = "info/about";
		return viewName;
	}

	/**
	 * One of the Useful pages. The name completes the page name, which is the view name and
	 * the data folder both, so a new one is a template plus its folder.
	 */
	public String processUseful(Model model, String name)
	{
		String pageName = "useful-" + name;

		PageData pageData = readFile("data/info/" + pageName + "/" + pageName + ".json", PageData.class);

		processDefault(model, pageData.getName());
		model.addAttribute("pageData", pageData);

		String viewName = "info/" + pageName;
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
		model.addAttribute("pageData", pageData);

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
		model.addAttribute("pageData", pageData);

		String viewName = "hardware/checklists/" + name;
		return viewName;
	}

	/**
	 * The bytes of one image, read from the image folder, which sits outside the resource tree.
	 * A name with no file behind it answers 404 rather than an error page.
	 */
	public ResponseEntity<Resource> processImage(String imageName)
	{
//		imageName = FIXED_IMAGE;
		ImageMetadata metadata = getImageManager().getImageMetadata(imageName);
		FileSystemResource	imageResource	= new FileSystemResource(metadata.getFile());
		MediaType			mediaType		= metadata.getMediaType();

		ResponseEntity<Resource> answer = ResponseEntity.ok()
				.contentType(mediaType)
				.body(imageResource);
		return answer;
	}

	public String processPi(Model model)
	{
		processDefault(model, "The net");

		return "info/pi";
	}
}
