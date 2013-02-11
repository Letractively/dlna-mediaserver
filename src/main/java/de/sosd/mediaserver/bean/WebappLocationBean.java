package de.sosd.mediaserver.bean;

import java.net.MalformedURLException;
import java.net.URL;

public class WebappLocationBean {

    private final String protocol;
    private final String address;
    private final int    port;
    private final String webappName;

    private final URL    url;

    public WebappLocationBean(final String protocol, final String address,
            final int port, final String webappName)
            throws MalformedURLException {
        super();
        this.protocol = protocol;
        this.address = address;
        this.port = port;
        this.webappName = webappName;

        this.url = new URL(protocol, address, port, webappName);
    }

    public String getProtocol() {
        return this.protocol;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * @return the webappName
     */
    public String getWebappName() {
        return this.webappName;
    }

    public URL getUrl() {
        return this.url;
    }

    public String getUrlString() {
        return getUrl().toString();
    }

}
