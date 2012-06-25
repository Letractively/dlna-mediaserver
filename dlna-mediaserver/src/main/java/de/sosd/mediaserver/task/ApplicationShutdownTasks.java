package de.sosd.mediaserver.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.db.StorageService;
import de.sosd.mediaserver.service.dlna.UPNPNetwork;

@Service
public class ApplicationShutdownTasks implements ApplicationListener<ContextClosedEvent> {

	@Autowired
	private StorageService storage;
	
	@Autowired
	private UPNPNetwork upnp;
	
	@Override
	public void onApplicationEvent(final ContextClosedEvent event) {
		setSystemOffline();
	}
	

	@Transactional(propagation=Propagation.REQUIRED)
	private void setSystemOffline() {
		this.upnp.sendByeBye();
		this.upnp.stopListening();
		final SystemDomain system = this.storage.getSystemProperties();
		system.setOnline(false);
		this.storage.store(system);
	}

}
