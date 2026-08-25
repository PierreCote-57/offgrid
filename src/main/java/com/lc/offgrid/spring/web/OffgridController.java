package com.lc.offgrid.spring.web;

import com.lc.offgrid.spring.tools.BaseWebController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	private OffgridProcessor m_service;

	@Autowired
	private BeanFactory m_beanFactory;

	public BeanFactory getBeanFactory()
	{
		return m_beanFactory;
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

	@GetMapping("/about")
	public String about(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model)
	{
		return processRequest(request, response, model, () -> getService().processAbout(model));
	}

	@GetMapping("/about/useful-links")
	public String usefulLinks(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model)
	{
		return processRequest(request, response, model, () -> getService().processUsefulLinks(model));
	}

	@GetMapping("/about/useful-contacts")
	public String usefulContacts(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model)
	{
		return processRequest(request, response, model, () -> getService().processUsefulContacts(model));
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

	@GetMapping(value = {"/pi"}, produces = "text/html")
	public String pi(
			HttpServletRequest request,
			HttpServletResponse response,
			Model model)
	{
		return processRequest(request, response, model, () -> getService().processPi(model));
	}

}
