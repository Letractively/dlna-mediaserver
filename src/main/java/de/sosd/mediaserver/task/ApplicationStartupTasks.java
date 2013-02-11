package de.sosd.mediaserver.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.dao.DidlDao;
import de.sosd.mediaserver.dao.SystemDao;
import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.ScanFolderDomain;
import de.sosd.mediaserver.domain.db.ScanFolderState;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.http.DLNAContentDirectoryEventServlet;
import de.sosd.mediaserver.service.MediaserverConfiguration;
import de.sosd.mediaserver.service.NetworkDeviceService;
import de.sosd.mediaserver.service.dlna.UPNPNetwork;

@Service
public class ApplicationStartupTasks implements
        ApplicationListener<ContextRefreshedEvent> {

    private final static Log         logger = LogFactory
                                                    .getLog(ApplicationStartupTasks.class);

    @Autowired
    private SystemDao                systemDao;

    @Autowired
    private DidlDao                  didlDao;

    @Autowired
    private NetworkDeviceService     networkService;

    @Autowired
    private MediaserverConfiguration cfg;

    @Autowired
    private UPNPNetwork              upnp;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        final String usn = initializeSystem();

        this.cfg.setUSN(usn);
        this.cfg.loacWebappConfiguration();
        DLNAContentDirectoryEventServlet.setUSN(this.cfg.getUSN());
        setSystemOnline(usn);
        applyDatabaseOptimizations();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private String initializeSystem() {

        final Set<String> result = this.networkService
                .findPropableSystemsForNetworkDevices();
        if (result.size() > 1) {
            logger.error("found multiple systems for my network list, this should not happen!");
        }

        if (!result.isEmpty()) {
            return new ArrayList<String>(result).get(0);
        }

        // first time init
        final String usn = this.systemDao.initSystem();
        this.cfg.setUSN(usn);
        this.cfg.loacWebappConfiguration();
        this.networkService.updateDeviceList();

        return this.cfg.getUSN();
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    private void applyDatabaseOptimizations() {
        final List<DidlDomain> list = this.didlDao
                .getAllDidlWithContentSizeNull();

        for (final DidlDomain dd : list) {
            dd.getContainerContentSize();
            this.didlDao.store(dd);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void setSystemOnline(final String usn) {
        final SystemDomain system = this.systemDao.getSystem(usn);
        system.setOnline(true);
        for (final ScanFolderDomain sfd : system.getScanFolder()) {
            if (!ScanFolderState.NOT_FOUND.equals(sfd.getScanState())) {
                sfd.setScanState(ScanFolderState.IDLE);
            }
        }
        system.setMetaInfoGenerationRunning(false);
        system.setThumbnailGenerationRunning(false);
        this.systemDao.store(system);
        this.upnp.startListening();
    }

}
