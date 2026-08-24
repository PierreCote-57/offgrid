package com.logicielcote.offgrid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the site's pages. */
@Controller
public class ClientController {

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("headline", "offgrid");
		var viewName = "index";
		return viewName;
	}
}
