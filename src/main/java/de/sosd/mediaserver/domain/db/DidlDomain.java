package de.sosd.mediaserver.domain.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringEscapeUtils;
import org.hibernate.annotations.Index;

import de.sosd.mediaserver.service.db.ThumbnailPurger;
import de.sosd.mediaserver.util.DidlXmlCreator;


@Entity(name="DIDL")
@Table(name="dlna")
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@EntityListeners(value = { ThumbnailPurger.class })
@org.hibernate.annotations.Entity(
		dynamicUpdate = true
)
public class DidlDomain implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -212770432498342620L;

	@Id
    @Column(name="id",length=36)	
    private String id;
    @Column(name="title",length=255, nullable=false)	
    private String title;
    @Index(name="idx_path", columnNames= {"path"})
    @Column(name="path", length=4096, nullable = true)    
    private String path;    
    @Column(name="video_codec",length=20)	
    private String videoCodec;
    @Column(name="audio_codec",length=20)	
    private String audioCodec;
//    @Column(name="contributor",length=1024)	
//    private String contributor;
//    @Column(name="creator",length=1024)	
//    private String creator;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="date")	
    private Date date;
    @Column(name="description",length=1024)	
    private String description;
    @Column(name="language",length=32)	
    private String language;
//    @Column(name="publisher",length=1024)	
//    private String publisher;
//    @Column(name="relation",length=1024)	
//    private String relation;
//    @Column(name="rights",length=1024)	
//    private String rights;
    @Column(name="class", nullable=false)	
    @Enumerated(EnumType.ORDINAL)
    private ClassNameWcType classType;
    @Column(name="restricted", nullable=false)	
    private boolean restricted = true;
    @Column(name="neverPlayable")	
    private Boolean neverPlayable;
    @Column(name="artist",length=255)	   
    private String artist;
    @Column(name="album",length=255)	   
    private String album;
//    @Column(name="albumArtUri",length=1024)	
//    private String albumArtUri;
    @Column(name="genre",length=64)	
    private String genre;
    @Column(name="year")	
    private Integer year;  
    @Column(name="track")	
    private Integer track;     
    
    /**** item-info ****/    
    @Column(name="value",length=150)
    private String url;
//    @Column(name = "importUri",length=1024)
//    private String importUri;
    @Column(name = "protocolInfo",length=150)
    private String protocolInfo;
    @Column(name = "size")
    private Long size;
    @Column(name="duration", length=16)
    private String duration;    
    @Column(name = "bitrate")
    private Integer bitrate;
    @Column(name = "sample_frequency")
    private Integer sampleFrequency;
    @Column(name = "bits_per_sample")
    private Integer bitsPerSample;
    @Column(name = "nr_audio_channels")
    private Integer nrAudioChannels;
    @Column(name = "resolution",length=16)
    private String resolution;
    @Column(name = "color_depth")
    private Integer colorDepth;
