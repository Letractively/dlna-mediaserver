package de.sosd.mediaserver.dao;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.MediaserverConfiguration;

@Service
public class SystemDao {

	@PersistenceContext(name = "mediaserver")
	protected EntityManager manager;
	
	@Autowired
	private MediaserverConfiguration cfg;
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public SystemDomain getSystem(String usn) {
		return manager.find(SystemDomain.class, usn);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void store(final SystemDomain system) {
		manager.persist(system);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(final SystemDomain system, final List<Object> itemsToPurge) {
		for (final Object item : itemsToPurge) {
			this.manager.remove(item);
		}
		store(system);
	}	

	@Transactional(propagation = Propagation.REQUIRED)
	public String initSystem() {
		final SystemDomain system = new SystemDomain();
		system.setUsn(UUID.randomUUID().toString());
		system.setUpdateId(1);
		system.setLastDataChange(new Date());
		system.setDidlRoot(new DidlDomain(system));
		
		store(system);
		return system.getUsn();
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void setMetaInfoGenerationRunning(final boolean value, final String usn) {
		final SystemDomain systemProperties = getSystem(usn);
		systemProperties.setMetaInfoGenerationRunning(value);
		manager.persist(systemProperties);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void setThumbnailGenerationRunning(final boolean value, final String usn) {
		final SystemDomain systemProperties = getSystem(usn);
		systemProperties.setThumbnailGenerationRunning(value);
		manager.persist(systemProperties);	
	}
	
	@Transactional(propagation = Propagation.SUPPORTS)
	public int getSystemUpdateId(String usn) {
		return (Integer) manager.createQuery("select system.updateId from System system where system.usn = ?").setParameter(1, usn).getSingleResult();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void setSystemOnline(final boolean value, final String usn) {
		manager.createQuery("update System set online = ? where usn = ?").setParameter(1, value).setParameter(2, usn).executeUpdate();
	}
}
