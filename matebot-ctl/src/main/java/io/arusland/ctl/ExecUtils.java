package io.arusland.ctl;

import java.io.IOException;

/**
 * @author Ruslan Absalyamov
 * @since 2017-11-08
 */
public class ExecUtils {
    public static void runCommand(String... cmd) throws IOException {
        Runtime rt = Runtime.getRuntime();
        rt.exec(cmd);
    }
}
