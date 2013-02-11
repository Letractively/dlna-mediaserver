package de.sosd.mediaserver.util;

import java.io.File;

public class ScanFile {

    private final ScanFolder parent;

    private final String     id;

    private final File       f;

    public ScanFile(final ScanFolder dir, final String id, final File f) {
        super();
        this.parent = dir;
        this.id = id;
        this.f = f;
    }

    public ScanFolder getParent() {
        return this.parent;
    }

    /**
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return the f
     */
    public File getFile() {
        return this.f;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "" + (this.f != null ? this.f.getAbsolutePath() : "") + " ["
                + this.id + "]";
    }

}
