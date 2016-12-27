package io.arusland.bots.utils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * Created by ruslan on 28.12.2016.
 */
public final class ProcessUtil {
    private static final Logger log = Logger.getLogger(ProcessUtil.class);

    public static int getCurrentPID() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int index= name.indexOf("@");

        if (index > 0) {
            return Integer.parseInt(name.substring(0, index));
        }

        return -1;
    }

    public static void writePID(File outdir) throws IOException {
        int pid = ProcessUtil.getCurrentPID();

        if (pid > 0) {
            FileUtils.writeStringToFile(new File(outdir, "matebot.pid"), String.valueOf(pid), "UTF-8");
        }
    }

    public static void writePIDSafe(File outdir) {
        try {
            writePID(outdir);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("write pid failed", e);
        }
    }
}
