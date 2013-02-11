package de.sosd.mediaserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileIdTest {

    private static long errorCount   = 0l;
    private static long fserrorCount = 0l;
    private static long fileCount    = 0l;

    public static void main(final String[] args)
            throws NoSuchAlgorithmException,
            IOException {
        final String[] paths = new String[] { "E:/", "D:/", "C:/",
                "//HERMINE/audio",
                "//HERMINE/incoming", "//HERMINE/private",
                "//HERMINE/software", "//HERMINE/video", "//HERMINE/wg" };

        final Map<String, File> idFileMap = new HashMap<String, File>();
        final StringBuilder errors = new StringBuilder();
        final StringBuilder fserrors = new StringBuilder();
        for (final String path : paths) {
            fillMap(new File(path), idFileMap, errors, fserrors);
        }

        final String errorString = errors.toString();
        System.err.println(errorString);

        final File errorLog = new File("error.log");
        FileWriter fw = new FileWriter(errorLog);

        fw.write(errorString);
        fw.flush();
        fw.close();

        final String fserrorString = fserrors.toString();
        System.err.println(fserrorString);

        final File fserrorLog = new File("fserror.log");
        fw = new FileWriter(fserrorLog);

        fw.write(fserrorString);
        fw.flush();
        fw.close();
    }

    private static void fillMap(final File srcDir,
            final Map<String, File> idFileMap,
            final StringBuilder errors, final StringBuilder fserrors) {

        try {

            for (final File f : srcDir.listFiles()) {
                if (f.isDirectory()) {
                    fillMap(f, idFileMap, errors, fserrors);
                } else {
                    if (f.length() > 0) {
                        ++fileCount;
                        String hash;

                        hash = getHashCode(f);
                        if (idFileMap.containsKey(hash)) {
                            ++errorCount;
                            errors.append("already exists! : " + hash
                                    + " - "
                                    + idFileMap.get(hash).getAbsolutePath()
                                    + " <-> " + f.getAbsolutePath() + "\n");
                        } else {
                            idFileMap.put(hash, f);
                            System.out.println("stat : [" + fileCount + ":"
                                    + errorCount + ":" + fserrorCount
                                    + "], id : " + hash
                                    + " - " + f.getAbsolutePath());
                        }

                    }
                }

            }

        } catch (final Throwable e) {
            fserrors.append(srcDir.getAbsolutePath() + " -> "
                    + e.getLocalizedMessage() + "\n");
            ++fserrorCount;
        }
    }

    public static String getHashCode(final File input)
            throws NoSuchAlgorithmException, IOException {
        final MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        final byte[] buffer = new byte[1024];
        if (input.length() < 10 * 100 * 1024) {

            final FileInputStream fis = new FileInputStream(input);

            int read = 0;
            do {
                read = fis.read(buffer);
                md.update(buffer);
            } while (read > 0);
            fis.close();
        } else {
            final int ffw = Math.round(input.length() / (100 * 1024));
            long seek = ffw;
            final RandomAccessFile raf = new RandomAccessFile(input, "r");
            for (int i = 0; i < 10; ++i) {
                raf.read(buffer);
                md.update(buffer);
                raf.seek(seek);
                seek += ffw;
            }
            raf.close();
        }

        final byte[] digest = md.digest();

        final UUID id = UUID.nameUUIDFromBytes(digest);
        return id.toString();
    }

}
