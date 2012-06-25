package de.sosd.mediaserver.task;

import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.sosd.mediaserver.service.FilesystemService;
import de.sosd.mediaserver.service.MPlayerFileService;
import de.sosd.mediaserver.service.dlna.UPNPNetwork;

@Service
public class ScheduledTasks implements InitializingBean, ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	private UPNPNetwork upnp;
	
	@Autowired
	private FilesystemService fs;
	
	@Autowired
	private MPlayerFileService mfs;

	@Autowired
	private ProcessWatchdogService pws;
	
	private boolean upstart = true;

	@Async
	@Scheduled(fixedDelay=10000)
	public void sendUPNPAlive() throws IOException {
		if (!this.upstart) {
			this.upnp.sendAlive();
		}
	}
	
	@Scheduled(fixedDelay = 60000)
	public void rescanFilesystemForChanges() {
		if (!this.upstart) {
			this.fs.scanFilesystem();
		}
	}
	
	@Async
	@Scheduled(fixedDelay = 60000)
	public void rescanDatabaseForMissingMetaInfos() {
		if (!this.upstart) {
			this.mfs.createMetaInfos();
		}
	}
	
	@Async
	@Scheduled(fixedDelay = 60000)
	public void rescanDatabaseForMissingThumbnails() {
		if (!this.upstart) {
			this.mfs.createThumbnails();
		}
	}

	@Async
	@Scheduled(fixedDelay = 5000)
	public void checkProcesses() {
		if (!this.upstart) {
			this.pws.checkProcesses();
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.upstart = true;
		
	}
	
	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		this.upstart = false;
		
	}
		
	
}
