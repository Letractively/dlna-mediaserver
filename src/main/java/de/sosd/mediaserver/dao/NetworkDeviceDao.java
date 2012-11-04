package de.sosd.mediaserver.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.bean.NetworkDeviceBean;
import de.sosd.mediaserver.domain.db.NetworkDeviceDomain;
import de.sosd.mediaserver.domain.db.SystemDomain;

@Service
public class NetworkDeviceDao {

	@PersistenceContext(name = "mediaserver")
	protected EntityManager manager;

	@Autowired
	private SystemDao systemDao;
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateDevices(List<NetworkDeviceBean> updated, String usn) {
		SystemDomain system = systemDao.getSystem(usn);
		for (NetworkDeviceBean ndb : updated) {
			updateDevice(ndb, system);
		}
	}
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<NetworkDeviceBean> getAllActiveDevices(String usn) {
		@SuppressWarnings("unchecked")
		List<NetworkDeviceDomain> resultList = manager.createQuery(
						"select ndd from network ndd where ndd.system.usn = ? and ndd.active = ? and ndd.present = ?"
				)
				.setParameter(1, usn).setParameter(2, true)
				.setParameter(3, true)
				.getResultList();

		List<NetworkDeviceBean> result = new ArrayList<NetworkDeviceBean>(
				resultList.size());
		for (NetworkDeviceDomain ndd : resultList) {
			result.add(convert(ndd));
		}
		return result;
	}


	@Transactional(propagation = Propagation.SUPPORTS)
	public NetworkDeviceBean updateDevice(NetworkDeviceBean ndb, String usn) {
		SystemDomain system = systemDao.getSystem(usn);
		return updateDevice(ndb, system);
	}
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public NetworkDeviceBean findDevice(NetworkDeviceBean ndb) {
		NetworkDeviceDomain result = manager.find(NetworkDeviceDomain.class, ndb.getMacAddress());
		
		if (result != null) {
			return convert(result);
		}
		return null;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public NetworkDeviceBean updateDevice(NetworkDeviceBean ndb, SystemDomain system) {
		NetworkDeviceDomain ndd = manager.find(NetworkDeviceDomain.class, ndb.getMacAddress());
		
		if (ndd == null) {
			ndd = new NetworkDeviceDomain(ndb, system);
		} else {
			ndd.updateFrom(ndb);
		}
		
		manager.persist(ndd);
		return convert(ndd);
	}
	
	private NetworkDeviceBean convert(NetworkDeviceDomain ndd) {
		return new NetworkDeviceBean(ndd.getHostName(), ndd
				.getInterfaceName(), ndd.getDisplayName(), ndd
				.getIpAddress(), ndd.getMacAddress(), ndd.isActivated(),
				ndd.isPresent(), ndd.isLoopback());
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	public Set<String> findPropableSystemsForNetworkDevices(
			List<NetworkDeviceBean> systemDevices) {
		@SuppressWarnings("unchecked")
		final List<NetworkDeviceDomain> allDevices = manager.createQuery("select ndd from network ndd").getResultList();
		
		final Set<String> result = new HashSet<String>();
		for (NetworkDeviceDomain ndd : allDevices) {
			for (NetworkDeviceBean ndb : systemDevices) {
				if (ndd.getMacAddress().equals(ndb.getMacAddress())) {
					result.add(ndd.getSystem().getUsn());
				}
			}
		}
		
		return result;
	}

}
