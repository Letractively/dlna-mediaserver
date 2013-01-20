package de.sosd.mediaserver.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.sosd.mediaserver.domain.db.FileDomain;

public class ScanContext {

	private final String scanFolderId;
	private final ScanFolder scanFolder;
	
	private final List<ScanFile> files = new ArrayList<ScanFile>();
	private final List<FileDomain> mediaFiles = new ArrayList<FileDomain>();
	private final Set<String> deletedFileIds = new HashSet<String>();
	
	public ScanContext(final String scanFolderId, final File dir) {
		this.scanFolderId = scanFolderId;
		this.scanFolder = new ScanFolder(scanFolderId, dir);
	}

	public String getScanFolderId() {
		return this.scanFolderId;
	}

	public ScanFolder getScanFolder() {
		return this.scanFolder;
	}
	
	public List<ScanFile> getFiles() {
		return this.files;
	}

	public List<FileDomain> getMediaFiles() {
		return this.mediaFiles;
	}

	public void addDeletedMediaFile(final String fd) {
		this.deletedFileIds.add(fd);	
	}
	
	public Set<String> getDeletedMediaFiles() {
		return this.deletedFileIds;
	}
	
}
