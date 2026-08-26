package com.lc.offgrid.spring.web;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.misc.imaging.ImageMetadata;
import com.lc.offgrid.misc.imaging.OffgridImageManager;
import com.lc.offgrid.pojo.page.MaintenancePage;
import com.lc.offgrid.pojo.page.PageData;
import com.lc.offgrid.pojo.page.PostPage;
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

		Date infoDate = new Date();
		String infoMessage = String.format("Welcome to Offgrid %s", infoDate);
		addInfoMessage(infoMessage);

		Date warningDate = new Date();
		String warningMessage = String.format("Welcome to WARNING %s", warningDate);
		addWarningMessage(warningMessage);

		Date errorDate = new Date();
		String errorMessage = String.format("Welcome to ERROR %s", errorDate);
		addErrorMessage(errorMessage);

		String viewName = "index";
		return viewName;
	}

	/**
	 * One of the Info pages. The name is the view name and the data folder both, so a new one
	 * is a template plus its folder.
	 */
	public String processInfo(Model model, String name)
	{
		String fileName = String.format("data/info/%1$s/%1$s.json", name);
		PageData pageData = readFile(fileName, PageData.class);
		String pageTitle = pageData.getName();

		processDefault(model, pageTitle);
		model.addAttribute("pageData", pageData);

		String viewName = String.format("info/%s", name);
		return viewName;
	}

	/**
	 * One of the hardware pages — the van, the Bronco. The name is the view name and the data
	 * folder both.
	 */
	public String processHardware(Model model, String name)
	{
		String fileName = String.format("data/hardware/%1$s/%1$s.json", name);
		PageData pageData = readFile(fileName, PageData.class);
		String pageTitle = pageData.getName();

		processDefault(model, pageTitle);
		model.addAttribute("pageData", pageData);

		String viewName = String.format("hardware/%s", name);
		return viewName;
	}

	/**
	 * A checklist page. The name is both the template under templates/hardware/checklists
	 * and the folder holding its JSON, so one mapping serves every checklist.
	 */
	public String processChecklist(Model model, String name)
	{
		String fileName = String.format("data/hardware/checklists/%1$s/%1$s.json", name);
		PageData pageData = readFile(fileName, PageData.class);
		String pageTitle = pageData.getName();

		processDefault(model, pageTitle);
		model.addAttribute("pageData", pageData);

		String viewName = String.format("hardware/checklists/%s", name);
		return viewName;
	}

	/**
	 * A maintenance page — the van's record, the Bronco's. The name is both the template under
	 * templates/hardware/maintenance and the folder holding its JSON.
	 */
	public String processMaintenance(Model model, String name)
	{
		String fileName = String.format("data/hardware/maintenance/%1$s/%1$s.json", name);
		MaintenancePage pageData = readFile(fileName, MaintenancePage.class);
		String pageTitle = pageData.getName();

		processDefault(model, pageTitle);
		model.addAttribute("pageData", pageData);

		String viewName = String.format("hardware/maintenance/%s", name);
		return viewName;
	}

	/**
	 * A blog entry. The name is both the template under templates/posts and the folder holding
	 * its JSON.
	 */
	public String processPost(Model model, String name)
	{
		String fileName = String.format("data/posts/%1$s/%1$s.json", name);
		PostPage pageData = readFile(fileName, PostPage.class);
		String pageTitle = pageData.getName();

		processDefault(model, pageTitle);
		model.addAttribute("pageData", pageData);

		String viewName = String.format("posts/%s", name);
		return viewName;
	}

	/**
	 * The bytes of one image, read from the image folder, which sits outside the resource tree.
	 * A name with no file behind it answers 404 rather than an error page.
	 */
	public ResponseEntity<Resource> processImage(String imageName)
	{
//		imageName = FIXED_IMAGE;
		OffgridImageManager	manager			= getImageManager();
		ImageMetadata		metadata		= manager.getImageMetadata(imageName);
		File				imageFile		= metadata.getFile();
		FileSystemResource	imageResource	= new FileSystemResource(imageFile);
		MediaType			mediaType		= metadata.getMediaType();

		ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
		builder = builder.contentType(mediaType);

		ResponseEntity<Resource> answer = builder.body(imageResource);
		return answer;
	}

	public String processPi(Model model)
	{
		processDefault(model, "The net");

		String viewName = "info/pi";
		return viewName;
	}
}
