package com.lc.offgrid.webapp.spring.site;

import com.lc.basics.tools.file.BaseFileHandler;
import com.lc.basics.tools.file.BasicFileReader;
import com.lc.offgrid.common.misc.QueryUtil;
import com.lc.offgrid.common.misc.sky.SkyDataMaker;
import com.lc.offgrid.common.misc.files.AbstractFileManager;
import com.lc.offgrid.common.misc.files.LocalFileManager.*;
import com.lc.offgrid.common.misc.files.ResourceFileManager.*;
import com.lc.offgrid.common.misc.imaging.ImageMetadata;
import com.lc.offgrid.common.misc.imaging.ImageSize;
import com.lc.offgrid.common.pojo.page.DestinationPage;
import com.lc.offgrid.common.pojo.page.PageData;
import com.lc.offgrid.common.pojo.part.Dataset;
import com.lc.offgrid.common.pojo.part.GoogleMap;
import com.lc.offgrid.common.pojo.part.Point;
import com.lc.offgrid.common.pojo.part.SkyBody;
import com.lc.offgrid.common.pojo.part.SkyData;
import com.lc.offgrid.common.pojo.part.SkyDataChart;
import com.lc.offgrid.common.pojo.part.SkyDataTable;
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
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope("prototype")
public class OffgridWebProcessor extends BaseWebProcessor
{
	private static final MediaType SVG_MEDIA_TYPE = MediaType.valueOf("image/svg+xml");

	private static final String SKY_TEMPLATE = "fragments/block/sky-fragment";
	private static final String SKY_CHART_FRAGMENT = "chart";

	/** The 50th parallel marker in Campbell River, until the browser says otherwise. */
	private static final double SKY_LATITUDE = 50.0;
	private static final double SKY_LONGITUDE = -125.230450;
	private static final String SKY_TIME_ZONE = "America/Vancouver";

	/** The chart's width when the caller states none, and the range it will draw at. */
	private static final String SKY_CHART_CAPTION = "Not to scale";
	private static final int SKY_CHART_WIDTH = 430;
	private static final int MINIMUM_CHART_WIDTH = 200;
	private static final int MAXIMUM_CHART_WIDTH = 2000;

	private static final double MAXIMUM_LATITUDE = 90.0;
	private static final double MAXIMUM_LONGITUDE = 180.0;

	/** Rise, transit and set, made up, until the astronomy for them is written. */
	private static final Map<String, String[]> PLACEHOLDER_TIMES = Map.of(
			"Sun", new String[] {"06:41", "13:32 (46°)", "20:21"},
			"Moon", new String[] {"22:07", "05:14 (52°)", "13:26"},
			"Mercury", new String[] {"07:18", "13:55 (44°)", "20:33"},
			"Venus", new String[] {"05:12", "12:24 (41°)", "19:37"},
			"Mars", new String[] {"09:04", "15:11 (43°)", "21:19"},
			"Jupiter", new String[] {"01:47", "09:33 (57°)", "17:20"},
			"Saturn", new String[] {"19:52", "01:38 (32°)", "07:26"},
			"Uranus", new String[] {"22:41", "06:12 (61°)", "13:44"},
			"Neptune", new String[] {"19:33", "01:14 (35°)", "06:57"});

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

	@Autowired
	private SpringTemplateEngine templateEngine;

	public SpringTemplateEngine getTemplateEngine()
	{
		return templateEngine;
	}

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

	public String processSky(Model model, String path, Class<? extends PageData> clazz,
			String latitudeText, String longitudeText)
	{
		String answer = processPage(model, path, clazz);

		double latitude = readCoordinate(latitudeText, SKY_LATITUDE, MAXIMUM_LATITUDE);
		double longitude = readCoordinate(longitudeText, SKY_LONGITUDE, MAXIMUM_LONGITUDE);

		SkyDataTable skyTable = makeSkyTable(latitude, longitude);
		model.addAttribute("skyTable", skyTable);

		return answer;
	}

	/**
	 * One coordinate off the query string, or the default when it is missing, unparseable or
	 * off the globe. A hand-edited URL gets the marker back and a page, not an error.
	 */
	private double readCoordinate(String text, double defaultValue, double limit)
	{
		if (null == text || text.isBlank())
		{
			return defaultValue;
		}

		double value;
		try
		{
			value = Double.parseDouble(text.trim());
		}
		catch (NumberFormatException failure)
		{
			getLogger().info("Sky page: coordinate %s does not parse, using %s", text, defaultValue);
			return defaultValue;
		}

		if (value < -limit || value > limit)
		{
			getLogger().info("Sky page: coordinate %s is outside %s, using %s", text, limit, defaultValue);
			return defaultValue;
		}
		return value;
	}

