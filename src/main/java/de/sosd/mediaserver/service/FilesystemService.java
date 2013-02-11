package de.sosd.mediaserver.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.dao.DidlDao;
import de.sosd.mediaserver.dao.FilesystemDao;
import de.sosd.mediaserver.dao.SystemDao;
import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.FileDomain;
import de.sosd.mediaserver.domain.db.ScanFolderDomain;
import de.sosd.mediaserver.domain.db.ScanFolderState;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.dlna.DIDLService;
import de.sosd.mediaserver.util.DidlChangeMap;
import de.sosd.mediaserver.util.ScanContext;
import de.sosd.mediaserver.util.ScanFile;
import de.sosd.mediaserver.util.ScanFolder;

@Service
public class FilesystemService {

    private final static Log         logger = LogFactory
                                                    .getLog(FilesystemService.class);

    @Autowired
    private FilesystemDao            fsDao;

    @Autowired
    private SystemDao                systemDao;

    @Autowired
    private DidlDao                  didlDao;

    @Autowired
    private IdService                idservice;

    @Autowired
    private DIDLService              didl;

    @Autowired
    private MediaserverConfiguration cfg;

    public boolean addScanDirectory(final File directory) {
        try {
            addScanDirectoryTransactional(directory);
            return true;
        } catch (final Throwable t) {
            logger.error(
                    "error while adding directory, probably because it already exists",
                    t);
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void addScanDirectoryTransactional(final File directory) {
        if (directory.isDirectory()) {
            final String id = this.idservice.getId(directory);
            if (!this.fsDao.isDirectoryPresent(id)) {
                final ScanFolderDomain scanFolder = new ScanFolderDomain(id,
                        directory, null);

                final SystemDomain system = this.systemDao.getSystem(this.cfg
                        .getUSN());
                system.getScanFolder().add(scanFolder);
                scanFolder.setSystem(system);
                scanFolder.setDidlRoot(this.didl.createDidlContainer(
                        scanFolder, system.getDidlRoot()));
                this.systemDao.store(system);
                logger.info("added new scan-directory : "
                        + scanFolder.getPath());
            }
        }
    }

    public void scanFilesystem() {
        // get list of directories due for scanning
        logger.info("scanner [start]");
        final List<ScanContext> scanContexts = createScanContexts();
        scanDirectories(scanContexts);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private List<ScanContext> createScanContexts() {
        final List<ScanContext> scanContexts = new ArrayList<ScanContext>();
        final long currentTimeMillis = System.currentTimeMillis();
        final SystemDomain system = this.systemDao.getSystem(this.cfg.getUSN());
        for (final ScanFolderDomain sfd : system.getScanFolder()) {
            if (!ScanFolderState.SCANNING.equals(sfd.getScanState())
                    &&
                    (sfd.getLastScan() == null ||
                    sfd.getLastScan().getTime() + sfd.getScanInterval() * 60000 < currentTimeMillis)) {

                // should scan

                final File dir = new File(sfd.getPath());
                if (!dir.exists() || !dir.isDirectory()) {
                    // this could happen with external storage ..
                    sfd.setScanState(ScanFolderState.NOT_FOUND);
                    this.didlDao.setOnline(sfd.getId(), false);
                    system.increaseUpdateId();
                } else {
                    if (ScanFolderState.NOT_FOUND.equals(sfd.getScanState())) {
                        this.didlDao.setOnline(sfd.getId(), true);
                        system.increaseUpdateId();
                    }

                    sfd.setScanState(ScanFolderState.SCANNING);
                    scanContexts.add(new ScanContext(sfd.getId(), dir));
                }
            }
        }
        this.systemDao.store(system);

        return scanContexts;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void scanDirectories(final List<ScanContext> scanContexts) {
        long changedCount = 0l;
        boolean systemChanged = false;

        final SystemDomain system = this.systemDao.getSystem(this.cfg.getUSN());
        final Set<Object> itemsToPurge = new HashSet<Object>();
        final Map<String, ScanContext> idScanContextMap = new HashMap<String, ScanContext>();
        final Set<String> foundFileIds = new HashSet<String>();
        final DidlChangeMap touchedDidlMap = new DidlChangeMap();

        for (final ScanContext sc : scanContexts) {
            idScanContextMap.put(sc.getScanFolderId(), sc);
        }

        // collect all Files, no Folders!
        for (final ScanContext sc : scanContexts) {
            logger.info("scanner [scan] " + sc.getScanFolder());
            final List<String> knownFileIds = this.fsDao.getAllFileIds(sc
                    .getScanFolderId());
            collectNewFiles(sc.getScanFolder(), sc.getFiles(), knownFileIds,
                    foundFileIds);
            logger.info("scanner [found files] " + sc.getFiles().size() + " "
                    + sc.getScanFolder());

            changedCount += sc.getFiles().size();

            knownFileIds.removeAll(foundFileIds);
            for (final String removed : knownFileIds) {
                sc.addDeletedMediaFile(removed);
                changedCount += 1;
            }
        }

        if (changedCount > 0) {
            final List<String> allDidlIds = this.didlDao.getAllDidlIds();
            for (final String id : allDidlIds) {
                touchedDidlMap.addDidl(id, null);
            }
            for (final ScanFolderDomain sfd : system.getScanFolder()) {
                if (idScanContextMap.containsKey(sfd.getId())) {
                    final ScanContext sc = idScanContextMap.get(sfd.getId());
                    logger.info("scanner [filter] " + sc.getScanFolder());
                    for (final ScanFile f : sc.getFiles()) {
                        final FileDomain fd = new FileDomain(f.getId(), null,
                                f.getFile());
                        if (this.didl.createDidl(fd, f, touchedDidlMap, sfd)) {
                            sc.getMediaFiles().add(fd);
                        }
                    }
                    logger.info("scanner [found new files] "
                            + sc.getMediaFiles().size() + " "
                            + sc.getScanFolder());
                }
            }
        }

        // update folders
        for (final ScanFolderDomain sfd : system.getScanFolder()) {
            if (idScanContextMap.containsKey(sfd.getId())) {
                final ScanContext sc = idScanContextMap.get(sfd.getId());
                boolean changedFiles = false;
                for (final FileDomain fd : sc.getMediaFiles()) {
                    changedFiles |= sfd.addFile(fd);
                    logger.info("scanner [add file] " + fd.getName() + "\t\t("
                            + fd.getPath() + ")");
                }
                if (changedFiles || !sc.getDeletedMediaFiles().isEmpty()) {
                    changedFiles |= updateDidl(sfd.getDidlRoot(), sfd,
                            touchedDidlMap, sc.getDeletedMediaFiles(),
                            itemsToPurge);
                }
                // remove scanfolder mark
                sfd.setScanState(ScanFolderState.IDLE);
                sfd.setLastScan(new Date());

                systemChanged |= changedFiles;
            }
        }

        // something changed increase updateId
        if (systemChanged) {
            logger.info("scanner [collect new stats]");
            system.increaseUpdateId();
            system.setLastDataChange(new Date());

            if (this.didl.foundUnsupportedFiles()) {
                logger.info("scanner [dlna-unsupported] "
                        + this.didl.getMissingClassTypeExtensions() + ", "
                        + this.didl.getMissingProtocolInfoExtensions());
            }
        }
        logger.info("scanner [update database]");
        this.systemDao.update(system, new ArrayList<Object>(itemsToPurge));
        logger.info("scanner [done]");

    }

    @Transactional(propagation = Propagation.REQUIRED)
    private boolean updateDidl(final DidlDomain root,
            final ScanFolderDomain sfd, final DidlChangeMap map,
            final Set<String> removedItemIds, final Set<Object> itemsToPurge) {
        boolean changed = false;
        // remove files first
        for (final String removedId : removedItemIds) {
            if (map.hasDidl(removedId)) {
                final DidlDomain item = map.getDidl(removedId);
                changed |= removeDidl(root, map, item, itemsToPurge);
            }
        }
        return changed;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private boolean removeDidl(final DidlDomain root, final DidlChangeMap map,
            final DidlDomain item, final Set<Object> itemsToPurge) {
        if (root.getId().equals(item.getId())) {
            return false;
        }

        boolean changed = false;
        DidlDomain parent;
        if (item.getParent() != null) {
            parent = map.getDidl(item.getParent().getId());
            // remove child
            final FileDomain file = item.getFile();
            changed = parent.removeChild(item);
            this.didlDao.store(parent);
            // item.setFile(null);
            // item.setParent(null);
            // item.setReference(null);
            // for (DidlDomain ref : item.getReferences()) {
            // removeDidl(root, map, ref, itemsToPurge);
            // }
            // for (DidlDomain content : item.getContainerContent()) {
            // removeDidl(root, map, content, itemsToPurge);
            // }
            // item.getContainerContent().clear();

            if (file != null) {
                changed |= file.getParent().removeFile(file);
                // file.setParent(null);
                // file.setDidl(null);
                // storage.removeFile(file);
                logger.info("scanner [removed file] " + file.getName() + "\t("
                        + file.getPath() + ")");
            } else {
                logger.info("scanner [removed folder] " + item.getTitle()
                        + "\t(" + item.getId() + ")");
            }

            if (parent.getContainerContent().isEmpty()) {
                changed |= removeDidl(root, map, parent, itemsToPurge);
            }
        }
        return changed;
    }

    private void collectNewFiles(final ScanFolder folder,
            final List<ScanFile> list, final List<String> knownFileIds,
            final Set<String> foundFileIds) {
        if (folder.getFile().listFiles() != null
                && folder.getFile().isDirectory() && folder.getFile().canRead()) {
            for (final File f : folder.getFile().listFiles()) {
                final String id = this.idservice.getId(f);
                if (f.isFile()) {
                    foundFileIds.add(id);
                    if (!knownFileIds.contains(id)) {
                        final ScanFile addedFile = folder.addFile(id, f);
                        if (addedFile != null) {
                            list.add(addedFile);
                        }
                    }
                } else {
                    final ScanFolder addedFolder = folder.addFolder(id, f);
                    if (addedFolder != null) {
                        collectNewFiles(addedFolder, list, knownFileIds,
                                foundFileIds);
                    }
                }
            }
        }
    }
}
