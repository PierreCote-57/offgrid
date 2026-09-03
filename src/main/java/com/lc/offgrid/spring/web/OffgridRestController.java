package com.lc.offgrid.spring.web;

import com.lc.offgrid.pojo.chat.ChatAnswer;
import com.lc.offgrid.pojo.chat.ChatRequest;
import com.lc.offgrid.spring.tools.BaseRestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
