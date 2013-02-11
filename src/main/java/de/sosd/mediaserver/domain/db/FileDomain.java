package de.sosd.mediaserver.domain.db;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "File")
@Table(name = "files")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class FileDomain implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 101035226431209778L;

    @Id
    @Column(name = "id", length = 36)
    private String            id;
    @Column(name = "name", length = 255, nullable = false)
    private String            name;
    @Column(name = "path", length = 2048, nullable = false)
    private String            path;
    @Column(name = "size", nullable = false)
    private long              size;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastModified", nullable = false)
    private Date              lastModified;
    @Column(name = "hidden", nullable = false)
    private boolean           hidden;
    @Column(name = "uuid", length = 36, nullable = true)
    private String            uuid;

    @ManyToOne(targetEntity = ScanFolderDomain.class, fetch = FetchType.LAZY)
    private ScanFolderDomain  parent;

    @OneToOne(targetEntity = DidlDomain.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "file")
    private DidlDomain        didl;

    public FileDomain() {
    }

    public FileDomain(final String id, final ScanFolderDomain parent,
            final File source) {
        setId(id);
        setParent(parent);
        if (parent != null) {
            getParent().getFiles().add(this);
        }
        setName(source.getName());
        setPath(source.getAbsolutePath());
        setLastModified(source.lastModified());
        setSize(source.length());
        setHidden(source.isHidden());

        this.parent = parent;
    }

    public FileDomain(final String id, final File source) {
        setId(id);

        setName(source.getName());
        setPath(source.getAbsolutePath());
        setLastModified(source.lastModified());
        setSize(source.length());
        setHidden(source.isHidden());
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(final String absolutePath) {
        this.path = absolutePath;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ScanFolderDomain getParent() {
        return this.parent;
    }

    public void setParent(final ScanFolderDomain parent) {
        this.parent = parent;
    }

    public void setDidl(final DidlDomain didl) {
        this.didl = didl;
    }

    public DidlDomain getDidl() {
        return this.didl;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(final long ts) {
        setLastModified(new Date(ts));
    }

    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.id == null ? 0 : this.id.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileDomain other = (FileDomain) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FileDomain ["
                + (this.id != null ? "id=" + this.id + ", " : "")
                + (this.name != null ? "name=" + this.name + ", " : "")
                + (this.path != null ? "path=" + this.path + ", " : "")
                + "size="
                + this.size
                + ", "
                + (this.lastModified != null ? "lastModified="
                        + this.lastModified + ", "
                        : "") + "hidden=" + this.hidden + ", "
                + (this.uuid != null ? "uuid=" + this.uuid + ", " : "")
                + (this.parent != null ? "parent=" + this.parent + ", " : "")
                + (this.didl != null ? "didl=" + this.didl : "") + "]";
    }

    /*********** DIDL-OPTIONS **************/

    // @XmlAttribute(name = "refID")
    // protected String refID;

}
