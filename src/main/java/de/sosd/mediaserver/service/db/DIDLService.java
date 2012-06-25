package de.sosd.mediaserver.service.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.sosd.mediaserver.domain.db.ClassNameWcType;
import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.FileDomain;
import de.sosd.mediaserver.domain.db.ScanFolderDomain;
import de.sosd.mediaserver.service.IdService;

@Service
public class DIDLService {

	@SuppressWarnings("unused")
	private final static Log logger = LogFactory.getLog(DIDLService.class);

	private static final Map<String, ClassNameWcType> extensionClassTypeMap = new HashMap<String, ClassNameWcType>();
    private static final Map<String, String> extensionProtocolInfoMap = new HashMap<String, String>();
    private static final Map<String, String> extensionHttpMimeMap = new HashMap<String, String>();
    
    private final static String http_mime_bin  = "application/x-octet-stream";
    private final static String http_mime_avi  = "video/avi";
    private final static String http_mime_asf  = "video/x-ms-asf";
    private final static String http_mime_wmv  = "video/x-ms-wmv";
    private final static String http_mime_mp4  = "video/mp4";
    private final static String http_mime_mpeg = "video/mpeg";
    private final static String http_mime_mpeg2= "video/mpeg2";
    private final static String http_mime_mp2t = "video/mp2t";
    private final static String http_mime_mp2p = "video/mp2p";
    private final static String http_mime_mov  = "video/quicktime";
    private final static String http_mime_aac  = "audio/x-aac";
    private final static String http_mime_ac3  = "audio/x-ac3";
    private final static String http_mime_mp3  = "audio/mpeg";
    private final static String http_mime_ogg  = "application/ogg";
    private final static String http_mime_wma  = "audio/x-ms-wma";
    @SuppressWarnings("unused")
	private final static String http_mime_xml  = "text/xml";
    @SuppressWarnings("unused")
	private final static String http_mime_html = "text/html; charset=\"utf-8\"";
    private final static String http_mime_text = "text/plain";    
     
