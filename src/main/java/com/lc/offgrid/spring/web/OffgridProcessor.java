package com.lc.offgrid.spring.web;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.pojo.page.PageData;
import com.lc.offgrid.spring.tools.BaseWebProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Component
@Scope("prototype")
public class OffgridProcessor extends BaseWebProcessor
{
	private static final BasicLogger	LOGGER			= BasicLogger.getLogger(OffgridProcessor.class);

	/** Temporary: every image request answers with this one file, whatever name was asked for. */
	private static final String			FIXED_IMAGE		= "IMG_0627.JPG";

	@Value("${folder.image}")
	private String		folderImage;

	@Value("${folder.data}")
	private String		folderData;

	public String getFolderImage()
	{
		return folderImage;
	}

	public String getFolderData()
	{
		return folderData;
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
		model.addAttribute("pageData", pageData);

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
		String	imageFile	= FIXED_IMAGE;
		Path	imagePath	= Paths.get(getFolderImage() + "/" + imageFile);

		if (!Files.isReadable(imagePath))
		{
			LOGGER.warn("No image file: %s", imagePath);

			ResponseEntity<Resource> missing = ResponseEntity.notFound().build();
			return missing;
		}

		FileSystemResource	imageResource	= new FileSystemResource(imagePath);
		MediaType			mediaType		= mediaTypeOf(imageFile);

		ResponseEntity<Resource> answer = ResponseEntity.ok()
				.contentType(mediaType)
				.body(imageResource);
		return answer;
	}

	/**
	 * The media type an image filename implies, from its extension. Anything unrecognized is
	 * served as raw bytes.
	 */
	public static MediaType mediaTypeOf(String imageName)
	{
		String		lowerName	= imageName.toLowerCase();
		MediaType	answer		= MediaType.APPLICATION_OCTET_STREAM;

		if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg"))
		{
			answer = MediaType.IMAGE_JPEG;
		}
		else if (lowerName.endsWith(".png"))
		{
			answer = MediaType.IMAGE_PNG;
		}
		else if (lowerName.endsWith(".gif"))
		{
			answer = MediaType.IMAGE_GIF;
		}
		else if (lowerName.endsWith(".webp"))
		{
			answer = MediaType.valueOf("image/webp");
		}
		return answer;
	}

	public String processPi(Model model)
	{
		processDefault(model, "The net");

		return "admin/pi";
	}
}
