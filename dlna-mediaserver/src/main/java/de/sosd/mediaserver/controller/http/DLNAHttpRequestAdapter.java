package de.sosd.mediaserver.controller.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.LastModified;

@Service
public class DLNAHttpRequestAdapter implements HandlerAdapter {

	@Override
	public boolean supports(final Object handler) {
		return (handler instanceof DLNAHttpRequestHandler);
	}

	@Override
	public ModelAndView handle(final HttpServletRequest request,
			final HttpServletResponse response, final Object handler) throws Exception {

		((DLNAHttpRequestHandler) handler).handleRequest(request, response);
		return null;
	}

	@Override
	public long getLastModified(final HttpServletRequest request, final Object handler) {
		if (handler instanceof LastModified) {
			return ((LastModified) handler).getLastModified(request);
		}
		return -1L;

	}
}
