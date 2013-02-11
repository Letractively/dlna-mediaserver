package de.sosd.mediaserver.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.dao.SystemDao;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.MediaserverConfiguration;
import de.sosd.mediaserver.service.dlna.UPNPNetwork;

@Service
public class ApplicationShutdownTasks implements
        ApplicationListener<ContextClosedEvent> {

    @Autowired
    private SystemDao                systemDao;

    @Autowired
    private UPNPNetwork              upnp;

    @Autowired
    private MediaserverConfiguration cfg;

    @Override
    public void onApplicationEvent(final ContextClosedEvent event) {
        setSystemOffline();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void setSystemOffline() {
        this.upnp.sendByeBye();
        this.upnp.stopListening();
        final SystemDomain system = this.systemDao.getSystem(this.cfg.getUSN());
        system.setOnline(false);
        this.systemDao.store(system);
    }

}
