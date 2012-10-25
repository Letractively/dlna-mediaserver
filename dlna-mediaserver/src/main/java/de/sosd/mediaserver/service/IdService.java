package de.sosd.mediaserver.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class IdService {

	private final static Log logger = LogFactory.getLog(IdService.class);
	
	public String getId(final File f) {
		try {
			if (f.isDirectory()) {
				return getId4Directory(f);
			}
			if (f.isFile()) {
				return getId4Directory(f);
	//			return getId4File(f);
			}
			} catch (final Throwable t) {
				logger.error("error while computing file-id for " + f.getAbsolutePath(),t);
			}
		return null;
		
	}
	
	public String getUUID(final File f) throws NoSuchAlgorithmException, IOException {
		return getId4File(f);
	}
	
	private String getId4Directory(final File directory) throws NoSuchAlgorithmException {
		final String absolutePath = directory.getAbsolutePath();
//		int hashCode = absolutePath.hashCode();
//		int length = absolutePath.length();
//		return new UUID(0l +hashCode, 0l+length).toString();
		
//		
		final MessageDigest md = MessageDigest.getInstance("SHA1");
		md.reset();
		md.update(absolutePath.getBytes());
		final byte[] digest = md.digest();
		final UUID id = UUID.nameUUIDFromBytes(digest);
		return id.toString();		
	}
	
	private String getId4File(final File file) throws NoSuchAlgorithmException,
			IOException {
		final MessageDigest md = MessageDigest.getInstance("SHA1");
		md.reset();
		final byte[] buffer = new byte[1024];
		if (file.length() < (10 * 100 * 1024)) {

			final FileInputStream fis = new FileInputStream(file);

			int read = 0;
			do {
				read = fis.read(buffer);
				md.update(buffer);
			} while (read > 0);
			fis.close();
		} else {
			final int ffw = Math.round(file.length() / (100 * 1024));
			long seek = ffw;
			final RandomAccessFile raf = new RandomAccessFile(file, "r");
			for (int i = 0; i < 10; ++i) {
				raf.read(buffer);
				md.update(buffer);
				raf.seek(seek);
				seek += ffw;
			}
			raf.close();
		}

		final byte[] digest = md.digest();
//		System.out.println(digest.length);
//		return new String(digest);
		final UUID id = UUID.nameUUIDFromBytes(digest);
		return id.toString();
	}

}
