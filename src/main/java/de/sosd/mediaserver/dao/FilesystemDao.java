package de.sosd.mediaserver.dao;

import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.bean.StringKeyValuePair;
import de.sosd.mediaserver.bean.ui.FrontendFolderBean;
import de.sosd.mediaserver.domain.db.FileDomain;
import de.sosd.mediaserver.domain.db.ScanFolderDomain;
import de.sosd.mediaserver.domain.db.SystemDomain;

@Service
public class FilesystemDao {

	@PersistenceContext(name = "mediaserver")
	protected EntityManager manager;
	
	@Autowired
	private SystemDao systemDao;
	
//	@SuppressWarnings("unchecked")
//	@Transactional(propagation = Propagation.SUPPORTS)
//	public List<StringKeyValuePair> getAllKnownFiles() {
//		final Query q = this.manager.createQuery("select new de.sosd.mediaserver.bean.StringKeyValuePair(file.id,file.path) from File as file");
//		return q.getResultList();
//	}

	@Transactional(propagation = Propagation.SUPPORTS)
	public String getPathForFile(final String id) {
		final FileDomain file = this.manager.find(FileDomain.class, id);
		if (file != null) {
			return file.getPath();
		}
		return null;
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	public ScanFolderDomain getScanfolder(final String id) {
		try {
			return this.manager.find(ScanFolderDomain.class, id);
		} catch (final EmptyResultDataAccessException nre) {
			return null;		
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void store(final ScanFolderDomain scfd) {
		this.manager.persist(scfd);
		
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void store(final FileDomain fd) {
		this.manager.persist(fd);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public FileDomain getFile(final String id) {
		return this.manager.find(FileDomain.class, id);
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<String> getAllFileIds(String scanFolderId) {
		return this.manager.createQuery("select id from File where parent.id = ?1").setParameter(1, scanFolderId).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<FrontendFolderBean> getAllFrontendScanFolders() {
		return this.manager.createQuery("select new de.sosd.mediaserver.bean.ui.FrontendFolderBean(dir.id, dir.path, dir.lastScan, dir.scanInterval, dir.scanState,dir.folderCount, dir.fileCount, dir.overallSize) from Folder as dir").getResultList();
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public StringKeyValuePair createDirectory(final ScanFolderDomain scanFolder, final String usn) {
		final SystemDomain systemProperties = systemDao.getSystem(usn);
		scanFolder.setSystem(systemProperties);
		systemProperties.getScanFolder().add(scanFolder);
		this.manager.persist(systemProperties);
		
		return new StringKeyValuePair(scanFolder.getId(), scanFolder.getPath());
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void createFile(final String id, final String folderId, final File source) {
		final ScanFolderDomain parent = this.manager.find(ScanFolderDomain.class, folderId);
		final FileDomain f = new FileDomain(id, parent, source);	
		this.manager.persist(f);
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	public boolean isDirectoryPresent(final String id) {
		return this.manager.find(ScanFolderDomain.class, id) != null;
	}
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public boolean isFilePresent(final String id) {
		return this.manager.find(FileDomain.class, id) != null;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeDirectory(final String id) {
		final ScanFolderDomain d = this.manager.find(ScanFolderDomain.class, id);
		
		SystemDomain system = d.getSystem();
		system.getScanFolder().remove(d);
		d.setSystem(null);
		this.manager.persist(system);
		this.manager.remove(d);		
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeFile(final String id) {
		final FileDomain d = this.manager.find(FileDomain.class, id);
		removeFile(d);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeFile(FileDomain file) {
		this.manager.remove(file);			
	}	
	
	
}
