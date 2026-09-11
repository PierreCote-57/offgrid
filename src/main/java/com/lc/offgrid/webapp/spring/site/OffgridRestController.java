package com.lc.offgrid.webapp.spring.site;

import com.lc.offgrid.webapp.pojo.chat.ChatAnswer;
import com.lc.offgrid.webapp.pojo.chat.ChatRequest;
import com.lc.offgrid.webapp.pojo.sky.SkyInfoRestAnswer;
import com.lc.offgrid.webapp.spring.tools.BaseRestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the site's REST calls. Peer of OffgridWebController, which serves the pages: this one
 * answers JSON and names no view. Chat is its first endpoint.
 */
@RestController
@RequestMapping("/rest")
public class OffgridRestController extends BaseRestController
{
	@Autowired
	private BeanFactory beanFactory;

	public BeanFactory getBeanFactory()
	{
		return beanFactory;
	}
	public OffgridRestProcessor getProcessor()
	{
		return getBeanFactory().getBean(OffgridRestProcessor.class);
	}

	@PostMapping("/chat")
	public ResponseEntity<ChatAnswer> chat(HttpServletRequest request, HttpServletResponse response,
			@RequestBody ChatRequest chatRequest)
	{
		return processRequest(request, response, () -> getProcessor().processChat(chatRequest));
	}

	/**
	 * The sky where the caller is, right now. All three parameters are optional, and absent
	 * they are the 50th parallel marker in Campbell River in its own zone. They arrive as text
	 * so a value that does not parse falls back rather than failing the request.
	 */
	@GetMapping("/sky/data")
	public ResponseEntity<SkyInfoRestAnswer> skyData(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required = false) String timezone,
			@RequestParam(required = false) String lat,
			@RequestParam(required = false) String lng)
	{
		return processRequest(request, response, () -> getProcessor().processSkyData(timezone, lat, lng));
	}
}
