package de.sosd.mediaserver.dao.support;

import java.io.File;
import java.io.IOException;

import javax.persistence.PostRemove;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.ThumbnailDomain;
import de.sosd.mediaserver.service.MediaserverConfiguration;

@Configurable
public class ThumbnailPurger {

    @Autowired
    private MediaserverConfiguration cfg;

    private final static Log         logger = LogFactory
                                                    .getLog(ThumbnailPurger.class);

    @PostRemove
    public void onDelete(final DidlDomain domain) {
        final ThumbnailDomain thumbnail = domain.getThumbnail();
        if (thumbnail != null) {
            final File previews = new File(this.cfg.getPreviews());
            final File file = new File(previews, domain.getId() + "."
                    + thumbnail.getType());

            if (file.exists()) {
                try {
                    FileUtils.forceDelete(file);
                } catch (final IOException e) {
                    logger.error(
                            "could not remove thumbnail-file "
                                    + file.getAbsolutePath(), e);
                }
            }
        }

    }

}
