package de.sosd.mediaserver.bean;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

public class NetworkDeviceBean {

    private final String  hostName;

    private final String  interfaceName;
    private final String  displayName;

    private final String  ipAddress;
    private final String  macAddress;

    private boolean       activated;
    private boolean       present;

    private final boolean loopback;

    public NetworkDeviceBean(final String hostName, final String interfaceName,
            final String displayName, final String ipAddress,
            final String macAddress,
            final boolean activated, final boolean present,
            final boolean loopback) {
        super();
        this.hostName = hostName;
        this.interfaceName = interfaceName;
        this.displayName = displayName;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.activated = activated;
        this.present = present;
        this.loopback = loopback;
    }

    public NetworkDeviceBean(final String hostName, final String interfaceName,
            final String displayName, final String ipAddress,
            final byte[] macAddress,
            final boolean activated, final boolean present,
            final boolean loopback) {
        super();
        this.hostName = hostName;
        this.interfaceName = interfaceName;
        this.displayName = displayName;
        this.ipAddress = ipAddress;
        this.macAddress = getMac(macAddress);
        this.activated = activated;
        this.present = present;
        this.loopback = loopback;
    }

    public NetworkDeviceBean(final NetworkInterface iface,
            final InetAddress address) throws SocketException {
        this(address.getHostName(),
                iface.getName(),
                iface.getDisplayName(),
                address.getHostAddress(),
                iface.getHardwareAddress(),
                false,
                iface.isUp(),
                iface.isLoopback());
    }

    private final String getMac(final byte[] mac) {

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i],
                    i < mac.length - 1 ? "-" : ""));
        }
        return sb.toString();
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
     * @return the hostName
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * @return the interfaceName
     */
    public String getInterfaceName() {
        return this.interfaceName;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /**
     * @return the macAddress
     */
    public String getMacAddress() {
        return this.macAddress;
    }

    /**
     * @return is its a loopback device
     */
    public boolean isLoopback() {
        return this.loopback;
    }

}
