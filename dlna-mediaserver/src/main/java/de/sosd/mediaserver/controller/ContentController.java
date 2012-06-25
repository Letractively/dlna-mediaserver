package de.sosd.mediaserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("content/*")
public class ContentController {
	
	private static final Logger logger = LoggerFactory.getLogger(ContentController.class);
	
	@RequestMapping(value = "show", method = RequestMethod.GET)
	public RedirectView content() {
		logger.debug("content");
		return new RedirectView("/manager", true);
	}
	
}
