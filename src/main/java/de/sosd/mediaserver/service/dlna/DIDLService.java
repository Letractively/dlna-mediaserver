package de.sosd.mediaserver.service.dlna;

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

import de.sosd.mediaserver.dao.DidlDao;
import de.sosd.mediaserver.domain.db.ClassNameWcType;
import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.FileDomain;
import de.sosd.mediaserver.domain.db.ScanFolderDomain;
import de.sosd.mediaserver.util.DidlChangeMap;
import de.sosd.mediaserver.util.ScanFile;
import de.sosd.mediaserver.util.ScanFolder;

@Service
public class DIDLService {

    @SuppressWarnings("unused")
    private final static Log                          logger                                   = LogFactory
                                                                                                       .getLog(DIDLService.class);

    private static final Map<String, ClassNameWcType> extensionClassTypeMap                    = new HashMap<String, ClassNameWcType>();
    private static final Map<String, String>          extensionProtocolInfoMap                 = new HashMap<String, String>();
    private static final Map<String, String>          extensionHttpMimeMap                     = new HashMap<String, String>();

    private final static String                       http_mime_bin                            = "application/x-octet-stream";
    private final static String                       http_mime_avi                            = "video/avi";
    private final static String                       http_mime_asf                            = "video/x-ms-asf";
    private final static String                       http_mime_wmv                            = "video/x-ms-wmv";
    private final static String                       http_mime_mp4                            = "video/mp4";
    private final static String                       http_mime_mpeg                           = "video/mpeg";
    private final static String                       http_mime_mpeg2                          = "video/mpeg2";
    private final static String                       http_mime_mp2t                           = "video/mp2t";
    private final static String                       http_mime_mp2p                           = "video/mp2p";
    private final static String                       http_mime_mov                            = "video/quicktime";
    private final static String                       http_mime_aac                            = "audio/x-aac";
    private final static String                       http_mime_ac3                            = "audio/x-ac3";
    private final static String                       http_mime_mp3                            = "audio/mpeg";
    private final static String                       http_mime_ogg                            = "application/ogg";
    private final static String                       http_mime_wma                            = "audio/x-ms-wma";
    @SuppressWarnings("unused")
    private final static String                       http_mime_xml                            = "text/xml";
    @SuppressWarnings("unused")
    private final static String                       http_mime_html                           = "text/html; charset=\"utf-8\"";
    private final static String                       http_mime_text                           = "text/plain";

    // upnp mime types
    private final static String                       upnp_avi                                 = "http-get:*:video/avi:";
    private final static String                       upnp_asf                                 = "http-get:*:video/x-ms-asf:";
    private final static String                       upnp_wmv                                 = "http-get:*:video/x-ms-wmv:";
    private final static String                       upnp_mp4                                 = "http-get:*:video/mp4:";
    private final static String                       upnp_mpeg                                = "http-get:*:video/mpeg:";
    private final static String                       upnp_mpeg2                               = "http-get:*:video/mpeg2:";
    private final static String                       upnp_mp2t                                = "http-get:*:video/mp2t:";
    private final static String                       upnp_mp2p                                = "http-get:*:video/mp2p:";
    private final static String                       upnp_mov                                 = "http-get:*:video/quicktime:";
    private final static String                       upnp_aac                                 = "http-get:*:audio/x-aac:";
    private final static String                       upnp_ac3                                 = "http-get:*:audio/x-ac3:";
    private final static String                       upnp_mp3                                 = "http-get:*:audio/mpeg:";
    private final static String                       upnp_ogg                                 = "http-get:*:audio/x-ogg:";
    private final static String                       upnp_wma                                 = "http-get:*:audio/x-ms-wma:";

