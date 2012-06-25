package de.sosd.mediaserver.domain.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "System")
@Table(name = "system")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@org.hibernate.annotations.Entity(
		dynamicUpdate = true
)
public class SystemDomain implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4685441126356442627L;

	@Id
	@Column(name = "hostname", length = 36)
	private String hostname;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_change", nullable = false)
	private Date lastDataChange = new Date();

	// some stats
	@Column(name = "folder_count", nullable = false)
	private int folderCount;
	@Column(name = "file_count", nullable = false)
	private int fileCount;
	@Column(name = "content_size", nullable = false)
	private long overallSize;

	@Column(name = "update_id", nullable = false)
	private int updateId;

	@Column(name = "name", nullable = false)
	private String name = "Mediaserver";

	@Column(name = "previews")
	private String previewCache;

	@Column(name = "usn", nullable = false)
	private String usn = UUID.randomUUID().toString();

	@Column(name = "upnp_interface", nullable = false)
	private String networkInterface = "eth0";

	@Column(name = "mplayer")
	private String mplayerPath;

	@Column(name = "mencoder")
	private String mencoderPath;

	@Column(name = "online", nullable = false)
	private boolean online;

	@Column(name = "meta_running", nullable = true)
	private Boolean metaInfoGenerationRunning = false;

	@Column(name = "thumb_running", nullable = true)
	private Boolean thumbnailGenerationRunning = false;

	@OneToMany(targetEntity = ScanFolderDomain.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "system")
	private final List<ScanFolderDomain> scanFolder = new ArrayList<ScanFolderDomain>(
			5);

	@OneToOne(targetEntity = DidlDomain.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private DidlDomain didlRoot;

	public String getHostname() {
		return this.hostname;
	}

	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	public Date getLastDataChange() {
		return this.lastDataChange;
	}

	public void setLastDataChange(final Date lastDataChange) {
		this.lastDataChange = lastDataChange;
	}

	public int getFileCount() {
		return this.fileCount;
	}

	public void setFileCount(final int fileCount) {
		this.fileCount = fileCount;
	}

	public int getFolderCount() {
		return this.folderCount;
	}

	public void setFolderCount(final int folderCount) {
		this.folderCount = folderCount;
	}

	public int getUpdateId() {
		return this.updateId;
	}

	public void setUpdateId(final int updateId) {
		this.updateId = updateId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getPreviewCache() {
		return this.previewCache;
	}

	public void setPreviewCache(final String previewCache) {
		this.previewCache = previewCache;
	}

	public void setNetworkInterface(final String interfaceName) {
		this.networkInterface = interfaceName;

	}

	public String getNetworkInterface() {
		return this.networkInterface;
	}

	public String getUsn() {
		return this.usn;
	}

	public void setUsn(final String usn) {
		this.usn = usn;
	}

	public String getMplayerPath() {
		return this.mplayerPath;
	}

	public void setMplayerPath(final String mplayerPath) {
		this.mplayerPath = mplayerPath;
	}

	public String getMencoderPath() {
		return this.mencoderPath;
	}

	public void setMencoderPath(final String mencoderPath) {
		this.mencoderPath = mencoderPath;
	}

	public List<ScanFolderDomain> getScanFolder() {
		return this.scanFolder;
	}

	public DidlDomain getDidlRoot() {
		return this.didlRoot;
	}

	public void setDidlRoot(final DidlDomain didlRoot) {
		this.didlRoot = didlRoot;
	}

	public void setOverallSize(final long size) {
		this.overallSize = size;
	}

	public long getOverAllSize() {
		return this.overallSize;
	}

	public void increaseUpdateId() {
		setUpdateId( getUpdateId() + 1 );
	}

	public boolean isOnline() {
		return this.online;
	}

	public void setOnline(final boolean online) {
		this.online = online;
	}

	public void addFile(final long size) {
		setFileCount(getFileCount() + 1);
		setOverallSize(getOverAllSize() + size);
	}

	public void removeFile(final long size) {
		setFileCount(getFileCount() - 1);
		setOverallSize(getOverAllSize() - size);
	}

	public void addFolder() {
		setFolderCount(getFolderCount() + 1);
	}

	public void removeFolder() {
		setFolderCount(getFolderCount() - 1);
	}

	public Boolean getMetaInfoGenerationRunning() {
		return this.metaInfoGenerationRunning;
	}

	public Boolean getThumbnailGenerationRunning() {
		return this.thumbnailGenerationRunning;
	}

	public void setThumbnailGenerationRunning(final Boolean thumbnailGenerationRunning) {
		this.thumbnailGenerationRunning = thumbnailGenerationRunning;
	}

	public void setMetaInfoGenerationRunning(final Boolean metaInfoGenerationRunning) {
		this.metaInfoGenerationRunning = metaInfoGenerationRunning;
	}

}
