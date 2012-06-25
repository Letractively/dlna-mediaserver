package de.sosd.mediaserver.process;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.sosd.mediaserver.domain.db.DidlDomain;
import de.sosd.mediaserver.domain.db.FileDomain;
import de.sosd.mediaserver.service.db.StorageService;
import de.sosd.mediaserver.task.ProcessWatchdogService;

@Configurable
public class MPlayerMetaInfoReaderProcess extends DefaultExecuteResultHandler implements ProcessKilledNotifier {
	
	private final static Log logger = LogFactory.getLog(MPlayerMetaInfoReaderProcess.class);
	
	@Autowired
	private ProcessWatchdogService watcher;
	
	private final String fileId;
	private final ByteArrayOutputStream out;
	private final ByteArrayOutputStream err;
	private final File mplayer;
	
	@Autowired
	private StorageService storage;
	private final String path;

	private String[] arguments;
	
	public MPlayerMetaInfoReaderProcess(final File mplayer, final String fileId, final String path) {
		super();
		this.fileId = fileId;
		this.path = path;
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

	public MPlayerMetaInfoReaderProcess execute() throws ExecuteException, IOException {
		prepare();
//		if (! path.toLowerCase().endsWith("mts")) {
			final CommandLine cmdLine = new CommandLine(this.mplayer);
			cmdLine.addArguments(this.arguments);
			cmdLine.addArgument(this.path, false);
//			ExecuteWatchdog watchdog = new ExecuteWatchdog(5*1000);
			final Executor executor = new DefaultExecutor();
			executor.setExitValue(1);
//			executor.setWatchdog(watchdog);
//			executor.setProcessDestroyer(MPlayerFileService.PROCESS_SHUTDOWN_HOOK);
			executor.setStreamHandler(new PumpStreamHandler(this.out, this.err));
			executor.execute(cmdLine, this);

//		} else {
//			throw new ExecuteException("mts will not work with mplayer -identify", 0);
//		}
		return this;
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	private void prepare() {
		this.storage.getFile(this.fileId).getDidl();
//		if (didl.getPassedMPlayer() != null && !didl.getPassedMPlayer()) {
			// maybe the file was not readable so fetch it from our server ;)
//			int stop = (int)(didl.getSize() / 100);
			//this.path = new DidleWrapper(didl).getUrl(didl);// + "?stop=" + stop;
//		this.path = escapePath(this.path);
//		}
		this.arguments = new String[]{"-slave", "-ao", "null", "-vo", "null", "-frames", "0", "-nocache", "-noidle", "-identify", "-speed", "24.0"};
		this.watcher.addProcessToWatch(new String[]{"mplayer","-slave", "-ao", "null", "-vo", "null", "-frames", "0", "-nocache", "-noidle", "-identify", "-speed", "24.0", this.path}, 60, this);
	}
	
//	private String escapePath(String string) {
//		String result = string.replaceAll(" ", "\\\\ ");
//		return "\"" + result + "\"";
//	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private void update() {
		try {
		final FileDomain fd = this.storage.getFile(this.fileId);
		final String filtered_text = this.out.toString().replace('\r', '\n');
		
		final String[] lines = filtered_text.split("\n");
		final Map<String,String> parsedValues = new HashMap<String,String>();

		/**
		ID_VIDEO_ID=0 
		ID_AUDIO_ID=1
		ID_CLIP_INFO_NAME0=Software
		ID_CLIP_INFO_VALUE0=VirtualDubMod 1.5.10.2 (build 2540/release)
		ID_CLIP_INFO_N=1
		ID_FILENAME=
		ID_DEMUXER=avi
		ID_VIDEO_FORMAT=XVID
		ID_VIDEO_BITRATE=930680
		ID_VIDEO_WIDTH=640
		ID_VIDEO_HEIGHT=272
		ID_VIDEO_FPS=23.976
		ID_VIDEO_ASPECT=0.0000
		ID_LENGTH=5502.62
		ID_SEEKABLE=1
		ID_CHAPTERS=0 
		ID_VIDEO_CODEC=ffodivx
		ID_VIDEO_ASPECT=2.3529
		ID_AUDIO_CODEC=ffmp3

		ID_DEMUXER=audio
		ID_AUDIO_FORMAT=85
		ID_AUDIO_BITRATE=192000
		ID_AUDIO_RATE=44100
		ID_AUDIO_NCH=0
		ID_LENGTH=80.00
		ID_SEEKABLE=1
		ID_CHAPTERS=0
		ID_CLIP_INFO_NAME0=Title
		ID_CLIP_INFO_VALUE0=b-vert�re
		ID_CLIP_INFO_NAME1=Artist
		ID_CLIP_INFO_VALUE1=Bela B.
		ID_CLIP_INFO_NAME2=Album
		ID_CLIP_INFO_VALUE2=Bingo
		ID_CLIP_INFO_NAME3=Year
		ID_CLIP_INFO_VALUE3=2006
		ID_CLIP_INFO_NAME4=Comment
		ID_CLIP_INFO_VALUE4=-] MST [-
		ID_CLIP_INFO_NAME5=Track
		ID_CLIP_INFO_VALUE5=1
		ID_CLIP_INFO_NAME6=Genre
		ID_CLIP_INFO_VALUE6=Punk Rock
		ID_CLIP_INFO_N=7
		 * */		
		for (final String line : lines) {
			
			
			if (line.startsWith("ID")) {
				final String[] split = line.split("=");
				if (split.length ==2) {
					parsedValues.put(split[0].substring(3).toLowerCase(), split[1]);
 				}
			}


		}
		// optimize
		final String infoNr = parsedValues.remove("clip_info_n");
		if (infoNr != null) {
			final int idx = Integer.parseInt(infoNr);
			for (int i = 0; i < idx; ++i) {
				final String name = parsedValues.remove("clip_info_name" + i);
				final String value = parsedValues.remove("clip_info_value" + i);
				
				parsedValues.put(name.toLowerCase(), value);
			}
		}
		// duration="01:56:36.780" resolution="620x256" bitrate="107520" nrAudioChannels="2" sampleFrequency="48000"
		final DidlDomain dd = fd.getDidl();
//			if (dd.getUrl().toLowerCase().endsWith("mp3")) {
//				System.out.println("mp3");
//			}
		
		dd.setAlbum(parsedValues.get("album"));
		dd.setBitrate(getBitrate(parsedValues.get("video_bitrate"), parsedValues.get("audio_bitrate")));
		dd.setArtist(parsedValues.get("artist"));
		dd.setAudioCodec(parsedValues.get("audio_codec"));
		dd.setTrack(getInteger(parsedValues.get("track")));
		dd.setVideoCodec(parsedValues.get("video_codec"));
		dd.setYear(getInteger(parsedValues.get("year")));
//		String title = parsedValues.get("title");
//		if (title != null) {
//			dd.setTitle(title);
//		}
//			dd.setBitsPerSample(bitsPerSample);
//			dd.setColorDepth(colorDepth);

		dd.setDuration(getDuration(parsedValues.get("length")));
		dd.setGenre(parsedValues.get("genre"));
//			dd.setLanguage(language);
		dd.setNrAudioChannels(getInteger(parsedValues.get("audio_nch")));
		dd.setResolution(getResoulution(parsedValues.get("video_width"), parsedValues.get("video_height")));
		dd.setSampleFrequency(getInteger(parsedValues.get("audio_rate")));
		
//			if (preview != null && preview.exists()) {
//				service.
//			}
//			dd.setThumbnail(thumbnail)
		final boolean notReadable = filtered_text.toLowerCase().contains("file not found") || filtered_text.toLowerCase().contains("failed to open");
		dd.setPassedMPlayer(! notReadable);
		dd.increaseUpdateId();
		
		logger.info("update meta-infos [" + fd.getId() + "]");
		this.storage.store(fd);	
		} finally {
			try {
				this.err.close();
				this.out.close();
			} catch (final IOException e) {
				logger.error("could not close outputstreams", e);
			}	
		}
	}

	private Integer getInteger(final String i) {
		if (i == null) {
			return null;
		}
		try {
			return Integer.parseInt(i);
		} catch (final NumberFormatException nfe) {
			return null;
		}
	}

	private String getResoulution(final String width, final String height) {
		if ((width != null) && (height != null)) {
			return width + "x" + height;
		}
		return null;
	}

	private Integer getBitrate(final String video, final String audio) {
		Integer result = getInteger(video);
		// if video, then this shouldn't be null
		if (result != null) {
			return result;
		} 
		// otherwise its prob audio
		result = getInteger(audio);
		
		// or image -> null
		return result;
	}

	private String getDuration(final String f) {
		if (f == null) {
			return null;
		}
		try {
			 float value = Float.parseFloat(f);
			 final int hours = (int)(value / 3600);
			 value -= hours*3600;
			 final int minutes =(int)(value / 60);
			 value -= minutes*60;
			 final int seconds = (int)(value);
			 value -= seconds;
			 final int millis = (int)(value * 1000);
			 
			 
			 // "01:56:36.780"
			 
			 return get2DigitString(hours) + ":" + get2DigitString(minutes) + ":" + get2DigitString(seconds) + "." + get3DigitString(millis);
		} catch (final NumberFormatException nfe) {
			return null;
		}
	}

	private String get3DigitString(final int i) {
		if (i < 100) {
			return "0" + get2DigitString(i);
		} else {
			return "" + i;
		}
	}

	private String get2DigitString(final int i) {
		if (i < 10) {
			return "0" + i;
		} else {
			return "" + i;
		}
	}

	public String getFileId() {
		return this.fileId;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void notifyProcessKilled() {
		final FileDomain fd = this.storage.getFile(this.fileId);
		final DidlDomain dd = fd.getDidl();
		dd.setPassedMPlayer(true);
		this.storage.store(dd);	
	}
	
	
}