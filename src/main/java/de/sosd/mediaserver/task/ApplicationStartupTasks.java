package de.sosd.mediaserver.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.domain.db.ScanFolderDomain;
import de.sosd.mediaserver.domain.db.ScanFolderState;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.http.DLNAContentDirectoryEventServlet;
import de.sosd.mediaserver.service.MediaserverConfiguration;
import de.sosd.mediaserver.service.db.StorageService;
import de.sosd.mediaserver.service.dlna.UPNPNetwork;

@Service
public class ApplicationStartupTasks implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private StorageService storage;
	
	@Autowired
	private MediaserverConfiguration cfg;
	
	@Autowired
	private UPNPNetwork upnp;
	
	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		DLNAContentDirectoryEventServlet.setUSN(this.cfg.getUSN());
		setSystemOnline();
		
	}

	@Transactional(propagation=Propagation.REQUIRED)
	private void setSystemOnline() {
		final SystemDomain system = this.storage.getSystemProperties();
		system.setOnline(true);
		for (final ScanFolderDomain sfd : system.getScanFolder()) {
			if (! ScanFolderState.NOT_FOUND.equals(sfd.getScanState())) {
				sfd.setScanState(ScanFolderState.IDLE);
			}
		}
		system.setMetaInfoGenerationRunning(false);
		system.setThumbnailGenerationRunning(false);
		this.storage.store(system);
		this.upnp.startListening();	
	}

}
