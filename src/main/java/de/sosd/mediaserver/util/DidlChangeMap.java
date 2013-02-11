package de.sosd.mediaserver.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import de.sosd.mediaserver.dao.DidlDao;
import de.sosd.mediaserver.domain.db.DidlDomain;

@Configurable
public class DidlChangeMap {

    private final Map<String, DidlDomain> map = new HashMap<String, DidlDomain>();

    @Autowired
    private DidlDao                       storage;

    public boolean hasDidl(final String id) {
        return this.map.containsKey(id);
    }

    public DidlDomain getDidl(final String id) {
        DidlDomain didl = this.map.get(id);
        if (didl == null) {
            // is not cached
            didl = this.storage.getDidl(id);
            return addDidl(id, didl);
        } else {
            return didl;
        }
    }

    public DidlDomain addDidl(final String id, final DidlDomain didl) {
        this.map.put(id, didl);
        return didl;
    }
}
