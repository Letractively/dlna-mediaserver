package de.sosd.mediaserver.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		this.cachedImagesSources.clear();
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
		if (didl == null) {
			return "-1";
		}
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

	/*
	 *   
  <res resolution="160x103" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W160/H103/Xjpeg-scale.desc/DLNA-PNJPEG_TN-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res size="50386" resolution="857x551" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_MED;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/disk/DLNA-PNJPEG_MED-OP01-FLAGS00f00000/photo/O0$2$19I14367/07.JPG</res>
  <res resolution="800x514" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_MED;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W800/H514/Xjpeg-scale.desc/DLNA-PNJPEG_MED-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="42x27" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W42/H27/Xjpeg-scale.desc/DLNA-PNJPEG_TN-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="160x103" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W160/H103/Xjpeg-scale.desc/DLNA-PNJPEG_TN-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="138x89" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W138/H89/Xjpeg-scale.desc/DLNA-PNJPEG_TN-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="176x113" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W176/H113/Xjpeg-scale.desc/DLNA-PNJPEG_SM-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="120x77" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W120/H77/Xjpeg-scale.desc/DLNA-PNJPEG_TN-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="720x463" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_MED;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W720/H463/Xjpeg-scale.desc/DLNA-PNJPEG_MED-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="480x309" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W480/H309/Xjpeg-scale.desc/DLNA-PNJPEG_SM-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="480x309" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W480/H309/Xjpeg-scale.desc/DLNA-PNJPEG_SM-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="320x206" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W320/H206/Xjpeg-scale.desc/DLNA-PNJPEG_SM-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="544x350" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W544/H350/Xjpeg-scale.desc/DLNA-PNJPEG_SM-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
  <res resolution="640x411" protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000">http://192.168.178.100:9000/cgi-bin/W640/H411/Xjpeg-scale.desc/DLNA-PNJPEG_SM-OP00-CI1-FLAGS00f00000/photo/O0$2$19I14367/07.jpg</res>
	 * 
	 * */
	
	
	private final static int[] provided_thumb_widths = 		{16				,48				,120			,160		,640		,1024		,4096		}; // max widths
	private final static int[] provided_thumb_heights = 	{16				,48				,120			,160		,480		,768		,4096		}; // ax heights
	private final static String[] provided_thumb_types =	{"JPEG_TN_ICO"	,"JPEG_SM_ICO"	,"JPEG_LRG_ICO"	,"JPEG_TN"	,"JPEG_SM"	,"JPEG_MED"	,"JPEG_LRG"	}; // name for max width/height
	
	private class CachedImageSource {
		
		private final String name;
		private final int width;
		private final int height;
		private final boolean scale;
		private final String url;
		
		public CachedImageSource(final String name, final int width,final  int height, final boolean scale, final String url) {
			this.name = name;
			this.width = width;
			this.height = height;
			this.scale = scale;
			this.url = url;
		}
		
		protected void writeAlbumArtURI(StringBuffer out) {
			out.append("<upnp:albumArtURI dlna:profileID=\"");
			out.append(name);
//			out.append(";resolution=");
//			out.append(width);
//			out.append("x");
//			out.append(height);
			out.append("\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">");
			out.append(url);
			if (scale) {
				out.append("?width=");
				out.append(width);
				out.append("&amp;height=");
				out.append(height);
			}
			out.append("</upnp:albumArtURI>");			
		}

		protected void writeRes(StringBuffer out) {
			out.append("<res resolution=\"");
			out.append(width);
			out.append("x");
			out.append(height);
			out.append("\" protocolInfo=\"http-get:*:image/jpeg:DLNA.ORG_PN=");
			out.append(name);
			out.append(";DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000\">");
			out.append("");
			out.append(url);
			if (scale) {
				out.append("?width=");
				out.append(width);
				out.append("&amp;height=");
				out.append(height);
			}			
			out.append("</res>");			
		}
	}
	
	private final List<CachedImageSource> cachedImagesSources = new ArrayList<CachedImageSource>();
	
	public void writeAlbumArtURI(DidlDomain dd, StringBuffer out) {
		if (cachedImagesSources.isEmpty()) {
			findCacheableImages(dd);
		}
		for (CachedImageSource cis : cachedImagesSources) {
			cis.writeAlbumArtURI(out);
		}
	}
	
	public void writeThumbRes(DidlDomain dd, StringBuffer out) {
		if (cachedImagesSources.isEmpty()) {
			findCacheableImages(dd);
		}
		for (CachedImageSource cis : cachedImagesSources) {
			cis.writeRes(out);
		}
	}

	private void findCacheableImages(DidlDomain dd) {
		if ((dd.getThumbnail() != null)) {
			String url = getUrl(dd.getThumbnail(), dd.getId());
			String resolution = dd.getThumbnail().getResolution();
			findCacheableImages(url, resolution, true);
		} else {
			switch (dd.getClassType()) {
			case OBJECT_ITEM_IMAGE_ITEM:
			case OBJECT_ITEM_IMAGE_ITEM_PHOTO: {
				String url = getUrl(dd);
				String resolution = dd.getResolution();	
				findCacheableImages(url, resolution, false);
				break;
			}
			default : {
				break;
			}
			}
		}	
	}

	private void findCacheableImages(String url, String resolution, boolean addSelf) {
		try {
			if (url != null && resolution != null) {
				String[] xy = resolution.split("x");
				int src_w = Integer.parseInt(xy[0]);
				int src_h = Integer.parseInt(xy[1]);
				int src_name = 0;
				int trg_w =0;
				int trg_h =0;
				for (int i = 0; i< provided_thumb_types.length; ++i) {
					if (src_w > provided_thumb_widths[i] || src_h > provided_thumb_heights[i]) {
						float scale = Math.min(((float)provided_thumb_widths[i] / src_w), ((float)provided_thumb_heights[i] / src_h));
						trg_w = Math.round( src_w * scale );
						trg_h = Math.round( src_h * scale );
						cachedImagesSources.add(new CachedImageSource(provided_thumb_types[i], trg_w, trg_h, true, url));
						src_name = i;
					} else {
						break;
					}
				}
				if (addSelf) {
					while (src_name < provided_thumb_widths.length && provided_thumb_widths[src_name] < src_w) {
						++src_name;
					}
					cachedImagesSources.add(new CachedImageSource(provided_thumb_types[src_name], src_w, src_h, false, url));
				}
			}
		} catch (NumberFormatException NFE) {
			
		}
	}

	
}
