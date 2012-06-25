package de.sosd.mediaserver.service;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.bean.FrontendFolderBean;
import de.sosd.mediaserver.bean.FrontendSettingsBean;
import de.sosd.mediaserver.domain.db.ScanFolderDomain;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.db.StorageService;

@Service
public class WebInterfaceBackendService {

	@Autowired
	private StorageService storage;
	
	@Autowired
	private FilesystemService fs;
	
	@Autowired
	private MediaserverConfiguration cfg;

	public void addScanFolder(final String folderPath) {
		this.fs.addScanDirectory(new File(folderPath));
	}

	public void removeFolderById(final String folderId) {
		this.storage.removeDirectory(folderId);
	}

	public List<FrontendFolderBean> loadScanFolders() {
		return this.storage.getAllFrontendScanFolders();
	}

	public FrontendSettingsBean loadSettings() {
		final SystemDomain sys = this.storage.getSystemProperties();
		return new FrontendSettingsBean(sys.getName(), sys.getNetworkInterface(), this.cfg.getHttpServerUrl(), sys.getPreviewCache(), sys.getMplayerPath(), sys.getMencoderPath());
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateScanInterval(final String folderId, final int scanInterval) {
		final ScanFolderDomain scfd = this.storage.getScanfolder(folderId);
		if (scfd != null) {
			scfd.setScanInterval(scanInterval);
		}
		this.storage.store(scfd);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void updateServerSettings(final String name, final String networkInterface,
			final String previews, final String mplayer, final String mencoder) {
		final SystemDomain sys = this.storage.getSystemProperties();
		
		if (!name.equals(sys.getName())) {
			sys.setName(name);
			sys.increaseUpdateId();
		}
		sys.setNetworkInterface(networkInterface);
		sys.setPreviewCache(previews);
		sys.setMplayerPath(mplayer);
		sys.setMencoderPath(mencoder);
		
		this.storage.store(sys);
	}

}
