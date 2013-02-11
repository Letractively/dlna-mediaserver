package de.sosd.mediaserver.service;

import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.bean.WebappLocationBean;
import de.sosd.mediaserver.dao.SystemDao;
import de.sosd.mediaserver.domain.db.SystemDomain;

@Service
public class MediaserverConfiguration {

    private final static Log logger     = LogFactory
                                                .getLog(MediaserverConfiguration.class);

    @Autowired
    private SystemDao        systemDao;

    private String           protocol   = "HTTP";
    private String           address    = "localhost";
    private int              port       = 8080;
    private String           webappName = "mediaserver";

    private String           usn        = null;

    public String getServerName() {
        final SystemDomain systemProperties = this.systemDao
                .getSystem(getUSN());
        return systemProperties.getName();
    }

    public String getUSN() {
        return this.usn;
    }

    /**
     * @return the webappName
     */
    private String getWebappName() {
        return this.webappName;
    }

    /**
     * @return the port
     */
    private int getPort() {
        return this.port;
    }

    /**
     * @param port
     *            the port to set
     */
    @Transactional
    public void updateWebappConfiguration(final String protocol,
            final String address, final int port, final String webappName) {
        this.port = port;
        this.webappName = webappName;
        this.address = address;
        this.protocol = protocol;
        final SystemDomain system = this.systemDao.getSystem(getUSN());
        system.setHostname(getAddress());
        system.setPort(getPort());
        system.setWebappName(getWebappName());
        system.setProtocol(getProtocol());

        this.systemDao.store(system);
    }

    /**
     * @param port
     *            the port to set
     */
    public void loacWebappConfiguration() {
        final SystemDomain system = this.systemDao.getSystem(getUSN());
        this.address = system.getHostname();
        this.port = system.getPort();
        this.webappName = system.getWebappName();
        this.protocol = system.getProtocol();
    }

    public String getMPlayerPath() {
        final SystemDomain systemProperties = this.systemDao
                .getSystem(getUSN());
        return systemProperties.getMplayerPath();
    }

    public String getPreviews() {
        final SystemDomain systemProperties = this.systemDao
                .getSystem(getUSN());
        return systemProperties.getPreviewCache();
    }

    public WebappLocationBean getWebappLocation(final String protocol,
            final String address, final int port, final String webappName) {
        try {
            return new WebappLocationBean(protocol, address, port, webappName);
        } catch (final MalformedURLException e) {
            return getDefaultWebappLocation();
        }
    }

    private WebappLocationBean getDefaultWebappLocation() {
        try {
            return new WebappLocationBean(getProtocol(), getAddress(),
                    getPort(), getWebappName());
        } catch (final MalformedURLException e) {
            logger.error("default url is malformed : " + getProtocol() + "://"
                    + getAddress() + ":" + getPort() + "/" + getWebappName());
        }
        return null;
    }

    public WebappLocationBean getWebappLocation(final String address) {
        try {
            return new WebappLocationBean(getProtocol(), address, getPort(),
                    getWebappName());
        } catch (final MalformedURLException e) {
            logger.error("default url is malformed : " + getProtocol() + "://"
                    + address + ":" + getPort() + "/" + getWebappName());
        }
        return null;

    }

    private String getAddress() {
        return this.address;
    }

    private String getProtocol() {
        return this.protocol;
    }

    public void setUSN(final String usn) {
        this.usn = usn;
    }

    public boolean isSystemAvailable() {
        return getUSN() != null;
    }

    public String getHostname() {
        return getAddress();
    }

}
