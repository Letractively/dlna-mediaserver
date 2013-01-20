package de.sosd.mediaserver.util;

import java.io.File;
import java.util.List;
import java.util.Vector;

public class ScanFolder extends ScanFile {

	private List<ScanFile> files;
	private List<ScanFolder> folders;
	
	public ScanFolder(String id, File f) {
		this(null, id, f);
	}
	
	public ScanFolder(ScanFolder parent, String id, File f) {
		super(parent, id, f);
		this.files = new Vector<ScanFile>();
		this.folders = new Vector<ScanFolder>();		
	}
	
	public ScanFolder addFolder(String id, File f) {
		ScanFolder result = new ScanFolder(this, id, f);
		if (getFolders().add(result)) {
			return result;
		}
		return null;
	}

	public ScanFile addFile(String id, File f) {
		ScanFile result = new ScanFile(this, id, f);
		if (getFiles().add(result)) {
			return result;
		}
		return null;
	}	
	
	public List<ScanFile> getFiles() {
		return files;
	}
	
	public List<ScanFolder> getFolders() {
		return folders;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString();
	}
	
	
	
}
