package de.sosd.mediaserver.bean;

import java.net.MalformedURLException;
import java.net.URL;

public class WebappLocationBean {

	private final String protocol;	
	private final String address;
	private final int port;
	private final String webappName;
	
	private final URL url;
	
	
	public WebappLocationBean(String protocol, String address, int port, String webappName) throws MalformedURLException {
		super();
		this.protocol = protocol;
		this.address = address;
		this.port = port;
		this.webappName = webappName;
		
		this.url = new URL(protocol, address, port, webappName);
	}

	public String getProtocol() {
		return protocol;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the webappName
	 */
	public String getWebappName() {
		return webappName;
	}

	public URL getUrl() {
		return url;
	}
	
	public String getUrlString() {
		return getUrl().toString();
	}

}
