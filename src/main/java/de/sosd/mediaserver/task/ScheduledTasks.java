package de.sosd.mediaserver.task;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.sosd.mediaserver.service.FilesystemService;
import de.sosd.mediaserver.service.MPlayerFileService;
import de.sosd.mediaserver.service.MediaserverConfiguration;
import de.sosd.mediaserver.service.NetworkDeviceService;
import de.sosd.mediaserver.service.dlna.UPNPNetwork;

@Service
public class ScheduledTasks {
	
	@Autowired
	private UPNPNetwork upnp;
	
	@Autowired
	private FilesystemService fs;
	
	@Autowired
	private MPlayerFileService mfs;

	@Autowired
	private ProcessWatchdogService pws;
	
	@Autowired
	private NetworkDeviceService nds;
	
	@Autowired
	private MediaserverConfiguration cfg;
	
	@Async
	@Scheduled(fixedDelay=10000)
	public void sendUPNPAlive() throws IOException {
		if (cfg.isSystemAvailable()) {
			this.upnp.sendAlive();
		}
	}
	
	@Scheduled(fixedDelay = 60000)
	public void rescanFilesystemForChanges() {
		if (cfg.isSystemAvailable()) {
			this.fs.scanFilesystem();
		}
	}
	
	@Async
	@Scheduled(fixedDelay = 60000)
	public void rescanDatabaseForMissingMetaInfos() {
		if (cfg.isSystemAvailable()) {
			this.mfs.createMetaInfos();
		}
	}
	
	@Async
	@Scheduled(fixedDelay = 60000)
	public void rescanDatabaseForMissingThumbnails() {
		if (cfg.isSystemAvailable()) {
			this.mfs.createThumbnails();
		}
	}

	@Async
	@Scheduled(fixedDelay = 5000)
	public void checkProcesses() {
		if (cfg.isSystemAvailable()) {
			this.pws.checkProcesses();
		}
	}
	
	@Async
	@Scheduled(fixedDelay=1000) 
	public void checkNetworkDevices() {
		if (cfg.isSystemAvailable()) {
			nds.updateDeviceList();
		}
	}
	
}
