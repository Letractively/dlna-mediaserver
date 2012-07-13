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

import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.FileDomain;
import de.sosd.mediaserver.domain.db.ScanFolderDomain;
import de.sosd.mediaserver.domain.db.ScanFolderState;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.db.DIDLService;
import de.sosd.mediaserver.service.db.StorageService;
import de.sosd.mediaserver.util.ScanContext;
import de.sosd.mediaserver.util.ScanFile;
import de.sosd.mediaserver.util.ScanFolder;

@Service
public class FilesystemService {

	private final static Log logger = LogFactory.getLog(FilesystemService.class);
	
	@Autowired
	private StorageService storage;
	
	@Autowired
	private IdService idservice;
	
	@Autowired
	private DIDLService didl;
	

	
	public boolean addScanDirectory(final File directory) {
		try {
			addScanDirectoryTransactional(directory);
			return true;
		} catch (final Throwable t) {
			logger.error("error while adding directory, probably because it already exists", t);
			return false;
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void addScanDirectoryTransactional(final File directory) {
		if (directory.isDirectory()) {
			final String id = this.idservice.getId(directory);
			if (!this.storage.isDirectoryPresent(id)) {
				final ScanFolderDomain scanFolder = new ScanFolderDomain(id, directory, null);
				
				final SystemDomain system = this.storage.getSystemProperties();
				system.getScanFolder().add(scanFolder);
				scanFolder.setSystem(system);
				scanFolder.setDidl(this.didl.createDidlContainer(scanFolder, system.getDidlRoot()));
				
				
				this.storage.store(system);
				
				logger.info("new scan-directory add : " + scanFolder.getPath());
			}
		}
	}

	public void scanFilesystem() {
		// get list of directories due for scanning
		logger.info("scanner [start]");
		final List<ScanContext> scanContexts = createScanContexts();
		scanDirectories(scanContexts);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)	
	private List<ScanContext> createScanContexts() {
		final List<ScanContext> scanContexts = new ArrayList<ScanContext>();
		final long currentTimeMillis = System.currentTimeMillis();
		final SystemDomain system = this.storage.getSystemProperties();
		for (final ScanFolderDomain sfd : system.getScanFolder()) {
			if (
			!ScanFolderState.SCANNING.equals(sfd.getScanState()) &&
				((sfd.getLastScan() == null) || 
				((sfd.getLastScan().getTime() + (sfd.getScanInterval() * 60000)) < currentTimeMillis))) {
				
				// should scan
				
				final File dir = new File(sfd.getPath());
				if (! dir.exists() || ! dir.isDirectory()) {
					// this could happen with external storage ..
					sfd.setScanState(ScanFolderState.NOT_FOUND);
					sfd.getDidl().goOnline(false);
					system.increaseUpdateId();
				} else {
					if (ScanFolderState.NOT_FOUND.equals(sfd.getScanState())) {
						sfd.getDidl().goOnline(true);
						system.increaseUpdateId();
					}
					
					sfd.setScanState(ScanFolderState.SCANNING);
					scanContexts.add(new ScanContext(sfd.getId(), dir));
				}
			}
		}		
		this.storage.store(system);
		
		return scanContexts;
	}
		
	@Transactional(propagation=Propagation.REQUIRED)
	private void scanDirectories(final List<ScanContext> scanContexts) {
		long changedCount = 0l;
		boolean systemChanged = false;
		
		final SystemDomain 				system 				= this.storage.getSystemProperties();
		final List<Object> 				itemsToPurge 		= new ArrayList<Object>();	
		final Map<String, ScanContext> 	idScanContextMap 	= new HashMap<String, ScanContext>();		
		final Set<String> 				foundFileIds  					= new HashSet<String>();
		final Map<String, DidlDomain> 	touchedDidlMap 					= new HashMap<String, DidlDomain>();

		for (final ScanContext sc : scanContexts) {
			idScanContextMap.put(sc.getScanFolderId(), sc);
		}		
		
		// collect all Files, no Folders!
		for (final ScanContext sc : scanContexts) {
			logger.info("scanner [scan] "+sc.getScanFolder());
			List<String> knownFileIds = this.storage.getAllFileIds(sc.getScanFolderId());
			collectNewFiles(sc.getScanFolder(), sc.getFiles(), knownFileIds, foundFileIds);
			logger.info("scanner [found files] " + sc.getFiles().size() + " " +sc.getScanFolder());
			
			
			changedCount += sc.getFiles().size();
			
			knownFileIds.removeAll(foundFileIds);
			for (String removed : knownFileIds) {
				sc.addDeletedMediaFile(removed);
				changedCount += 1;
			}
		}	
		
		if (changedCount > 0) {
			final List<String> allDidlIds= this.storage.getAllDidlIds();	
			for (String id : allDidlIds) {
				touchedDidlMap.put(id, null);
			}
			for (final ScanFolderDomain sfd : system.getScanFolder()) {
				if (idScanContextMap.containsKey(sfd.getId())) {
					final ScanContext sc = idScanContextMap.get(sfd.getId());
					logger.info("scanner [filter] "+sc.getScanFolder());
					for (final ScanFile f : sc.getFiles()) {
						final FileDomain fd = new FileDomain(f.getId(), null, f.getFile());					
						if (this.didl.createDidl(fd, f, touchedDidlMap, sfd)) {
							sc.getMediaFiles().add(fd);
						}
					}
					logger.info("scanner [found new files] " + sc.getMediaFiles().size() + " " +sc.getScanFolder());
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
					logger.info("scanner [add file] " + fd.getName() + "\t\t(" + fd.getPath() + ")");
				}				
				if (changedFiles || !sc.getDeletedMediaFiles().isEmpty()) {
					changedFiles |= updateDidl(sfd.getDidl(), sfd, sc.getDeletedMediaFiles(), itemsToPurge);
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
				logger.info("scanner [dlna-unsupported] " + this.didl.getMissingClassTypeExtensions()+ ", " + this.didl.getMissingProtocolInfoExtensions());
			}
		}		
		logger.info("scanner [update database]");
		this.storage.update(system, itemsToPurge);
		logger.info("scanner [done]");
		
	}

	private boolean updateDidl(final DidlDomain self, final ScanFolderDomain sfd, final Set<String> removedItemIds, final List<Object> itemsToPurge) {
		boolean changedChilds = false;
		boolean changedSelf = false;
		final List<DidlDomain> childs = new ArrayList<DidlDomain>(self.getContainerContent());
		// before adding new childs traverse existing ones and remove deleted ones
		for (final DidlDomain item : childs) {
			if (removedItemIds.contains(item.getId())) {
				// remove child
				final FileDomain file = item.getFile();
				changedSelf = self.removeChild(item);			
				item.setFile(null);
				itemsToPurge.add(item);
				
				changedSelf |= file.getParent().removeFile(file);
				itemsToPurge.add(file);
				
				logger.info("scanner [removed file] " + file.getName() + "\t\t(" + file.getPath() + ")");
				// delete item and file?			
			} else {
				// check childs for changes
				if (item.isContainer()) {
					changedChilds |= updateDidl(item, sfd, removedItemIds, itemsToPurge);
				}
			}
		}
	
		if (self.isContainer() && self.getContainerContent().isEmpty()) {
			// check if empty
			self.getParent().removeChild(self);
			sfd.removeFolder(self);
			itemsToPurge.add(self);
			logger.info("scanner [removed folder] " + self.getTitle() + " [" + self.getId() + "]");
			return true;
		}
		
		if (changedSelf) {
			self.increaseUpdateId();
		}
		
		return changedSelf || changedChilds;
	}

//	private int countDidl(DidlDomain parent) {
//		int result = 1;
//		for (DidlDomain didl : parent.getContainerContent()) {
//			if (didl.isContainer()) {
//				result += countDidl(didl);
//			}
//		}
//		return result;
//	}
//
//	private boolean updateDidl(DidlDomain self,
//			Map<String, List<DidlDomain>> didlParentIdDidlMap, Set<String> removedItemIds, List<Object> itemsToPurge) {
//		boolean changedChilds = false;
//		boolean changedSelf = false;
//		List<DidlDomain> childs = new ArrayList<DidlDomain>(self.getContainerContent());
//		// before adding new childs traverse existing ones and remove deleted ones
//		for (DidlDomain item : childs) {
//			if (removedItemIds.contains(item.getId())) {
//				// remove child
//				FileDomain file = item.getFile();
//				changedSelf = self.getContainerContent().remove(item);			
//				item.setParent(null);
//				item.setFile(null);
//				itemsToPurge.add(item);
//				
//				changedSelf |= file.getParent().removeFile(file);
//				itemsToPurge.add(file);
//				
//				logger.info("scanner [removed file] " + file.getName() + "\t\t(" + file.getPath() + ")");
//				// delete item and file?			
//			} else {
//				// check childs for changes
//				changedChilds |= updateDidl(item, didlParentIdDidlMap, removedItemIds, itemsToPurge);
//			}			
//		}
//		
//		// add new childs to own container
//		if (didlParentIdDidlMap.containsKey(self.getId())) {
//			List<DidlDomain> content = didlParentIdDidlMap.remove(self.getId());
//			
//			// add 
//			for (DidlDomain item : content) {
//				item.setParent(self);
//				changedSelf |= self.getContainerContent().add(item);
//			}	
//		}
//		
//		if (self.isContainer() && self.getContainerContent().isEmpty()) {
//			// check if empty
//			self.getParent().getContainerContent().remove(self);
//			self.setParent(null);
//			itemsToPurge.add(self);
//			logger.info("scanner [removed folder] " + self.getTitle() + " [" + self.getId() + "]");
//			return true;
//		}
//		
//		if (changedSelf) {
//			self.increaseUpdateId();
//		}
//		return changedSelf || changedChilds;
//	}

	private void collectNewFiles(final ScanFolder folder, final List<ScanFile> list, final List<String> knownFileIds, Set<String> foundFileIds) {
		if ((folder.getFile().listFiles() != null) && folder.getFile().isDirectory() && folder.getFile().canRead()) {
			for (final File f : folder.getFile().listFiles()) {
				final String id = this.idservice.getId(f);			
				if (f.isFile()) {
					foundFileIds.add(id);
					if (! knownFileIds.contains(id)) {
						ScanFile addedFile = folder.addFile(id, f);
						if (addedFile != null) {
							list.add(addedFile);
						}
					}
				} else {
					ScanFolder addedFolder = folder.addFolder(id, f);
					if (addedFolder !=  null) {
						collectNewFiles(addedFolder,list,knownFileIds,foundFileIds);
					}
				}
			}
		}
	}
}
