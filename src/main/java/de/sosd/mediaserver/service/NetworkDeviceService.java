package de.sosd.mediaserver.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.sosd.mediaserver.bean.NetworkDeviceBean;
import de.sosd.mediaserver.dao.NetworkDeviceDao;

@Service
public class NetworkDeviceService {
	private final static Log logger = LogFactory
			.getLog(NetworkDeviceService.class);

	private final static Map<String, NetworkDeviceBean> deviceMap = new ConcurrentHashMap<String, NetworkDeviceBean>();

	@Autowired
	private NetworkDeviceDao dao;

	@Autowired
	private MediaserverConfiguration cfg;

	public void updateDeviceConfiguration(List<NetworkDeviceBean> updated) {
		dao.updateDevices(updated, cfg.getUSN());
		updateDevices(updated);
	}

	public List<NetworkDeviceBean> getActiveDevices() {
		List<NetworkDeviceBean> result = new ArrayList<NetworkDeviceBean>();
		for (NetworkDeviceBean ndb : deviceMap.values()) {
			if (ndb.isActivated() && ndb.isPresent()) {
				result.add(ndb);
			}
		}
		return result;
	}

	public List<NetworkDeviceBean> getAllDevices() {
		return new ArrayList<NetworkDeviceBean>(deviceMap.values());
	}

	public void updateDeviceList() {
		updateDevices(findDevices());
	}

	private void updateDevices(List<NetworkDeviceBean> deviceList) {
		final List<NetworkDeviceBean> newDevices = new ArrayList<NetworkDeviceBean>();
		Set<String> missingDevices = new HashSet<String>(deviceMap.keySet());
		for (NetworkDeviceBean ndb : deviceList) {
			if (!deviceMap.containsKey(ndb.getIpAddress())) {
				newDevices.add(ndb);
			} else {
				if (ndb.isPresent()) {
					missingDevices.remove(ndb.getIpAddress());
				}
			}
		}
		for (String missing : missingDevices) {
			final NetworkDeviceBean ndb = deviceMap.get(missing);
			ndb.setPresent(false);
			dao.updateDevice(ndb, cfg.getUSN());
		}
		for (NetworkDeviceBean ndb : newDevices) {
			NetworkDeviceBean existingDevice = dao.findDevice(ndb);
			if (existingDevice != null) {
				ndb.setActivated(existingDevice.isActivated());
			} else {
				ndb.setActivated(true);
			}
			final NetworkDeviceBean enabled = dao.updateDevice(ndb,	cfg.getUSN());
			deviceMap.put(enabled.getIpAddress(), enabled);
		}
	}

	private List<NetworkDeviceBean> findDevices() {
		final List<NetworkDeviceBean> result = new ArrayList<NetworkDeviceBean>();

		try {
			final Enumeration<NetworkInterface> ifaces = NetworkInterface
					.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				final NetworkInterface iface = ifaces.nextElement();
				if (!iface.isLoopback()) {
					final Enumeration<InetAddress> addressList = iface
							.getInetAddresses();
					while (addressList.hasMoreElements()) {
						final InetAddress address = addressList.nextElement();
						if (address instanceof Inet4Address) {
							result.add(new NetworkDeviceBean(iface, address));
						} else {
							logger.trace("ignore ipv6 (" + address
									+ ") on interface: " + iface.getName());
						}
					}
				}
			}
		} catch (SocketException se) {
			logger.error("error resolving network devices : ", se);
		}
		return result;
	}

	public Set<String> findPropableSystemsForNetworkDevices() {
		return dao.findPropableSystemsForNetworkDevices(findDevices());
	}

}
