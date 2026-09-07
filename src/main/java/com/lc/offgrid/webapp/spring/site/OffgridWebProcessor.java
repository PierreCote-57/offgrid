package com.lc.offgrid.webapp.spring.site;

import com.lc.basics.tools.file.BaseFileHandler;
import com.lc.basics.tools.file.BasicFileReader;
import com.lc.offgrid.common.misc.QueryUtil;
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
import com.lc.offgrid.common.pojo.part.SkyChart;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope("prototype")
public class OffgridWebProcessor extends BaseWebProcessor
{
	private static final MediaType SVG_MEDIA_TYPE = MediaType.valueOf("image/svg+xml");

	private static final String SKY_TEMPLATE = "fragments/block/sky";
	private static final String SKY_CHART_FRAGMENT = "chart";

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

	public String processSky(Model model, String path, Class<? extends PageData> clazz)
	{
		String answer = processPage(model, path, clazz);

		SkyChart skyChart = makeSkyChart();
		model.addAttribute("skyChart", skyChart);

		return answer;
	}

	public ResponseEntity<Resource> processSkyChart()
	{
		SkyChart skyChart = makeSkyChart();
		Resource chartResource = makeChartImage(skyChart);

		ResponseEntity<Resource> answer = makeResponseOk(SVG_MEDIA_TYPE, chartResource);
		return answer;
	}

	/**
	 * The chart fragment rendered on its own, as the bytes of a standalone SVG document.
	 */
	private Resource makeChartImage(SkyChart skyChart)
	{
		Context context = new Context();
		context.setVariable("skyChart", skyChart);

		String svgText = getTemplateEngine().process(SKY_TEMPLATE, Set.of(SKY_CHART_FRAGMENT), context);
		byte[] svgBytes = svgText.getBytes(StandardCharsets.UTF_8);

		ByteArrayResource answer = new ByteArrayResource(svgBytes);
		return answer;
	}

