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
import de.sosd.mediaserver.service.MPlayerFileService;
import de.sosd.mediaserver.service.MediaserverConfiguration;
import de.sosd.mediaserver.service.db.StorageService;

@Configurable
public class MetaInfoReaderThread extends Thread {

	private final static Log logger = LogFactory
			.getLog(MPlayerFileService.class);

	@Autowired
	private StorageService storage;

	@Autowired
	private MediaserverConfiguration cfg;

	@Override
	public void run() {
		super.run();
		try {
			this.storage.setMetaInfoGenerationRunning(true);
			createMetaInfosAsync(this.cfg.getMPlayerPath());
		} finally {
			this.storage.setMetaInfoGenerationRunning(false);
		}

	}

	private void createMetaInfosAsync(final String mplayer) {
		if ((mplayer != null) && (mplayer.length() > 0)) {
			final File mplayerFile = new File(mplayer);
			final Set<String> fileIds = new HashSet<String>();
			final ArrayList<ConcurrentExecute> processArray = new ArrayList<ConcurrentExecute>();
			ConcurrentExecute ce = new ConcurrentExecute();
			processArray.add(ce);
			final List<StringKeyValuePair> updateableFiles = new ArrayList<StringKeyValuePair>();
			updateableFiles.addAll(this.storage.getVideoFileIdsWithoutMeta());
			updateableFiles.addAll(this.storage.getAudioFileIdsWithoutMeta());

			for (final StringKeyValuePair skvp : updateableFiles) {
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
	}

	private class ConcurrentExecute {

		private final static int concurrentProcessCount = 10;
		private final List<MPlayerMetaInfoReaderProcess> processes = new ArrayList<MPlayerMetaInfoReaderProcess>(
				concurrentProcessCount);

		public boolean add(final File mplayerFile, final StringKeyValuePair idPath) {
			this.processes.add(new MPlayerMetaInfoReaderProcess(mplayerFile, idPath
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
