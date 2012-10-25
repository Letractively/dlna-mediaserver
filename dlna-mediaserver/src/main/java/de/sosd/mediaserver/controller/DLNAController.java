package de.sosd.mediaserver.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.upnp.schemas.service.contentdirectory._1.Browse;
import org.upnp.schemas.service.contentdirectory._1.BrowseResponse;
import org.upnp.schemas.service.contentdirectory._1.GetSearchCapabilities;
import org.upnp.schemas.service.contentdirectory._1.GetSearchCapabilitiesResponse;
import org.upnp.schemas.service.contentdirectory._1.GetSortCapabilities;
import org.upnp.schemas.service.contentdirectory._1.GetSortCapabilitiesResponse;
import org.upnp.schemas.service.contentdirectory._1.GetSystemUpdateID;
import org.upnp.schemas.service.contentdirectory._1.GetSystemUpdateIDResponse;
import org.upnp.schemas.service.contentdirectory._1.Search;
import org.upnp.schemas.service.contentdirectory._1.SearchResponse;
import org.xmlsoap.schemas.soap.envelope.Envelope;

import de.sosd.mediaserver.service.MediaserverConfiguration;
import de.sosd.mediaserver.service.ThumbnailService;
import de.sosd.mediaserver.service.db.DIDLService;
import de.sosd.mediaserver.service.db.StorageService;
import de.sosd.mediaserver.service.dlna.ContentDirectoryService;
import de.sosd.mediaserver.util.DeviceByRequestHeader;
import de.sosd.mediaserver.util.PreferredNamespaceMapper;

@Controller
@RequestMapping("/dlna/*")
public class DLNAController {

	@Autowired
	private MediaserverConfiguration cfg;

	@Autowired
	private ContentDirectoryService service;

	@Autowired
	private ThumbnailService thumbs;
	
	final JAXBContext context;	
	
	public static final String DLNA_BASE = "/dlna";
	private static final String SERVER_DESCRIPTION_REF = DLNA_BASE + "/Mediaserver.xml";
	// private static final String SERVER_CONNECTION_SCPD_REF =
	// "/connection/scpd";
	// private static final String SERVER_CONNECTION_CONTROL_REF =
	// "/connection/control";
	// private static final String SERVER_CONNECTION_EVENT_REF =
	// "/connection/event";
	//
	// private static final String SERVER_CONTENT_SCPD_REF = "/content/scpd";
	// private static final String SERVER_CONTENT_EVENT_REF =
	// "/content/control";
	// private static final String SERVER_CONTENT_CONTROL_REF =
	// "/content/event";
	// private static final String SERVER_MRR_SCPD_REF = "/mrr/scpd";
	// private static final String SERVER_MRR_CONTROL_REF = "/mrr/control";

	private final static Log logger = LogFactory
			.getLog(DLNAController.class);

