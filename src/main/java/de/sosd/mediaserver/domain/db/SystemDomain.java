package de.sosd.mediaserver.domain.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "System")
@Table(name = "system")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class SystemDomain implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4685441126356442627L;

	@OneToOne(targetEntity = DidlDomain.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="didl_root")
	private DidlDomain didlRoot;
	
	@Column(name = "file_count", nullable = false)
	private int fileCount;

	// some stats
	@Column(name = "folder_count", nullable = false)
	private int folderCount;

	@Column(name = "hostname", nullable = false)
	private String hostname = "localhost";

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_change", nullable = false)
	private Date lastDataChange = new Date();

	@Column(name = "mencoder")
	private String mencoderPath;

	@Column(name = "meta_running", nullable = true)
	private Boolean metaInfoGenerationRunning = false;

	@Column(name = "mplayer")
	private String mplayerPath;
	
	@Column(name = "name", nullable = false)
	private String name = "Mediaserver";

	@OneToMany(targetEntity = NetworkDeviceDomain.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "system")
	private final List<NetworkDeviceDomain> networkDevices = new ArrayList<NetworkDeviceDomain>(5);
	
	@Column(name = "online", nullable = false)
	private boolean online = false;
	
	@Column(name = "content_size", nullable = false)
	private long overallSize;
	@Column(name = "port", nullable = false)
	private int port = 8080;

	@Column(name = "previews")
	private String previewCache;

	@Column(name = "protocol", nullable = false)
	private String protocol = "http";

	@OneToMany(targetEntity = ScanFolderDomain.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "system")
	private final List<ScanFolderDomain> scanFolder = new ArrayList<ScanFolderDomain>(5);

	@Column(name = "thumb_running", nullable = true)
	private Boolean thumbnailGenerationRunning = false;

	@Column(name = "update_id", nullable = false)
	private int updateId;

	@Id
	@Column(name = "usn", length = 36)
	private String usn;

	@Column(name = "webapp_name", nullable = false)
	private String webappName = "/mediaserver";

	public void addFile(final long size) {
		setFileCount(getFileCount() + 1);
		setOverallSize(getOverallSize() + size);
	}

	public void addFolder(DidlDomain didl) {
		setFolderCount(getFolderCount() + 1);
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
		SystemDomain other = (SystemDomain) obj;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		return true;
	}

	public DidlDomain getDidlRoot() {
		return this.didlRoot;
	}

	public int getFileCount() {
		return this.fileCount;
	}
	
	public int getFolderCount() {
		return this.folderCount;
	}

	public String getHostname() {
		return this.hostname;
	}

	public Date getLastDataChange() {
		return this.lastDataChange;
	}

	public String getMencoderPath() {
		return this.mencoderPath;
	}

	public Boolean getMetaInfoGenerationRunning() {
		return this.metaInfoGenerationRunning;
	}

	public String getMplayerPath() {
		return this.mplayerPath;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * @return the networkDevices
	 */
	public List<NetworkDeviceDomain> getNetworkDevices() {
		return networkDevices;
	}

	public long getOverallSize() {
		return this.overallSize;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	public String getPreviewCache() {
		return this.previewCache;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	public List<ScanFolderDomain> getScanFolder() {
		return this.scanFolder;
	}

	public Boolean getThumbnailGenerationRunning() {
		return this.thumbnailGenerationRunning;
	}

	public int getUpdateId() {
		return this.updateId;
	}

	public String getUsn() {
		return this.usn;
	}

	/**
	 * @return the webappName
	 */
	public String getWebappName() {
		return webappName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hostname == null) ? 0 : hostname.hashCode());
		return result;
	}

	public void increaseUpdateId() {
		setUpdateId( getUpdateId() + 1 );
	}

	public boolean isOnline() {
		return this.online;
	}

	public void removeFile(final long size) {
		setFileCount(getFileCount() - 1);
		setOverallSize(getOverallSize() - size);
	}

	public void removeFolder(DidlDomain didl) {
		setFolderCount(getFolderCount() - 1);
	}

	public void setDidlRoot(final DidlDomain didlRoot) {
		this.didlRoot = didlRoot;
		didlRoot.setSystem(this);
		addFolder(didlRoot);
	}

	public void setFileCount(final int fileCount) {
		this.fileCount = fileCount;
	}

	public void setFolderCount(final int folderCount) {
		this.folderCount = folderCount;
	}

	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	public void setLastDataChange(final Date lastDataChange) {
		this.lastDataChange = lastDataChange;
	}

	public void setMencoderPath(final String mencoderPath) {
		this.mencoderPath = mencoderPath;
	}

	public void setMetaInfoGenerationRunning(final Boolean metaInfoGenerationRunning) {
		this.metaInfoGenerationRunning = metaInfoGenerationRunning;
	}

	public void setMplayerPath(final String mplayerPath) {
		this.mplayerPath = mplayerPath;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setOnline(final boolean online) {
		this.online = online;
	}

	public void setOverallSize(final long size) {
		this.overallSize = size;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public void setPreviewCache(final String previewCache) {
		this.previewCache = previewCache;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setThumbnailGenerationRunning(final Boolean thumbnailGenerationRunning) {
		this.thumbnailGenerationRunning = thumbnailGenerationRunning;
	}

	public void setUpdateId(final int updateId) {
		this.updateId = updateId;
	}

	public void setUsn(final String usn) {
		this.usn = usn;
	}
	
	/**
	 * @param webappName the webappName to set
	 */
	public void setWebappName(String webappName) {
		this.webappName = webappName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		return "SystemDomain ["
				+ (hostname != null ? "hostname=" + hostname + ", " : "")
				+ (lastDataChange != null ? "lastDataChange=" + lastDataChange
						+ ", " : "")
				+ "folderCount="
				+ folderCount
				+ ", fileCount="
				+ fileCount
				+ ", overallSize="
				+ overallSize
				+ ", updateId="
				+ updateId
				+ ", "
				+ (name != null ? "name=" + name + ", " : "")
				+ (previewCache != null ? "previewCache=" + previewCache + ", "
						: "")
				+ (usn != null ? "usn=" + usn + ", " : "")
				+ (mplayerPath != null ? "mplayerPath=" + mplayerPath + ", "
						: "")
				+ (mencoderPath != null ? "mencoderPath=" + mencoderPath + ", "
						: "")
				+ "online="
				+ online
				+ ", "
				+ (metaInfoGenerationRunning != null ? "metaInfoGenerationRunning="
						+ metaInfoGenerationRunning + ", "
						: "")
				+ (thumbnailGenerationRunning != null ? "thumbnailGenerationRunning="
						+ thumbnailGenerationRunning + ", "
						: "")
				+ (scanFolder != null ? "scanFolder="
						+ scanFolder.subList(0,
								Math.min(scanFolder.size(), maxLen)) + ", "
						: "")
				+ (didlRoot != null ? "didlRoot=" + didlRoot : "") + "]";
	}


	
	
}
