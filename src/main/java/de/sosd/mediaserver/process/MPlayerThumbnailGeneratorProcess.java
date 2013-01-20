package de.sosd.mediaserver.process;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.dao.DidlDao;
import de.sosd.mediaserver.dao.FilesystemDao;
import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.FileDomain;
import de.sosd.mediaserver.domain.db.ThumbnailDomain;
import de.sosd.mediaserver.task.ProcessWatchdogService;

@Configurable
public class MPlayerThumbnailGeneratorProcess extends DefaultExecuteResultHandler implements ProcessKilledNotifier {
	
	private final static Log logger = LogFactory.getLog(MPlayerThumbnailGeneratorProcess.class);
	
	@Autowired
	private ProcessWatchdogService watcher;
	
	private final String fileId;
	private final File thumbnailFolder;
	private File tempFolder;
	private final ByteArrayOutputStream out;
	private final ByteArrayOutputStream err;
	private final File mplayer;

	@Autowired
	private FilesystemDao fsDao;
	
	@Autowired
	private DidlDao didlDao;
	
	private final String path;
	private boolean seekable;

	private String[] arguments;

	
	public MPlayerThumbnailGeneratorProcess(final File mplayer, final File previewFolder, final String fileId, final String path) {
		super();
		this.fileId = fileId;
		this.path = path;
		this.thumbnailFolder = previewFolder;
		this.mplayer = mplayer;
		this.out = new ByteArrayOutputStream();
		this.err = new ByteArrayOutputStream();
	}
	
	@Override
	public void onProcessComplete(final int exitValue) {
		super.onProcessComplete(exitValue);
		update();
	}
	
	@Override
	public void onProcessFailed(final ExecuteException e) {
		super.onProcessFailed(e);
		update();	
	}

