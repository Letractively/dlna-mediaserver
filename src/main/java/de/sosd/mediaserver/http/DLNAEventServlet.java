/**
 * 
 */
package de.sosd.mediaserver.http;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author simon
 * 
 */
public abstract class DLNAEventServlet extends HttpServlet {

    /**
	 * 
	 */
    private static final long     serialVersionUID  = 1L;
    private static final String   METHOD_DELETE     = "DELETE";
    private static final String   METHOD_HEAD       = "HEAD";
    private static final String   METHOD_GET        = "GET";
    private static final String   METHOD_OPTIONS    = "OPTIONS";
    private static final String   METHOD_POST       = "POST";
    private static final String   METHOD_PUT        = "PUT";
    private static final String   METHOD_TRACE      = "TRACE";

    private static final String   HEADER_IFMODSINCE = "If-Modified-Since";
    private static final String   HEADER_LASTMOD    = "Last-Modified";

    private static final String   LSTRING_FILE      =
                                                            "javax.servlet.http.LocalStrings";
    private static ResourceBundle lStrings          =
                                                            ResourceBundle
                                                                    .getBundle(LSTRING_FILE);

    /*
     * Sets the Last-Modified entity header field, if it has not already been
     * set and if the value is meaningful. Called before doGet, to ensure that
     * headers are set before response data is written. A subclass might have
     * set this header already, so we check.
     */

    private void maybeSetLastModified(final HttpServletResponse resp,
            final long lastModified) {
        if (resp.containsHeader(HEADER_LASTMOD)) {
            return;
        }
        if (lastModified >= 0) {
            resp.setDateHeader(HEADER_LASTMOD, lastModified);
        }
    }

    @Override
    /**
     *
     * Dispatches client requests to the protected
     * <code>service</code> method. There's no need to
     * override this method.
     *
     * 
     * @param req	the {@link HttpServletRequest} object that
     *			contains the request the client made of
     *			the servlet
     *
     *
     * @param res	the {@link HttpServletResponse} object that
     *			contains the response the servlet returns
     *			to the client				
     *
     *
     * @exception IOException	if an input or output error occurs
     *				while the servlet is handling the
     *				HTTP request
     *
     * @exception ServletException	if the HTTP request cannot
     *					be handled
     *
     * 
     * @see javax.servlet.Servlet#service
     *
     */
    public void service(final ServletRequest req, final ServletResponse res)
            throws ServletException, IOException {
        HttpServletRequest request;
        HttpServletResponse response;

        try {
            request = (HttpServletRequest) req;
            response = (HttpServletResponse) res;
        } catch (final ClassCastException e) {
            throw new ServletException("non-HTTP request or response");
        }
        service(request, response);
    }

    /**
     * 
     * Receives standard HTTP requests from the public <code>service</code>
     * method and dispatches them to the <code>do</code><i>XXX</i> methods
     * defined in this class. This method is an HTTP-specific version of the
     * {@link javax.servlet.Servlet#service} method. There's no need to override
     * this method.
     * 
     * 
     * 
     * @param req
     *            the {@link HttpServletRequest} object that contains the
     *            request the client made of the servlet
     * 
     * 
     * @param resp
     *            the {@link HttpServletResponse} object that contains the
     *            response the servlet returns to the client
     * 
     * 
     * @exception IOException
     *                if an input or output error occurs while the servlet is
     *                handling the HTTP request
     * 
     * @exception ServletException
     *                if the HTTP request cannot be handled
     * 
     * @see javax.servlet.Servlet#service
     * 
     */

    @Override
    protected void service(final HttpServletRequest req,
            final HttpServletResponse resp)
            throws ServletException, IOException {
        final String method = req.getMethod();

        if (method.equals(METHOD_GET)) {
            final long lastModified = getLastModified(req);
            if (lastModified == -1) {
                // servlet doesn't support if-modified-since, no reason
                // to go through further expensive logic
                doGet(req, resp);
            } else {
                final long ifModifiedSince = req
                        .getDateHeader(HEADER_IFMODSINCE);
                if (ifModifiedSince < lastModified / 1000 * 1000) {
                    // If the servlet mod time is later, call doGet()
                    // Round down to the nearest second for a proper compare
                    // A ifModifiedSince of -1 will always be less
                    maybeSetLastModified(resp, lastModified);
                    doGet(req, resp);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                }
            }

        } else if (method.equals(METHOD_HEAD)) {
            final long lastModified = getLastModified(req);
            maybeSetLastModified(resp, lastModified);
            doHead(req, resp);

        } else if (method.equals(METHOD_POST)) {
            doPost(req, resp);

        } else if (method.equals(METHOD_PUT)) {
            doPut(req, resp);

        } else if (method.equals(METHOD_DELETE)) {
            doDelete(req, resp);

        } else if (method.equals(METHOD_OPTIONS)) {
            doOptions(req, resp);

        } else if (method.equals(METHOD_TRACE)) {
            doTrace(req, resp);
        } else if (method.equals("SUBSCRIBE")) {
            doSubscribe(req, resp);
        } else if (method.equals("UNSUBSCRIBE")) {
            doUnsubscribe(req, resp);
        } else if (method.equals("NOTIFY")) {
            doNotify(req, resp);
        } else {
            //
            // Note that this means NO servlet supports whatever
            // method was requested, anywhere on this server.
            //

            String errMsg = lStrings.getString("http.method_not_implemented");
            final Object[] errArgs = new Object[1];
            errArgs[0] = method;
            errMsg = MessageFormat.format(errMsg, errArgs);

            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
        }
    }

    public abstract void doNotify(HttpServletRequest request,
            HttpServletResponse response);

    public abstract void doUnsubscribe(HttpServletRequest request,
            HttpServletResponse response);

    /**
     * 
     SUBSCRIBE
     * /upnphost/udhisapi.dll?event=uuid:2641a233-6d3b-42fa-9f8d-667662
     * c2e8fb+urn:upnp-org:serviceId:ContentDirectory HTTP/1.1 HOST:
     * 192.168.101.227:2869 CALLBACK: <http://192.168.101.50:9000/> NT:
     * upnp:event TIMEOUT: Second-300 Connection: close
     * 
     * HTTP/1.1 200 OK Server: Microsoft-Windows-NT/5.1 UPnP/1.0
     * UPnP-Device-Host/1.0 Microsoft-HTTPAPI/2.0 Timeout: Second-300 SID:
     * uuid:bc093999-d308-4c49-b3e1-575af7660b62 Date: Sat, 09 Jun 2012 13:17:02
     * GMT Connection: close Content-Length: 0
     * */
    public abstract void doSubscribe(HttpServletRequest request,
            HttpServletResponse response);

}
