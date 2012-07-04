/**
 * 
 */
package de.sosd.mediaserver.service;

import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.process.MetaInfoReaderThread;
import de.sosd.mediaserver.process.ThumbnailCreationThread;
import de.sosd.mediaserver.service.db.StorageService;

/***
AVI file format detected.
ID_VIDEO_ID=0
[aviheader] Video stream found, -vid 0
ID_AUDIO_ID=1
[aviheader] Audio stream found, -aid 1
VIDEO:  [XVID]  640x272  12bpp  23.976 fps  930.7 kbps (113.6 kbyte/s)
Clip info:
Software: VirtualDubMod 1.5.10.2 (build 2540/release)
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
Opening video filter: [screenshot]
==========================================================================
Opening video decoder: [ffmpeg] FFmpeg's libavcodec codec family
Selected video codec: [ffodivx] vfm: ffmpeg (FFmpeg MPEG-4)
==========================================================================
ID_VIDEO_CODEC=ffodivx
Audio: no sound
Starting playback...
Could not find matching colorspace - retrying with -vf scale...
Opening video filter: [scale]
Movie-Aspect is 2.35:1 - prescaling to correct movie aspect.
ID_VIDEO_ASPECT=2.3529
[swscaler @ 017834F4]using unscaled yuv420p -> rgb24 special converter
VO: [png] 640x272 => 640x272 RGB 24-bit
png: . - Output directory already exists and is writable.
V: 121.6 2917/2917 ??% ??% ??,?% 0 0

Exiting... (End of file)
ID_EXIT=EOF		 
 */


/**
c:\Program Files (x86)\SMPlayer\mplayer>mplayer -identify -ao null -ss 120 -vf screenshot -frames 1 -vo png:z=9 f:\dlna-test-ordner\Musik\Bela_B.-Bingo-DE-2006-MST\01-bela_b.-b-vertuere-mst.mp3
MPlayer Sherpya-SVN-r30369-4.2.5 (C) 2000-2009 MPlayer Team

Playing f:\dlna-test-ordner\Musik\Bela_B.-Bingo-DE-2006-MST\01-bela_b.-b-vertuere-mst.mp3.
ID_AUDIO_ID=0
Audio only file format detected.
Clip info:
Title: b-vert�re
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
ID_FILENAME=f:\dlna-test-ordner\Musik\Bela_B.-Bingo-DE-2006-MST\01-bela_b.-b-vertuere-mst.mp3
ID_DEMUXER=audio
ID_AUDIO_FORMAT=85
ID_AUDIO_BITRATE=192000
ID_AUDIO_RATE=44100
ID_AUDIO_NCH=0
ID_LENGTH=80.00
ID_SEEKABLE=1
ID_CHAPTERS=0
==========================================================================
Opening audio decoder: [ffmpeg] FFmpeg/libavcodec audio decoders
AUDIO: 44100 Hz, 2 ch, s16le, 192.0 kbit/13.61% (ratio: 24000->176400)
ID_AUDIO_BITRATE=192000
ID_AUDIO_RATE=44100
ID_AUDIO_NCH=2
Selected audio codec: [ffmp3] afm: ffmpeg (FFmpeg MPEG layer-3 audio)
==========================================================================
AO: [null] 44100Hz 2ch s16le (2 bytes per sample)
ID_AUDIO_CODEC=ffmp3
Video: no video
Starting playback...
A:   0.0 (00.0) of 80.0 (01:20.0) ??,?%

Exiting... (End of file)
ID_EXIT=EOF

	
*/

/**
 * @author simon
 *
 */
@Service
public class MPlayerFileService {

	private final static Log logger = LogFactory.getLog(MPlayerFileService.class);
	
	public final static ShutdownHookProcessDestroyer PROCESS_SHUTDOWN_HOOK = new ShutdownHookProcessDestroyer();
	
	@Autowired
	private StorageService storage;
	
	private Thread metaInfoReader = null;
	private Thread thumbnailCreator = null;
	
	
	public void createMetaInfos() {
		final SystemDomain system = this.storage.getSystemProperties();
		final Boolean running = system.getMetaInfoGenerationRunning();
		if (((running == null) || !running.booleanValue())) {
			if (this.metaInfoReader != null) {
				this.metaInfoReader.interrupt();
			}
			this.metaInfoReader = new MetaInfoReaderThread(system);
			this.metaInfoReader.start();			
		} else {
			logger.info("skipped create-meta-info, already running");
		}
	}
	
	public void createThumbnails() {
		final SystemDomain system = this.storage.getSystemProperties();
		final Boolean running = system.getThumbnailGenerationRunning();
		if (((running == null) || !running.booleanValue())) {
			if (this.thumbnailCreator != null) {
				this.thumbnailCreator.interrupt();
			}
			this.thumbnailCreator = new ThumbnailCreationThread(system);
			this.thumbnailCreator.start();
		} else {
			logger.info("skipped create-thumbnails, already running");
		}
	}
		


	
}
