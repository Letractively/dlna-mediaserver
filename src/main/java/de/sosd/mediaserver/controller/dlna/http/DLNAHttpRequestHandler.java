package de.sosd.mediaserver.controller.dlna.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

@Component
public class DLNAHttpRequestHandler implements Controller {

    private final static Log logger = LogFactory
                                            .getLog(DLNAHttpRequestHandler.class);

    @Override
    public ModelAndView handleRequest(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        final String header = request.getHeader("Callback");
        logger.debug("callback accepted : " + header);

        response.setStatus(HttpStatus.OK.value());
        return null;
    }

}
