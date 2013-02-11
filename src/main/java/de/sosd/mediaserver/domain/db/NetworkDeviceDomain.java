package de.sosd.mediaserver.domain.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.sosd.mediaserver.bean.NetworkDeviceBean;

@Entity(name = "network")
@Table(name = "network")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class NetworkDeviceDomain implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -3083801779204348634L;

    @ManyToOne(targetEntity = SystemDomain.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "system")
    private SystemDomain      system;

    @Column(name = "name", length = 36, nullable = false)
    private String            interfaceName;
    @Column(name = "display_name", length = 255, nullable = false)
    private String            displayName;
    @Column(name = "host_name", length = 255, nullable = false)
    private String            hostName;
    @Column(name = "ip", length = 15, nullable = false)
    private String            ipAddress;
    @Id
    @Column(name = "mac_address", length = 17)
    private String            macAddress;
    @Column(name = "activated", nullable = false)
    private boolean           activated;
    @Column(name = "present", nullable = false)
    private boolean           present;
    @Column(name = "loopback", nullable = false)
    private boolean           loopback;

    public NetworkDeviceDomain() {
    }

    public NetworkDeviceDomain(final NetworkDeviceBean ndb,
            final SystemDomain system) {
        this.system = system;
        updateFrom(ndb);
    }

    public void updateFrom(final NetworkDeviceBean ndb) {
        setInterfaceName(ndb.getInterfaceName());
        setHostName(ndb.getHostName());
        setDisplayName(ndb.getDisplayName());
        setIpAddress(ndb.getIpAddress());
        setMacAddress(ndb.getMacAddress());
        setActivated(ndb.isActivated());
        setPresent(ndb.isPresent());
        setLoopback(ndb.isLoopback());
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * @param hostName
     *            the hostName to set
     */
    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return the interfaceName
     */
    public String getInterfaceName() {
        return this.interfaceName;
    }

    /**
     * @param interfaceName
     *            the interfaceName to set
     */
    public void setInterfaceName(final String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /**
     * @param ipAddress
     *            the ipAddress to set
     */
    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return the macAddress
     */
    public String getMacAddress() {
        return this.macAddress;
    }

    /**
     * @param macAddress
     *            the macAddress to set
     */
    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * @return the activated
     */
    public boolean isActivated() {
        return this.activated;
    }

    /**
     * @param activated
     *            the activated to set
     */
    public void setActivated(final boolean activated) {
        this.activated = activated;
    }

    /**
     * @return the present
     */
    public boolean isPresent() {
        return this.present;
    }

    /**
     * @param present
     *            the present to set
     */
    public void setPresent(final boolean present) {
        this.present = present;
    }

    /**
     * @return the system
     */
    public SystemDomain getSystem() {
        return this.system;
    }

    /**
     * @param system
     *            the system to set
     */
    public void setSystem(final SystemDomain system) {
        this.system = system;
    }

    public void setLoopback(final boolean loopback) {
        this.loopback = loopback;
    }

    public boolean isLoopback() {
        return this.loopback;
    }

}
