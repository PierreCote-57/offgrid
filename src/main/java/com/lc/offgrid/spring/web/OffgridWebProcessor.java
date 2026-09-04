package com.lc.offgrid.spring.web;

import com.lc.basics.tools.file.BasicFileReader;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.misc.BasicRuntimeException;
import com.lc.offgrid.misc.QueryUtil;
import com.lc.offgrid.misc.files.LocalFileManager;
import com.lc.offgrid.misc.files.ResourceFileManager;
import com.lc.offgrid.misc.imaging.ImageMetadata;
import com.lc.offgrid.misc.imaging.OffgridImageManager;
import com.lc.offgrid.pojo.page.DestinationPage;
import com.lc.offgrid.pojo.page.MaintenancePage;
import com.lc.offgrid.pojo.page.PageData;
import com.lc.offgrid.pojo.page.PostPage;
import com.lc.offgrid.pojo.part.Dataset;
import com.lc.offgrid.pojo.part.GoogleMap;
import com.lc.offgrid.pojo.part.Point;
import com.lc.offgrid.spring.tools.BaseController;
import com.lc.offgrid.spring.tools.BaseWebController;
import com.lc.offgrid.spring.tools.BaseWebProcessor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class OffgridWebProcessor extends BaseWebProcessor
{
	private static final MediaType SVG_MEDIA_TYPE = MediaType.valueOf("image/svg+xml");

	/**
	 * Two lines of text on the site's paper, in the site's colours. The box is 3:2, the
	 * shape every thumbnail rule in site.css already reserves. Both lines state a
	 * textLength, so each one spans the box whatever it says — a long name is squeezed to
	 * fit rather than running past the edge.
	 */
	private static final String MESSAGE_IMAGE_SVG = """
			<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 400" width="600" height="400" role="img">
				<rect x="1" y="1" width="598" height="398" fill="#faf7f0" stroke="#d6dfd8" stroke-width="2"/>
				<text x="300" y="175" text-anchor="middle" textLength="540" lengthAdjust="spacingAndGlyphs" font-family="system-ui, -apple-system, sans-serif" font-size="104" fill="#a02a1f">%1$s</text>
				<text x="300" y="290" text-anchor="middle" textLength="540" lengthAdjust="spacingAndGlyphs" font-family="system-ui, -apple-system, sans-serif" font-size="52" fill="#243027">%2$s</text>
			</svg>
			""";

	@Autowired
	private OffgridImageManager imageManager;

	@Autowired
	private ResourceFileManager.JsonResourceFileManager jsonManager ;

	@Autowired
	private LocalFileManager documentManager;

	public OffgridImageManager getImageManager()
	{
		return imageManager;
	}

	public ResourceFileManager.JsonResourceFileManager getJsonManager()
	{
		return jsonManager;
	}

	public LocalFileManager getDocumentManager()
	{
		return documentManager;
	}

	public String processPage(Model model, String path, Class<? extends PageData> clazz)
	{
		processDefault(model, path);
		PageData pageData = readPageJson(path, clazz);

		model.addAttribute("PageName", pageData.getName());
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
	 * A name with no file behind it answers a drawn image that says so, at 200, so the reason
	 * appears where the picture would have been rather than as a broken-image icon.
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
			getLogger().info("Unable to locate image %s", imageName);

			Resource messageImage = makeMessageImage("Not found", imageName);
			ResponseEntity<Resource> answer = makeResponseOk(SVG_MEDIA_TYPE, messageImage);
			return answer;
		}
	}

	/**
	 * The bytes of one document, read from the document folder, which sits outside the resource
	 * tree. The content type comes from the file itself, so any kind of document is answered as
	 * what it is. A name with no file behind it throws, and the error dispatch answers 404 with
	 * error.html.
	 */
	public ResponseEntity<Resource> processDocument(String documentName)
	{
		try
		{
			LocalFileManager manager = getDocumentManager();
			File documentFile = manager.getFile(documentName);
			FileSystemResource documentResource = new FileSystemResource(documentFile);
			MediaType mediaType = readMediaType(documentFile);

			ResponseEntity<Resource> answer = makeResponseOk(mediaType, documentResource);
			return answer;
		}
		catch (Exception e)
		{
			getLogger().info("Unable to locate document %s", documentName);
			throw makeNotFound("Unable to locate document %s", documentName);
		}
	}

	/**
	 * The content type of a file, as the file system reports it. A type it cannot name is
	 * answered as bytes.
	 */
	private MediaType readMediaType(File file) throws IOException
	{
		Path path = file.toPath();
		String contentType = Files.probeContentType(path);
		MediaType mediaType = null == contentType
				? MediaType.APPLICATION_OCTET_STREAM
				: MediaType.parseMediaType(contentType);
		return mediaType;
	}

	/**
	 * The rows of one dataset, as JSON, for the browser page. datasets.json maps the id to
	 * the file that holds them; an id nobody defined throws, and the error dispatch answers 404
	 * with error.html.
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
			throw makeNotFound("Unknown dataset: %s", id);
		}

		String fileName = String.format("data/shared/browser/%s", dataset.getFile());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> pageList = readFile(fileName, List.class);
		hydratePageList(pageList);
		String jsonTo = getGson().toJson(pageList);

		ResponseEntity<String> answer = makeResponseOk(MediaType.APPLICATION_JSON, jsonTo);
		return answer;
	}

	private <T extends PageData> T readPageJson(String path, Class<T> clazz)
	{
		File jsonFile = getJsonManager().getFile(path);
		if (null == jsonFile)
		{
			throw makeNotFound("Unable to locate page: %s", path);
		}
		T pageData = readFile(jsonFile.getAbsolutePath(), clazz);

		// Hydrate map as needed
		if (pageData instanceof DestinationPage destinationPage)
		{
			Map<String, GoogleMap> map = destinationPage.getGoogleMap();
			Point point = destinationPage.getLocation();
			for (GoogleMap googleMap : map.values())
			{
				if (null == googleMap.getLat())
				{
					googleMap.setLat(point.getLat());
				}
				if (null == googleMap.getLng())
				{
					googleMap.setLng(point.getLng());
				}
			}
		}

		return pageData;
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
	/**
	 * Two lines of text as an image. The browser draws the text with its own fonts, so this
	 * depends on nothing being installed on the machine that serves it.
	 */
	private Resource makeMessageImage(String line1, String line2)
	{
		String safeLine1 = escapeXml(line1);
		String safeLine2 = escapeXml(line2);
		String svgText = String.format(MESSAGE_IMAGE_SVG, safeLine1, safeLine2);
		byte[] svgBytes = svgText.getBytes(StandardCharsets.UTF_8);

		ByteArrayResource answer = new ByteArrayResource(svgBytes);
		return answer;
	}

	/**
	 * Text made safe to sit between two SVG tags.
	 */
	private String escapeXml(String text)
	{
		String noAmpersand = text.replace("&", "&amp;");
		String noLessThan = noAmpersand.replace("<", "&lt;");
		String answer = noLessThan.replace(">", "&gt;");
		return answer;
	}

	/**
	 * The exception a caller throws to answer 404. The reason travels to error.html when
	 * server.error.include-message allows it, so it is written for whoever reads that page.
	 */
	private ResponseStatusException makeNotFound(String format, Object ... args)
	{
		String message = String.format(format, args);
		ResponseStatusException answer = new ResponseStatusException(HttpStatus.NOT_FOUND, message);
		return answer;
	}
}
