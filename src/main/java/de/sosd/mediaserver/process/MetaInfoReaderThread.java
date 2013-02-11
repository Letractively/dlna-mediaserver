package de.sosd.mediaserver.process;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.bean.StringKeyValuePair;
import de.sosd.mediaserver.dao.DidlDao;
import de.sosd.mediaserver.dao.FilesystemDao;
import de.sosd.mediaserver.dao.SystemDao;
import de.sosd.mediaserver.domain.db.FileDomain;
import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.MPlayerFileService;
import de.sosd.mediaserver.service.MediaserverConfiguration;

@Configurable
public class MetaInfoReaderThread extends Thread {

    private final static Log         logger = LogFactory
                                                    .getLog(MPlayerFileService.class);

    @Autowired
    private SystemDao                systemDao;

    @Autowired
    private FilesystemDao            fsDao;

    @Autowired
    private DidlDao                  didlDao;

    @Autowired
    private MediaserverConfiguration cfg;

    private final SystemDomain       system;

    public MetaInfoReaderThread(final SystemDomain system) {
        this.system = system;
    }

    @Override
    public void run() {
        super.run();
        try {
            this.systemDao
                    .setMetaInfoGenerationRunning(true, this.cfg.getUSN());
            logger.info("check for missing meta-infos");
            createMetaInfosAsync(this.system.getMplayerPath());
            logger.info("meta-infos up to date");
        } finally {
            this.systemDao.setMetaInfoGenerationRunning(false,
                    this.cfg.getUSN());
        }

    }

    private void createMetaInfosAsync(final String mplayer) {
        if (mplayer != null && mplayer.length() > 0) {
            final File mplayerFile = new File(mplayer);
            final Set<String> fileIds = new HashSet<String>();
            final ArrayList<ConcurrentExecute> processArray = new ArrayList<ConcurrentExecute>();
            ConcurrentExecute ce = new ConcurrentExecute();
            processArray.add(ce);
            final List<StringKeyValuePair> mplayerUpdateableFiles = new ArrayList<StringKeyValuePair>();
            mplayerUpdateableFiles.addAll(this.didlDao
                    .getVideoFileIdsWithoutMeta());
            mplayerUpdateableFiles.addAll(this.didlDao
                    .getAudioFileIdsWithoutMeta());

            for (final StringKeyValuePair skvp : mplayerUpdateableFiles) {
                if (!fileIds.contains(skvp.getKey())) {
                    fileIds.add(skvp.getKey());
                    if (!ce.add(mplayerFile, skvp)) {
                        ce = new ConcurrentExecute();
                        processArray.add(ce);
                    }
                } else {
                    logger.debug("found and ignored double! : " + skvp.getKey());
                }
            }
            final List<MPlayerMetaInfoReaderProcess> failed = new ArrayList<MPlayerMetaInfoReaderProcess>();
            for (final ConcurrentExecute ce_e : processArray) {
                failed.addAll(ce_e.execute());
            }

            if (!failed.isEmpty()) {
                logger.error("some meta-infos could not be read : "
                        + failed.size());
            }
        }

        final List<String> imageIoUpdateableFiles = this.didlDao
                .getImageFileIdsWithoutMeta();
        for (final String fileId : imageIoUpdateableFiles) {
            imageIoUpdateMetaInfo(fileId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void imageIoUpdateMetaInfo(final String fileId) {
        final FileDomain file = this.fsDao.getFile(fileId);

        try {
            final BufferedImage read = ImageIO.read(new File(file.getPath()));
            read.flush();
            file.getDidl().setResolution(
                    read.getWidth() + "x" + read.getHeight());
            file.getDidl().setGenerateThumbnail(false);
            // TODO set protocolInfo accordingly (depends on resolution here)
        } catch (final IOException t) {
            // if we can't read it, clients probably can't as well -> ignore
            file.getDidl().setGenerateThumbnail(false);
        } catch (final NullPointerException t) {
            // if we can't read it, clients probably can't as well -> ignore
            // (this happened on some MacOsx files ...)
            file.getDidl().setGenerateThumbnail(false);
        }
        this.didlDao.store(file.getDidl());
    }

    private class ConcurrentExecute {

        private final static int                         concurrentProcessCount = 10;
        private final List<MPlayerMetaInfoReaderProcess> processes              = new ArrayList<MPlayerMetaInfoReaderProcess>(
                                                                                        concurrentProcessCount);

        public boolean add(final File mplayerFile,
                final StringKeyValuePair idPath) {
            this.processes.add(new MPlayerMetaInfoReaderProcess(mplayerFile,
                    idPath
                            .getKey(), idPath.getValue()));
            return this.processes.size() < concurrentProcessCount;
        }

        public List<MPlayerMetaInfoReaderProcess> execute() {
            final List<MPlayerMetaInfoReaderProcess> readers = new ArrayList<MPlayerMetaInfoReaderProcess>();
            for (final MPlayerMetaInfoReaderProcess reader : this.processes) {
                try {
                    reader.execute();
                    readers.add(reader);
                } catch (final ExecuteException e) {
                    logger.error(
                            "error reading meta-infos for File ["
                                    + reader.getFileId() + "]", e);
                } catch (final IOException e) {
                    logger.error(
                            "error reading meta-infos for File ["
                                    + reader.getFileId() + "]", e);
                }
            }
            for (final MPlayerMetaInfoReaderProcess reader : readers) {
                try {
                    reader.waitFor(45 * 1000);
                    // mark as done
                    this.processes.remove(reader);
                } catch (final InterruptedException e) {
                    logger.error(
                            "error reading meta-infos for File ["
                                    + reader.getFileId() + "] - interrupted", e);
                }
            }

            return this.processes;
        }
    }

}