	private static final String CRLF = "\r\n";
	private final static String CAPABILITIES = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ CRLF
			+ "<root xmlns:dlna=\"urn:schemas-dlna-org:device-1-0\" xmlns=\"urn:schemas-upnp-org:device-1-0\">"
			+ CRLF
			+ "	<specVersion>"
			+ CRLF
			+ "		<major>1</major>"
			+ CRLF
			+ "		<minor>0</minor>"
			+ CRLF
			+ "	</specVersion>"
			+ CRLF
			+
			// "	<URLBase>$SERVER_BASE_URL</URLBase>" + CRLF +
			"	<device>"
			+ CRLF
			+ "		<UDN>uuid:$UDN</UDN>"
			+ CRLF
			+ "		<friendlyName>$SERVER_NAME</friendlyName>"
			+ CRLF
			+ "		<deviceType>urn:schemas-upnp-org:device:MediaServer:1</deviceType>"
			+ CRLF
			+ "		<manufacturer>Home</manufacturer>"
			+ CRLF
			+ "		<manufacturerURL>$SERVER_URL</manufacturerURL>"
			+ CRLF
			+ "		<modelName>Mediaserver</modelName>"
			+ CRLF
			+ "		<modelNumber>01</modelNumber>"
			+ CRLF
			+ "		<modelURL>$SERVER_URL</modelURL>"
			+ CRLF
			+ "		<serialNumber>{$UDN}</serialNumber>"
			+ CRLF
			+ "		<dlna:X_DLNADOC xmlns:dlna=\"urn:schemas-dlna-org:device-1-0\">DMS-150</dlna:X_DLNADOC>"
			+ CRLF
			+
			 "		<dlna:X_DLNADOC xmlns:dlna=\"urn:schemas-dlna-org:device-1-0\">M-DMS-1.50</dlna:X_DLNADOC>"
			 + CRLF +
			 "		<modelDescription>UPnP/AV 1.0 Compliant Media Server</modelDescription>"
			 + CRLF +
			 "		<UPC/>" + CRLF +
			"		<iconList>"
			+ CRLF
			+ "			<icon>"
			+ CRLF
			+ "				<mimetype>image/png</mimetype>"
			+ CRLF
			+ "				<width>256</width>"
			+ CRLF
			+ "				<height>256</height>"
			+ CRLF
			+ "				<depth>24</depth>"
			+ CRLF
			+ "				<url>/mediaserver/resources/icons/thumbnail-256.png</url>"
			+ CRLF
			+ "			</icon>"
			+ CRLF
			+ "			<icon>"
			+ CRLF
			+ "				<mimetype>image/jpeg</mimetype>"
			+ CRLF
			+ "				<width>120</width>"
			+ CRLF
			+ "				<height>120</height>"
			+ CRLF
			+ "				<depth>24</depth>"
			+ CRLF
			+ "				<url>/mediaserver/resources/icons/thumbnail-120.jpg</url>"
			+ CRLF
			+ "			</icon>"
			+ CRLF
			+ "		</iconList>"
			+ CRLF
			+
			"		<presentationURL>$SERVER_PRESENTATION_URL</presentationURL>" +
			CRLF +
			"		<serviceList>"
			+ CRLF
			+ "			<service>"
			+ CRLF
			+ "				<serviceType>urn:schemas-upnp-org:service:ConnectionManager:1</serviceType>"
			+ CRLF
			+ "				<serviceId>urn:upnp-org:serviceId:ConnectionManager</serviceId>"
			+ CRLF
			+ "				<controlURL>/mediaserver/dlna/soap</controlURL>"
			+ CRLF
			+ "				<eventSubURL>/mediaserver/event/ConnectionManager</eventSubURL>"
			+ CRLF
			+ "				<SCPDURL>/mediaserver/resources/UPnP_AV_ConnectionManager_1.0.xml</SCPDURL>"
			+ CRLF
			+ "			</service>"
			+ CRLF
			+ "			<service>"
			+ CRLF
			+ "				<serviceType>urn:schemas-upnp-org:service:ContentDirectory:1</serviceType>"
			+ CRLF
			+ "				<serviceId>urn:upnp-org:serviceId:ContentDirectory</serviceId>"
			+ CRLF
			+ "				<controlURL>/mediaserver/dlna/soap</controlURL>"
			+ CRLF
			+ "				<eventSubURL>/mediaserver/event/ContentDirectory</eventSubURL>"
			+ CRLF
			+ "				<SCPDURL>/mediaserver/resources/UPnP_AV_ContentDirectory_1.0.xml</SCPDURL>"
			+ CRLF + "			</service>" + CRLF +
			 "			<service>" + CRLF +
			 "				<serviceType>urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1</serviceType>"
			 + CRLF +
			 "				<serviceId>urn:microsoft.com:serviceId:X_MS_MediaReceiverRegistrar</serviceId>"
			 + CRLF +
			 "				<controlURL>/mediaserver/dlna/soap/</controlURL>" + CRLF +
		     "				<eventSubURL>/mediaserver/event/MediaReceiverRegistrar</eventSubURL>"
				+ CRLF	+		 
			 "				<SCPDURL>/mediaserver/resources/X_MS_MediaReceiverRegistrar.xml</SCPDURL>" + CRLF +
			 "			</service>" + CRLF +
			"		</serviceList>" + CRLF + "	</device>" + CRLF + "</root>";

	// /resources/UPnP_AV_ContentDirectory_1.0.xml
	
	public DLNAController() throws JAXBException {
		context = JAXBContext.newInstance(
					Envelope.class, 
					Browse.class, BrowseResponse.class, 
					GetSystemUpdateID.class,GetSystemUpdateIDResponse.class, 
					Search.class, SearchResponse.class, 
					GetSortCapabilities.class, GetSortCapabilitiesResponse.class, 
					GetSearchCapabilities.class, GetSearchCapabilitiesResponse.class);
	}
	
