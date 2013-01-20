package de.sosd.mediaserver.util;

import java.io.File;

public class ScanFile {

	private ScanFolder parent;
	
	private String id;
	
	private File f;

	public ScanFile(ScanFolder dir, String id, File f) {
		super();
		this.parent = dir;
		this.id = id;
		this.f = f;
	}

	public ScanFolder getParent() {
		return parent;
	}	
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the f
	 */
	public File getFile() {
		return f;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "" + (f != null ? f.getAbsolutePath() : "") + " [" + id + "]";
	}
	
	
	
}
