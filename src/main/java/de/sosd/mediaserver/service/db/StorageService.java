package de.sosd.mediaserver.service.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.bean.FrontendFolderBean;
import de.sosd.mediaserver.bean.StringKeyValuePair;
import de.sosd.mediaserver.domain.db.ClassNameWcType;
import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.FileDomain;
import de.sosd.mediaserver.domain.db.ScanFolderDomain;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.MediaserverConfiguration;
import de.sosd.mediaserver.util.DidlXmlCreator;

@Service
public class StorageService {

//	private final static Logger logger = LoggerFactory.getLogger(StorageService.class);
	
	@PersistenceContext(name = "mediaserver")
	protected EntityManager manager;

	@Autowired
	private MediaserverConfiguration cfg;
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public SystemDomain getSystemProperties() {
		final SystemDomain system = this.manager.find(SystemDomain.class, this.cfg.getHostname());
		if (system == null) {
			return initSystem();	
		}
		return system;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void store(final SystemDomain stats) {
		this.manager.persist(stats);
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	public DidlXmlCreator getContainerContentById(final String containerId, final int startIdx,
			final int count, final String filter, final String sort) {
		final Query q = this.manager
				.createQuery("select didl from DIDL as didl where didl.parent.id = ?1 and (didl.online is null or didl.online = ?2)"
						+ filter  + sort);
		q.setParameter(1, containerId);
		q.setParameter(2, true);
		q.setFirstResult(startIdx);
		q.setMaxResults(count);
		@SuppressWarnings("unchecked")
		final List<DidlDomain> resultList = q.getResultList();

		final Long totalMatches = getContainerContentChildCountById(containerId, filter);
		final DidlXmlCreator didlLite = new DidlXmlCreator();
		didlLite.setTotalMatches(totalMatches);
		
		for (final DidlDomain dd : resultList) {
			didlLite.addDidlObject(dd);			
		}

		return didlLite;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.SUPPORTS)
	public DidlXmlCreator getSearchItems(final String objectId, final String where, final ArrayList<Object> searchParameters, final int startIdx, final int count,
			final String filter, final String sort) {
		List<String> searchPaths = manager.createQuery("select path from DIDL where id = ?1").setParameter(1, objectId).setMaxResults(1).getResultList();
		Long totalMatches = 0l;
		List<DidlDomain> resultList = new ArrayList<DidlDomain>();
		if (!searchPaths.isEmpty()) {
			String searchPath = searchPaths.get(0) + "%";
			// " order by didl.classType desc, didl.parent.id,  didl.title asc, didl.date desc" +
			final Query q = this.manager.createQuery("select didl from DIDL as didl" + where + filter +  sort);
			int idx = 1;
			for (final Object param : searchParameters) {
				q.setParameter(idx++, param);
			}
			q.setParameter(idx++, searchPath);
			q.setFirstResult(startIdx);
			q.setMaxResults(count);
			totalMatches = getSearchItemsCount(searchPath, where, searchParameters, filter);
			resultList = q.getResultList();
		}
		final DidlXmlCreator didlLite = new DidlXmlCreator();
		didlLite.setTotalMatches(totalMatches);
		for (final DidlDomain dd : resultList) {
			didlLite.addDidlObject(dd);
		}
		return didlLite;
	}	
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public Long getContainerContentChildCountById(final String containerId, final String filter) {
		return (Long) this.manager.createQuery("select count(didl) from DIDL as didl where didl.parent.id = ?1 and (didl.online is null or didl.online = ?2)"
				+ filter).setParameter(1, containerId).setParameter(2, true).getSingleResult();
	}
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public Long getSearchItemsCount(String searchPath, final String where, final ArrayList<Object> searchParameters, final String filter) {
		final Query q = this.manager.createQuery("select count(didl) from DIDL as didl" + where + filter);
		int idx = 1;
		for (final Object param : searchParameters) {
			q.setParameter(idx++, param);
		}
		q.setParameter(idx++, searchPath);
		return  (Long) q.getSingleResult();
	}
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public DidlXmlCreator getInfoById(final String containerId, final int startIdx, final int count,
			final String filter, final String sort) {
		final Query q = this.manager.createQuery("select didl from DIDL as didl where didl.id = ?1 and (didl.online is null or didl.online = ?2)"
				+ filter + sort);
		q.setParameter(1, containerId);
		q.setParameter(2, true);
		q.setFirstResult(startIdx);
		q.setMaxResults(count);

		@SuppressWarnings("unchecked")
		final
		List<DidlDomain> resultList = q.getResultList();

		final DidlXmlCreator didlLite = new DidlXmlCreator();
		didlLite.setTotalMatches(resultList.size());
		for (final DidlDomain dd : resultList) {
			didlLite.addDidlObject(dd);
		}

		return didlLite;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void store(final DidlDomain didl) {
		this.manager.persist(didl);
	}
	

	@Transactional(propagation = Propagation.REQUIRED)
	public SystemDomain initSystem() {
		final SystemDomain stats = new SystemDomain();
		stats.setHostname(this.cfg.getHostname());
		stats.setUpdateId(1);
		stats.setLastDataChange(new Date());
		stats.setDidlRoot(new DidlDomain(stats));
		
		store(stats);
		return stats;
	}

//	@SuppressWarnings("unchecked")
//	@Transactional(propagation = Propagation.SUPPORTS)
//	public List<StringKeyValuePair> getAllKnownFiles() {
//		final Query q = this.manager.createQuery("select new de.sosd.mediaserver.bean.StringKeyValuePair(file.id,file.path) from File as file");
//		return q.getResultList();
//	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<String> getAllFileIds(String scanFolderId) {
		return this.manager.createQuery("select id from File where parent.id = ?1").setParameter(1, scanFolderId).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<String> getAllDidlIds() {
		return this.manager.createQuery("select id from DIDL").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<FrontendFolderBean> getAllFrontendScanFolders() {
		return this.manager.createQuery("select new de.sosd.mediaserver.bean.FrontendFolderBean(dir.id, dir.path, dir.lastScan, dir.scanInterval, dir.scanState,dir.folderCount, dir.fileCount, dir.overallSize) from Folder as dir").getResultList();
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public StringKeyValuePair createDirectory(final ScanFolderDomain scanFolder) {
		final SystemDomain systemProperties = getSystemProperties();
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
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeDidl(DidlDomain item) {
		this.manager.remove(item);	
	}	

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
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public DidlDomain getDidl(String id) {
		try {
			return this.manager.find(DidlDomain.class, id);
		} catch (final EmptyResultDataAccessException nre) {
			return null;		
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void store(final ScanFolderDomain scfd) {
		this.manager.persist(scfd);
		
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void update(final SystemDomain system, final List<Object> itemsToPurge) {
		for (final Object item : itemsToPurge) {
			this.manager.remove(item);
		}
		store(system);
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
	@Transactional(propagation=Propagation.SUPPORTS)
	public List<StringKeyValuePair> getVideoFileIdsWithoutMeta() {
		final Query q = this.manager.createQuery("select new de.sosd.mediaserver.bean.StringKeyValuePair(file.id,file.path) from DIDL where (online is null or online = ?6) and (passedMPlayer is null or passedMPlayer = ?5) and (" +
				"classType = ?1 or " +
				"classType = ?2 or " +
				"classType = ?3 or " +
				"classType = ?4)"
				);
		
		q.setParameter(1, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM);
		q.setParameter(2, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE);
		q.setParameter(3, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MUSIC_VIDEO_CLIP);
		q.setParameter(4, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_VIDEO_BROADCAST);
		q.setParameter(5, false);
		q.setParameter(6, true);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation=Propagation.SUPPORTS)
	public List<StringKeyValuePair> getAudioFileIdsWithoutMeta() {
		final Query q = this.manager.createQuery("select new de.sosd.mediaserver.bean.StringKeyValuePair(file.id,file.path) from DIDL where (online is null or online = ?6) and (passedMPlayer is null or passedMPlayer = ?5) and (" +
				"classType = ?1 or " +
				"classType = ?2 or " +
				"classType = ?3 or " +
				"classType = ?4)"
				);
		
		q.setParameter(1, ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM);
		q.setParameter(2, ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_AUDIO_BOOK);
		q.setParameter(3, ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_AUDIO_BROADCAST);
		q.setParameter(4, ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK);
		q.setParameter(5, false);
		q.setParameter(6, true);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation=Propagation.SUPPORTS)
	public List<StringKeyValuePair> getVideoFileIdsWithoutThumbnail() {
		final Query q = this.manager.createQuery("select new de.sosd.mediaserver.bean.StringKeyValuePair(file.id,file.path) from DIDL where (online is null or online = ?7) and (generateThumbnail is null or generateThumbnail = ?6) and passedMPlayer = ?5 and (" +
				"classType = ?1 or " +
				"classType = ?2 or " +
				"classType = ?3 or " +
				"classType = ?4)"
				);
		
		q.setParameter(1, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM);
		q.setParameter(2, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE);
		q.setParameter(3, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MUSIC_VIDEO_CLIP);
		q.setParameter(4, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_VIDEO_BROADCAST);
		q.setParameter(5, true);
		q.setParameter(6, true);
		q.setParameter(7, true);
		return q.getResultList();
	}
	// .setMaxResults(120)

	@SuppressWarnings("unchecked")
	public List<String> getImageFileIdsWithoutMeta() {
		final Query q = this.manager.createQuery("select file.id from DIDL where (online is null or online = ?7) and (generateThumbnail is null or generateThumbnail = ?6) and (" +
					"classType = ?1 or " +
					"classType = ?2" +
				")"
				);
		
		q.setParameter(1, ClassNameWcType.OBJECT_ITEM_IMAGE_ITEM);
		q.setParameter(2, ClassNameWcType.OBJECT_ITEM_IMAGE_ITEM_PHOTO);
		q.setParameter(6, true);
		q.setParameter(7, true);
		return q.getResultList();
	}	
	
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public String getDidlThumbnailType(final String id) {
		return (String)this.manager.createQuery("select thumbnail.type from DIDL where id = ?1").setParameter(1, id).getSingleResult();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void markMPlayerPassed(final String id) {
		final DidlDomain dd = this.manager.find(DidlDomain.class, id);
		dd.setPassedMPlayer(true);
		this.manager.persist(dd);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void setMetaInfoGenerationRunning(final boolean value) {
		final SystemDomain systemProperties = getSystemProperties();
		systemProperties.setMetaInfoGenerationRunning(value);
		this.manager.persist(systemProperties);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void setThumbnailGenerationRunning(final boolean value) {
		final SystemDomain systemProperties = getSystemProperties();
		systemProperties.setThumbnailGenerationRunning(value);
		this.manager.persist(systemProperties);	
	}
	
	
	// optimizations through update
	
	@SuppressWarnings("unchecked")
	@Transactional(propagation=Propagation.SUPPORTS)
	public List<DidlDomain> getAllDidlWithContentSizeNull() {
		return this.manager.createQuery("select didl from DIDL as didl where didl.containerContentSize is null").getResultList();
	}











}
