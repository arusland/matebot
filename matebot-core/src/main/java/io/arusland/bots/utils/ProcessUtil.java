package io.arusland.bots.utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileLock;

/**
 * Created by ruslan on 28.12.2016.
 */
public final class ProcessUtil {
    private static final Logger log = Logger.getLogger(ProcessUtil.class);

    public static int getCurrentPID() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int index = name.indexOf("@");

        if (index > 0) {
            return Integer.parseInt(name.substring(0, index));
        }

        return -1;
    }

    public static boolean writePID(File outdir) {
        int pid = ProcessUtil.getCurrentPID();
        final File file = new File(outdir, "matebot.pid");

        try {
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();

            if (fileLock != null) {
                randomAccessFile.writeBytes(String.valueOf(pid));

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            fileLock.release();
                            randomAccessFile.close();
                            file.delete();
                        } catch (Exception e) {
                            log.error("Unable to remove lock file: " + file, e);
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
            log.error("Unable to create and/or lock file: " + file, e);
        }

        return false;
    }
}
