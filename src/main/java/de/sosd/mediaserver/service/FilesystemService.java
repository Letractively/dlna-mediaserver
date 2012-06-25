package de.sosd.mediaserver.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

@Service
public class FilesystemService {

	private final static Log logger = LogFactory.getLog(FilesystemService.class);
	
	@Autowired
	private StorageService storage;
	
	@Autowired
	private IdService idservice;
	
	@Autowired
	private DIDLService didl;
	
	public void scanFilesystem() {
		// get list of directories due for scanning
		logger.info("scanner [start]");
		final List<ScanContext> scanContexts = createScanContexts();
		scanDirectories(scanContexts);
	}
	
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
	
	private void scanDirectories(final List<ScanContext> scanContexts) {
		long fileCount = 0l;
		
		final Set<String> foundFileIds  = new HashSet<String>();
		Map<String, String> allFileIdScanFolderIdMap = new HashMap<String,String>();
		final Map<String, List<DidlDomain>> didlParentIdDidlMap = new HashMap<String, List<DidlDomain>>();
		final Map<String, DidlDomain> idNewDidlMap = new HashMap<String, DidlDomain>();
		
		// collect all Files, no Folders!
		for (final ScanContext sc : scanContexts) {
			logger.info("scanner [scan] "+sc.getScanFolder().getAbsolutePath());
			collectAllFiles(sc.getScanFolder(), sc.getFiles());
			logger.info("scanner [found files] " + sc.getFiles().size() + " " +sc.getScanFolder().getAbsolutePath());
			
			fileCount += sc.getFiles().size();
		}	
		
		if (fileCount > 0) {
			final List<String> allDidlIds= this.storage.getAllDidlIds();		
			for (final ScanContext sc : scanContexts) {
				for (String fileId : this.storage.getAllFileIds(sc.getScanFolderId())) {
					allFileIdScanFolderIdMap.put(fileId, sc.getScanFolderId());
				}
				
				logger.info("scanner [filter] "+sc.getScanFolder().getAbsolutePath());
				for (final File f : sc.getFiles()) {
					final String id = this.idservice.getId(f);
					foundFileIds.add(id);
					if (! allFileIdScanFolderIdMap.containsKey(id)) {
						final FileDomain fd = new FileDomain(id, null, f);					
						if (this.didl.createDidl(fd, f, allDidlIds, didlParentIdDidlMap,idNewDidlMap)) {
							sc.getMediaFiles().add(fd);
						}
					}
				}
				logger.info("scanner [found new files] " + sc.getMediaFiles().size() + " " +sc.getScanFolder().getAbsolutePath());
			}	
		}
		
		final Map<String, String> removedFileIdScanFolderIdMap = new HashMap<String,String>();
		for (final Entry<String,String> entry : allFileIdScanFolderIdMap.entrySet()) {
			if (! foundFileIds.contains(entry.getKey())) {
				removedFileIdScanFolderIdMap.put(entry.getKey(), entry.getValue());
			}
		}
		changeDataBase(scanContexts, didlParentIdDidlMap, idNewDidlMap, removedFileIdScanFolderIdMap);
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
	private boolean changeDataBase(final List<ScanContext> scanContexts,
			final Map<String, List<DidlDomain>> didlParentIdDidlMap, final Map<String, DidlDomain> idDidlMap, final Map<String, String> removedFileIdScanFolderIdMap) {
		boolean result = false;
		
		final SystemDomain system = this.storage.getSystemProperties();
		final List<Object> itemsToPurge = new ArrayList<Object>();
//		List<Object> itemsToSave = new ArrayList<Object>();
		// update didl
//		if (removedFileIds.size() > 0 || !didlParentIdDidlMap.isEmpty()) {
//			DidlDomain didlRoot = system.getDidlRoot();		
//			result |= updateDidl(idDidlMap, didlParentIdDidlMap, removedFileIds, itemsToSave, itemsToPurge); //updateDidl(didlRoot, didlParentIdDidlMap, removedFileIds, itemsToPurge);			
//		}
		
		final Map<String, ScanContext> idScanContextMap = new HashMap<String, ScanContext>();
		for (final ScanContext sc : scanContexts) {
			idScanContextMap.put(sc.getScanFolderId(), sc);
		}
		
		for (final Entry<String, String> entry : removedFileIdScanFolderIdMap.entrySet()) {
			try {
				idScanContextMap.get(entry.getValue()).addDeletedMediaFile(entry.getKey());
			} catch (final NullPointerException npe) {
				logger.error("NPE!", npe);
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
					changedFiles |= updateDidl(sfd.getDidl(), sfd, didlParentIdDidlMap, sc.getDeletedMediaFiles(), itemsToPurge);
				}
				// remove scanfolder mark
				sfd.setScanState(ScanFolderState.IDLE);
				sfd.setLastScan(new Date());
				
				result |= changedFiles;
			}
		}
		
		// something changed increase updateId
		if (result) {
			logger.info("scanner [collect new stats]");
			system.increaseUpdateId();
			system.setLastDataChange(new Date());
			
			if (this.didl.foundUnsupportedFiles()) {
				logger.info("scanner [dlna-unsupported] " + this.didl.getMissingClassTypeExtensions()+ ", " + this.didl.getMissingProtocolInfoExtensions());
			}
		}
		
		logger.info("scanner [update database]");
		this.storage.update(system, itemsToPurge);
		
//		if (result) {
//			// update stats
//			
//			// for system
//			system.increaseUpdateId();
//			system.setFileCount(0);
//			system.setFolderCount(didl.getDefaultFolderCount() + system.getScanFolder().size());
//			system.setOverallSize(0l);
//			// for scanfolder
//			for (ScanFolderDomain sfd : system.getScanFolder()) {
////				sfd.setFileCount(sfd.getFiles().size());
////				long overallSize = 0l;
////				for (FileDomain fd : sfd.getFiles()) {
////					overallSize += fd.getSize();
////				}
////				sfd.setOverallSize(overallSize);
////				sfd.setFolderCount(countDidl(sfd.getDidl()));
//
//				
//			}
//			storage.store(system);
//			
//
//		}
		logger.info("scanner [done]");
		return result;
	}

	private boolean updateDidl(final DidlDomain self, final ScanFolderDomain sfd,
			final Map<String, List<DidlDomain>> didlParentIdDidlMap,
			final Set<String> removedItemIds, final List<Object> itemsToPurge) {
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
					changedChilds |= updateDidl(item, sfd, didlParentIdDidlMap, removedItemIds, itemsToPurge);
				}
			}
		}
		
		// add new childs to own container
		if (didlParentIdDidlMap.containsKey(self.getId())) {
			final List<DidlDomain> content = didlParentIdDidlMap.remove(self.getId());
			
			// add 
			for (final DidlDomain item : content) {
				changedSelf |= self.addChild(item);
				if (item.isContainer()) {
					final int count = sfd.addFolder(item);
					logger.info("scanner [added folder] " + item.getTitle() + " [" + item.getId() + ", "+count+"]");
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

	private void collectAllFiles(final File dir, final List<File> list) {
		if ((dir.listFiles() != null) && dir.isDirectory() && dir.canRead()) {
			for (final File f : dir.listFiles()) {
				if (f.isFile()) {
					list.add(f);
				} else {
					collectAllFiles(f,list);
				}
			}
		}
	}
}