	private String getCapablities() {
		final String result = new String(CAPABILITIES);

		return result
				.replace("$SERVER_URL", this.cfg.getHttpServerUrl())
				// .replace("$SERVER_BASE_URL", cfg.getHttpServerUrl() + "/")
				.replace("$SERVER_PRESENTATION_URL", this.cfg.getHttpServerUrl()) 
				// WebUserInterface.getMainRef())
				// .replace("$SERVER_CONTENT_CONTROL_URL", "/mediaserver" +
				// getContentControlRef())
				// .replace("$SERVER_CONTENT_EVENT_URL", "/mediaserver" +
				// getContentEventRef())
				// .replace("$SERVER_CONTENT_SCPD_URL", "/mediaserver" +
				// getContentScpdRef() )
				// .replace("$SERVER_CONNECTION_CONTROL_URL", "/mediaserver" +
				// getConnectionControlRef())
				// .replace("$SERVER_CONNECTION_EVENT_URL", "/mediaserver" +
				// getConnectionEventRef())
				// .replace("$SERVER_MRR_SCPD_URL", "/mediaserver" +
				// getMrrScpdRef())
				// .replace("$SERVER_MRR_CONTROL_URL", "/mediaserver" +
				// getMrrControlRef())
				.replace("$UDN", this.cfg.getUSN())
				.replace("	", "")
				.replace("$SERVER_NAME", this.cfg.getServerName() + " [" + this.cfg.getHostname() + "]")
				.replace(CRLF, "");
	}

