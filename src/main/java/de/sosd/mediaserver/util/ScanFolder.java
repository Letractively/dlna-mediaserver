package de.sosd.mediaserver.util;

import java.io.File;
import java.util.List;
import java.util.Vector;

public class ScanFolder extends ScanFile {

    private final List<ScanFile>   files;
    private final List<ScanFolder> folders;

    public ScanFolder(final String id, final File f) {
        this(null, id, f);
    }

    public ScanFolder(final ScanFolder parent, final String id, final File f) {
        super(parent, id, f);
        this.files = new Vector<ScanFile>();
        this.folders = new Vector<ScanFolder>();
    }

    public ScanFolder addFolder(final String id, final File f) {
        final ScanFolder result = new ScanFolder(this, id, f);
        if (getFolders().add(result)) {
            return result;
        }
        return null;
    }

    public ScanFile addFile(final String id, final File f) {
        final ScanFile result = new ScanFile(this, id, f);
        if (getFiles().add(result)) {
            return result;
        }
        return null;
    }

    public List<ScanFile> getFiles() {
        return this.files;
    }

    public List<ScanFolder> getFolders() {
        return this.folders;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString();
    }

}
