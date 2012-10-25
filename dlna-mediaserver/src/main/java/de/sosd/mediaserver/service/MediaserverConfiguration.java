package de.sosd.mediaserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.db.StorageService;

public class MediaserverConfiguration {

//	private final static Log logger = LogFactory.getLog(MediaserverConfiguration.class);
	
	@Autowired
	private StorageService storage;
	
	private String webappName;	
	private String hostname;
	private int port;

	private String resolvedHostname;
	
//	public MediaserverConfiguration() {
//		initialize();
//	}
	
	public String getResolvedHostname() {
		if (this.resolvedHostname != null) {
			return this.resolvedHostname;
		}
		return this.hostname;
	}	

	public String getNetworkInterface() {
		final SystemDomain systemProperties = this.storage.getSystemProperties();
		return systemProperties.getNetworkInterface();
	}

	public String getHttpServerUrl() {
		return "http://" + getHostname() + ":" + getPort() + "/" + getWebappName();
	}

	public String getServerName() {
		final SystemDomain systemProperties = this.storage.getSystemProperties();
		return systemProperties.getName();
	}

	public String getUSN() {
		final SystemDomain systemProperties = this.storage.getSystemProperties();
		return systemProperties.getUsn();
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void setNetworkProperties(final String interfaceName, final String hostAddress,
			final String hostName) {
		final SystemDomain systemProperties = this.storage.getSystemProperties();
		systemProperties.setNetworkInterface(interfaceName);
		this.resolvedHostname = hostName;
		this.storage.store(systemProperties);
	}

//	public void initialize() {
//		/*
//		 *   
//		 * <Environment name="mediaserver.webapp.path" value="mediaserver" type="java.lang.String" override="false"/>
//		   <Environment name="mediaserver.server.url" value="192.168.101.227" type="java.lang.String" override="false"/>
//		   <Environment name="mediaserver.server.port" value="9090" type="java.lang.Integer" override="false"/>
//		 * 
//		 * */	
//		try {
//		    Context ctx = new InitialContext();
//		    ctx = (Context) ctx.lookup("java:comp/env");
//		    String webapp = (String) ctx.lookup("mediaserver.webapp.name");
//		    String hostname = (String) ctx.lookup("mediaserver.server.hostname");
//		    Integer port = (Integer) ctx.lookup("mediaserver.server.port");
//		    
//		    setHostname(hostname);
//		    setPort(port);
//		    setWebappName(webapp);
//
//		}
//		catch (NamingException e) {
//		    // what?
//			logger.error("Error on init, can't read server-url from tomcat's context.xml ",e);
//		}		
//		
//		logger.info("Mediaserver started : " + getHttpServerUrl());
//		
//	}	


	/**
	 * @return the webappName
	 */
	public String getWebappName() {
		return this.webappName;
	}

	/**
	 * @param webappName the webappName to set
	 */
	public void setWebappName(final String webappName) {
		this.webappName = webappName;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return this.hostname;
	}

	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(final int port) {
		this.port = port;
	}

	public String getMPlayerPath() {
		final SystemDomain systemProperties = this.storage.getSystemProperties();
		return systemProperties.getMplayerPath();
	}

	public String getPreviews() {
		final SystemDomain systemProperties = this.storage.getSystemProperties();
		return systemProperties.getPreviewCache();
	}



}
