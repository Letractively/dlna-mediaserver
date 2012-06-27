package de.sosd.mediaserver.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.ThumbnailDomain;
import de.sosd.mediaserver.service.MediaserverConfiguration;
import de.sosd.mediaserver.service.db.DIDLService;

@Configurable
public class DidlXmlCreator {

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private int objectCounter;
	private int totalMatches;
	private int updateId;
	private final StringBuffer buffer;
	private String serverID;
	private String httpServerUrl;	
	
	@Autowired
	private MediaserverConfiguration cfg;
	
	@Autowired
	private DIDLService service;


	
	public DidlXmlCreator() {
		this.buffer = new StringBuffer();
	}
	
	public void addDidlObject(final DidlDomain didlObject) {
		this.objectCounter++;
		didlObject.intoDidlXml(this.buffer, this);
	}

	
	private String getServerID() {
		if (this.serverID == null) {
			this.serverID = this.cfg.getHostname();
		}
		return this.serverID;
	}
	
	private String getHttpServerUrl() {
		if (this.httpServerUrl == null) {
			this.httpServerUrl = this.cfg.getHttpServerUrl();
		}
		return this.httpServerUrl;
	}
	
	/**
	 * @return the totalMatches
	 */
	public int getTotalMatches() {
		return this.totalMatches;
	}

	/**
	 * @param totalMatches
	 *            the totalMatches to set
	 */
	public void setTotalMatches(final int totalMatches) {
		this.totalMatches = totalMatches;
	}

	/**
	 * @return the updateId
	 */
	public int getUpdateId() {
		return this.updateId;
	}

	/**
	 * @param updateId
	 *            the updateId to set
	 */
	public void setUpdateId(final int updateId) {
		this.updateId = updateId;
	}

	public int getTotalObjectCount() {
		return this.objectCounter;
	}

	public int getTotalMatchesCount() {
		return this.totalMatches;
	}

	public String getXml() {
		final StringBuffer result = new StringBuffer();
		// node
		result.append("<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">");
		// content
		result.append(this.buffer);
		// close
		result.append("</DIDL-Lite>");
		return result.toString();
	}

	public int getUpdateID() {
		// TODO Auto-generated method stub
		return this.updateId;
	}

	public void setTotalMatches(final Long longValue) {
		if (longValue == null) {
			setTotalMatches(0);
		} else {
			setTotalMatches(longValue.intValue());
		}
	}

	public String translateId(final DidlDomain didl) {
		return translateId(didl.getId());
	}

	public String translateId(String id) {
		if (id == null) {
			return "-1";
		} else {
			if (getServerID().equals(id)) {
				return "0";
			}
			return id;
		}
	}	
	
	
	public String formatDateWithTime(final Date date) {
		return this.sdf.format(date);
	}

	public String formatDate(final Date date) {
		return this.sdf.format(date);
	}

	public String getUrl(final DidlDomain didl) {
		return getHttpServerUrl() + "/dlna/content/" + this.service.getServletPath(didl.getClassType()) + "/" + didl.getUrl();
	}

	public String getUrl(final ThumbnailDomain thumbnail, final String id) {
		return getHttpServerUrl() + "/dlna/content/thumb/" +  id + "." + thumbnail.getType();
	}

	public String getProtocolInfo(final ThumbnailDomain thumbnail) {
		return this.service.getProtocolInfoForExtension(thumbnail.getType());
		
//		if (thumbnail.getType().equals("jpg")) {
//			return "http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000";
//		}
//		return null;
	}

	
}
