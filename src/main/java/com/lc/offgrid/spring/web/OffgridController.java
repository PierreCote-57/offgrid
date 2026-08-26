package com.lc.offgrid.spring.web;

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
		return getBeanFactory().getBean(OffgridProcessor.class);
	}

	@GetMapping("/")
	public String home(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model)
	{
		return processRequest(request, response, model, () -> getService().processHome(model));
	}

	@GetMapping("/info/{name}")
	public String useful(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model,
			@PathVariable String name)
	{
		return processRequest(request, response, model, () -> getService().processInfo(model, name));
	}

	@GetMapping("/hardware/{name}")
	public String hardware(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model,
			@PathVariable String name)
	{
		return processRequest(request, response, model, () -> getService().processHardware(model, name));
	}

	@GetMapping("/hardware/checklists/{name}")
	public String checklist(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model,
			@PathVariable String name)
	{
		return processRequest(request, response, model, () -> getService().processChecklist(model, name));
	}

	@GetMapping("/hardware/howto/{name}")
	public String howto(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model,
			@PathVariable String name)
	{
		return processRequest(request, response, model, () -> getService().processHowto(model, name));
	}

	@GetMapping("/hardware/maintenance/{name}")
	public String maintenance(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model,
			@PathVariable String name)
	{
		return processRequest(request, response, model, () -> getService().processMaintenance(model, name));
	}

	@GetMapping("/posts/{name}")
	public String post(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model,
			@PathVariable String name)
	{
		return processRequest(request, response, model, () -> getService().processPost(model, name));
	}

	@GetMapping("/shared/browser")
	public String browser(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model)
	{
		return processRequest(request, response, model, () -> getService().processBrowser(model));
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
	public ResponseEntity<String> browserData(
			@PathVariable String id)
	{
		return getService().processBrowserData(id);
	}

	/**
	 * An image, straight from the image folder. This one answers with bytes rather than a view
	 * name, so it goes to the processor directly instead of through processRequest.
	 */
	@GetMapping("/image/{imageName}")
	public ResponseEntity<Resource> image(
			@PathVariable String imageName)
	{
		return getService().processImage(imageName);
	}

	@GetMapping(value = {"/pi"}, produces = "text/html")
	public String pi(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model)
	{
		return processRequest(request, response, model, () -> getService().processPi(model));
	}

}
