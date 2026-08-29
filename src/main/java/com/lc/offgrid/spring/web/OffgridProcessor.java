package com.lc.offgrid.spring.web;

import com.lc.basics.tools.file.BasicFileReader;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.offgrid.misc.QueryUtil;
import com.lc.offgrid.misc.files.ResourceFileManager;
import com.lc.offgrid.misc.imaging.ImageMetadata;
import com.lc.offgrid.misc.imaging.OffgridImageManager;
import com.lc.offgrid.pojo.page.MaintenancePage;
import com.lc.offgrid.pojo.page.PageData;
import com.lc.offgrid.pojo.page.PostPage;
import com.lc.offgrid.pojo.part.Dataset;
import com.lc.offgrid.spring.tools.BaseController;
import com.lc.offgrid.spring.tools.BaseWebController;
import com.lc.offgrid.spring.tools.BaseWebProcessor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class OffgridProcessor extends BaseWebProcessor
{
	private static final BasicLogger	LOGGER			= BasicLogger.getLogger(OffgridProcessor.class);

	/** Temporary: every image request answers with this one file, whatever name was asked for. */
	private static final String			FIXED_IMAGE		= "IMG_0627.JPG";

	@Autowired
	private OffgridImageManager imageManager;

	@Autowired
	private ResourceFileManager.JsonResourceFileManager jsonManager ;

	public OffgridImageManager getImageManager()
	{
		return imageManager;
	}

	public ResourceFileManager.JsonResourceFileManager getJsonManager()
	{
		return jsonManager;
	}

	public String processPage(Model model, String path, Class<? extends PageData> clazz)
	{
		File jsonFile = getJsonManager().getFile(path);
		PageData pageData = readFile(jsonFile.getAbsolutePath(), clazz);
		String pageTitle = pageData.getName();

		processDefault(model, pageTitle);
		model.addAttribute("pageData", pageData);

		// Required by pages accessed from a browser
		HttpServletRequest request = BaseWebController.PageContext.getPageContext().getRequest();
		String referrer = request.getHeader("referer");
		model.addAttribute("backQuery", QueryUtil.extractQueryParam(referrer));
		model.addAttribute("backName", QueryUtil.extractQueryParamPretty(referrer, "dataset"));

		return path;
	}

	public String processBrowser(Model model, String path, Class<? extends PageData> clazz, String datasetName)
	{
		String answer = processPage(model, path, clazz);

		Dataset dataset = findDataset(datasetName);
		if (null != dataset)
		{
			model.addAttribute("PageName", dataset.getTitle());
		}

		return answer;
	}

	/**
	 * The bytes of one image, read from the image folder, which sits outside the resource tree.
	 * A name with no file behind it answers 404 rather than an error page.
	 */
	public ResponseEntity<Resource> processImage(String imageName)
	{
//		imageName = FIXED_IMAGE;
		try
		{
			OffgridImageManager manager = getImageManager();
			ImageMetadata metadata = manager.getImageMetadata(imageName);
			File imageFile = metadata.getFile();
			FileSystemResource imageResource = new FileSystemResource(imageFile);
			MediaType mediaType = metadata.getMediaType();

			ResponseEntity<Resource> answer = makeResponseOk(mediaType, imageResource);
			return answer;
		}
		catch (Exception e)
		{
			getLogger().error("Unable to locate image %s", imageName);
			return makeResponseNotFound();
		}
	}

	/**
	 * The rows of one dataset, as JSON, for the browser page. datasets.json maps the id to
	 * the file that holds them; an id nobody defined answers 404 rather than an error page.
	 *
	 * The file is answered as it stands. Resolving a row's pointers into the row itself is
	 * this method's job to come.
	 */
	public ResponseEntity<String> processBrowserData(String id)
	{
		Dataset dataset = findDataset(id);
		if (null == dataset)
		{
			getLogger().error("Unknown dataset: %s", id);
			return makeResponseNotFound();
		}

		String fileName = String.format("data/shared/browser/%s", dataset.getFile());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> pageList = readFile(fileName, List.class);
		hydratePageList(pageList);
		String jsonTo = getGson().toJson(pageList);

		ResponseEntity<String> answer = makeResponseOk(MediaType.APPLICATION_JSON, jsonTo);
		return answer;
	}

	@SuppressWarnings("unchecked")
	private void hydratePageList(List<Map<String, Object>> pageList)
	{
		for (int i  = 0; i < pageList.size(); i++)
		{
			Map<String, Object> page = pageList.get(i);
			String fileText = (String) page.get("file");
			if (null != fileText)
			{
				try
				{
					File file = getJsonManager().getFile(fileText);
					Map<String, Object> realPage = BasicFileReader.readJsonFile(file, Map.class);
					realPage.putAll(page);
					pageList.set(i, realPage);
				}
				catch (Exception e)
				{
					getLogger().error("Error reading file pointer: %s", fileText);
				}
			}
		}
	}

	/**
	 * The datasets.json entry for an id, or null when nothing defines it. The file is public —
	 * the browser reads it too, to build its controls — so it lives under static.
	 */
	private Dataset findDataset(String id)
	{
		String fileName = "static/shared/browser/datasets.json";
		Dataset[] datasetList = readFile(fileName, Dataset[].class);

		for (Dataset dataset : datasetList)
		{
			String datasetId = dataset.getId();
			if (datasetId.equals(id))
			{
				return dataset;
			}
		}
		return null;
	}

	private <T> ResponseEntity<T> makeResponseOk(MediaType mediaType, T response)
	{
		ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
		builder = builder.contentType(mediaType);
		ResponseEntity<T> answer = builder.body(response);
		return answer;
	}
	private <T> ResponseEntity<T> makeResponseNotFound()
	{
		return ResponseEntity.notFound().build();
	}
}
