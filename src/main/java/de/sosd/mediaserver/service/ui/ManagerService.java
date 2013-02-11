package de.sosd.mediaserver.service.ui;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.bean.ui.FrontendFolderBean;
import de.sosd.mediaserver.bean.ui.FrontendSettingsBean;
import de.sosd.mediaserver.dao.FilesystemDao;
import de.sosd.mediaserver.dao.SystemDao;
import de.sosd.mediaserver.domain.db.ScanFolderDomain;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.FilesystemService;
import de.sosd.mediaserver.service.MediaserverConfiguration;

@Service
public class ManagerService {

    @Autowired
    private SystemDao                systemDao;

    @Autowired
    private FilesystemDao            fsDao;

    @Autowired
    private FilesystemService        fs;

    @Autowired
    private MediaserverConfiguration cfg;

    public void addScanFolder(final String folderPath) {
        this.fs.addScanDirectory(new File(folderPath));
    }

    public void removeFolderById(final String folderId) {
        this.fsDao.removeDirectory(folderId);
    }

    public List<FrontendFolderBean> loadScanFolders() {
        return this.fsDao.getAllFrontendScanFolders();
    }

    public FrontendSettingsBean loadSettings() {
        final SystemDomain sys = this.systemDao.getSystem(this.cfg.getUSN());
        return new FrontendSettingsBean(sys.getName(), "", "",
                sys.getPreviewCache(), sys.getMplayerPath(),
                sys.getMencoderPath());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateScanInterval(final String folderId, final int scanInterval) {
        final ScanFolderDomain scfd = this.fsDao.getScanfolder(folderId);
        if (scfd != null) {
            scfd.setScanInterval(scanInterval);
        }
        this.fsDao.store(scfd);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateServerSettings(final String name,
            final String networkInterface,
            final String previews, final String mplayer, final String mencoder) {
        final SystemDomain sys = this.systemDao.getSystem(this.cfg.getUSN());

        if (!name.equals(sys.getName())) {
            sys.setName(name);
            sys.increaseUpdateId();
        }

        sys.setPreviewCache(previews);
        sys.setMplayerPath(mplayer);
        sys.setMencoderPath(mencoder);

        this.systemDao.store(sys);
    }

}
