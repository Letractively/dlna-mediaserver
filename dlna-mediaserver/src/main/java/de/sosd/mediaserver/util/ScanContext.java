package de.sosd.mediaserver.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.sosd.mediaserver.domain.db.FileDomain;

public class ScanContext {

	private final String scanFolderId;
	private final File scanFolder;
	
	private final List<File> files = new ArrayList<File>();
	private final List<FileDomain> mediaFiles = new ArrayList<FileDomain>();
	private final Set<String> deletedFileIds = new HashSet<String>();
	
	public ScanContext(final String scanFolderId, final File dir) {
		this.scanFolderId = scanFolderId;
		this.scanFolder = dir;
	}

	public String getScanFolderId() {
		return this.scanFolderId;
	}

	public File getScanFolder() {
		return this.scanFolder;
	}
	
	public List<File> getFiles() {
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
