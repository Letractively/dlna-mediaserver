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
    private SystemDao       systemDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateDevices(final List<NetworkDeviceBean> updated,
            final String usn) {
        final SystemDomain system = this.systemDao.getSystem(usn);
        for (final NetworkDeviceBean ndb : updated) {
            updateDevice(ndb, system);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public List<NetworkDeviceBean> getAllActiveDevices(final String usn) {
        @SuppressWarnings("unchecked")
        final List<NetworkDeviceDomain> resultList = this.manager
                .createQuery(
                        "select ndd from network ndd where ndd.system.usn = ? and ndd.active = ? and ndd.present = ?"
                )
                .setParameter(1, usn).setParameter(2, true)
                .setParameter(3, true)
                .getResultList();

        final List<NetworkDeviceBean> result = new ArrayList<NetworkDeviceBean>(
                resultList.size());
        for (final NetworkDeviceDomain ndd : resultList) {
            result.add(convert(ndd));
        }
        return result;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public NetworkDeviceBean updateDevice(final NetworkDeviceBean ndb,
            final String usn) {
        final SystemDomain system = this.systemDao.getSystem(usn);
        return updateDevice(ndb, system);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public NetworkDeviceBean findDevice(final NetworkDeviceBean ndb) {
        final NetworkDeviceDomain result = this.manager.find(
                NetworkDeviceDomain.class, ndb.getMacAddress());

        if (result != null) {
            return convert(result);
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NetworkDeviceBean updateDevice(final NetworkDeviceBean ndb,
            final SystemDomain system) {
        NetworkDeviceDomain ndd = this.manager.find(NetworkDeviceDomain.class,
                ndb.getMacAddress());

        if (ndd == null) {
            ndd = new NetworkDeviceDomain(ndb, system);
        } else {
            ndd.updateFrom(ndb);
        }

        this.manager.persist(ndd);
        return convert(ndd);
    }

    private NetworkDeviceBean convert(final NetworkDeviceDomain ndd) {
        return new NetworkDeviceBean(ndd.getHostName(), ndd
                .getInterfaceName(), ndd.getDisplayName(), ndd
                .getIpAddress(), ndd.getMacAddress(), ndd.isActivated(),
                ndd.isPresent(), ndd.isLoopback());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Set<String> findPropableSystemsForNetworkDevices(
            final List<NetworkDeviceBean> systemDevices) {
        @SuppressWarnings("unchecked")
        final List<NetworkDeviceDomain> allDevices = this.manager.createQuery(
                "select ndd from network ndd").getResultList();

        final Set<String> result = new HashSet<String>();
        for (final NetworkDeviceDomain ndd : allDevices) {
            for (final NetworkDeviceBean ndb : systemDevices) {
                if (ndd.getMacAddress().equals(ndb.getMacAddress())) {
                    result.add(ndd.getSystem().getUsn());
                }
            }
        }

        return result;
    }

}
