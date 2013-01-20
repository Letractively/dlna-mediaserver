package de.sosd.mediaserver.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import de.sosd.mediaserver.dao.DidlDao;
import de.sosd.mediaserver.domain.db.DidlDomain;

@Configurable
public class DidlChangeMap {

	private final Map<String, DidlDomain> 	map	= new HashMap<String, DidlDomain>();
	
	@Autowired
	private DidlDao storage;
	
	public boolean hasDidl(String id) {
		return map.containsKey(id);		
	}
		
	public DidlDomain getDidl(String id) {
		DidlDomain didl = map.get(id);
		if (didl == null) {
			// is not cached
			didl = storage.getDidl(id);
			return addDidl(id, didl);
		} else {
			return didl;
		}
	}
	
	public DidlDomain addDidl(String id, DidlDomain didl) {
		map.put(id, didl);
		return didl;
	}
}
