package de.sosd.mediaserver.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.bean.StringKeyValuePair;
import de.sosd.mediaserver.bean.WebappLocationBean;
import de.sosd.mediaserver.domain.db.ClassNameWcType;
import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.util.DidlXmlCreator;

@Service
public class DidlDao {

    // private final static Logger logger =
    // LoggerFactory.getLogger(StorageService.class);

    @PersistenceContext(name = "mediaserver")
    protected EntityManager manager;

    @Transactional(propagation = Propagation.SUPPORTS)
    public DidlXmlCreator getContainerContentById(final String containerId,
            final int startIdx,
            final int count, final String filter, final String sort,
            final WebappLocationBean wlb) {
        final Query q = this.manager
                .createQuery("select didl from DIDL as didl where didl.parent.id = ?1 and didl.online = ?2"
                        + filter + sort);
        q.setParameter(1, containerId);
        q.setParameter(2, true);
        q.setFirstResult(startIdx);
        q.setMaxResults(count);
        @SuppressWarnings("unchecked")
        final List<DidlDomain> resultList = q.getResultList();

        final Long totalMatches = getContainerContentChildCountById(
                containerId, filter);
        final DidlXmlCreator didlLite = new DidlXmlCreator(wlb);
        didlLite.setTotalMatches(totalMatches);

        for (final DidlDomain dd : resultList) {
            didlLite.addDidlObject(dd);
        }

        return didlLite;
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS)
    public DidlXmlCreator getSearchItems(final String objectId,
            final String where, final ArrayList<Object> searchParameters,
            final int startIdx, final int count,
            final String filter, final String sort, final WebappLocationBean wlb) {
        final List<String> searchPaths = this.manager
                .createQuery("select path from DIDL where id = ?1")
                .setParameter(1, objectId).setMaxResults(1).getResultList();
        Long totalMatches = 0l;
        List<DidlDomain> resultList = new ArrayList<DidlDomain>();
        if (!searchPaths.isEmpty()) {
            final String searchPath = searchPaths.get(0) + "%";
            // " order by didl.classType desc, didl.parent.id,  didl.title asc, didl.date desc"
            // +
            final Query q = this.manager
                    .createQuery("select didl from DIDL as didl" + where
                            + filter + " and didl.online = ?"
                            + (searchParameters.size() + 2) + " " + sort);

            int idx = 1;
            for (final Object param : searchParameters) {
                q.setParameter(idx++, param);
            }
            q.setParameter(idx++, searchPath);
            q.setParameter(idx++, true);
            q.setFirstResult(startIdx);
            q.setMaxResults(count);
            totalMatches = getSearchItemsCount(searchPath, where,
                    searchParameters, filter);
            resultList = q.getResultList();
        }
        final DidlXmlCreator didlLite = new DidlXmlCreator(wlb);
        didlLite.setTotalMatches(totalMatches);
        for (final DidlDomain dd : resultList) {
            didlLite.addDidlObject(dd);
        }
        return didlLite;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Long getContainerContentChildCountById(final String containerId,
            final String filter) {
        return (Long) this.manager
                .createQuery(
                        "select count(didl) from DIDL as didl where didl.parent.id = ?1 and didl.online = ?2"
                                + filter).setParameter(1, containerId)
                .setParameter(2, true).getSingleResult();
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Long getSearchItemsCount(final String searchPath,
            final String where, final ArrayList<Object> searchParameters,
            final String filter) {
        final Query q = this.manager
                .createQuery("select count(didl) from DIDL as didl" + where
                        + filter + " and didl.online = ?"
                        + (searchParameters.size() + 2));

        int idx = 1;
        for (final Object param : searchParameters) {
            q.setParameter(idx++, param);
        }
        q.setParameter(idx++, searchPath);
        q.setParameter(idx++, true);
        return (Long) q.getSingleResult();
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public DidlXmlCreator getInfoById(final String containerId,
            final int startIdx, final int count,
            final String filter, final String sort, final WebappLocationBean wlb) {
        final Query q = this.manager
                .createQuery("select didl from DIDL as didl where didl.id = ?1"
                        + filter + sort);
        q.setParameter(1, containerId);
        q.setParameter(2, true);
        q.setMaxResults(count);

        @SuppressWarnings("unchecked")
        final List<DidlDomain> resultList = q.getResultList();

        final DidlXmlCreator didlLite = new DidlXmlCreator(wlb);
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

    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<String> getAllDidlIds() {
        return this.manager.createQuery("select id from DIDL").getResultList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removeDidl(final DidlDomain item) {
        this.manager.remove(item);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public DidlDomain getDidl(final String id) {
        try {
            return this.manager.find(DidlDomain.class, id);
        } catch (final EmptyResultDataAccessException nre) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<StringKeyValuePair> getVideoFileIdsWithoutMeta() {
        final Query q = this.manager
                .createQuery("select new de.sosd.mediaserver.bean.StringKeyValuePair(file.id,file.path) from DIDL where online = ?6 and (passedMPlayer is null or passedMPlayer = ?5) and ("
                        +
                        "classType = ?1 or " +
                        "classType = ?2 or " +
                        "classType = ?3 or " +
                        "classType = ?4)"
                );

        q.setParameter(1, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM);
        q.setParameter(2, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE);
        q.setParameter(3,
                ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MUSIC_VIDEO_CLIP);
        q.setParameter(4,
                ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_VIDEO_BROADCAST);
        q.setParameter(5, false);
        q.setParameter(6, true);
        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<StringKeyValuePair> getAudioFileIdsWithoutMeta() {
        final Query q = this.manager
                .createQuery("select new de.sosd.mediaserver.bean.StringKeyValuePair(file.id,file.path) from DIDL where online = ?6 and (passedMPlayer is null or passedMPlayer = ?5) and ("
                        +
                        "classType = ?1 or " +
                        "classType = ?2 or " +
                        "classType = ?3 or " +
                        "classType = ?4)"
                );

        q.setParameter(1, ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM);
        q.setParameter(2, ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_AUDIO_BOOK);
        q.setParameter(3,
                ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_AUDIO_BROADCAST);
        q.setParameter(4, ClassNameWcType.OBJECT_ITEM_AUDIO_ITEM_MUSIC_TRACK);
        q.setParameter(5, false);
        q.setParameter(6, true);
        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<StringKeyValuePair> getVideoFileIdsWithoutThumbnail() {
        final Query q = this.manager
                .createQuery("select new de.sosd.mediaserver.bean.StringKeyValuePair(file.id,file.path) from DIDL where online = ?7 and (generateThumbnail is null or generateThumbnail = ?6) and passedMPlayer = ?5 and ("
                        +
                        "classType = ?1 or " +
                        "classType = ?2 or " +
                        "classType = ?3 or " +
                        "classType = ?4)"
                );

        q.setParameter(1, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM);
        q.setParameter(2, ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MOVIE);
        q.setParameter(3,
                ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_MUSIC_VIDEO_CLIP);
        q.setParameter(4,
                ClassNameWcType.OBJECT_ITEM_VIDEO_ITEM_VIDEO_BROADCAST);
        q.setParameter(5, true);
        q.setParameter(6, true);
        q.setParameter(7, true);
        return q.getResultList();
    }

    // .setMaxResults(120)

    @SuppressWarnings("unchecked")
    public List<String> getImageFileIdsWithoutMeta() {
        final Query q = this.manager
                .createQuery("select file.id from DIDL where online = ?7 and (generateThumbnail is null or generateThumbnail = ?6) and ("
                        +
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

    @Transactional(propagation = Propagation.SUPPORTS)
    public String getDidlThumbnailType(final String id) {
        return (String) this.manager
                .createQuery("select thumbnail.type from DIDL where id = ?1")
                .setParameter(1, id).getSingleResult();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void markMPlayerPassed(final String id) {
        final DidlDomain dd = this.manager.find(DidlDomain.class, id);
        dd.setPassedMPlayer(true);
        this.manager.persist(dd);
    }

    // optimizations through update

    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<DidlDomain> getAllDidlWithContentSizeNull() {
        return this.manager
                .createQuery(
                        "select didl from DIDL as didl where didl.containerContentSize is null")
                .getResultList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void setOnline(final String scanFolderId, final boolean value) {
        this.manager
                .createQuery("update DIDL set online = ?2 where folder = ?1")
                .setParameter(1, scanFolderId).setParameter(2, value)
                .executeUpdate();
    }

}