	public MPlayerThumbnailGeneratorProcess execute() throws ExecuteException, IOException {
		debug();
		prepare();
//		if (! path.toLowerCase().endsWith("mts")) {
			final CommandLine cmdLine = new CommandLine(this.mplayer);
			cmdLine.addArguments(this.arguments);
			cmdLine.addArgument(this.path, false);
//			ExecuteWatchdog watchdog = new ExecuteWatchdog(30*1000);
			final Executor executor = new DefaultExecutor();
			executor.setExitValue(1);
//			executor.setWatchdog(watchdog);
//			executor.setProcessDestroyer(MPlayerFileService.PROCESS_SHUTDOWN_HOOK);
			executor.setStreamHandler(new PumpStreamHandler(this.out, this.err));
			executor.setWorkingDirectory(this.tempFolder);
			executor.execute(cmdLine, this);
			
//		} else {
//			throw new ExecuteException("mts will not work with mplayer -identify", 0);
//		}
		return this;
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	private void prepare() throws IOException {
		debug();
//		tempFolder = new File (fileId + "-THUMB");
//		if (! tempFolder.mkdirs() && ! tempFolder.exists()) {
			this.tempFolder = new File (this.thumbnailFolder ,this.fileId + "-THUMB");
			if (!this.tempFolder.mkdirs() && !this.tempFolder.exists()) {
				throw new IOException("can't create tempdirectory to store previews ..." + this.tempFolder.getAbsolutePath());
			}
//		}		
		final DidlDomain didl = this.fsDao.getFile(this.fileId).getDidl();
//		if (didl.getPassedMPlayer() != null && !didl.getPassedMPlayer()) {
			// maybe the file was not readable so fetch it from our server ;)
			// int stop = (int)(didl.getSize() / 10);
			//this.path = new DidleWrapper(didl).getUrl(didl);// + "?stop=" + stop;
		
			
			this.seekable = (didl.getSeekable() == null) || didl.getSeekable().booleanValue();
			if (this.seekable) {
				
//				String duration = didl.getDuration();
//				
//				// 00:42:40.219
//				String[] split = duration.split(":");
//				int seconds = 0;
//				try {
//					seconds += Integer.parseInt( split[0] ) * 60 * 60;
//					seconds += Integer.parseInt( split[1] ) * 60;
//					seconds += Float.parseFloat( split[2] );
//				} catch (NumberFormatException nfe) {
//					
//				}
//				String seek = "120";
//				if (seconds == 0 || seconds < 120) {
//					seek = "" + seconds
//				} else {
//					
//				}
				
				this.arguments = new String[]{"-slave","-frames","10", "-nocache", "-noidle", "-speed", "24.0" ,"-ao", "null","-ss", "120", "-vf", "screenshot", "-vo", "jpeg:outdir=."};
				this.watcher.addProcessToWatch(new String[]{"mplayer", "-slave","-frames","10", "-nocache", "-noidle", "-speed", "24.0" ,"-ao", "null","-ss", "120", "-vf", "screenshot", "-vo", "jpeg:outdir=.", this.path}, 60, this);
			} else {
				this.arguments = new String[]{"-slave","-frames","250", "-nocache", "-noidle", "-speed", "24.0" ,"-ao", "null","-vf", "screenshot", "-vo", "jpeg:outdir=."};				
				this.watcher.addProcessToWatch(new String[]{"mplayer", "-slave","-frames","250", "-nocache", "-noidle", "-speed", "24.0" ,"-ao", "null","-vf", "screenshot", "-vo", "jpeg:outdir=." , this.path}, 60, this);
			}

			
//		} 
	}
	
	
//	private String escapePath(String string) {
//		String result = string.replaceAll(" ", "\\\\ ");
//		return "\"" + result + "\"";
//	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private void update() {
		debug();
		this.thumbnailFolder.mkdirs();
		final FileDomain fd = this.fsDao.getFile(this.fileId);
		final DidlDomain dd = fd.getDidl();
		File screenshot = new File(this.tempFolder, "00000001.jpg");
		long max_size = 0l;
		final File[] fileList = this.tempFolder.listFiles();
		if (fileList != null) {
			for (final File sf : this.tempFolder.listFiles()) {
				if (sf.length() > max_size) {
					screenshot = sf;
					max_size = sf.length();
				}
			}
		} else {
			logger.error("for soem reason the tempfolder " + this.tempFolder.getAbsolutePath() + " does not exist anymore, this is odd!");
		}
		
		try {
			if (screenshot.exists()) {
				final String type = "jpg";
				final File thumbnailFile = new File(this.thumbnailFolder,dd.getId().concat(".").concat(type) );
				if (thumbnailFile.exists()) {
					// replace it
					FileUtils.forceDelete(thumbnailFile);
				}							
//						ThumbnailDomain thumb = resizeImageTo(160f, type, thumbnailFile, screenshot);
				final ThumbnailDomain thumb = moveImageTo(type, thumbnailFile, screenshot);
				logger.info("update add thumb for [" + dd.getId() + "]");
				dd.setThumbnail(thumb);
				dd.increaseUpdateId();
				dd.setGenerateThumbnail(false);
				this.fsDao.store(fd);	
			} else {
				if (this.out.toString().contains("Stream not seekable!") && ((dd.getSeekable() == null) || dd.getSeekable().booleanValue())) {
					dd.setSeekable(false);
					this.didlDao.store(dd);	
				} else {
					dd.setGenerateThumbnail(false);
					this.didlDao.store(dd);	
					logger.error("update add thumb for [" + dd.getId() + "] failed, no thumbnail created in " + this.tempFolder.getAbsolutePath());
					logger.error("errors : \n" + this.err.toString());
					logger.error("console : \n" + this.out.toString());
				}
			}
		} catch (final IOException e) {
			logger.error("can't scale screenshot-image ...",e);	
		} finally {
			try {
				this.err.close();
				this.out.close();
				if (this.tempFolder.exists()) {
					FileUtils.deleteDirectory(this.tempFolder);
				}
			} catch (final IOException e) {
				logger.error("could not remove tempdirectory : " + this.tempFolder.getAbsolutePath(), e);
			}			
		}	
	}

	private ThumbnailDomain moveImageTo(final String type, final File thumbnailFile,
			final File screenshot) throws IOException {
		final BufferedImage originalImage = ImageIO.read(screenshot);
		screenshot.renameTo(thumbnailFile);
		return new ThumbnailDomain(type,originalImage.getWidth()+ "x" + originalImage.getHeight());
	}

	public String getFileId() {
		return this.fileId;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void notifyProcessKilled() {
		debug();
		final FileDomain fd = this.fsDao.getFile(this.fileId);
		final DidlDomain dd = fd.getDidl();
		dd.setGenerateThumbnail(false);
		this.didlDao.store(dd);	
	}

	private void debug() {
		this.path.toLowerCase();
		//if (lcpath.endsWith("ma4v") || lcpath.endsWith("mts") || lcpath.endsWith("mp4") ) 
		if (this.fileId.equalsIgnoreCase("88cb970a-ce52-3943-a057-5fa18b65cdf6"))	
		{
			logger.debug("critical file found");
		}
		
	}
	
	
}