	public static String getDescriptionRef() {
		return  SERVER_DESCRIPTION_REF;
	}
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);

	@RequestMapping(value = "Mediaserver.xml", method = {RequestMethod.GET, RequestMethod.POST })
	public void showDescription(final HttpServletRequest request,
			final HttpServletResponse response) {
		logger.debug("server url requested! " + request.getRemoteAddr());		
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//		response.addHeader("Cache-Control", "no-cache");
//		response.addHeader("Expires", "0");

		try {
			final String capablities = getCapablities();
			response.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
			response.addHeader("Content-Length", "" + capablities.getBytes().length);
			response.addHeader("Date", sdf.format(new Date(System.currentTimeMillis())) + " GMT");
			response.addHeader("Accept-Ranges", "bytes");
			response.addHeader("Connection", "keep-alive");
			response.addHeader("EXT", "");
			response.addHeader("Server", "UPnP/1.0, SOSD Mediaserver");
			response.getWriter().write(capablities);
		} catch (final IOException e) {
			logger.error(e);
		}

	}

	@Autowired
	private StorageService storage;
	
	@Autowired
	private DIDLService didlService;
	
	private final static String[] rangeHeaders = {"RANGE", "range", "timeseekrange.dlna.org"};
	
	@RequestMapping(value="content/{type}/{uuid}.{extension}", method={RequestMethod.GET, RequestMethod.HEAD, RequestMethod.POST})
	public void getMedia(
			@PathVariable("type") final String type,
			@PathVariable("uuid") final String uuid,
			@PathVariable("extension") final String extension,
			@RequestParam(value="start", required = false, defaultValue = "0") long start, 
			@RequestParam(value="stop", required = false, defaultValue = "-1") long stop,
			@RequestParam(required=false, value="width")Integer width,
			@RequestParam(required=false, value="height")Integer height,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException {
			logRequest("getMedia", request);
			boolean resizeTheImage = false;
			if (type.equals("video") || type.equals("audio")) {
				String range = null;
				for (String header : rangeHeaders) {
					range = request.getHeader(header);
					if (range != null) {
						break;
					}
				}
				if (range != null) {
					logger.info("type : " + type + " RANGE: " + range);
					final String r0 = range.split("=")[1];
					
					final String[] ranges = r0.split("-");
					start = Long.parseLong(ranges[0]);
					if (ranges.length == 2) {
						stop = Long.parseLong(ranges[1]);
					}
				}
			} else {
				resizeTheImage = (width != null && height != null) && (type.equals("image") || type.equals("thumb"));
			}

			
			
			RandomAccessFile input = null;
			try {
				File content;
				
				if (type.equals("thumb")) {
					content = thumbs.getFile(uuid, extension, width, height);
				} else {
					final String path = this.storage.getPathForFile(uuid);	
					if (path == null) {
						throw new FileNotFoundException("no file for uuid " + uuid);
					}
					content = new File(path);
				} 
				if (resizeTheImage && type.equals("image")) {
					content = thumbs.getFile(uuid, extension, width, height, content);
				}
				
				if (!content.exists()) {
					throw new FileNotFoundException("file " + content.getAbsolutePath() + " is not present!");
				}				
				response.setContentType(this.didlService.getMimeTypeForExtension(extension));
									
				final ServletOutputStream output = response.getOutputStream();
					
				input = new RandomAccessFile(content, "r");			
				if (stop == -1) {
					stop = input.length();
				}
				input.seek(start);
				
				final byte[] buffer = new byte[1024];
				int size = 0;
				long pos = start;
				do {
					size = input.read(buffer);
					if (size > 0) {
						output.write(buffer, 0, size);
					}
					pos +=size;
					
				} while ((size == buffer.length) && (pos < stop));
				response.setContentLength((int)(pos - start));
				
				response.setStatus(HttpStatus.OK.value());
			} catch (final FileNotFoundException e) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
				logger.error(e);
			} catch (final IOException e) {
				response.setStatus(HttpStatus.METHOD_FAILURE.value());
				logger.error(e);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (final IOException e) {

					}
				}
			}
		}
	
	private void logRequest(String method, HttpServletRequest request) throws IOException {
		@SuppressWarnings("rawtypes")
		Enumeration headerNames = request.getHeaderNames();
		StringBuffer sb = new StringBuffer(method + " -> " + request.getRemoteAddr() + " -> "+request.getPathInfo()+"\n");
		while (headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			sb.append(headerName + ": " + request.getHeader(headerName) + "\n");
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ServletInputStream inputStream = request.getInputStream();
		byte[] buffer = new byte[4096];
		int read = 0;
		do {
			read = inputStream.read(buffer );
			if (read > 0 ){
				baos.write(buffer, 0, read);
			}
		} while (read > 0);			
		sb.append(baos);
		logger.info(sb.toString());	
	}

//	@RequestMapping(value = "soap", method = {RequestMethod.POST}, headers = {"SOAPACTION=\"urn:schemas-upnp-org:service:ContentDirectory:1#Browse\""})
//	public void contentBrowse(HttpServletRequest request,
//			HttpServletResponse response) throws JAXBException, IOException, ParserConfigurationException
//	{
//		response.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
//		response.addHeader("Accept-Ranges", "bytes");
//		response.addHeader("Connection", "keep-alive");		
//		
//		logger.debug("do browse");
//		JAXBContext soap = JAXBContext.newInstance(Envelope.class);
//		Envelope envelope = (Envelope)soap.createUnmarshaller().unmarshal(request.getInputStream());
//		
//		JAXBContext browse = JAXBContext.newInstance(BrowseRequest.class, BrowseResponse.class);
//		BrowseRequest brequest = (BrowseRequest)browse.createUnmarshaller().unmarshal((Node) envelope.getBody().getAny().get(0));
//		
//		
//		DeviceByRequestHeader device = new DeviceByRequestHeader(request);
//		BrowseResponse result = new BrowseResponse();
////		logger.info("Browse");
//		if (brequest.getBrowseFlag().equalsIgnoreCase("BrowseMetadata")) {
//			service.browseMetadata(brequest, result, device);
//
//		} else 
//		if (brequest.getBrowseFlag().equalsIgnoreCase("BrowseDirectChildren")) {
//			service.browseDirectChildren(brequest, result, device);
//		}
//
//		Document target = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//		Marshaller m = browse.createMarshaller();
//		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
//		m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",  new PreferredNamespaceMapper());
//		m.setProperty("jaxb.fragment", Boolean.FALSE);	
//		m.marshal(result, target);
//		
//		Marshaller sm = soap.createMarshaller();
//		sm.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
//		sm.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",  new PreferredNamespaceMapper());
//		sm.setProperty("jaxb.fragment", Boolean.FALSE);	
//		
//		envelope.getBody().getAny().clear();
//		envelope.getBody().getAny().add(target.getDocumentElement());	
//		sm.marshal(envelope, response.getOutputStream());	
//		sm.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//		sm.marshal(envelope, System.out);
//	}
	
//	@RequestMapping(value = "soap", method = {RequestMethod.POST}, headers = {"SOAPACTION=\"urn:schemas-upnp-org:service:ContentDirectory:1#GetSystemUpdateID\""})
//	public void contentGetSystemUpdateID(HttpServletRequest request, HttpServletResponse response)
//	{
//		logger.debug("do getSystemUpdateID");
//		
//		
//		
//	}
//	
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1#IsAuthorized\""
			})
	public void handleX_MS_MediaReceiverRegistrarIsAuthorized(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {	
		logRequest("handleX_MS_MediaReceiverRegistrarIsAuthorized", request);
		
		response.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
		response.addHeader("Accept-Ranges", "bytes");
		response.addHeader("Connection", "keep-alive");	
		response.getWriter()
				.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
						"<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<s:Body>" +
								"<u:IsAuthorizedResponse xmlns:u=\"urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1\">" +
									"<Result>1</Result>" +
								"</u:IsAuthorizedResponse>" +
							"</s:Body>" +
						"</s:Envelope>"
					);

		
	}
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1#IsValidated\""
			})
	public void handleX_MS_MediaReceiverRegistrarIsValidated(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {		
		logRequest("handleX_MS_MediaReceiverRegistrarIsValidated", request);
		
		response.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
		response.addHeader("Accept-Ranges", "bytes");
		response.addHeader("Connection", "keep-alive");	
		response.getWriter()
				.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
						"<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<s:Body>" +
								"<u:IsValidatedResponse xmlns:u=\"urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1\">" +
									"<Result>1</Result>" +
								"</u:IsValidatedResponse>" +
							"</s:Body>" +
						"</s:Envelope>"
					);
	}
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1#RegisterDevice\""
			})
	public void handleX_MS_MediaReceiverRegistrarRegistrationRespMsg(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {	
		logRequest("handleX_MS_MediaReceiverRegistrarRegistrationRespMsg", request);
		
		response.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
		response.addHeader("Accept-Ranges", "bytes");
		response.addHeader("Connection", "keep-alive");	
		response.getWriter()
				.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
						"<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<s:Body>" +
								"<u:RegistrationRespMsg xmlns:u=\"urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1\">" +
									"Success" +
								"</u:RegistrationRespMsg>" +
							"</s:Body>" +
						"</s:Envelope>"
					);
	}	
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:schemas-upnp-org:service:ConnectionManager:1#GetCurrentConnectionInfo\""
			})
	public void handleConnectionManagerGetCurrentConnectionInfo(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		logRequest("handleConnectionManagerGetCurrentConnectionInfo", request);
	}	
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:schemas-upnp-org:service:ConnectionManager:1#ConnectionComplete\""
			})
	public void handleConnectionManagerConnectionComplete(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		logRequest("handleConnectionManagerConnectionComplete", request);
	}	
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:schemas-upnp-org:service:ConnectionManager:1#PrepareForConnection\""
			})
	public void handleConnectionManagerPrepareForConnection(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		logRequest("handleConnectionManagerPrepareForConnection", request);
	}	
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:schemas-upnp-org:service:ConnectionManager:1#GetProtocolInfo\""
			})
	public void handleConnectionManagerGetProtocolInfo(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		logRequest("GetProtocolInfo", request);
	}	
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:schemas-upnp-org:service:ConnectionManager:1#GetCurrentConnectionIDs\""
			})
	public void handleConnectionManagerGetCurrentConnectionIDs(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		logRequest("GetCurrentConnectionIDs", request);
	}	
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:schemas-upnp-org:service:ContentDirectory:1#Browse\""
			})
	public void handleContentDirectoryBrowse(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		handleContentDirectory(request, response);		
	}

	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:schemas-upnp-org:service:ContentDirectory:1#GetSystemUpdateID\""
			})
	public void handleContentDirectoryGetSystemUpdateID(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		handleContentDirectory(request, response);		
	}
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:schemas-upnp-org:service:ContentDirectory:1#Search\""
			})
	public void handleContentDirectorySearch(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		handleContentDirectory(request, response);		
	}
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:schemas-upnp-org:service:ContentDirectory:1#GetSortCapabilities\""
			})
	public void handleContentDirectoryGetSortCapabilities(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		handleContentDirectory(request, response);		
	}
	
	@RequestMapping(value = "soap",  method = { RequestMethod.POST }, headers = {
			"SOAPACTION=\"urn:schemas-upnp-org:service:ContentDirectory:1#GetSearchCapabilities\""
			})
	public void handleContentDirectoryGetSearchCapabilities(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		handleContentDirectory(request, response);
	}
	
	
	private void handleContentDirectory(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException, JAXBException {
		
		final DeviceByRequestHeader device = new DeviceByRequestHeader(request);
		
		logger.trace("contentControl requested!");
		response.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
		response.addHeader("Accept-Ranges", "bytes");
		response.addHeader("Connection", "keep-alive");		
		final ServletInputStream is = request.getInputStream();

			
		final Unmarshaller um = context.createUnmarshaller();
		final Envelope envelope = (Envelope)um.unmarshal(is); //parsed.getValue();
		//System.out.println("recieved " + envelope.getClass().getCanonicalName());
		final List<Object> any = envelope.getBody().getAny();
		Object result = null;
		for (final Object o : any) {
			try {
				if (o instanceof Browse) {
					result = handle((Browse)o, device);
					// only one item expected
					break;
				} else {
					if (o instanceof GetSystemUpdateID) {
						result = handle((GetSystemUpdateID)o);
						// only one item expected
						break;
					} else {
						if (o instanceof Search) {
							result = handle((Search)o, device);
							// only one item expected
							break;
						} else {
							if (o instanceof GetSortCapabilities) {
								result = handle((GetSortCapabilities)o);
								// only one item expected
								break;
							} else {
								if (o instanceof GetSearchCapabilities) {
									result = handle((GetSearchCapabilities)o);
									// only one item expected
									break;
								} else {
									logger.error("got unsupported soap-call : " + o.getClass().getCanonicalName());
								}
							}
						}
					}
				}
			
			} catch (final Throwable t) {
				logger.error("error while processing soap-request : " + o.getClass().getCanonicalName(), t);		
			}
		}	
		final PrintWriter writer = response.getWriter();
		writer.write("<?xml version=\"1.0\"?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>");
		if (result != null) {
			final Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",  new PreferredNamespaceMapper());
			m.setProperty("jaxb.fragment", Boolean.TRUE);
			m.marshal(result, writer);
//			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//			m.marshal(result, System.out);
		} else {
			logger.error("soap-call resulted in no response!");
		}
		writer.write("</s:Body></s:Envelope>");
		
//		System.out.print("<?xml version=\"1.0\"?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>");
//		//m.marshal(result, System.out);
//		System.out.println(result.getResult());
//		System.out.println("</s:Body></s:Envelope>");
	}

	private GetSearchCapabilitiesResponse handle(final GetSearchCapabilities request) {
		final GetSearchCapabilitiesResponse response = new GetSearchCapabilitiesResponse();
		this.service.getSearchCapabilities(request, response);
		return response;
	}

	private GetSortCapabilitiesResponse handle(final GetSortCapabilities request) {
		final GetSortCapabilitiesResponse response = new GetSortCapabilitiesResponse();
		this.service.getSortCapabilities(request, response);
		return response;
	}

	private SearchResponse handle(final Search request, final DeviceByRequestHeader device) throws JAXBException, IOException {
		final SearchResponse response = new SearchResponse();
		this.service.search(request, response, device);
		return response;
	}

	private GetSystemUpdateIDResponse handle(final GetSystemUpdateID request) {
		final GetSystemUpdateIDResponse response = new GetSystemUpdateIDResponse();
		response.setId(this.service.getSystemUpdateId());
		return response;
	}

	private BrowseResponse handle(final Browse request, final DeviceByRequestHeader device) throws JAXBException, IOException {
		final BrowseResponse response = new BrowseResponse();
		switch (request.getBrowseFlag()) {
			case BROWSE_METADATA: {
				this.service.browseMetadata(request, response, device);
				break;
			}
			case BROWSE_DIRECT_CHILDREN: {
				this.service.browseDirectChildren(request, response, device);
				break;
			}			
		}
		
		return response;
	}

}
