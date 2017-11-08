package io.arusland.ctl;

import java.io.IOException;

/**
 * @author Ruslan Absalyamov
 * @since 08.11.2017
 */
public class ExecUtils {
    public static void runCommand(String... cmd) throws IOException {
        Runtime rt = Runtime.getRuntime();
        rt.exec(cmd);
    }
}