//    @Column(name = "usage_info")
//    private String usageInfo;
//    @Column(name = "rights_info_uri",length=1024)
//    private String rightsInfoURI;
//    @Column(name = "content_info_uri",length=1024)
//    private String contentInfoURI;
//    @Column(name = "protection",length=1024)
//    private String protection;
//    @Column(name = "daylight_saving",length=1024)
//    private String daylightSaving;    
    @Column(name = "update_id")
    private int updateId = 0;
    
    /**** thumbnail-info ****/
    @Embedded
    private ThumbnailDomain thumbnail;
    
    @ManyToOne(targetEntity=DidlDomain.class, fetch = FetchType.LAZY, optional=true)
    private DidlDomain parent;	
 	@OneToMany(targetEntity=DidlDomain.class, fetch=FetchType.LAZY, mappedBy="parent", cascade=CascadeType.ALL)
	private List<DidlDomain> containerContent = new ArrayList<DidlDomain>(5);
 	@Column(name="child_count")
 	private Integer containerContentSize;
 	@OneToOne(optional=true, targetEntity=FileDomain.class,fetch=FetchType.LAZY)
 	private FileDomain file;
	@OneToOne(optional=true, targetEntity=ScanFolderDomain.class,fetch=FetchType.LAZY, mappedBy="didl")
 	private ScanFolderDomain folder;
	@OneToOne(optional=true, targetEntity=SystemDomain.class,fetch=FetchType.LAZY, mappedBy="didlRoot")
 	private SystemDomain system;
	
    @ManyToOne(targetEntity=DidlDomain.class, fetch = FetchType.LAZY, optional=true)
    private DidlDomain reference;		
 	@OneToMany(targetEntity=DidlDomain.class, fetch=FetchType.LAZY, mappedBy="reference", cascade=CascadeType.ALL)
	private List<DidlDomain> references = new ArrayList<DidlDomain>(5);
	
	/*********** DIDL-OPTIONS **************/
	@Column(name="searchable")
	private Boolean searchable = true;
	@Column(name="mplayer_pass")
	private Boolean passedMPlayer;
	@Column(name="seekable")
	private Boolean seekable = true;
	@Column(name="thumb_generate")
	private Boolean generateThumbnail = true;	
	@Column(name="online")
	private Boolean online = true;	
	
	public DidlDomain() {
		super();
	}
	
	public DidlDomain(final String title, final FileDomain file, final ClassNameWcType classType, final String url, final String protocolInfo, DidlDomain parent) {
		this(file.getId(), title, file.getPath(), classType, parent);
		this.date = file.getLastModified();
		this.url = url;
		this.protocolInfo = protocolInfo;
		this.size = file.getSize();
		this.file = file;
		file.setDidl(this);
	}

	public DidlDomain(final ScanFolderDomain folder, DidlDomain parent) {
		this(folder.getId(), folder.getName(), folder.getPath(), ClassNameWcType.OBJECT_CONTAINER_STORAGE_VOLUME, parent);
		folder.setDidl(this);
	}

	public DidlDomain(final SystemDomain system) {
		this(system.getHostname(),"/", "root",ClassNameWcType.OBJECT_CONTAINER_STORAGE_SYSTEM,null);
		setPath(system.getHostname() + ":");
		system.setDidlRoot(this);
		system.addFolder(
				new DidlDomain(UUID.randomUUID().toString(), "Filesystem", "The Filesystem", ClassNameWcType.OBJECT_CONTAINER_STORAGE_VOLUME, this)
		);		
	}

	public DidlDomain(final String id, final String title, final String description, final ClassNameWcType classType, DidlDomain parent) {
		this();
		this.id = id;
		this.title = title;
		this.date = new Date();
		this.description = description;
		this.classType = classType;
		
		if (parent != null) {
			setParent(parent);
			getParent().addChild(this);
			
			setPath(getParent().getPath() + "/" + getTitle());
		}
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * @return the date
	 */
	public Date getDate() {
		return this.date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(final Date date) {
		this.date = date;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return this.language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(final String language) {
		this.language = language;
	}

	/**
	 * @return the classType
	 */
	public ClassNameWcType getClassType() {
		return this.classType;
	}

	/**
	 * @param classType the classType to set
	 */
	public void setClassType(final ClassNameWcType classType) {
		this.classType = classType;
	}

	/**
	 * @return the restricted
	 */
	public boolean isRestricted() {
		return this.restricted;
	}

	/**
	 * @param restricted the restricted to set
	 */
	public void setRestricted(final boolean restricted) {
		this.restricted = restricted;
	}

	/**
	 * @return the neverPlayable
	 */
	public Boolean getNeverPlayable() {
		return this.neverPlayable;
	}

	/**
	 * @param neverPlayable the neverPlayable to set
	 */
	public void setNeverPlayable(final Boolean neverPlayable) {
		this.neverPlayable = neverPlayable;
	}

	/**
	 * @return the album
	 */
	public String getAlbum() {
		return this.album;
	}

	/**
	 * @param album the album to set
	 */
	public void setAlbum(final String album) {
		this.album = album;
	}

	/**
	 * @return the genre
	 */
	public String getGenre() {
		return this.genre;
	}

	/**
	 * @param genre the genre to set
	 */
	public void setGenre(final String genre) {
		this.genre = genre;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(final String url) {
		this.url = url;
	}

	/**
	 * @return the protocolInfo
	 */
	public String getProtocolInfo() {
		return this.protocolInfo;
	}

	/**
	 * @param protocolInfo the protocolInfo to set
	 */
	public void setProtocolInfo(final String protocolInfo) {
		this.protocolInfo = protocolInfo;
	}

	/**
	 * @return the size
	 */
	public Long getSize() {
		return this.size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(final Long size) {
		this.size = size;
	}

	/**
	 * @return the duration
	 */
	public String getDuration() {
		return this.duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(final String duration) {
		this.duration = duration;
	}

	/**
	 * @return the bitrate
	 */
	public Integer getBitrate() {
		return this.bitrate;
	}

	/**
	 * @param bitrate the bitrate to set
	 */
	public void setBitrate(final Integer bitrate) {
		this.bitrate = bitrate;
	}

	/**
	 * @return the sampleFrequency
	 */
	public Integer getSampleFrequency() {
		return this.sampleFrequency;
	}

	/**
	 * @param sampleFrequency the sampleFrequency to set
	 */
	public void setSampleFrequency(final Integer sampleFrequency) {
		this.sampleFrequency = sampleFrequency;
	}

	/**
	 * @return the bitsPerSample
	 */
	public Integer getBitsPerSample() {
		return this.bitsPerSample;
	}

	/**
	 * @param bitsPerSample the bitsPerSample to set
	 */
	public void setBitsPerSample(final Integer bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}

	/**
	 * @return the nrAudioChannels
	 */
	public Integer getNrAudioChannels() {
		return this.nrAudioChannels;
	}

	/**
	 * @param nrAudioChannels the nrAudioChannels to set
	 */
	public void setNrAudioChannels(final Integer nrAudioChannels) {
		this.nrAudioChannels = nrAudioChannels;
	}

	/**
	 * @return the resolution
	 */
	public String getResolution() {
		return this.resolution;
	}

	/**
	 * @param resolution the resolution to set
	 */
	public void setResolution(final String resolution) {
		this.resolution = resolution;
	}

	/**
	 * @return the colorDepth
	 */
	public Integer getColorDepth() {
		return this.colorDepth;
	}

	/**
	 * @param colorDepth the colorDepth to set
	 */
	public void setColorDepth(final Integer colorDepth) {
		this.colorDepth = colorDepth;
	}

	/**
	 * @return the thumbnail
	 */
	public ThumbnailDomain getThumbnail() {
		return this.thumbnail;
	}

	/**
	 * @param thumbnail the thumbnail to set
	 */
	public void setThumbnail(final ThumbnailDomain thumbnail) {
		this.thumbnail = thumbnail;
	}

	/**
	 * @return the parent
	 */
	public DidlDomain getParent() {
		return this.parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(final DidlDomain parent) {
		this.parent = parent;
	}

	/**
	 * @return the file
	 */
	public FileDomain getFile() {
		return this.file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(final FileDomain file) {
		this.file = file;
	}

	/**
	 * @return the folder
	 */
	public ScanFolderDomain getFolder() {
		return this.folder;
	}

	/**
	 * @param folder the folder to set
	 */
	public void setFolder(final ScanFolderDomain folder) {
		this.folder = folder;
	}

	/**
	 * @return the system
	 */
	public SystemDomain getSystem() {
		return this.system;
	}

	/**
	 * @param system the system to set
	 */
	public void setSystem(final SystemDomain system) {
		this.system = system;
	}

	/**
	 * @return the searchable
	 */
	public Boolean getSearchable() {
		return this.searchable;
	}

	/**
	 * @param searchable the searchable to set
	 */
	public void setSearchable(final Boolean searchable) {
		this.searchable = searchable;
	}

	/**
	 * @return the containerContent
	 */
	public List<DidlDomain> getContainerContent() {
		return this.containerContent;
	}
	
	public int getUpdateId() {
		return this.updateId;
	}
	
	public void setUpdateId(final int updateId) {
		if (updateId < 0) {
			this.updateId = 0;
		} else {
			this.updateId = updateId;
		}
	}
	
	/**
	 * @return the videoCodec
	 */
	public String getVideoCodec() {
		return this.videoCodec;
	}

	/**
	 * @param videoCodec the videoCodec to set
	 */
	public void setVideoCodec(final String videoCodec) {
		this.videoCodec = videoCodec;
	}

	/**
	 * @return the audioCodec
	 */
	public String getAudioCodec() {
		return this.audioCodec;
	}

	/**
	 * @param audioCodec the audioCodec to set
	 */
	public void setAudioCodec(final String audioCodec) {
		this.audioCodec = audioCodec;
	}

	/**
	 * @return the artist
	 */
	public String getArtist() {
		return this.artist;
	}

	/**
	 * @param artist the artist to set
	 */
	public void setArtist(final String artist) {
		this.artist = artist;
	}

	/**
	 * @return the year
	 */
	public Integer getYear() {
		return this.year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(final Integer year) {
		this.year = year;
	}

	/**
	 * @return the track
	 */
	public Integer getTrack() {
		return this.track;
	}

	/**
	 * @param track the track to set
	 */
	public void setTrack(final Integer track) {
		this.track = track;
	}

	/**
	 * @param containerContent the containerContent to set
	 */

	public void setReference(final DidlDomain reference) {
		reference.getReferences().add(this);
		this.reference = reference;
	}
	
	public DidlDomain getReference() {
		return this.reference;
	}
	
	public List<DidlDomain> getReferences() {
		return this.references;
	}
	
	public void setPassedMPlayer(final Boolean passedMPlayer) {
		this.passedMPlayer = passedMPlayer;
	}
	
	public Boolean getPassedMPlayer() {
		return this.passedMPlayer;
	}
	
	public Integer getContainerContentSize() {
		if (containerContentSize == null) {
			setContainerContentSize(getContainerContent().size());
		}
		return containerContentSize;
	}
	
	public void setContainerContentSize(Integer containerContentSize) {
		this.containerContentSize = containerContentSize;
	}
	
	@Transient
	private boolean updateIdIncreased = false;
	
	public void increaseUpdateId() {
		if (! updateIdIncreased) {
			setUpdateId(getUpdateId() + 1);
			updateIdIncreased = true;
			
			if (! isContainer() && getParent() != null) {
				getParent().increaseUpdateId();
			}
		}
	}

	public boolean isContainer() {
		switch (getClassType()) {
		case OBJECT_CONTAINER_ALBUM:
		case OBJECT_CONTAINER_ALBUM_MUSIC_ALBUM:			
		case OBJECT_CONTAINER_ALBUM_PHOTO_ALBUM:
		case OBJECT_CONTAINER_BOOKMARK_FOLDER:			
		case OBJECT_CONTAINER_CHANNEL_GROUP:			
		case OBJECT_CONTAINER_CHANNEL_GROUP_AUDIO_CHANNEL_GROUP:			
		case OBJECT_CONTAINER_CHANNEL_GROUP_VIDEO_CHANNEL_GROUP:			
		case OBJECT_CONTAINER_EPG_CONTAINER:			
		case OBJECT_CONTAINER_GENRE:			
		case OBJECT_CONTAINER_GENRE_MOVIE_GENRE:			
		case OBJECT_CONTAINER_GENRE_MUSIC_GENRE:
		case OBJECT_CONTAINER_PERSON:			
		case OBJECT_CONTAINER_PERSON_MUSIC_ARTIST:
		case OBJECT_CONTAINER_PLAYLIST_CONTAINER:			
		case OBJECT_CONTAINER_STORAGE_FOLDER:			
		case OBJECT_CONTAINER_STORAGE_SYSTEM:			
		case OBJECT_CONTAINER_STORAGE_VOLUME: return true;
		case OBJECT_ITEM:			
		case OBJECT_ITEM_AUDIO_ITEM:			
		case OBJECT_ITEM_AUDIO_ITEM_AUDIO_BOOK:			
		case OBJECT_ITEM_AUDIO_ITEM_AUDIO_BROADCAST:			
		case OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK:			
		case OBJECT_ITEM_BOOKMARK_ITEM:			
		case OBJECT_ITEM_EPG_ITEM:			
		case OBJECT_ITEM_EPG_ITEM_AUDIO_PROGRAM:			
		case OBJECT_ITEM_EPG_ITEM_VIDEO_PROGRAM:			
		case OBJECT_ITEM_IMAGE_ITEM:			
		case OBJECT_ITEM_IMAGE_ITEM_PHOTO:			
		case OBJECT_ITEM_PLAYLIST_ITEM:			
		case OBJECT_ITEM_TEXT_ITEM:			
		case OBJECT_ITEM_VIDEO_ITEM:			
		case OBJECT_ITEM_VIDEO_ITEM_MOVIE:			
		case OBJECT_ITEM_VIDEO_ITEM_MUSIC_VIDEO_CLIP:
		case OBJECT_ITEM_VIDEO_ITEM_VIDEO_BROADCAST:  return false;
		default : return !getContainerContent().isEmpty();
		}
	}

	public Boolean getSeekable() {
		return this.seekable;
	}
	
	public void setSeekable(final Boolean seekable) {
		this.seekable = seekable;
	}
	
	public Boolean getGenerateThumbnail() {
		return this.generateThumbnail;
	}
	
	public void setGenerateThumbnail(final Boolean generateThumbnail) {
		this.generateThumbnail = generateThumbnail;
	}
	
	public void setOnline(Boolean online) {
		this.online = online;
	}
	
	public Boolean getOnline() {
		return online;
	}
	
	public boolean isOnline() {
		return getOnline() == null || getOnline().booleanValue();
	}
	
	public void goOnline(boolean value) {
		// check ourselfs
		if (isOnline() != value) {
			// update ourselfs
			setOnline(value);
			increaseUpdateId();
			// check all children
			for (DidlDomain dd : getContainerContent()) {
				dd.goOnline(value);
			}
			// check all references
			for (DidlDomain dd : getReferences()) {
				dd.goOnline(value);
			}
		}
	}
	
	/*
	 *   
	<item id="{A8379C6F-6CDC-4367-ABE4-064569E67404}.0.6BA8E00E" refID="{A8379C6F-6CDC-4367-ABE4-064569E67404}.0.8" restricted="1" parentID="6BA8E00E">
	    <dc:title>5.Days.of.War</dc:title>
	    <dc:creator>[Unbekannter Autor]</dc:creator>
	    <res size="1469855744" duration="1:48:26.000" bitrate="225923" resolution="704x288" protocolInfo="http-get:*:video/avi:DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01500000000000000000000000000000" sampleFrequency="48000" bitsPerSample="16" nrAudioChannels="6" microsoft:codec="{5634504D-0000-0010-8000-00AA00389B71}" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">http://192.168.101.227:10243/WMPNSSv4/2341708733/0_e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.avi</res>
	    <res duration="1:48:26.000" bitrate="379846" resolution="352x480" protocolInfo="http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=10;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=01500000000000000000000000000000" nrAudioChannels="2" microsoft:codec="{E06D8026-DB46-11CF-B4D1-00805F6CBBEA}" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">http://192.168.101.227:10243/WMPNSSv4/2341708733/e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.mpg?formatID=29</res>
	    <res duration="1:48:26.000" bitrate="379846" resolution="352x576" protocolInfo="http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=10;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=01500000000000000000000000000000" nrAudioChannels="2" microsoft:codec="{E06D8026-DB46-11CF-B4D1-00805F6CBBEA}" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">http://192.168.101.227:10243/WMPNSSv4/2341708733/e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.mpg?formatID=35</res>
	    <res duration="1:48:26.000" bitrate="193923" resolution="704x288" protocolInfo="http-get:*:video/x-ms-asf:DLNA.ORG_PN=VC1_ASF_AP_L1_WMA;DLNA.ORG_OP=10;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=01500000000000000000000000000000" sampleFrequency="44100" nrAudioChannels="2" microsoft:codec="{31435657-0000-0010-8000-00AA00389B71}" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">http://192.168.101.227:10243/WMPNSSv4/2341708733/e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.asf?formatID=40</res>
	    <res duration="1:48:26.000" bitrate="193923" resolution="704x288" protocolInfo="rtsp-rtp-udp:*:video/x-ms-asf:DLNA.ORG_PN=VC1_ASF_AP_L1_WMA;DLNA.ORG_OP=10;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=83100000000000000000000000000000;DLNA.ORG_MAXSP=5" sampleFrequency="44100" nrAudioChannels="2" microsoft:codec="{31435657-0000-0010-8000-00AA00389B71}" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">rtsp://192.168.101.227:554/WMPNSSv4/2341708733/e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.asf?formatID=41</res>
	    <res duration="1:48:26.000" bitrate="56250" resolution="704x288" protocolInfo="http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_BASE;DLNA.ORG_OP=10;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=01500000000000000000000000000000" sampleFrequency="48000" nrAudioChannels="2" microsoft:codec="{33564D57-0000-0010-8000-00AA00389B71}" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">http://192.168.101.227:10243/WMPNSSv4/2341708733/e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.wmv?formatID=70</res>
	    <res duration="1:48:26.000" bitrate="56250" resolution="704x288" protocolInfo="rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_BASE;DLNA.ORG_OP=10;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=83100000000000000000000000000000;DLNA.ORG_MAXSP=5" sampleFrequency="48000" nrAudioChannels="2" microsoft:codec="{33564D57-0000-0010-8000-00AA00389B71}" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">rtsp://192.168.101.227:554/WMPNSSv4/2341708733/e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.wmv?formatID=71</res>
	    <res duration="1:48:26.000" bitrate="37500" resolution="352x144" protocolInfo="http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_BASE;DLNA.ORG_OP=10;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=01500000000000000000000000000000" sampleFrequency="44100" nrAudioChannels="2" microsoft:codec="{33564D57-0000-0010-8000-00AA00389B71}" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">http://192.168.101.227:10243/WMPNSSv4/2341708733/e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.wmv?formatID=80</res>
	    <res duration="1:48:26.000" bitrate="37500" resolution="352x144" protocolInfo="rtsp-rtp-udp:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_BASE;DLNA.ORG_OP=10;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=83100000000000000000000000000000;DLNA.ORG_MAXSP=5" sampleFrequency="44100" nrAudioChannels="2" microsoft:codec="{33564D57-0000-0010-8000-00AA00389B71}" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">rtsp://192.168.101.227:554/WMPNSSv4/2341708733/e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.wmv?formatID=81</res>
	    <upnp:class>object.item.videoItem</upnp:class>
	    <upnp:genre>[Unbekanntes Genre]</upnp:genre>
	    <upnp:artist role="Performer">[Unbekannter Autor]</upnp:artist>
	    <upnp:album>[Unbekannte Serie]</upnp:album>
	    <dc:date>2011-06-14</dc:date>
	    <upnp:actor>[Unbekannter Autor]</upnp:actor>
	    <upnp:scheduledStartTime>2011-06-14T20:21:00</upnp:scheduledStartTime>
	    <upnp:albumArtURI dlna:profileID="JPEG_SM" xmlns:dlna="urn:schemas-dlna-org:metadata-1-0/">http://192.168.101.227:10243/WMPNSSv4/2341708733/0_e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.jpg?albumArt=true</upnp:albumArtURI>
	    <upnp:albumArtURI dlna:profileID="JPEG_TN" xmlns:dlna="urn:schemas-dlna-org:metadata-1-0/">http://192.168.101.227:10243/WMPNSSv4/2341708733/e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.jpg?albumArt=true,formatID=13</upnp:albumArtURI>
	    <desc id="artist" nameSpace="urn:schemas-microsoft-com:WMPNSS-1-0/" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">
	      <microsoft:artistPerformer>[Unbekannter Autor]</microsoft:artistPerformer>
	    </desc>
	    <desc id="UserRating" nameSpace="urn:schemas-microsoft-com:WMPNSS-1-0/" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">
	      <microsoft:userEffectiveRatingInStars>3</microsoft:userEffectiveRatingInStars>
	      <microsoft:userEffectiveRating>50</microsoft:userEffectiveRating>
	    </desc>
	    <desc id="folderPath" nameSpace="urn:schemas-microsoft-com:WMPNSS-1-0/" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">
	      <microsoft:folderPath>Filme</microsoft:folderPath>
	    </desc>
  	</item>
	 * 
	 * 
	 * */
	public void intoDidlXml(final StringBuffer out, final DidlXmlCreator helper) {
		if (isContainer()) {
			containerIntoDidl(out, helper);
		} else {
			itemIntoDidl(out, helper);
		}		
	}

	private void containerIntoDidl(final StringBuffer out, final DidlXmlCreator helper) {
//	  <container id="0$0$0$0" childCount="0" parentID="0$0$0" restricted="true">
//	    <dc:title>Planet Of The Apes Legacy Collection</dc:title>
//	    <dc:date>2009-09-11T23:53:10</dc:date>
//	    <upnp:class>object.container.storageFolder</upnp:class>
//	  </container>
		// write node and attributes
		out.append("<container id=\"");
		out.append(helper.translateId(this));
		out.append("\" childCount=\"");
		out.append(getContainerContentSize());
		if (getReference() != null) {
			out.append("\" refID=\"");
			out.append(helper.translateId(getReference()));
		}
		out.append("\" restricted=\"");
		if (isRestricted()) {
			out.append("1");
		} else {
			out.append("0");
		}
		
		if (getSearchable() != null) {
			out.append("\" searchable=\"");
			if (getSearchable().booleanValue()) {		
				out.append("1");
			} else {
				out.append("0");
			}
		}
		out.append("\" parentID=\"");
		out.append(helper.translateId(getParent()));
		out.append("\">");
		
		// write content
		addTitle(out);
		addDate(out, helper);
		addClass(out);
		
		// addUpdateId
		// <xsd:element name="containerUpdateID" type="xsd:unsignedInt"/>
		out.append("<containerUpdateID>");
		out.append(getUpdateId());
		out.append("</containerUpdateID>");
		// close node
		out.append("</container>");
	}

	private void itemIntoDidl(final StringBuffer out, final DidlXmlCreator helper) {
		// write node and attributes
		out.append("<item id=\"");
		out.append(helper.translateId(this));
		if (getReference() != null) {
			out.append("\" refID=\"");
			out.append(helper.translateId(getReference()));
		}
		out.append("\" restricted=\"");
		if (isRestricted()) {
			out.append("1");
		} else {
			out.append("0");
		}
		out.append("\" parentID=\"");
		out.append(helper.translateId(getParent()));
		out.append("\">");
		
		// write content
		addTitle(out);
		addCreator(out);
		addStreamRes(out, helper);
		addThumbRes(out,helper);
		addClass(out);
		addGenre(out);
		addArtist(out);
		addAlbum(out);
		addTrack(out);
		addDate(out, helper);
		addActor(out);
		addScheduledStartTime(out, helper);
		addAlbumArtURI(out, helper);
		addArtistMS(out);
		addUserRatingMS(out);
		addFolderPathMS(out);
		// <xsd:element name="objectUpdateID" type="xsd:unsignedInt"/>
		out.append("<objectUpdateID>");
		out.append(getUpdateId());
		out.append("</objectUpdateID>");		
		// close node
		out.append("</item>");	
	}



	private void addFolderPathMS(final StringBuffer out) {
		/*
	    <desc id="folderPath" nameSpace="urn:schemas-microsoft-com:WMPNSS-1-0/" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">
	      <microsoft:folderPath>Filme</microsoft:folderPath>
	    </desc>
		*/
		if (getParent() != null) {
			out.append("<desc id=\"folderPath\" nameSpace=\"urn:schemas-microsoft-com:WMPNSS-1-0/\" xmlns:microsoft=\"urn:schemas-microsoft-com:WMPNSS-1-0/\">");
			out.append("<microsoft:folderPath>");
			out.append(getXmlString(getParent().getTitle()));
			out.append("</microsoft:folderPath>");
			out.append("</desc>");		
		}
	}



	private void addUserRatingMS(final StringBuffer out) {
		/*
		 <desc id="UserRating" nameSpace="urn:schemas-microsoft-com:WMPNSS-1-0/" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">
	      <microsoft:userEffectiveRatingInStars>3</microsoft:userEffectiveRatingInStars>
	      <microsoft:userEffectiveRating>50</microsoft:userEffectiveRating>
	    </desc>
		 * */
		out.append("<desc id=\"UserRating\" nameSpace=\"urn:schemas-microsoft-com:WMPNSS-1-0/\" xmlns:microsoft=\"urn:schemas-microsoft-com:WMPNSS-1-0/\">");
		out.append("<microsoft:userEffectiveRatingInStars>3</microsoft:userEffectiveRatingInStars>");
		out.append("<microsoft:userEffectiveRating>50</microsoft:userEffectiveRating>");
		out.append("</desc>");
	}

	private void addScheduledStartTime(final StringBuffer out, final DidlXmlCreator helper) {
		// <upnp:scheduledStartTime>2011-06-14T20:21:00</upnp:scheduledStartTime>
		out.append("<upnp:scheduledStartTime>");
		out.append(helper.formatDateWithTime(new Date()));
		out.append("</upnp:scheduledStartTime>");
				
	}

	private void addDate(final StringBuffer out, final DidlXmlCreator helper) {
		// <dc:date>2011-06-14</dc:date>
		if (getDate() != null) {
			out.append("<dc:date>");
			out.append(helper.formatDate(getDate()));
			out.append("</dc:date>");
		}
	}


	private void addAlbum(final StringBuffer out) {
		// <upnp:album>[Unbekannte Serie]</upnp:album>
		if (getAlbum() != null) {
			out.append("<upnp:album>");
			out.append(getXmlString(getAlbum()));
			out.append("</upnp:album>");
		}
	}

	private void addAlbumArtURI(final StringBuffer out, final DidlXmlCreator helper) {
		/* 
		 <upnp:albumArtURI dlna:profileID="JPEG_SM"	xmlns:dlna="urn:schemas-dlna-org:metadata-1-0/"
		 >
		 	http://192.168.101.227:10243/WMPNSSv4/2341708733/0_e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.jpg?albumArt=true
		 </upnp:albumArtURI>
		*/
		helper.writeAlbumArtURI(this, out);
	}	

	private void addArtist(final StringBuffer out) {
		// <upnp:artist role="Performer">[Unbekannter Autor]</upnp:artist>
		if (getArtist() != null) {
			out.append("<upnp:artist>");
			out.append(getXmlString(getArtist()));
			out.append("</upnp:artist>");
		}
	}

	private void addArtistMS(final StringBuffer out) {
		/*
	    <desc id="artist" nameSpace="urn:schemas-microsoft-com:WMPNSS-1-0/" xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/">
	      <microsoft:artistPerformer>[Unbekannter Autor]</microsoft:artistPerformer>
	    </desc>
	    */
		if (getArtist() != null) {
			out.append("<desc id=\"artist\" nameSpace=\"urn:schemas-microsoft-com:WMPNSS-1-0/\" xmlns:microsoft=\"urn:schemas-microsoft-com:WMPNSS-1-0/\"><microsoft:artistPerformer>");
			out.append(getXmlString(getArtist()));
			out.append("</microsoft:artistPerformer></desc>");
		}
	}	
	
	private void addActor(final StringBuffer out) {
		// <upnp:actor>[Unbekannter Autor]</upnp:actor>
		if (getArtist() != null) {
			out.append("<upnp:actor>");
			out.append(getXmlString(getArtist()));
			out.append("</upnp:actor>");
		}
	}	
	
	private void addGenre(final StringBuffer out) {
		// <upnp:genre>[Unbekanntes Genre]</upnp:genre>
		if (getGenre() != null) {
			out.append("<upnp:genre>");
			out.append(getXmlString(getGenre()));
			out.append("</upnp:genre>");
		}
	}

	private void addTrack(StringBuffer out) {
		// <upnp:originalTrackNumber>1</upnp:originalTrackNumber>
		if (getTrack() != null) {
			out.append("<upnp:originalTrackNumber>");
			out.append(getTrack());
			out.append("</upnp:originalTrackNumber>");					
		}	
	}
	
	private void addClass(final StringBuffer out) {
		// <upnp:class>object.item.videoItem</upnp:class>
		out.append("<upnp:class>");
		out.append(getClassType().value());
		out.append("</upnp:class>");		
	}

	private void addStreamRes(final StringBuffer out, final DidlXmlCreator helper) {
		/*
		 	    <res 
					size="1469855744" 
					duration="1:48:26.000" 
					bitrate="225923" 
					resolution="704x288" 
					protocolInfo="http-get:*:video/avi:DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01500000000000000000000000000000" 
					sampleFrequency="48000" 
					bitsPerSample="16" 
					nrAudioChannels="6" 
					microsoft:codec="{5634504D-0000-0010-8000-00AA00389B71}" 
					xmlns:microsoft="urn:schemas-microsoft-com:WMPNSS-1-0/"
				>
					http://192.168.101.227:10243/WMPNSSv4/2341708733/0_e0E4Mzc5QzZGLTZDREMtNDM2Ny1BQkU0LTA2NDU2OUU2NzQwNH0uMC42QkE4RTAwRQ.avi
				</res>
		 */
		if ((getProtocolInfo() != null) && (getUrl() != null)) {
			out.append("<res");
			if (getSize() != null) {
				out.append("");
			}
			if (getSize() != null) {
				out.append(" size=\"");
				out.append(getSize());
				out.append("\"");
			}			
			if (getDuration() != null) {
				out.append(" duration=\"");
				out.append(getDuration());
				out.append("\"");
			}	
			if (getBitrate() != null) {
				out.append(" bitrate=\"");
				out.append(getBitrate());
				out.append("\"");
			}				
			if (getResolution() != null) {
				out.append(" resolution=\"");
				out.append(getResolution());
				out.append("\"");
			}			
			out.append(" protocolInfo=\"");
			out.append(getXmlString(getProtocolInfo()));
			out.append("\"");
			if (getSampleFrequency() != null) {
				out.append(" sampleFrequency=\"");
				out.append(getSampleFrequency());
				out.append("\"");
			}			
			if (getBitsPerSample() != null) {
				out.append(" bitsPerSample=\"");
				out.append(getBitsPerSample());
				out.append("\"");
			}			
			if (getNrAudioChannels() != null) {
				out.append(" nrAudioChannels=\"");
				out.append(getNrAudioChannels());
				out.append("\"");
			}			
			out.append(">");
			// TODO get real server-url
			out.append(helper.getUrl(this));
			out.append("</res>");
		}
	}

	private void addThumbRes(final StringBuffer out, final DidlXmlCreator helper) {
		/*
		   <res 
		   	protocolInfo="http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=00f00000000000000000000000000000"
		   >
		   	http://192.168.101.50:9000/disk/DLNA-PNJPEG_TN-CI1-FLAGS00f00000/defaultalbumart/v_i_d_e_o.jpg/O0$3$27I1293.jpg?scale=160x160
		   </res>
		 */
		helper.writeThumbRes(this, out);
//		if (getThumbnail() != null) {
//			final String pi = helper.getProtocolInfo(getThumbnail());
//			if (pi != null) {
//				out.append("<res");
//				if (getSize() != null) {
//					out.append("");
//				}			
//				if (getThumbnail().getResolution() != null) {
//					out.append(" resolution=\"");
//					out.append(getThumbnail().getResolution());
//					out.append("\"");
//				}			
//				out.append(" protocolInfo=\"");
//				out.append(pi);
//				out.append("\"");	
//				out.append(">");
//				// TODO get real server-url
//				out.append(helper.getUrl(getThumbnail(), getId()));
//				out.append("</res>");
//			}
//		}		
	}

	private void addCreator(final StringBuffer out) {
		// <dc:creator>[Unbekannter Autor]</dc:creator>
		if (getArtist() != null) {
			out.append("<dc:creator>");
			out.append(getXmlString(getArtist()));
			out.append("</dc:creator>");
		}
	}

	private void addTitle(final StringBuffer out) {
		// <dc:title>5.Days.of.War</dc:title>
		if (getTitle() != null) {
			out.append("<dc:title>");
			out.append(getXmlString(getTitle()));
			out.append("</dc:title>");
		}
	}

	public boolean removeChild(DidlDomain item) {	
		if (getContainerContent().remove(item)) {
			item.setParent(null);
			increaseUpdateId();
			setContainerContentSize(getContainerContent().size());
			return true;
		}
		
		return false;
	}

	public boolean addChild(DidlDomain item) {
		if (getContainerContent().add(item)) {
			item.setParent(this);
			increaseUpdateId();
			setContainerContentSize(getContainerContent().size());
			return true;
		}
		
		return false;
	} 
	
	private String getXmlString(String str) {
		return StringEscapeUtils.escapeXml(new String(str.getBytes(DidlXmlCreator.utf8),(DidlXmlCreator.utf8)));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DidlDomain other = (DidlDomain) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DidlDomain ["
				+ (id != null ? "id=" + id + ", " : "")
				+ (title != null ? "title=" + title + ", " : "")
				+ (path != null ? "path=" + path + ", " : "")
				+ (videoCodec != null ? "videoCodec=" + videoCodec + ", " : "")
				+ (audioCodec != null ? "audioCodec=" + audioCodec + ", " : "")
				+ (date != null ? "date=" + date + ", " : "")
				+ (description != null ? "description=" + description + ", "
						: "")
			;
	}
	
	
}
