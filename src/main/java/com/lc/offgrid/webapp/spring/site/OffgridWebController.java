package com.lc.offgrid.webapp.spring.site;

import com.lc.offgrid.common.misc.imaging.ImageSize;
import com.lc.offgrid.common.pojo.page.CampsitePage;
import com.lc.offgrid.common.pojo.page.DestinationPage;
import com.lc.offgrid.common.pojo.page.LakePage;
import com.lc.offgrid.common.pojo.page.MaintenancePage;
import com.lc.offgrid.common.pojo.page.PageData;
import com.lc.offgrid.common.pojo.page.PostPage;
import com.lc.offgrid.webapp.spring.tools.BaseWebController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Serves the site's pages.
 */
@Controller
@RequestMapping({"/", "/admin"})
public class OffgridWebController extends BaseWebController
{
	@Autowired
	private BeanFactory beanFactory;

	public BeanFactory getBeanFactory()
	{
		return beanFactory;
	}
	public OffgridWebProcessor getProcessor()
	{
		return getBeanFactory().getBean(OffgridWebProcessor.class);
	}

	private Class<? extends PageData> getPageDataClass(String type)
	{
		Class<? extends PageData> pageDataClass = switch (type)
		{
			case "howto" -> PageData.class;
			case "checklist" -> PageData.class;
			case "maintenance" -> MaintenancePage.class;
			case "lake" -> LakePage.class;
			case "park" -> DestinationPage.class;
			case "rec-site" -> CampsitePage.class;
			case "campground" -> CampsitePage.class;
			default -> DestinationPage.class;
		};
		return  pageDataClass;
	}

	@GetMapping("/")
	public String home(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		String path = String.format("/index");
		return processRequest(request, response, model, () -> getProcessor().processPage(model, path, PageData.class));
	}

	@GetMapping("/destinations/{type}/{name}")
	public String destination(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable String type, @PathVariable String name)
	{
		String path = String.format("/destinations/%1$s/%2$s", type, name);
		Class<? extends PageData> clazz = getPageDataClass(type);
		return processRequest(request, response, model, () -> getProcessor().processPage(model, path, clazz));
	}

	@GetMapping("/hardware/{name}")
	public String checklist(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable String name)
	{
		String path = String.format("/hardware/%1$s", name);
		return processRequest(request, response, model, () -> getProcessor().processPage(model, path, PageData.class));
	}
	@GetMapping("/hardware/{type}/{name}")
	public String hardwareType(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable String type, @PathVariable String name)
	{
		String path = String.format("/hardware/%1$s/%2$s", type, name);
		Class<? extends PageData> clazz = getPageDataClass(type);
		return processRequest(request, response, model, () -> getProcessor().processPage(model, path, clazz));
	}

	@GetMapping("/posts/{name}")
	public String post(HttpServletRequest request, HttpServletResponse response, Model model, @PathVariable String name)
	{
		String path = String.format("/post/%1$s", name);
		return processRequest(request, response, model, () -> getProcessor().processPage(model, path, PostPage.class));
	}

	/**
	 * The sky page, for an observer. The coordinates are optional, and absent they are the
	 * 50th parallel marker in Campbell River; they arrive as text so a value that does not
	 * parse falls back to the marker rather than failing the request.
	 */
	@GetMapping("/info/sky")
	public String sky(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam(required = false) String latitude,
			@RequestParam(required = false) String longitude)
	{
		String path = String.format("/info/%1$s", "sky");
		return processRequest(request, response, model,
				() -> getProcessor().processSky(model, path, PageData.class, latitude, longitude));
	}

	/**
	 * The sky chart, as an image of its own, drawn at the width asked for. It answers bytes
	 * rather than a view name, so it goes to the processor directly instead of through
	 * processRequest.
	 */
	@GetMapping("/sky/chart.svg")
	public ResponseEntity<Resource> skyChart(HttpServletRequest request,
			@RequestParam(required = false) String width)
	{
		ResponseEntity<Resource> response = getProcessor().processSkyChart(width);
		logVisit(request, "chart-" + width, System.nanoTime());
		return response;
	}

	@GetMapping("/info/{name}")
	public String info(HttpServletRequest request, HttpServletResponse response, Model model, @PathVariable String name)
	{
		String path = String.format("/info/%1$s", name);
		return processRequest(request, response, model, () -> getProcessor().processPage(model, path, PageData.class));
	}

	@GetMapping("/shared/browser")
	public String browser(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam String dataset)
	{
		String path = String.format("/shared/%1$s", "browser");
		return processRequest(request, response, model, () -> getProcessor().processBrowser(model, path, PageData.class, dataset));
	}

	/**
	 * The rows the browser page draws. This one answers JSON rather than a view name, so it
	 * goes to the processor directly instead of through processRequest.
	 *
	 * The path segment keeps it clear of /shared/browser/datasets.json, which is a static file:
	 * a {id} mapping at that level would shadow it, since a controller mapping outranks the
	 * static resource handler.
	 */
	@GetMapping("/shared/browser/data/{id}")
	public ResponseEntity<String> browserData(@PathVariable String id)
	{
		return getProcessor().processBrowserData(id);
	}

	/**
	 * An image, straight from the image folder. This one answers with bytes rather than a view
	 * name, so it goes to the processor directly instead of through processRequest.
	 */
	@GetMapping("/image/{imageName}")
	public ResponseEntity<Resource> image(@PathVariable String imageName, @RequestParam(required = false) String size)
	{
		ImageSize imageSize = ImageSize.of(size);
		return getProcessor().processImage(imageName, imageSize);
	}

	@GetMapping("/document/{documentName}")
	public ResponseEntity<Resource> document(@PathVariable String documentName)
	{
		return getProcessor().processDocument(documentName);
	}

	@GetMapping(value = {"/pi"}, produces = "text/html")
	public String pi(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		String path = String.format("/info/%s", "pi");
		return processRequest(request, response, model, () -> getProcessor().processPage(model, path, PageData.class));
	}
}
