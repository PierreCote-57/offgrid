package com.lc.offgrid.webapp.spring.site;

import com.lc.basics.tools.file.BaseFileHandler;
import com.lc.basics.tools.file.BasicFileReader;
import com.lc.offgrid.common.misc.QueryUtil;
import com.lc.offgrid.common.misc.files.AbstractFileManager;
import com.lc.offgrid.common.misc.files.LocalFileManager.*;
import com.lc.offgrid.common.misc.files.ResourceFileManager.*;
import com.lc.offgrid.common.misc.imaging.ImageMetadata;
import com.lc.offgrid.common.misc.imaging.ImageSize;
import com.lc.offgrid.common.pojo.page.BlogPage;
import com.lc.offgrid.common.pojo.page.DestinationPage;
import com.lc.offgrid.common.pojo.page.PageData;
import com.lc.offgrid.common.pojo.part.Dataset;
import com.lc.offgrid.common.pojo.part.GoogleMap;
import com.lc.offgrid.common.pojo.part.Point;
import com.lc.offgrid.webapp.spring.tools.BaseWebController;
import com.lc.offgrid.webapp.spring.tools.BaseWebProcessor;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
	private JsonResourceFileManager jsonManager ;

	@Autowired
	private ImageFileManager imageManager;

	@Autowired
	private DocumentFileManager documentManager;

	public ImageFileManager getImageManager()
	{
		return imageManager;
	}

	public AbstractFileManager getJsonManager()
	{
		return jsonManager;
	}

	public AbstractFileManager getDocumentManager()
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

	public String processBlog(Model model, String path, Class<? extends PageData> clazz)
	{
		String answer = processPage(model, path, clazz);

		List<Map.Entry<String, BlogPage>> pageList = makeBlogList();
		model.addAttribute("blogList", pageList);

		return answer;
	}
	private List<Map.Entry<String, BlogPage>> makeBlogList()
	{
		List<Map.Entry<String, BlogPage>> pageList= new ArrayList<>();
		for (Map.Entry<String, File> entry : getJsonManager().getNameMap().entrySet())
		{
			String name = entry.getKey();
			File file = entry.getValue();
			if (file.getPath().contains("/blog/"))
			{
				BlogPage blogPage = readPageJson(name, BlogPage.class);
				if (null != blogPage.getDate())
				{
					Map.Entry<String, BlogPage> listEntry = new AbstractMap.SimpleEntry<>(name, blogPage);
					pageList.add(listEntry);
				}
				else
				{
					getLogger().warn("Ignoring blog page with missing date: '%s'", name);
				}
			}
		}
		pageList.sort((o1, o2) -> -o1.getValue().getDate().compareTo(o2.getValue().getDate()));
		return pageList;
	}

	/**
	 * The bytes of one image, read from the image folder, which sits outside the resource tree.
	 * A name with no file behind it answers a drawn image that says so, at 200, so the reason
	 * appears where the picture would have been rather than as a broken-image icon.
	 */
	public ResponseEntity<Resource> processImage(String imageName, ImageSize imageSize)
	{
//		imageName = FIXED_IMAGE;
		try
		{
			ImageMetadata metadata = findImageFile(imageName, imageSize);
			File imageFile = metadata.getFile();
			FileSystemResource imageResource = new FileSystemResource(imageFile);
			MediaType mediaType = metadata.getMediaType();

			ResponseEntity<Resource> answer = makeResponseOk(mediaType, imageResource);
			return answer;
		}
		catch (Exception e)
		{
			getLogger().info("Unable to locate image '%s'", imageName);
			Resource messageImage = makeMessageImage("Not found", imageName);
			ResponseEntity<Resource> answer = makeResponseOk(SVG_MEDIA_TYPE, messageImage);
			return answer;
		}
	}

	private ImageMetadata findImageFile(String imageName, ImageSize imageSize)
	{
		ImageFileManager manager = getImageManager();

		ImageMetadata metadata = null;
		for (int iSize = imageSize.ordinal(); iSize < ImageSize.values().length; iSize++)
		{
			ImageSize size = ImageSize.values()[iSize];
			String name = size.getImageName(imageName);
			metadata = manager.getImageMetadata(name);
			if (null != metadata)
			{
				break;
			}
		}

		return metadata;
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
			AbstractFileManager manager = getDocumentManager();
			File documentFile = manager.getFile(documentName);
			FileSystemResource documentResource = new FileSystemResource(documentFile);
			MediaType mediaType = readMediaType(documentFile);

			ResponseEntity<Resource> answer = makeResponseOk(mediaType, documentResource);
			return answer;
		}
		catch (Exception e)
		{
			getLogger().info("Unable to locate document '%s'", documentName);
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
	 * The file is answered as it stands. Resolving a row's pointers into the row itself is
	 * this method's job to come.
	 */
	public ResponseEntity<String> processBrowserData(String id)
	{
		Dataset dataset = findDataset(id);
		if (null == dataset)
		{
			getLogger().info("Unknown dataset: '%s'", id);
			throw makeNotFound("Unknown dataset: %s", id);
		}

		String fileName = String.format("data/shared/browser/%s", dataset.getFile());
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> pageList = BaseFileHandler.readFile(fileName, List.class);
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
		T pageData = BaseFileHandler.readFile(jsonFile.getAbsolutePath(), clazz);

		// Hydrate map as needed
		if (pageData instanceof DestinationPage destinationPage)
		{
			Map<String, GoogleMap> map = destinationPage.getGoogleMap();
			if (null != map)
			{
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
					getLogger().warn("Error in dataset with %,d entries reading file pointer: '%s'",
							pageList.size(), fileText);
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
		Dataset[] datasetList = BaseFileHandler.readFile(fileName, Dataset[].class);

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
