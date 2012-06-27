package de.sosd.mediaserver.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

public class DLNAContentDirectoryEventServlet extends DLNAEventServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private final static Log logger = LogFactory.getLog(DLNAContentDirectoryEventServlet.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);

	private static String uuid;
	
	
	@Override
	public void doNotify(final HttpServletRequest request,
			final HttpServletResponse response) {
		// TODO Auto-generated method stub
		response.addHeader("Server", "UPnP/1.0 UPnP-Device-Host/1.0");
		response.addHeader("Content-Length", "0");
		response.setStatus(HttpStatus.OK.value());
	}

	@Override
	public void doUnsubscribe(final HttpServletRequest request,
			final HttpServletResponse response) {
		// TODO Auto-generated method stub
		response.addHeader("Server", "UPnP/1.0 UPnP-Device-Host/1.0");
		response.addHeader("Content-Length", "0");
		response.setStatus(HttpStatus.OK.value());
	}

	@Override
	public void doSubscribe(final HttpServletRequest request,
			final HttpServletResponse response) {
//		@SuppressWarnings("rawtypes")
//		Enumeration headerNames = request.getHeaderNames();
//		StringBuffer sb = new StringBuffer("subscription on ContentDirectory from: " + request.getRemoteAddr() + "\n");
//		while (headerNames.hasMoreElements()) {
//			String headerName = (String) headerNames.nextElement();
//			sb.append(headerName + ": " + request.getHeader(headerName) + "\n");
//		}
//		logger.debug(sb.toString());		
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		response.addHeader("Server", "UPnP/1.0 UPnP-Device-Host/1.0");
		response.addHeader("Timeout", "Second-300");
		response.addHeader("SID", "uuid:" + getUSN());
		response.addHeader("Date", sdf.format(new Date(System.currentTimeMillis())) + " GMT");
		response.addHeader("Connection", "close");
		response.addHeader("Content-Length", "0");
		response.setStatus(HttpStatus.OK.value());
	}

	public static void setUSN(final String usn) {
		uuid = usn;
	}
	
	
	public static String getUSN() {
		// TODO Auto-generated method stub
		return uuid;
	}

}
