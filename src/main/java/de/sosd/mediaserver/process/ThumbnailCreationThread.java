package de.sosd.mediaserver.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import de.sosd.mediaserver.bean.StringKeyValuePair;
import de.sosd.mediaserver.dao.DidlDao;
import de.sosd.mediaserver.dao.SystemDao;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.MediaserverConfiguration;

@Configurable
public class ThumbnailCreationThread extends Thread {

    private final static Log         logger = LogFactory
                                                    .getLog(ThumbnailCreationThread.class);

    @Autowired
    private SystemDao                systemDao;

    @Autowired
    private DidlDao                  didlDao;

    @Autowired
    private MediaserverConfiguration cfg;

    private final SystemDomain       system;

    public ThumbnailCreationThread(final SystemDomain system) {
        this.system = system;
    }

    @Override
    public void run() {
        super.run();
        try {
            this.systemDao.setThumbnailGenerationRunning(true,
                    this.cfg.getUSN());
            logger.info("check for missing thumbnails");
            createThumbnailsAsync(this.system.getMplayerPath(),
                    this.system.getPreviewCache());
            logger.info("thumbnails up to date");
        } finally {
            this.systemDao.setThumbnailGenerationRunning(false,
                    this.cfg.getUSN());
        }
    }

    private void createThumbnailsAsync(final String mplayer,
            final String previewDir) {
        if (mplayer != null && mplayer.length() > 0 && previewDir != null
                && previewDir.length() > 0) {
            final File mplayerFile = new File(mplayer);
            final File previewFolder = new File(previewDir);

            final Set<String> fileIds = new HashSet<String>();
            final ArrayList<ConcurrentExecute> processArray = new ArrayList<ConcurrentExecute>();
            ConcurrentExecute ce = new ConcurrentExecute();
            processArray.add(ce);
            for (final StringKeyValuePair skvp : this.didlDao
                    .getVideoFileIdsWithoutThumbnail()) {
                if (!fileIds.contains(skvp.getKey())) {
                    fileIds.add(skvp.getKey());
                    if (!ce.add(mplayerFile, previewFolder, skvp)) {
                        ce = new ConcurrentExecute();
                        processArray.add(ce);
                    }
                } else {
                    logger.info("found and ignored double! : " + skvp.getKey());
                }
            }
            final List<MPlayerThumbnailGeneratorProcess> failed = new ArrayList<MPlayerThumbnailGeneratorProcess>();
            for (final ConcurrentExecute ce_e : processArray) {
                failed.addAll(
                        ce_e.execute()
                        );
            }

            if (!failed.isEmpty()) {
                logger.error("some thumbnails could not be created : "
                        + failed.size());
            }
        }
    }

    private class ConcurrentExecute {

        private final static int                             concurrentProcessCount = 10;
        private final List<MPlayerThumbnailGeneratorProcess> processes              = new ArrayList<MPlayerThumbnailGeneratorProcess>(
                                                                                            concurrentProcessCount);

        public boolean add(final File mplayerFile, final File previewFolder,
                final StringKeyValuePair idPath) {
            this.processes.add(new MPlayerThumbnailGeneratorProcess(
                    mplayerFile, previewFolder, idPath.getKey(), idPath
                            .getValue()));
            return this.processes.size() < concurrentProcessCount;
        }

        public List<MPlayerThumbnailGeneratorProcess> execute() {
            final List<MPlayerThumbnailGeneratorProcess> readers = new ArrayList<MPlayerThumbnailGeneratorProcess>();
            for (final MPlayerThumbnailGeneratorProcess reader : this.processes) {
                try {
                    reader.execute();
                    readers.add(reader);
                } catch (final ExecuteException e) {
                    logger.error(
                            "error creating thumb for File ["
                                    + reader.getFileId() + "]", e);
                } catch (final IOException e) {
                    logger.error(
                            "error creating thumb for File ["
                                    + reader.getFileId() + "]", e);
                }
            }
            for (final MPlayerThumbnailGeneratorProcess reader : readers) {
                try {
                    reader.waitFor(45 * 1000);
                    // mark as done
                    this.processes.remove(reader);
                } catch (final InterruptedException e) {
                    logger.error(
                            "error creating thumb for File ["
                                    + reader.getFileId() + "] - interrupted", e);
                }
            }

            return this.processes;
        }

    }

}
