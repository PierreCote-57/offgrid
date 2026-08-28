package com.lc.offgrid.spring.web;

import com.lc.offgrid.pojo.page.PageData;
import com.lc.offgrid.spring.tools.BaseWebController;
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

/**
 * Serves the site's pages.
 */
@Controller
@RequestMapping({"/", "/admin"})
public class OffgridController extends BaseWebController
{
	@Autowired
	private OffgridProcessor service;

	@Autowired
	private BeanFactory beanFactory;

	public BeanFactory getBeanFactory()
	{
		return beanFactory;
	}
	public OffgridProcessor getService()
	{
		return service;
	}

	@GetMapping("/")
	public String home(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		String path = String.format("/index");
		return processRequest(request, response, model, () -> getService().processPage(model, path, PageData.class));
	}

	@GetMapping("/destinations/{type}/{name}")
	public String destination(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable String type, @PathVariable String name)
	{
		String path = String.format("/destinations/%1$s/%2$s", type, name);
		return processRequest(request, response, model, () -> getService().processPage(model, path, PageData.class));
	}

	@GetMapping("/hardware/checklists/{name}")
	public String checklist(HttpServletRequest request, HttpServletResponse response, Model model, @PathVariable String name)
	{
		String path = String.format("/hardware/checklists/%1$s", name);
		return processRequest(request, response, model, () -> getService().processPage(model, path, PageData.class));
	}

	@GetMapping("/hardware/howto/{name}")
	public String howto(HttpServletRequest request, HttpServletResponse response, Model model, @PathVariable String name)
	{
		String path = String.format("/hardware/howto/%1$s", name);
		return processRequest(request, response, model, () -> getService().processPage(model, path, PageData.class));
	}

	@GetMapping("/hardware/maintenance/{name}")
	public String maintenance(HttpServletRequest request, HttpServletResponse response, Model model, @PathVariable String name)
	{
		String path = String.format("/hardware/maintenance/%1$s", name);
		return processRequest(request, response, model, () -> getService().processPage(model, path, PageData.class));
	}

	@GetMapping("/posts/{name}")
	public String post(HttpServletRequest request, HttpServletResponse response, Model model, @PathVariable String name)
	{
		String path = String.format("/post/%1$s", name);
		return processRequest(request, response, model, () -> getService().processPage(model, path, PageData.class));
	}

	@GetMapping("/info/{name}")
	public String useful(HttpServletRequest request, HttpServletResponse response, Model model, @PathVariable String name)
	{
		String path = String.format("/info/%1$s", name);
		return processRequest(request, response, model, () -> getService().processPage(model, path, PageData.class));
	}

	@GetMapping("/shared/browser")
	public String browser(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		String path = String.format("/shared/%1$s", "browser");
		return processRequest(request, response, model, () -> getService().processPage(model, path, PageData.class));
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
		return getService().processBrowserData(id);
	}

	/**
	 * An image, straight from the image folder. This one answers with bytes rather than a view
	 * name, so it goes to the processor directly instead of through processRequest.
	 */
	@GetMapping("/image/{imageName}")
	public ResponseEntity<Resource> image(@PathVariable String imageName)
	{
		return getService().processImage(imageName);
	}

	@GetMapping(value = {"/pi"}, produces = "text/html")
	public String pi(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		String path = String.format("/info/%s", "pi");
		return processRequest(request, response, model, () -> getService().processPage(model, path, PageData.class));
	}

}