	public ResponseEntity<Resource> processSkyChart(String widthText)
	{
		int width = readWidth(widthText);

		SkyDataChart skyChart = makeSkyChart();
		Resource chartResource = makeChartImage(skyChart, width);

		ResponseEntity<Resource> answer = makeResponseOk(SVG_MEDIA_TYPE, chartResource);
		return answer;
	}

	/**
	 * The chart fragment rendered on its own, as the bytes of a standalone SVG document.
	 */
	private Resource makeChartImage(SkyDataChart skyChart, int width)
	{
		Context context = new Context();
		context.setVariable("skyChart", skyChart);
		context.setVariable("width", width);

		String svgText = getTemplateEngine().process(SKY_TEMPLATE, Set.of(SKY_CHART_FRAGMENT), context);
		byte[] svgBytes = svgText.getBytes(StandardCharsets.UTF_8);

		ByteArrayResource answer = new ByteArrayResource(svgBytes);
		return answer;
	}

	/**
	 * A width off the query string, or the default when it is missing, unparseable or outside
	 * what the drawing is legible at. A hand-edited URL gets a chart, not an error.
	 */
	private int readWidth(String text)
	{
		if (null == text || text.isBlank())
		{
			return SKY_CHART_WIDTH;
		}

		int width;
		try
		{
			width = Integer.parseInt(text.trim());
		}
		catch (NumberFormatException failure)
		{
			getLogger().info("Sky chart: width %s does not parse, using %s", text, SKY_CHART_WIDTH);
			return SKY_CHART_WIDTH;
		}

		if (width < MINIMUM_CHART_WIDTH || width > MAXIMUM_CHART_WIDTH)
		{
			getLogger().info("Sky chart: width %s is outside %s to %s, using %s",
					width, MINIMUM_CHART_WIDTH, MAXIMUM_CHART_WIDTH, SKY_CHART_WIDTH);
			return SKY_CHART_WIDTH;
		}
		return width;
	}

	/**
	 * The chart for today. It is heliocentric, so it takes no observer.
	 */
	private SkyDataChart makeSkyChart()
	{
		LocalDate date = today();

		SkyDataMaker skyDataMaker = new SkyDataMaker();
		List<SkyBody> bodyList = skyDataMaker.makeBodyList(date);

		SkyDataChart skyChart = new SkyDataChart();
		skyChart.setDate(date);
		skyChart.setBodyList(bodyList);
		skyChart.setCaption(SKY_CHART_CAPTION);

		return skyChart;
	}

	/**
	 * The table for today: where the visitor says they are, the clock they read, and the same
	 * bodies the chart draws. The times on them are still made up.
	 */
	private SkyDataTable makeSkyTable(double latitude, double longitude)
	{
		LocalDate date = today();

		SkyDataMaker skyDataMaker = new SkyDataMaker();
		List<SkyBody> bodyList = skyDataMaker.makeBodyList(date);

		SkyDataTable skyTable = new SkyDataTable();
		skyTable.setDate(date);
		skyTable.setBodyList(bodyList);
		skyTable.setLatitude(latitude);
		skyTable.setLongitude(longitude);
		skyTable.setTimeZone(ZoneId.of(SKY_TIME_ZONE));

		applyPlaceholderTimes(skyTable);
		return skyTable;
	}

	private LocalDate today()
	{
		ZoneId timeZone = ZoneId.of(SKY_TIME_ZONE);
		LocalDate date = LocalDate.now(timeZone);
		return date;
	}

	/**
	 * Made-up rise, transit and set times, keyed by body name, until the astronomy for them is
	 * written.
	 */
	private void applyPlaceholderTimes(SkyData skyData)
	{
		for (SkyBody skyBody : skyData.getBodyList())
		{
			String[] timeList = PLACEHOLDER_TIMES.get(skyBody.getName());
			if (null == timeList)
			{
				continue;
			}
			skyBody.setRises(timeList[0]);
			skyBody.setTransit(timeList[1]);
			skyBody.setSets(timeList[2]);
		}
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
			ImageFileManager manager = getImageManager();
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
			AbstractFileManager manager = getDocumentManager();
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
