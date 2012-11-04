package de.sosd.mediaserver.bean;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

public class NetworkDeviceBean {

	private final String hostName;

	private final String interfaceName;
	private final String displayName;

	private final String ipAddress;
	private final String macAddress;

	private boolean activated;
	private boolean present;

	private final boolean loopback;

	public NetworkDeviceBean(String hostName, String interfaceName,
			String displayName, String ipAddress, String macAddress,
			boolean activated, boolean present, boolean loopback) {
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
	
	public NetworkDeviceBean(String hostName, String interfaceName,
			String displayName, String ipAddress, byte[] macAddress,
			boolean activated, boolean present, boolean loopback) {
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

	public NetworkDeviceBean(NetworkInterface iface, InetAddress address) throws SocketException {
		this(	address.getHostName(),	 
				iface.getName(),
				iface.getDisplayName(),
				address.getHostAddress(),
				iface.getHardwareAddress(),
				false,
				iface.isUp(),
				iface.isLoopback()
				);
	}

	private final String getMac(byte[] mac) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			sb.append(String.format("%02X%s", mac[i],
					(i < mac.length - 1) ? "-" : ""));
		}
		return sb.toString();
	}

	/**
	 * @return the activated
	 */
	public boolean isActivated() {
		return activated;
	}

	/**
	 * @param activated
	 *            the activated to set
	 */
	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	/**
	 * @return the present
	 */
	public boolean isPresent() {
		return present;
	}

	/**
	 * @param present
	 *            the present to set
	 */
	public void setPresent(boolean present) {
		this.present = present;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @return the interfaceName
	 */
	public String getInterfaceName() {
		return interfaceName;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @return the macAddress
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * @return is its a loopback device
	 */
	public boolean isLoopback() {
		return loopback;
	}
	
}
