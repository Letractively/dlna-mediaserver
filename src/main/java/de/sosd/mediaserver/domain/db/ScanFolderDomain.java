package de.sosd.mediaserver.domain.db;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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

@Entity(name = "Folder")
@Table(name = "folders")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@org.hibernate.annotations.Entity(
		dynamicUpdate = true
)
public class ScanFolderDomain implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1377006659396461007L;
	/**
	 * 
	 */
	
	@Id
	@Column(name = "id", length = 36)
	private String id;
	@Column(name = "name", length = 255, nullable = false)
	private String name;
	@Column(name = "path", length = 2048, nullable = false)
	private String path;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_scan", nullable = true)
	private Date lastScan;
	@Column(name = "scan_interval", nullable = false)
	private int scanInterval = 10;
	@Enumerated(EnumType.STRING)
	@Column(name = "scan_state", nullable = false)
	private ScanFolderState scanState = ScanFolderState.IDLE;
	@OneToOne(targetEntity = DidlDomain.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional=true)
	private DidlDomain didl;
	@OneToMany(targetEntity = FileDomain.class, fetch = FetchType.LAZY, mappedBy = "parent", cascade = CascadeType.ALL)
	private List<FileDomain> files = new ArrayList<FileDomain>(5);
	@ManyToOne(targetEntity=SystemDomain.class, fetch = FetchType.LAZY, optional = false)
	private SystemDomain system;

	// some stats
	@Column(name = "folder_count", nullable = false)
	private int folderCount;
	@Column(name = "file_count", nullable = false)
	private int fileCount;
	@Column(name = "content_size", nullable = false)
	private long overallSize;
	
	public ScanFolderDomain() {}
	
	public ScanFolderDomain(final String id, final File directory, final SystemDomain system) {
		super();
		this.id = id;
		this.name = directory.getName();
		if (name.length() == 0) {
			this.name = directory.getAbsolutePath();
		}
		this.path = directory.getAbsolutePath();
		this.system = system;
	}

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public Date getLastScan() {
		return this.lastScan;
	}

	public void setLastScan(final Date lastScan) {
		this.lastScan = lastScan;
	}

	public int getScanInterval() {
		return this.scanInterval;
	}

	public void setScanInterval(final int scanInterval) {
		this.scanInterval = scanInterval;
	}

	public DidlDomain getDidl() {
		return this.didl;
	}

	public void setDidl(final DidlDomain didl) {
		this.didl = didl;
		didl.setFolder(this);
		addFolder(didl);
	}

	public List<FileDomain> getFiles() {
		return this.files;
	}

	public void setFiles(final List<FileDomain> files) {
		this.files = files;
	}

	public SystemDomain getSystem() {
		return this.system;
	}

	public void setSystem(final SystemDomain system) {
		this.system = system;
	}

	public int getFolderCount() {
		return this.folderCount;
	}

	public void setFolderCount(final int folderCount) {
		this.folderCount = folderCount;
	}

	public int getFileCount() {
		return this.fileCount;
	}

	public void setFileCount(final int fileCount) {
		this.fileCount = fileCount;
	}

	public long getOverallSize() {
		return this.overallSize;
	}

	public void setOverallSize(final long overallSize) {
		this.overallSize = overallSize;
	}
	
	public void setScanState(final ScanFolderState scanState) {
		this.scanState = scanState;
	}
	
	public ScanFolderState getScanState() {
		return this.scanState;
	}

	public boolean addFile(final FileDomain fd) {
		if (getFiles().add(fd)) {	
			fd.setParent(this);
			setFileCount(getFileCount() +1);
			setOverallSize(getOverallSize() + fd.getSize());
			
			getSystem().addFile(fd.getSize());
			return true;
		}
		return false;
	}

	public boolean removeFile(final FileDomain fd) {
		if (getFiles().remove(fd)) {	
			fd.setParent(null);
			setFileCount(getFileCount() - 1);
			setOverallSize(getOverallSize() - fd.getSize());	
			getSystem().removeFile(fd.getSize());
			return true;
		}
		return false;	
	}

	public int addFolder(final DidlDomain container) {
		int result = 1;
		setFolderCount(getFolderCount() + 1);
		getSystem().addFolder(container);
		
		for (final DidlDomain item : container.getContainerContent()) {
			if (item.isContainer()) {
				result += addFolder(item);
			}
		}
		
		return result;
	}

	public void removeFolder(final DidlDomain container) {
		setFolderCount(getFolderCount() - 1);
		getSystem().removeFolder(container);
		
	}
	
}
