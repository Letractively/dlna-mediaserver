package de.sosd.mediaserver.bean;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import de.sosd.mediaserver.domain.db.ScanFolderState;

public class FrontendFolderBean {

	private String id;
	private String path;
	private Date lastScan;
	private int scanInterval;
	private String scanState;
	private int folderCount;
	private int fileCount;
	private long overallSize;

	public FrontendFolderBean() {
	}

	public FrontendFolderBean(final String folderPath) {
		final Random r = new Random();
		this.id = UUID.randomUUID().toString();
		this.path = folderPath;
		this.folderCount = r.nextInt(25) + 1;
		this.fileCount = r.nextInt(this.folderCount * 25) + 1;
		this.overallSize = 0l + (this.fileCount * (r.nextInt(200) + 1) * 1024 * 1024);
	}

	public FrontendFolderBean(final String id, final String path, final int folderCount,
			final int fileCount, final long overallSize) {
		super();
		this.id = id;
		this.path = path;
		this.folderCount = folderCount;
		this.fileCount = fileCount;
		this.overallSize = overallSize;
	}

	public FrontendFolderBean(final String id, final String path, final Date lastScan,
			final int scanInterval, final ScanFolderState scanState, final int folderCount,
			final int fileCount, final long overallSize) {
		super();
		setId( id );
		setPath(path);
		setLastScan( lastScan );
		setScanInterval(scanInterval);
		setFileCount(fileCount);
		setFolderCount(folderCount);
		setOverallSize(overallSize);
		setScanState(scanState);
	}	
	
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(final String path) {
		this.path = path;
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

	public String getSize() {
		Float f = new Float(this.overallSize);
		final String[] type = { "B", "KB", "MB", "GB", "TB", "PB" };
		int idx = 0;
		while ((f > 1024) && (idx < 5)) {
			++idx;
			f /= 1024;
		}
		;

		return new DecimalFormat("#.##").format(f) + " " + type[idx];
	}

	/**
	 * @return the lastScan
	 */
	public Date getLastScan() {
		return this.lastScan;
	}

	/**
	 * @param lastScan
	 *            the lastScan to set
	 */
	public void setLastScan(final Date lastScan) {
		this.lastScan = lastScan;
//		if (lastScan != null) {
//			final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss' ('dd.MM.yyyy')'");
//			this.lastScan = sdf.format(lastScan);
//		} else {
//			this.lastScan = "";
//		}
	}

	/**
	 * @return the scanInterval
	 */
	public int getScanInterval() {
		return this.scanInterval;
	}

	/**
	 * @param scanInterval
	 *            the scanInterval to set
	 */
	public void setScanInterval(final int scanInterval) {
		this.scanInterval = scanInterval;
	}

	/**
	 * @return the scanState
	 */
	public String getScanState() {
		return this.scanState;
	}

	/**
	 * @param scanState
	 *            the scanState to set
	 */
	public void setScanState(final String scanState) {
		this.scanState = scanState;
	}

	/**
	 * @param scanState
	 *            the scanState to set
	 */
	public void setScanState(final ScanFolderState scanState) {
		if (getLastScan() == null) {
			this.scanState = "folder.initial_scanning";
		} else {
			switch (scanState) {
				case IDLE: {
					this.scanState = "folder.up_to_date";
					break;
				}
				case SCANNING: {
					this.scanState = "folder.scanning";
					break;
				}
				case NOT_FOUND: {
					this.scanState = "folder.not_found";
					break;
				}
			}
		}
	}
}