    // upnp mime types
    private final static  String upnp_avi       = "http-get:*:video/avi:";
    private final static String upnp_asf       = "http-get:*:video/x-ms-asf:";
    private final static String upnp_wmv       = "http-get:*:video/x-ms-wmv:";
    private final static String upnp_mp4       = "http-get:*:video/mp4:";
    private final static String upnp_mpeg      = "http-get:*:video/mpeg:";
    private final static String upnp_mpeg2     = "http-get:*:video/mpeg2:";
    private final static String upnp_mp2t      = "http-get:*:video/mp2t:";
    private final static String upnp_mp2p      = "http-get:*:video/mp2p:";
    private final static String upnp_mov       = "http-get:*:video/quicktime:";
    private final static String upnp_aac       = "http-get:*:audio/x-aac:";
    private final static String upnp_ac3       = "http-get:*:audio/x-ac3:";
    private final static String upnp_mp3       = "http-get:*:audio/mpeg:";
    private final static String upnp_ogg       = "http-get:*:audio/x-ogg:";
    private final static String upnp_wma       = "http-get:*:audio/x-ms-wma:";    
    	
	
    private final static String[][] consts = {
	     {"MPG"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mpeg,"DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_mpeg}              // default
	    ,{"MPEG" ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mpeg,"DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_mpeg}
	    ,{"MPEG2",ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mpeg2,"DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_mpeg2}
	    ,{"M2V"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mpeg2,"DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_mpeg2}
	    ,{"TS"   ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mp2t,"DLNA.ORG_PN=MPEG_TS_HD_NA;DLNA.ORG_OP=00;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_mp2t}
	    ,{"M2TS" ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mp2t,"DLNA.ORG_PN=MPEG_TS_HD_NA;DLNA.ORG_OP=00;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_mp2t}
	    ,{"MTS"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mp2t,"DLNA.ORG_PN=MPEG_TS_HD_NA;DLNA.ORG_OP=00;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_mp2t}
	    ,{"VOB"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mp2p,"DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_mp2p}
	    ,{"AVI"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_avi,"DLNA.ORG_PN=AVI;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_avi}
	    ,{"ASF"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_asf,"*",http_mime_asf}
	    ,{"WMV"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_wmv,"DLNA.ORG_PN=WMVHIGH_FULL;DLNA.ORG_OP=00;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_wmv}
	    ,{"MP4"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mp4,"*",http_mime_mp4}
	    ,{"M4V"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mp4,"*",http_mime_mp4}
	    ,{"MOV"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_mov,"*",http_mime_mov}	    
	    ,{"XVID" ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),upnp_avi,"DLNA.ORG_PN=AVI;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_avi}
	    ,{"MKV"  ,ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),"http-get:*:video/x-matroska:","*", http_mime_bin}
	    
	    ,{"AAC"  ,ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),upnp_aac,"*",http_mime_aac}
	    ,{"AC3"  ,ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),upnp_ac3,"DLNA.ORG_PN=AC3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_ac3}
	    ,{"MP3"  ,ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),upnp_mp3,"DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_mp3}
	    ,{"OGG"  ,ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),upnp_ogg,"*",http_mime_ogg}
	    ,{"WMA"  ,ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),upnp_wma,"DLNA.ORG_PN=WMAFULL;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000",http_mime_wma}
	    ,{"WAV"  ,ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),"http-get:*:audio/L16:","MICROSOFT.COM_PN=WAV_PCM",http_mime_bin}
	    ,{"PCM"  ,ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),"http-get:*:audio/L16:","MICROSOFT.COM_PN=WAV_PCM",http_mime_bin}
	    
	    ,{"JPG"	,ClassNameWcType.OBJECT_ITEM_IMAGE_ITEM_PHOTO.value(), "http-get:*:image/jpeg:","DLNA.ORG_PN=JPEG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000", http_mime_bin}
	    ,{"JPEG",ClassNameWcType.OBJECT_ITEM_IMAGE_ITEM_PHOTO.value(), "http-get:*:image/jpeg:","DLNA.ORG_PN=JPEG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000", http_mime_bin}
	    ,{"GIF"	,ClassNameWcType.OBJECT_ITEM_IMAGE_ITEM_PHOTO.value(), "http-get:*:image/gif:","*", http_mime_bin}
	    ,{"PNG"	,ClassNameWcType.OBJECT_ITEM_IMAGE_ITEM_PHOTO.value(), "http-get:*:image/png:","DLNA.ORG_PN=PNG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000", http_mime_bin}
    
	    ,{"M3U" , ClassNameWcType.OBJECT_ITEM_PLAYLIST_ITEM.value(), "http-get:*:audio/m3u:","*", http_mime_text}
// removed to keep the db clean	    
//	    ,{"XML" , ClassNameWcType.OBJECT_ITEM_TEXT_ITEM.value(), "http-get:*:text/xml:","*", http_mime_xml}
//	    ,{"HTM" , ClassNameWcType.OBJECT_ITEM_TEXT_ITEM.value(), "http-get:*:text/html:","*", http_mime_html}
//	    ,{"HTML" , ClassNameWcType.OBJECT_ITEM_TEXT_ITEM.value(), "http-get:*:text/html:","*", http_mime_html}
//	    ,{"TXT" , ClassNameWcType.OBJECT_ITEM_TEXT_ITEM.value(), "http-get:*:text/plain:","*", http_mime_text}
    
    };
 
    static {
    	for (final String [] content : consts) {
    		extensionClassTypeMap.put(content[0], ClassNameWcType.fromValue(content[1]));
    		extensionProtocolInfoMap.put(content[0], content[2] + content[3]);
    		extensionHttpMimeMap.put(content[0], content[4]);
    	}
    }

    private final static Set<String> unsupportedExtensionsMissingClassType = new HashSet<String>();
    private final static Set<String> unsupportedExtensionsMissingProtocolInfo = new HashSet<String>();
    
//	@Autowired
//	private MediaserverConfiguration cfg;
	
	public String getMimeTypeForExtension(final String extension) {
		final String mime = extensionHttpMimeMap.get(extension.toUpperCase());
		if (mime == null) {
			return http_mime_bin;
		}
		return mime;
	}
	
	public String getServletPath(final ClassNameWcType type) {
		
		switch (type) {
		case OBJECT_ITEM_AUDIO_ITEM:
		case OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK:
		case OBJECT_ITEM_AUDIO_ITEM_AUDIO_BOOK:
		case OBJECT_ITEM_AUDIO_ITEM_AUDIO_BROADCAST:
			
			return "audio";
		case OBJECT_ITEM_IMAGE_ITEM:
		case OBJECT_ITEM_IMAGE_ITEM_PHOTO:		
			return "image";

		case OBJECT_ITEM_VIDEO_ITEM:
		case OBJECT_ITEM_VIDEO_ITEM_MOVIE:
		case OBJECT_ITEM_VIDEO_ITEM_MUSIC_VIDEO_CLIP:
		case OBJECT_ITEM_VIDEO_ITEM_VIDEO_BROADCAST:
			return "video";
			
		case OBJECT_ITEM_TEXT_ITEM:
		case OBJECT_ITEM_PLAYLIST_ITEM:
			return "text";
	
		default:
			return "unsupported";
		}
		
	}
	
	
	@Autowired
	private IdService idservice;
	

	public String getProtocolInfoForExtension(final String extension) {
		return extensionProtocolInfoMap.get(extension.toUpperCase());
	}
	
	public DidlDomain createDidlContainer(final ScanFolderDomain folder, final DidlDomain parent) {
		 final DidlDomain didl = new DidlDomain(folder);
		 for (final DidlDomain dd : parent.getContainerContent()) {
			 if (ClassNameWcType.OBJECT_CONTAINER_STORAGE_VOLUME.equals(dd.getClassType())) {
				 dd.addChild(didl);
			 }
		 }
		 
		 return didl;
	}
	
	private DidlDomain createDidlItem(final String title,final FileDomain file, final ClassNameWcType classType, final String url, final String protocolInfo) {
		return new DidlDomain(title, file, classType, url, protocolInfo);
	}	
	
	private DidlDomain createDidlContainer(final String id, final File file, final DidlDomain didl) {
		ClassNameWcType classType  = ClassNameWcType.OBJECT_CONTAINER_STORAGE_FOLDER;
		if (didl.getClassType().value().contains("audio")) {
			classType = ClassNameWcType.OBJECT_CONTAINER_ALBUM_MUSIC_ALBUM;
		} else {
			if (didl.getClassType().value().contains("image")) {
				classType = ClassNameWcType.OBJECT_CONTAINER_ALBUM_PHOTO_ALBUM;
			}
		}
	
		return new DidlDomain(id, file.getName(), file.getPath(), classType);
	}
	
	public boolean createDidl(final FileDomain fd, final File f, final List<String> allDidlIds, final Map<String, List<DidlDomain>> didlParentIdDidlMap, final Map<String, DidlDomain> idDidlMap) {
		final DidlDomain item = createDidlItem(fd);
		if (item != null) {
			createDidlContainerTree(item, f.getParentFile(),allDidlIds, didlParentIdDidlMap, idDidlMap);
			
			return true;			
		} else {
			return false;
		}		
	}
	
	private void createDidlContainerTree(final DidlDomain didl, final File parentFile, final List<String> allIds, final Map<String, List<DidlDomain>> existingMap, final Map<String, DidlDomain> newMap) {
		final String parentId = this.idservice.getId(parentFile);
		
		if (allIds.contains(parentId)) {
			// don't create anything
			
			// check if existingMap has key
			if (!existingMap.containsKey(parentId)) {
				existingMap.put(parentId, new ArrayList<DidlDomain>());
			}
		} else {
			
			final DidlDomain parent = createDidlContainer(parentId, parentFile, didl);
			
			newMap.put(parent.getId(), parent);
			// add live list to existing map
			existingMap.put(parent.getId(), parent.getContainerContent());
			allIds.add(parentId);
			// create parents for parent
			createDidlContainerTree(parent, parentFile.getParentFile(), allIds, existingMap,newMap);
		}
		
		existingMap.get(parentId).add(didl);
		didl.setParent(newMap.get(parentId));
	}

	public DidlDomain createDidlItem(final FileDomain file) {
		final int extensionCut = file.getName().lastIndexOf(".");
		if (extensionCut > 0) {		
			final String name = file.getName().substring(0, extensionCut);
			final String extension = file.getName().substring(extensionCut+1).toUpperCase();
			
			final ClassNameWcType classType = extensionClassTypeMap.get(extension);
			if (classType == null) {
				unsupportedExtensionsMissingClassType.add(extension);
			} else {
				
//			    <upnp:genre>Unbekannt</upnp:genre>
//			    <upnp:album>Filme</upnp:album>
//			    <upnp:albumArtURI 
//			    	dlna:profileID="JPEG_TN" 
//			    >http://192.168.101.50:9000/disk/DLNA-PNJPEG_TN-CI1-FLAGS00f00000/defaultalbumart/v_i_d_e_o.jpg/O0$3$27I21514.jpg?scale=160x160</upnp:albumArtURI>
//			    <res 
//			    	duration="1:26:39" 
//			    	size="733882368" 
//			    	resolution="640x272" 
//			    	protocolInfo="http-get:*:video/avi:DLNA.ORG_PN=AVI;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000" 
//			    >http://192.168.101.50:9000/disk/DLNA-PNAVI-OP01-FLAGS01700000/O0$3$27I21514.avi</res>
//			    <res 
//			    	protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000" 
//			    >http://192.168.101.50:9000/disk/DLNA-PNJPEG_TN-CI1-FLAGS00f00000/defaultalbumart/v_i_d_e_o.jpg/O0$3$27I21514.jpg?scale=160x160</res>
//				
				final String contentProtocol = extensionProtocolInfoMap.get(extension);
				if (contentProtocol != null) {
					return createDidlItem(name, file, classType,file.getId() + "." + extension,  contentProtocol);
				} else {
					unsupportedExtensionsMissingProtocolInfo.add(extension);
				}
			}
		} else {
			// no extension, no mapping -> don't show		
		}	
		
		return null;
	}

	public int getDefaultFolderCount() {
		return 2;
	}	
	
	public String getMissingClassTypeExtensions() {
			return getSortedExtensionString(unsupportedExtensionsMissingClassType);
	}

	private String getSortedExtensionString(
			final Set<String> set) {
		final StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (final String s : getSortedExtensionList(set)) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;				
			}
			sb.append(s);
		}
		return sb.toString();
	}

	public String getMissingProtocolInfoExtensions() {
		return getSortedExtensionString(unsupportedExtensionsMissingProtocolInfo);
}
	
	private List<String> getSortedExtensionList(
			final Set<String> set) {
		
		final ArrayList<String> list = new ArrayList<String>(set);
		Collections.sort(list);
		return list;
	}

	public boolean foundUnsupportedFiles() {
		return !unsupportedExtensionsMissingClassType.isEmpty() || !unsupportedExtensionsMissingProtocolInfo.isEmpty();
	}


	
}