    private final static String[][]                   consts                                   = {
            {
            "MPG",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mpeg,
            "DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_mpeg } // default
            ,
            {
            "MPEG",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mpeg,
            "DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_mpeg }
            ,
            {
            "MPEG2",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mpeg2,
            "DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_mpeg2 }
            ,
            {
            "M2V",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mpeg2,
            "DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_mpeg2 }
            ,
            {
            "TS",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mp2t,
            "DLNA.ORG_PN=MPEG_TS_HD_NA;DLNA.ORG_OP=00;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_mp2t }
            ,
            {
            "M2TS",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mp2t,
            "DLNA.ORG_PN=MPEG_TS_HD_NA;DLNA.ORG_OP=00;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_mp2t }
            ,
            {
            "MTS",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mp2t,
            "DLNA.ORG_PN=MPEG_TS_HD_NA;DLNA.ORG_OP=00;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_mp2t }
            ,
            {
            "VOB",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mp2p,
            "DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=11;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_mp2p }
            ,
            {
            "AVI",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_avi,
            "DLNA.ORG_PN=AVI;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_avi }
            ,
            { "ASF", ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_asf, "*", http_mime_asf }
            ,
            {
            "WMV",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_wmv,
            "DLNA.ORG_PN=WMVHIGH_FULL;DLNA.ORG_OP=00;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_wmv }
            ,
            { "MP4", ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mp4, "*", http_mime_mp4 }
            ,
            { "M4V", ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mp4, "*", http_mime_mp4 }
            ,
            { "MOV", ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_mov, "*", http_mime_mov }
            ,
            {
            "XVID",
            ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            upnp_avi,
            "DLNA.ORG_PN=AVI;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_avi }
            ,
            { "MKV", ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE.value(),
            "http-get:*:video/x-matroska:", "*", http_mime_bin }

            ,
            { "AAC",
            ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),
            upnp_aac, "*", http_mime_aac }
            ,
            {
            "AC3",
            ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),
            upnp_ac3,
            "DLNA.ORG_PN=AC3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_ac3 }
            ,
            {
            "MP3",
            ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),
            upnp_mp3,
            "DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_mp3 }
            ,
            { "OGG",
            ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),
            upnp_ogg, "*", http_mime_ogg }
            ,
            {
            "WMA",
            ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),
            upnp_wma,
            "DLNA.ORG_PN=WMAFULL;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000",
            http_mime_wma }
            ,
            { "WAV",
            ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),
            "http-get:*:audio/L16:", "MICROSOFT.COM_PN=WAV_PCM", http_mime_bin }
            ,
            { "PCM",
            ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK.value(),
            "http-get:*:audio/L16:", "MICROSOFT.COM_PN=WAV_PCM", http_mime_bin }
            // TODO acquire ProfileName / DLNA.ORG_PN with actual image-size
            ,
            {
            "JPG",
            ClassNameWcType.OBJECT_ITEM_IMAGE_ITEM_PHOTO.value(),
            "http-get:*:image/jpeg:",
            "DLNA.ORG_PN=JPEG_LRG;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000",
            http_mime_bin }
            ,
            {
            "JPEG",
            ClassNameWcType.OBJECT_ITEM_IMAGE_ITEM_PHOTO.value(),
            "http-get:*:image/jpeg:",
            "DLNA.ORG_PN=JPEG_LRG;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000",
            http_mime_bin }
            ,
            { "GIF", ClassNameWcType.OBJECT_ITEM_IMAGE_ITEM_PHOTO.value(),
            "http-get:*:image/gif:", "*", http_mime_bin }
            ,
            {
            "PNG",
            ClassNameWcType.OBJECT_ITEM_IMAGE_ITEM_PHOTO.value(),
            "http-get:*:image/png:",
            "DLNA.ORG_PN=PNG_LRG;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000",
            http_mime_bin }

            ,
            { "M3U", ClassNameWcType.OBJECT_ITEM_PLAYLIST_ITEM.value(),
            "http-get:*:audio/m3u:", "*", http_mime_text }
                                                                                                       // removed
                                                                                                       // to
                                                                                                       // keep
                                                                                                       // the
                                                                                                       // db
                                                                                                       // clean
                                                                                                       // ,{"XML"
                                                                                                       // ,
                                                                                                       // ClassNameWcType.OBJECT_ITEM_TEXT_ITEM.value(),
                                                                                                       // "http-get:*:text/xml:","*",
                                                                                                       // http_mime_xml}
                                                                                                       // ,{"HTM"
                                                                                                       // ,
                                                                                                       // ClassNameWcType.OBJECT_ITEM_TEXT_ITEM.value(),
                                                                                                       // "http-get:*:text/html:","*",
                                                                                                       // http_mime_html}
                                                                                                       // ,{"HTML"
                                                                                                       // ,
                                                                                                       // ClassNameWcType.OBJECT_ITEM_TEXT_ITEM.value(),
                                                                                                       // "http-get:*:text/html:","*",
                                                                                                       // http_mime_html}
                                                                                                       // ,{"TXT"
                                                                                                       // ,
                                                                                                       // ClassNameWcType.OBJECT_ITEM_TEXT_ITEM.value(),
                                                                                                       // "http-get:*:text/plain:","*",
                                                                                                       // http_mime_text}

                                                                                               };

    static {
        for (final String[] content : consts) {
            extensionClassTypeMap.put(content[0],
                    ClassNameWcType.fromValue(content[1]));
            extensionProtocolInfoMap.put(content[0], content[2] + content[3]);
            extensionHttpMimeMap.put(content[0], content[4]);
        }
    }

    private final static Set<String>                  unsupportedExtensionsMissingClassType    = new HashSet<String>();
    private final static Set<String>                  unsupportedExtensionsMissingProtocolInfo = new HashSet<String>();

    @Autowired
    private DidlDao                                   dao;

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

    public String getProtocolInfoForExtension(final String extension) {
        return extensionProtocolInfoMap.get(extension.toUpperCase());
    }

    public DidlDomain createDidlContainer(final ScanFolderDomain folder,
            final DidlDomain systemRoot) {
        for (final DidlDomain parent : systemRoot.getContainerContent()) {
            if (ClassNameWcType.OBJECT_CONTAINER_STORAGE_VOLUME.equals(parent
                    .getClassType())) {
                return new DidlDomain(folder, parent);
            }
        }

        return null;
    }

    public boolean createDidl(final FileDomain fd, final ScanFile f,
            final DidlChangeMap touchedDidlMap, final ScanFolderDomain sfd) {
        final String extension = getExtension(fd);
        final ClassNameWcType classType = extensionClassTypeMap.get(extension);
        final String contentProtocol = extensionProtocolInfoMap.get(extension);

        if (evaluate(fd, extension, classType, contentProtocol)) {
            final DidlDomain parent = createDidlContainerTree(f.getParent(),
                    touchedDidlMap, sfd);
            final DidlDomain didl = new DidlDomain(getTitle(fd), fd, classType,
                    fd.getId() + "." + extension, contentProtocol, parent);
            touchedDidlMap.addDidl(didl.getId(), didl);

            updateParentClassType(didl, parent);
            this.dao.store(didl);
            return true;
        }

        return false;
    }

    private void updateParentClassType(final DidlDomain didl,
            final DidlDomain parent) {
        if (didl.getClassType().value().contains("audio")) {
            parent.setClassType(ClassNameWcType.OBJECT_CONTAINER_ALBUM_MUSIC_ALBUM);
        } else {
            if (didl.getClassType().value().contains("image")) {
                parent.setClassType(ClassNameWcType.OBJECT_CONTAINER_ALBUM_PHOTO_ALBUM);
            }
        }
    }

    private boolean evaluate(final FileDomain fd, final String extension,
            final ClassNameWcType classType, final String contentProtocol) {
        if (fd.getSize() > 0 && extension != null && classType != null
                && contentProtocol != null) {
            return true;
        }
        if (extension != null) {
            if (classType == null) {
                unsupportedExtensionsMissingClassType.add(extension);
            }
            if (contentProtocol == null) {
                unsupportedExtensionsMissingProtocolInfo.add(extension);
            }
        }
        return false;
    }

    private String getTitle(final FileDomain fd) {
        final int extensionCut = fd.getName().lastIndexOf(".");
        if (extensionCut > 0) {
            return fd.getName().substring(0, extensionCut);
        }
        return fd.getName();
    }

    private String getExtension(final FileDomain fd) {
        final int extensionCut = fd.getName().lastIndexOf(".");
        if (extensionCut > 0) {
            return fd.getName().substring(extensionCut + 1).toUpperCase();
        }
        return null;
    }

    private DidlDomain createDidlContainerTree(final ScanFolder scanFolder,
            final DidlChangeMap touchedDidlMap, final ScanFolderDomain sfd) {
        final String id = scanFolder.getId();
        if (touchedDidlMap.hasDidl(id)) {
            // don't create anything
            return touchedDidlMap.getDidl(id);
        } else {
            final DidlDomain parent = createDidlContainerTree(
                    scanFolder.getParent(), touchedDidlMap, sfd);
            final DidlDomain didl = new DidlDomain(id, scanFolder.getFile()
                    .getName(), scanFolder.getFile().getPath(),
                    ClassNameWcType.OBJECT_CONTAINER_STORAGE_FOLDER, parent);
            sfd.addFolder(didl);
            this.dao.store(didl);
            return touchedDidlMap.addDidl(id, didl);
        }
    }

    public int getDefaultFolderCount() {
        return 2;
    }

    public String getMissingClassTypeExtensions() {
        return getSortedExtensionString(unsupportedExtensionsMissingClassType);
    }

    private String getSortedExtensionString(
            final Set<String> set) {
        final StringBuilder sb = new StringBuilder();
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
        return !unsupportedExtensionsMissingClassType.isEmpty()
                || !unsupportedExtensionsMissingProtocolInfo.isEmpty();
    }

}