	/**
	 * The chart for 10 September 2026, with made-up rise, transit and set times. Every number
	 * here is a constant until the astronomy is written.
	 */
	private SkyChart makeSkyChart()
	{
		List<SkyBody> bodyList = new ArrayList<>();

		SkyBody sun = addSkyBody(bodyList, "Sun", null, 0, 215, 195, 9, "c-sun");
		sun.setRises("06:41");
		sun.setTransit("13:32 (46\u00b0)");
		sun.setSets("20:21");

		SkyBody earth = addSkyBody(bodyList, "Earth", sun, 60, 273.4, 208.6, 4.5, "c-coral");
		earth.setLabel("Earth+Moon", 273.4, 193.6, "middle");
		earth.setArrowheadPoints("211.1,255.0 206.4,257.0 207.0,251.9");
		earth.setOrbitPeriod(365.26);

		SkyBody moon = addSkyBody(bodyList, "Moon", earth, 10, 264.6, 203.9, 2.5, "c-moon");
		moon.setArrowheadPoints("283.4,211.1 283.6,215.1 280.0,213.3");
		moon.setOrbitPeriod(27.32);
		moon.setRises("22:07");
		moon.setTransit("05:14 (52\u00b0)");
		moon.setSets("13:26");

		SkyBody mercury = addSkyBody(bodyList, "Mercury", sun, 20, 197.1, 204.0, 4.5, "c-coral");
		mercury.setLabel("Mercury", 197.1, 219.5, "middle");
		mercury.setArrowheadPoints("215.7,215.2 210.9,217.3 211.5,212.1");
		mercury.setOrbitPeriod(87.97);
		mercury.setRises("07:18");
		mercury.setTransit("13:55 (44\u00b0)");
		mercury.setSets("20:33");

		SkyBody venus = addSkyBody(bodyList, "Venus", sun, 40, 245.7, 220.6, 4.5, "c-coral");
		venus.setLabel("Venus", 245.7, 236.1, "middle");
		venus.setArrowheadPoints("213.4,235.1 208.7,237.2 209.2,232.0");
		venus.setOrbitPeriod(224.70);
		venus.setRises("05:12");
		venus.setTransit("12:24 (41\u00b0)");
		venus.setSets("19:37");

		SkyBody mars = addSkyBody(bodyList, "Mars", sun, 80, 236.2, 117.9, 4.5, "c-coral");
		mars.setLabel("Mars", 236.2, 108.4, "middle");
		mars.setArrowheadPoints("208.9,274.8 204.1,276.9 204.7,271.7");
		mars.setOrbitPeriod(686.98);
		mars.setRises("09:04");
		mars.setTransit("15:11 (43\u00b0)");
		mars.setSets("21:19");

		SkyBody jupiter = addSkyBody(bodyList, "Jupiter", sun, 110, 145.1, 110.1, 5.5, "c-amber");
		jupiter.setLabel("Jupiter", 145.1, 99.6, "middle");
		jupiter.setArrowheadPoints("205.4,304.6 200.7,306.7 201.3,301.5");
		jupiter.setOrbitPeriod(4332.59);
		jupiter.setRises("01:47");
		jupiter.setTransit("09:33 (57\u00b0)");
		jupiter.setSets("17:20");

		SkyBody saturn = addSkyBody(bodyList, "Saturn", sun, 130, 343.0, 172.1, 5.5, "c-amber");
		saturn.setLabel("Saturn", 343.0, 161.6, "middle");
		saturn.setArrowheadPoints("203.2,324.5 198.4,326.6 199.0,321.4");
		saturn.setOrbitPeriod(10759.22);
		saturn.setRises("19:52");
		saturn.setTransit("01:38 (32\u00b0)");
		saturn.setSets("07:26");

		SkyBody uranus = addSkyBody(bodyList, "Uranus", sun, 150, 284.5, 62.1, 5, "c-teal");
		uranus.setLabel("Uranus", 284.5, 52.1, "middle");
		uranus.setArrowheadPoints("200.9,344.4 196.1,346.4 196.7,341.3");
		uranus.setOrbitPeriod(30688.5);
		uranus.setRises("22:41");
		uranus.setTransit("06:12 (61\u00b0)");
		uranus.setSets("13:44");

		SkyBody neptune = addSkyBody(bodyList, "Neptune", sun, 170, 384.8, 187.5, 5, "c-teal");
		neptune.setLabel("Neptune", 384.8, 203.5, "middle");
		neptune.setArrowheadPoints("198.6,364.2 193.8,366.3 194.4,361.1");
		neptune.setOrbitPeriod(60182.0);
		neptune.setRises("19:33");
		neptune.setTransit("01:14 (35\u00b0)");
		neptune.setSets("06:57");

		SkyChart skyChart = new SkyChart();
		skyChart.setViewBoxWidth(430);
		skyChart.setViewBoxHeight(420);
		skyChart.setTitle("Heliocentric positions of the eight planets and the Moon on 10 September 2026");
		skyChart.setDescription("Top-down view from ecliptic north. The Sun is at the centre; each planet sits on"
				+ " its own orbit circle at its computed heliocentric ecliptic longitude. Arrowheads show the"
				+ " direction of travel, counter-clockwise in this view. The Moon is the small white dot on its"
				+ " own circle around Earth. Not to scale.");
		skyChart.setCaption("Not to scale");
		skyChart.setCaptionX(215);
		skyChart.setCaptionY(405);
		skyChart.setBodyList(bodyList);
		skyChart.setLatitude(50.0);
		skyChart.setLongitude(-125.27);
		skyChart.setTimeZoneName("America/Vancouver");
		skyChart.setDateText("10 September 2026");

		return skyChart;
	}

	private SkyBody addSkyBody(List<SkyBody> bodyList, String name, SkyBody parent, double orbitRadius,
			double dotX, double dotY, double dotRadius, String colourClass)
	{
		SkyBody skyBody = new SkyBody();
		skyBody.setName(name);
		skyBody.setParent(parent);
		skyBody.setOrbitRadius(orbitRadius);
		skyBody.setDotX(dotX);
		skyBody.setDotY(dotY);
		skyBody.setDotRadius(dotRadius);
		skyBody.setColourClass(colourClass);

		bodyList.add(skyBody);
		return skyBody;
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
