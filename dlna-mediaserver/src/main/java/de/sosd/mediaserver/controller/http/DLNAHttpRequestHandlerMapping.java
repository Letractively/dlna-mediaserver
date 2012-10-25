package de.sosd.mediaserver.controller.http;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

@Service
public class DLNAHttpRequestHandlerMapping extends AbstractHandlerMapping {

	@Autowired
	private DLNAHttpRequestHandler handler;
	
	@Override
	protected Object getHandlerInternal(final HttpServletRequest request)
			throws Exception {
		if ("SUBSCRIBE".equals(request.getMethod())) {
			return this.handler;
		}
		return null;
	}

	
	
	
}
